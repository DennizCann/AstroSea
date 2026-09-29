"use strict";

// Operator-only: first match the UID's verified email to the approved Play tester
// list, or obtain explicit approval for the specific verified account.
// Never run for anonymous accounts. No purchases are performed.
const { isDeepStrictEqual } = require("node:util");
const { validUid } = require("../membership");
const uid = process.argv[2];
const createProfile = process.argv[4] === "--create-profile";
if (!validUid(uid) || process.argv[3] !== "--approved-30-days"
    || (process.argv[4] && !createProfile) || process.argv.length > 5) {
  throw new Error("Usage: node functions/scripts/grant-beta.js VERIFIED_MATCHED_UID --approved-30-days");
}
const project = "astrosea-3de22";
let stage = "authentication";
function assert(ok, message) { if (!ok) throw new Error(message); }
async function main() {
  assert(!process.env.NODE_DEBUG && !process.env.DEBUG && !process.env.FIREBASE_TOKEN,
    "Debug logging and token overrides must be disabled");
  const logging = require("firebase-tools/lib/logger");
  logging.logger.silent = true;
  logging.logger.clear();
  const account = require("firebase-tools/lib/auth").getGlobalDefaultAccount();
  await require("firebase-tools/lib/requireAuth").requireAuth({
    project, user: account.user, tokens: account.tokens, nonInteractive: true,
  }, true);
  const { Client } = require("firebase-tools/lib/apiv2");
  const options = () => ({ skipLog: { body: true, resBody: true, queryParams: true }, retries: 0 });
  stage = "verified account";
  const auth = new Client({ urlPrefix: "https://identitytoolkit.googleapis.com", apiVersion: "v1" });
  const users = (await auth.post(`/projects/${project}/accounts:lookup`, { localId: [uid] }, options())).body.users;
  assert(users?.length === 1 && users[0].localId === uid && users[0].emailVerified === true
    && users[0].disabled !== true, "Account must exist, be verified and enabled");
  const db = new Client({ urlPrefix: "https://firestore.googleapis.com", apiVersion: "v1" });
  const parentPath = `/projects/${project}/databases/(default)/documents/users/${uid}`;
  const parent = (await db.get(parentPath, options())).body;
  assert(parent?.fields && parent.fields.membershipDeletionPending?.booleanValue !== true,
    "Existing profile required; deletion must not be pending");
  const { accessSecretVersion } = require("firebase-tools/lib/gcp/secretManager");
  const key = await accessSecretVersion(project, "ADAPTY_SECRET_KEY", "latest");
  const headers = { Authorization: `Api-Key ${key}`, "adapty-customer-user-id": uid, "Content-Type": "application/json" };
  async function provider(path, body, allowMissing = false) {
    const res = await fetch(`https://api.adapty.io/api/v2/server-side-api/${path}`, {
      method: body ? "POST" : "GET", headers, redirect: "error",
      signal: AbortSignal.timeout(20000), ...(body ? { body: JSON.stringify(body) } : {}),
    });
    const payload = await res.json();
    if (allowMissing && res.status === 404 && payload.error_code === "profile_does_not_exist") return null;
    assert(res.status === 200, `Provider returned HTTP ${res.status}`);
    const data = payload.data;
    assert(data?.customer_user_id === uid && Array.isArray(data.access_levels), "Profile identity mismatch");
    return data;
  }
  stage = "existing Adapty profile";
  let before = await provider("profile/", undefined, createProfile);
  if (!before) {
    stage = "approved profile creation";
    // Synthetic provisioning identifier, not a collected hardware/advertising ID.
    await provider("profile/", {
      analytics_disabled: true,
      installation_meta: { device_id: require("node:crypto").randomUUID(), platform: "Android" },
    });
    before = await provider("profile/");
  }
  const current = before.access_levels.find(x => x.access_level_id === "beta_premium");
  const now = Date.now();
  if (current && Date.parse(current.expires_at) > now + 29.9 * 86400000) {
    console.log("SKIP: approximately 30 days of beta access already present; not extended");
    return;
  }
  const startsAt = new Date(now).toISOString();
  const expiresAt = new Date(now + 30 * 86400000).toISOString();
  stage = "approved beta grant";
  await provider("purchase/profile/grant/access-level/", {
    access_level_id: "beta_premium", starts_at: startsAt, expires_at: expiresAt,
  });
  stage = "grant verification";
  const after = await provider("profile/");
  const beta = after.access_levels.find(x => x.access_level_id === "beta_premium");
  assert(beta && Math.abs(Date.parse(beta.expires_at) - Date.parse(expiresAt)) < 1000,
    "Unexpected beta expiry");
  assert(isDeepStrictEqual(before.subscriptions, after.subscriptions), "Subscription data changed; inspect before continuing");
  assert(isDeepStrictEqual(before.access_levels.filter(x => x.access_level_id !== "beta_premium"),
    after.access_levels.filter(x => x.access_level_id !== "beta_premium")), "Other access changed; inspect");
  assert(isDeepStrictEqual(parent, (await db.get(parentPath, options())).body), "Public profile changed; inspect");
  console.log(JSON.stringify({ granted: "beta_premium", startsAt, expiresAt, days: 30, subscriptionsUnchanged: true }));
}
main().catch(() => { console.error(`STOP: beta operation failed at ${stage}. Inspect state before retry; no blind retries.`); process.exitCode = 1; });
