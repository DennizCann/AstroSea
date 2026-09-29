"use strict";

const test = require("node:test");
const assert = require("node:assert/strict");
const { validUid, authorized, normalizeProfile, membershipView, fetchProfile, parseWebhook } = require("../membership");

const NOW = Date.parse("2026-09-29T12:00:00Z");
const UID = "firebase-user-1";
function level(overrides = {}) {
  return {
    access_level_id: "premium", store: "play_store", store_product_id: "astrosea_monthly",
    store_original_transaction_id: "original-1", starts_at: "2026-09-29T10:00:00Z",
    expires_at: "2026-10-29T10:00:00Z", renewal_cancelled_at: null,
    billing_issue_detected_at: null, is_in_grace_period: false, ...overrides,
  };
}
function profile(accessLevels = [level()], subscriptions = []) {
  return { data: { customer_user_id: UID, access_levels: accessLevels, subscriptions } };
}
function subscription(environment = "Production") {
  return {
    store: "play_store", store_product_id: "astrosea_monthly",
    store_original_transaction_id: "original-1", environment,
  };
}
function response(status, body) {
  return { status, ok: status >= 200 && status < 300, json: async () => body };
}

test("valid Firebase IDs cannot alter document paths or headers", () => {
  assert.equal(validUid(UID), true);
  for (const value of [null, "", "a/b", "a\nb", ".", "..", "x".repeat(129)]) assert.equal(validUid(value), false);
});

test("webhook authorization rejects missing, short, and different secrets", () => {
  const secret = "test-only-".repeat(8);
  assert.equal(authorized(secret, secret), true);
  assert.equal(authorized(secret + "x", secret), false);
  assert.equal(authorized(secret.replace("t", "X"), secret), false);
  assert.equal(authorized(undefined, secret), false);
  assert.equal(authorized("short", "short"), false);
});

test("normalization preserves store expiry, not a locally computed month", () => {
  const snapshot = normalizeProfile(profile([level({ expires_at: "2026-09-29T12:05:00Z" })], [subscription("Sandbox")]), UID, NOW);
  const item = snapshot.accessLevels[0];
  assert.equal(item.expiresAt, "2026-09-29T12:05:00.000Z");
  assert.equal(item.source, "sandbox");
  assert.equal(item.environment, "sandbox");
  assert.equal(membershipView(snapshot, NOW).hasPremiumAccess, true);
  assert.equal(membershipView(snapshot, NOW).hasSandboxAccess, true);
  assert.equal(membershipView(snapshot, NOW + 5 * 60000).hasPremiumAccess, false);
});

test("paid and beta entitlements coexist and expire independently", () => {
  const snapshot = normalizeProfile(profile([
    level(),
    level({ access_level_id: "beta_premium", expires_at: "2026-09-29T11:59:00Z" }),
  ], [subscription()]), UID, NOW);
  const view = membershipView(snapshot, NOW);
  assert.equal(view.hasPremiumAccess, true);
  assert.equal(view.accessLevels[0].source, "subscription");
  assert.equal(view.accessLevels[1].source, "beta");
  assert.equal(view.accessLevels[1].isActive, false);
});

test("beta access survives a paid subscription ending", () => {
  const snapshot = normalizeProfile(profile([
    level({ expires_at: "2026-09-28T10:00:00Z" }),
    level({ access_level_id: "beta_premium" }),
  ]), UID, NOW);
  assert.equal(membershipView(snapshot, NOW).hasPremiumAccess, true);
});

test("renewal cancellation is not immediate access revocation", () => {
  const snapshot = normalizeProfile(profile([level({ renewal_cancelled_at: "2026-09-29T11:00:00Z" })]), UID, NOW);
  assert.equal(membershipView(snapshot, NOW).hasPremiumAccess, true);
  assert.equal(snapshot.accessLevels[0].renewalCancelledAt, "2026-09-29T11:00:00.000Z");
});

test("future access and expired grace flags do not grant unlimited access", () => {
  const future = normalizeProfile(profile([level({ starts_at: "2026-10-01T00:00:00Z" })]), UID, NOW);
  assert.equal(membershipView(future, NOW).hasPremiumAccess, false);
  const expired = normalizeProfile(profile([level({ expires_at: "2026-09-29T10:00:00Z", is_in_grace_period: true })]), UID, NOW);
  assert.equal(membershipView(expired, NOW).hasPremiumAccess, false);
});

