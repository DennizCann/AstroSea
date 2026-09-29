"use strict";
const { test } = require("node:test");
const assert = require("node:assert/strict");
const { createBetaService, emailHash } = require("../beta-service");
const NOW = Date.parse("2026-09-30T00:00:00Z");
const DAY = 86400000;
function fixture({ listed = true, verified = true, existing = null, missing = false } = {}) {
  let now = NOW;
  const account = { uid: "beta-user", email: "Tester@example.com", emailVerified: verified };
  const path = `betaTesters/${emailHash(account.email)}`;
  const records = new Map([["users/beta-user", {}]]);
  if (listed) records.set(path, { enabled: true, days: 30 });
  const calls = [];
  let levels = existing ? [existing] : [];
  let failGrant = false;
  const db = { collection: c => ({ doc: id => ({ path: `${c}/${id}` }) }),
    async runTransaction(fn) {
      return fn({ get: async ref => ({ exists: records.has(ref.path), data: () => structuredClone(records.get(ref.path)),
        get: key => records.get(ref.path)?.[key] }),
      set: (ref, value) => records.set(ref.path, { ...records.get(ref.path), ...value }) });
    } };
  const fetchImpl = async (url, options) => {
    calls.push({ url, options });
    if (options.method === "GET" && missing) return { status: 404, json: async () => ({ error_code: "profile_does_not_exist" }) };
    if (options.method === "POST" && url.endsWith("/profile/")) missing = false;
    if (url.includes("grant/access-level")) {
      if (failGrant) throw new Error("Provider outage");
      const body = JSON.parse(options.body);
      levels = [{ access_level_id: "beta_premium", starts_at: body.starts_at, expires_at: body.expires_at }];
    }
    return { status: 200, json: async () => ({ data: { customer_user_id: account.uid, access_levels: levels, subscriptions: [] } }) };
  };
  const service = createBetaService({ db, auth: { getUser: async () => ({ ...account }) }, secret: () => "test-only", fetchImpl, clock: () => now });
  return { service, records, path, calls, account, time: t => { now = t; }, fail: value => { failGrant = value; } };
}
test("email normalization only trims/case folds; aliases remain distinct", () => {
  assert.equal(emailHash(" Tester@EXAMPLE.com "), emailHash("tester@example.com"));
  assert.notEqual(emailHash("tester+other@example.com"), emailHash("tester@example.com"));
});
test("unlisted and unverified users get no provider calls or grants", async () => {
  for (const options of [{ listed: false }, { verified: false }]) {
    const f = fixture(options); assert.equal(await f.service.ensure("beta-user"), false); assert.equal(f.calls.length, 0);
  }
});
test("listed verified account receives exactly 30 days once", async () => {
  const f = fixture({ missing: true });
  assert.equal(await f.service.ensure("beta-user"), true);
  const body = JSON.parse(f.calls.find(c => c.url.includes("grant/access-level")).options.body);
  assert.equal(Date.parse(body.expires_at) - Date.parse(body.starts_at), 30 * DAY);
  assert.equal(f.records.get(f.path).status, "complete");
  f.time(NOW + 31 * DAY);
  assert.equal(await f.service.ensure("beta-user"), false);
  assert.equal(f.calls.filter(c => c.url.includes("grant/access-level")).length, 1);
});
test("existing active or expired beta is preserved without regrant", async () => {
  for (const offset of [-DAY, 10 * DAY]) {
    const expiry = new Date(NOW + offset).toISOString();
    const f = fixture({ existing: { access_level_id: "beta_premium", starts_at: null, expires_at: expiry } });
    await f.service.ensure("beta-user");
    assert.equal(f.calls.length, 1);
    assert.equal(f.records.get(f.path).providerExpiresAt, expiry);
  }
});
test("failure retry preserves the original expiry and concurrent calls are blocked", async () => {
  const f = fixture(); f.fail(true);
  await assert.rejects(f.service.ensure("beta-user"));
  const expiry = f.records.get(f.path).expiresAt;
  await assert.rejects(f.service.ensure("beta-user"), { code: "refresh-in-progress" });
  f.time(NOW + 61000); f.fail(false);
  await f.service.ensure("beta-user");
  assert.equal(f.records.get(f.path).expiresAt, expiry);
});
test("disabled, deleting, different UID binding and revoked list entries cannot grant", async () => {
  const disabled = fixture(); disabled.account.disabled = true;
  await assert.rejects(disabled.service.ensure("beta-user"), { code: "account-unavailable" });
  const deleting = fixture(); deleting.records.set("users/beta-user", { membershipDeletionPending: true });
  await assert.rejects(deleting.service.ensure("beta-user"), { code: "account-unavailable" });
  for (const value of [{ uid: "previous-account" }, { enabled: false }]) {
    const f = fixture(); Object.assign(f.records.get(f.path), value);
    assert.equal(await f.service.ensure("beta-user"), false); assert.equal(f.calls.length, 0);
  }
});
