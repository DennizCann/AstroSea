"use strict";

const { createHash, randomUUID } = require("node:crypto");
const { MembershipError, validUid, normalizeProfile } = require("./membership");
const DAY = 86400000;
const emailHash = email => createHash("sha256").update(email.trim().toLowerCase()).digest("hex");

// betaTesters is server-only (no matching client Firestore allow rule). A grant
// is bound to one verified UID and is never restarted by login or re-registration.
function createBetaService({ db, auth, secret, fetchImpl = fetch, clock = Date.now }) {
  async function ensure(uid) {
    if (!validUid(uid)) throw new MembershipError("invalid-user");
    const account = await auth.getUser(uid);
    if (account.disabled) throw new MembershipError("account-unavailable");
    if (!account.emailVerified || !account.email) return false;
    const hash = emailHash(account.email);
    const ref = db.collection("betaTesters").doc(hash);
    const user = db.collection("users").doc(uid);
    const token = randomUUID();
    const now = clock();
    function eligible(parent, entry) {
      if (!parent.exists || parent.get("membershipDeletionPending") === true) {
        throw new MembershipError("account-unavailable");
      }
      return entry?.enabled === true && entry.days === 30 && (!entry.uid || entry.uid === uid);
    }
    const grant = await db.runTransaction(async tx => {
      const [parent, record] = await Promise.all([tx.get(user), tx.get(ref)]);
      const entry = record.data();
      if (!eligible(parent, entry) || entry.status === "complete") return null;
      if (entry.leaseUntil > now) throw new MembershipError("refresh-in-progress");
      const startedAt = entry.startedAt ?? now;
      const expiresAt = entry.expiresAt ?? startedAt + 30 * DAY;
      if (!Number.isFinite(startedAt) || !Number.isFinite(expiresAt) || expiresAt <= now) return null;
      tx.set(ref, { uid, startedAt, expiresAt, token, leaseUntil: now + 60000, status: "pending" }, { merge: true });
      return { startedAt, expiresAt };
    });
    if (!grant) return false;

    async function stillEligible() {
      const current = await auth.getUser(uid);
      if (current.disabled || !current.emailVerified || !current.email || emailHash(current.email) !== hash) {
        throw new MembershipError("account-unavailable");
      }
      await db.runTransaction(async tx => {
        const [parent, record] = await Promise.all([tx.get(user), tx.get(ref)]);
        if (!eligible(parent, record.data()) || record.get("token") !== token) {
          throw new MembershipError("account-unavailable");
        }
      });
    }
    async function provider(path, body, allowMissing = false) {
      const response = await fetchImpl(`https://api.adapty.io/api/v2/server-side-api/${path}`, {
        method: body ? "POST" : "GET", redirect: "error", signal: AbortSignal.timeout(5000),
        headers: { Authorization: `Api-Key ${secret()}`, "adapty-customer-user-id": uid, "Content-Type": "application/json" },
        ...(body ? { body: JSON.stringify(body) } : {}),
      });
      const payload = await response.json();
      if (allowMissing && response.status === 404 && payload?.error_code === "profile_does_not_exist") return null;
      if (response.status !== 200) throw new MembershipError("provider-unavailable");
      return normalizeProfile(payload, uid, clock());
    }
    let profile = await provider("profile/", undefined, true);
    if (!profile) {
      await stillEligible();
      // Random server-provisioning identifier, not hardware or advertising data.
      profile = await provider("profile/", {
        analytics_disabled: true,
        installation_meta: { device_id: randomUUID(), platform: "Android" },
      });
    }
    let beta = profile.accessLevels.find(level => level.id === "beta_premium");
    if (!beta) {
      await stillEligible();
      profile = await provider("purchase/profile/grant/access-level/", {
        access_level_id: "beta_premium",
        starts_at: new Date(grant.startedAt).toISOString(),
        expires_at: new Date(grant.expiresAt).toISOString(),
      });
      beta = profile.accessLevels.find(level => level.id === "beta_premium");
      if (!beta || Date.parse(beta.expiresAt) !== grant.expiresAt) throw new MembershipError("invalid-provider-response");
    }
    // Preserve an existing grant, including expired/revoked grants; no automatic extension.
    await stillEligible();
    await db.runTransaction(async tx => {
      const [parent, record] = await Promise.all([tx.get(user), tx.get(ref)]);
      if (!eligible(parent, record.data()) || record.get("token") !== token) throw new MembershipError("refresh-in-progress");
      tx.set(ref, { status: "complete", leaseUntil: 0, completedAt: clock(),
        providerExpiresAt: beta.expiresAt, providerStartsAt: beta.startsAt }, { merge: true });
    });
    return true;
  }
  return { ensure };
}
module.exports = { createBetaService, emailHash };