test("explicit lifetime grant differs from missing expiration", () => {
  const lifetime = normalizeProfile(profile([level({ starts_at: null, expires_at: null })]), UID, NOW);
  assert.equal(membershipView(lifetime, NOW + 100000000000).hasPremiumAccess, true);
  assert.throws(() => normalizeProfile(profile([level({ expires_at: undefined })]), UID, NOW));
});

test("malformed dates and provider identities are not interpreted as free or lifetime", () => {
  for (const value of ["garbage", "2026-10-29 12:00:00", 42, ""]) {
    assert.throws(() => normalizeProfile(profile([level({ expires_at: value })]), UID, NOW));
  }
  assert.throws(() => normalizeProfile(profile(), "another-user", NOW));
  assert.throws(() => normalizeProfile({ data: { customer_user_id: UID } }, UID, NOW));
  assert.throws(() => normalizeProfile(profile([level(), level()]), UID, NOW));
});

test("unrelated access levels and client-supplied premium fields confer no right", () => {
  const payload = profile([level({ access_level_id: "another_feature" })]);
  payload.data.isPremium = true;
  payload.data.premiumEndDate = "2099-01-01";
  assert.equal(membershipView(normalizeProfile(payload, UID, NOW), NOW).hasPremiumAccess, false);
});

test("unlinked grants are not labelled paid and sensitive profile fields are not retained", () => {
  const payload = profile();
  payload.data.email = "not-to-store@example.invalid";
  payload.data.custom_attributes = [{ key: "name", value: "not-to-store" }];
  const snapshot = normalizeProfile(payload, UID, NOW);
  assert.equal(snapshot.accessLevels[0].source, "manual");
  assert.equal(JSON.stringify(snapshot).includes("not-to-store"), false);
  assert.equal(JSON.stringify(snapshot).includes("original-1"), false);
});

test("provider requests use fixed HTTPS URL and server-only credentials", async () => {
  let request;
  const result = await fetchProfile(UID, "test-api-secret", async (url, init) => {
    request = { url, init };
    return response(200, profile());
  }, NOW);
  assert.equal(request.url, "https://api.adapty.io/api/v2/server-side-api/profile/");
  assert.equal(request.init.headers["adapty-customer-user-id"], UID);
  assert.equal(request.init.headers.Authorization, "Api-Key test-api-secret");
  assert.ok(request.init.signal);
  assert.equal(result.verifiedAt, "2026-09-29T12:00:00.000Z");
  assert.equal(JSON.stringify(result).includes("test-api-secret"), false);
});

test("only confirmed profile-not-found clears entitlements", async () => {
  const snapshot = await fetchProfile(UID, "secret", async () => response(404, { error_code: "profile_does_not_exist" }), NOW);
  assert.deepEqual(snapshot.accessLevels, []);
  for (const status of [401, 403, 404, 429, 500]) {
    await assert.rejects(fetchProfile(UID, "secret", async () => response(status, {}), NOW), { code: "provider-unavailable" });
  }
});

test("network failures, invalid JSON, and malformed successful bodies stay unavailable", async () => {
  await assert.rejects(fetchProfile(UID, "secret", async () => { throw Error("network"); }), { code: "provider-unavailable" });
  await assert.rejects(fetchProfile(UID, "secret", async () => ({ json: async () => { throw Error("json"); } })), { code: "provider-unavailable" });
  await assert.rejects(fetchProfile(UID, "secret", async () => response(200, {})), { code: "invalid-provider-response" });
});

test("webhook parsing supports verification, ignores anonymous profiles, hashes event IDs", () => {
  for (const body of [null, undefined, "", {}]) assert.deepEqual(parseWebhook(body), { verification: true });
  assert.deepEqual(parseWebhook({ event_type: "access_level_updated", customer_user_id: null }), { ignored: true });
  const event = parseWebhook({
    event_type: "subscription_started", customer_user_id: UID,
    event_properties: { profile_event_id: "event-1", email: "not-to-store" },
    event_datetime: "1990-01-01",
  });
  assert.equal(event.uid, UID);
  assert.equal(event.eventHash.length, 64);
  assert.equal(JSON.stringify(event).includes("not-to-store"), false);
  assert.equal(event.event_datetime, undefined);
  for (const body of [[], "json", { customer_user_id: UID }, { event_type: "x", customer_user_id: "bad/id" }]) {
    assert.throws(() => parseWebhook(body), { code: "invalid-event" });
  }
});
