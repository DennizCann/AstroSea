"use strict";

const { createHash, timingSafeEqual } = require("node:crypto");

const PROFILE_URL = "https://api.adapty.io/api/v2/server-side-api/profile/";
const ACCESS_LEVELS = new Set(["premium", "beta_premium"]);

class MembershipError extends Error {
  constructor(code) {
    super(code);
    this.code = code;
  }
}

function validUid(uid) {
  return typeof uid === "string" && uid.length > 0 && uid.length <= 128 &&
    !/[\x00-\x20\x7f/]/.test(uid) && uid !== "." && uid !== "..";
}

function authorized(header, secret) {
  if (typeof secret !== "string" || secret.length < 32 || typeof header !== "string") return false;
  const supplied = Buffer.from(header);
  const expected = Buffer.from(secret);
  return supplied.length === expected.length && timingSafeEqual(supplied, expected);
}

function isoDate(value, nullable = true) {
  if (value === null && nullable) return null;
  // Never interpret an absent/malformed expiry as lifetime access or use server-local time.
  if (typeof value !== "string" || !/(Z|[+-]\d{2}:\d{2})$/.test(value)) {
    throw new MembershipError("invalid-provider-response");
  }
  const time = Date.parse(value);
  if (!Number.isFinite(time)) throw new MembershipError("invalid-provider-response");
  return new Date(time).toISOString();
}

function optionalString(value) {
  return typeof value === "string" && value.length <= 256 ? value : null;
}

function normalizeProfile(payload, uid, now = Date.now()) {
  const profile = payload?.data;
  if (!profile || profile.customer_user_id !== uid || !Array.isArray(profile.access_levels) ||
      !Array.isArray(profile.subscriptions)) {
    throw new MembershipError("invalid-provider-response");
  }
  const seen = new Set();
  const accessLevels = profile.access_levels.filter((level) => ACCESS_LEVELS.has(level?.access_level_id))
    .map((level) => {
      if (seen.has(level.access_level_id)) throw new MembershipError("invalid-provider-response");
      seen.add(level.access_level_id);
      const subscription = profile.subscriptions.find((sub) =>
        sub.store === level.store && sub.store_product_id === level.store_product_id &&
        typeof level.store_original_transaction_id === "string" &&
        sub.store_original_transaction_id === level.store_original_transaction_id);
      const environment = subscription?.environment === "Sandbox" ? "sandbox" :
        subscription?.environment === "Production" ? "production" : "unknown";
      const source = level.access_level_id === "beta_premium" ? "beta" :
        environment === "sandbox" ? "sandbox" : subscription ? "subscription" : "manual";
      return {
        id: level.access_level_id,
        source,
        store: optionalString(level.store),
        productId: optionalString(level.store_product_id),
        basePlanId: optionalString(level.store_base_plan_id),
        environment,
        startsAt: isoDate(level.starts_at ?? null),
        expiresAt: isoDate(level.expires_at),
        renewalCancelledAt: isoDate(level.renewal_cancelled_at ?? null),
        billingIssueDetectedAt: isoDate(level.billing_issue_detected_at ?? null),
        isInGracePeriod: level.is_in_grace_period === true,
      };
    });
  return { schemaVersion: 1, verifiedAt: new Date(now).toISOString(), accessLevels };
}

function membershipView(snapshot, now = Date.now()) {
  if (!snapshot || snapshot.schemaVersion !== 1 || !Array.isArray(snapshot.accessLevels)) {
    throw new MembershipError("membership-unavailable");
  }
  const accessLevels = snapshot.accessLevels.map((level) => ({
    ...level,
    isActive: (level.startsAt === null || Date.parse(level.startsAt) <= now) &&
      (level.expiresAt === null || Date.parse(level.expiresAt) > now),
  }));
  return {
    schemaVersion: 1,
    verifiedAt: snapshot.verifiedAt,
    hasPremiumAccess: accessLevels.some((level) => level.isActive),
    hasSandboxAccess: accessLevels.some((level) => level.isActive && level.environment === "sandbox"),
    // Keep both rights. Ending beta access must not revoke a paid subscription, or vice versa.
    accessLevels,
  };
}

function readingLimit(membership) {
  // Only a verified membership response is passed here, never a public user doc.
  if (membership?.schemaVersion !== 1 || typeof membership.hasPremiumAccess !== "boolean") {
    throw new MembershipError("membership-unavailable");
  }
  return membership.hasPremiumAccess ? 25 : 3;
}

async function fetchProfile(uid, secret, fetchImpl = fetch, now = Date.now()) {
  if (!validUid(uid)) throw new MembershipError("invalid-user");
  if (typeof secret !== "string" || !secret.trim()) throw new MembershipError("missing-secret");
  let response;
  let payload;
  try {
    response = await fetchImpl(PROFILE_URL, {
      method: "GET",
      headers: { Authorization: `Api-Key ${secret}`, "adapty-customer-user-id": uid, Accept: "application/json" },
      signal: AbortSignal.timeout(5000),
    });
    payload = await response.json();
  } catch {
    throw new MembershipError("provider-unavailable");
  }
  if (response.status === 404 && payload?.error_code === "profile_does_not_exist") {
    return { schemaVersion: 1, verifiedAt: new Date(now).toISOString(), accessLevels: [] };
  }
  if (!response.ok) throw new MembershipError("provider-unavailable");
  return normalizeProfile(payload, uid, now);
}

function parseWebhook(body) {
  // Adapty sends an empty authenticated POST when validating an endpoint.
  if (body == null || body === "" || (typeof body === "object" && !Array.isArray(body) && Object.keys(body).length === 0)) {
    return { verification: true };
  }
  if (typeof body !== "object" || Array.isArray(body) || typeof body.event_type !== "string") {
    throw new MembershipError("invalid-event");
  }
  // Anonymous SDK profiles cannot be mapped to a Firebase account. Do not guess by email.
  if (body.customer_user_id == null || body.customer_user_id === "") return { ignored: true };
  if (!validUid(body.customer_user_id)) throw new MembershipError("invalid-event");
  const eventId = body.event_properties?.profile_event_id ?? body.profile_event_id;
  return {
    uid: body.customer_user_id,
    // Persist no raw webhook, email, device details, or payment transaction identifiers.
    eventHash: typeof eventId === "string" && eventId.length <= 256 ?
      createHash("sha256").update(eventId).digest("hex") : null,
  };
}

module.exports = { MembershipError, validUid, authorized, normalizeProfile, membershipView, readingLimit, fetchProfile, parseWebhook };
