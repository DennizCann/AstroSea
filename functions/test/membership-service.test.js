"use strict";

const assert = require("node:assert/strict");
const { test } = require("node:test");
const { createMembershipService } = require("../membership-service");

const UID = "membership-test-user";
const USER_PATH = `users/${UID}`;
const STATE_PATH = `${USER_PATH}/private/membershipState`;
const NOW = Date.parse("2026-09-29T12:00:00.000Z");

function copy(value) {
  return value === undefined ? undefined : structuredClone(value);
}

// Models optimistic transaction retries so the tests exercise parent deletion
// conflicts as well as sequential document reads. No Firebase service is used.
function memoryFirestore() {
  const documents = new Map();
  const versions = new Map();
  let beforeCommit = null;
  let retries = 0;
  function reference(path) {
    return { path, collection: (name) => collection(`${path}/${name}`) };
  }
  function collection(path) {
    return { doc: (id) => reference(`${path}/${id}`) };
  }
  function write(path, value) {
    if (value === undefined) documents.delete(path);
    else documents.set(path, copy(value));
    versions.set(path, (versions.get(path) ?? 0) + 1);
  }
  const db = {
    collection,
    async runTransaction(callback) {
      for (let attempt = 0; attempt < 10; attempt += 1) {
        const reads = new Map();
        const writes = [];
        const result = await callback({
          async get(ref) {
            const value = copy(documents.get(ref.path));
            reads.set(ref.path, versions.get(ref.path) ?? 0);
            return {
              exists: value !== undefined,
              data: () => copy(value),
              get: (field) => copy(value?.[field]),
            };
          },
          set: (ref, value, options) => writes.push({ ref, value, merge: options?.merge }),
          update: (ref, value) => writes.push({ ref, value, merge: true, update: true }),
        });
        if (beforeCommit) {
          const hook = beforeCommit;
          beforeCommit = null;
          await hook({ reads, writes });
        }
        if ([...reads].some(([path, version]) => (versions.get(path) ?? 0) !== version)) {
          retries += 1;
          continue;
        }
        for (const operation of writes) {
          const { ref, value, merge, update } = operation;
          if (update && !documents.has(ref.path)) throw new Error("Cannot update missing document");
          write(ref.path, merge ? { ...documents.get(ref.path), ...copy(value) } : value);
        }
        return result;
      }
      throw new Error("Transaction retry limit exceeded");
    },
  };
  return {
    db,
    read: (path) => copy(documents.get(path)),
    write,
    deleteTree(path) {
      for (const candidate of [...documents.keys()]) {
        if (candidate === path || candidate.startsWith(`${path}/`)) write(candidate, undefined);
      }
    },
    interceptNextCommit: (hook) => { beforeCommit = hook; },
    get retries() { return retries; },
  };
}

function profileResponse(active = true) {
  return {
    ok: true,
    status: 200,
    async json() {
      return {
        data: {
          customer_user_id: UID,
          access_levels: active ? [{
            access_level_id: "premium",
            store: "play_store",
            store_product_id: "astrosea_monthly",
            store_original_transaction_id: "test-transaction",
            starts_at: "2026-09-01T00:00:00.000Z",
            expires_at: "2026-10-01T00:00:00.000Z",
            renewal_cancelled_at: null,
            billing_issue_detected_at: null,
            is_in_grace_period: false,
          }] : [],
          subscriptions: active ? [{
            store: "play_store",
            store_product_id: "astrosea_monthly",
            store_original_transaction_id: "test-transaction",
            environment: "Production",
          }] : [],
        },
      };
    },
  };
}

function deferred() {
  let resolve;
  let reject;
  const promise = new Promise((yes, no) => { resolve = yes; reject = no; });
  return { promise, resolve, reject };
}

function fixture() {
  const store = memoryFirestore();
  const accounts = new Map([[UID, { uid: UID, disabled: false }]]);
  const calls = [];
  let now = NOW;
  let provider = async () => profileResponse();
  let authFailure = null;
  store.write(USER_PATH, { displayName: "Test", isPremium: false });
  const service = createMembershipService({
    db: store.db,
    auth: {
      async getUser(uid) {
        if (authFailure) throw authFailure;
        if (!accounts.has(uid)) throw Object.assign(new Error("Missing account"), { code: "auth/user-not-found" });
        return copy(accounts.get(uid));
      },
    },
    secret: () => "local-test-secret",
    clock: () => now,
    fetchImpl: async (...args) => {
      calls.push(args);
      return provider(...args);
    },
  });
  return {
    store, service, accounts, calls,
    advance: (milliseconds) => { now += milliseconds; },
    provider: (implementation) => { provider = implementation; },
    authFailure: (error) => { authFailure = error; },
    pendingProvider() {
      const started = deferred();
      const result = deferred();
      provider = async () => { started.resolve(); return result.promise; };
      return { started: started.promise, resolve: result.resolve, reject: result.reject };
    },
  };
}

function rejectsCode(promise, code) {
  return assert.rejects(promise, (error) => error?.code === code);
}

test("refresh stores verified state privately and leaves legacy membership untouched", async () => {
  const f = fixture();
  const view = await f.service.refresh(UID);
  assert.equal(view.hasPremiumAccess, true);
  assert.deepEqual(f.store.read(USER_PATH), { displayName: "Test", isPremium: false });
  const state = f.store.read(STATE_PATH);
  assert.equal(state.revision, 1);
  assert.equal(state.completedRevision, 1);
  assert.equal(state.snapshot.accessLevels[0].source, "subscription");
  assert.equal(f.calls[0][1].headers["adapty-customer-user-id"], UID);
});

test("a committed event retry uses its snapshot without another provider request", async () => {
  const f = fixture();
  const first = await f.service.refresh(UID, { webhook: true, eventHash: "event-one" });
  f.advance(60000);
  const retry = await f.service.refresh(UID, { webhook: true, eventHash: "event-one" });
  assert.deepEqual(retry, first);
  assert.equal(f.calls.length, 1);
  assert.equal(f.store.read(STATE_PATH).revision, 1);
});

test("a distinct webhook bypasses the callable throttle", async () => {
  const f = fixture();
  await f.service.refresh(UID, { webhook: true, eventHash: "event-one" });
  f.provider(async () => profileResponse(false));
  const second = await f.service.refresh(UID, { webhook: true, eventHash: "event-two" });
  assert.equal(second.hasPremiumAccess, false);
  assert.equal(f.calls.length, 2);
  assert.equal(f.store.read(STATE_PATH).lastEventHash, "event-two");
});

test("callable refresh reuses completed state before 30 seconds and refreshes at the boundary", async () => {
  const f = fixture();
  await f.service.refresh(UID);
  f.provider(async () => profileResponse(false));
  f.advance(29999);
  assert.equal((await f.service.refresh(UID)).hasPremiumAccess, true);
  assert.equal(f.calls.length, 1);
  f.advance(1);
  assert.equal((await f.service.refresh(UID)).hasPremiumAccess, false);
  assert.equal(f.calls.length, 2);
});

test("an in-flight callable refresh blocks another callable without another provider request", async () => {
  const f = fixture();
  const pending = f.pendingProvider();
  const first = f.service.refresh(UID);
  await pending.started;
  await rejectsCode(f.service.refresh(UID), "refresh-in-progress");
  assert.equal(f.calls.length, 1);
  pending.resolve(profileResponse());
  assert.equal((await first).hasPremiumAccess, true);
});

test("an older provider response cannot overwrite or return a newer completed snapshot", async () => {
  const f = fixture();
  const pending = f.pendingProvider();
  const older = f.service.refresh(UID, { webhook: true, eventHash: "older" });
  await pending.started;
  f.provider(async () => profileResponse(false));
  const newer = await f.service.refresh(UID, { webhook: true, eventHash: "newer" });
  pending.resolve(profileResponse(true));
  assert.deepEqual(await older, newer);
  assert.equal(newer.hasPremiumAccess, false);
  const state = f.store.read(STATE_PATH);
  assert.equal(state.revision, 2);
  assert.equal(state.completedRevision, 2);
  assert.equal(state.lastEventHash, "newer");
  assert.deepEqual(state.snapshot.accessLevels, []);
});

test("a superseded response fails while the newer refresh remains pending", async () => {
  const f = fixture();
  const firstProvider = f.pendingProvider();
  const older = f.service.refresh(UID, { webhook: true, eventHash: "older" });
  const olderRejected = rejectsCode(older, "refresh-in-progress");
  await firstProvider.started;
  const secondProvider = f.pendingProvider();
  const newer = f.service.refresh(UID, { webhook: true, eventHash: "newer" });
  await secondProvider.started;
  firstProvider.resolve(profileResponse(true));
  await olderRejected;
  assert.equal(f.store.read(STATE_PATH).snapshot, undefined);
  secondProvider.resolve(profileResponse(false));
  assert.equal((await newer).hasPremiumAccess, false);
});

test("provider failure retains the verified snapshot and allows a later retry", async () => {
  const f = fixture();
  await f.service.refresh(UID);
  const verified = f.store.read(STATE_PATH).snapshot;
  f.advance(30000);
  f.provider(async () => { throw new Error("Simulated network failure"); });
  await rejectsCode(f.service.refresh(UID), "provider-unavailable");
  assert.deepEqual(f.store.read(STATE_PATH).snapshot, verified);
  assert.equal(f.store.read(STATE_PATH).completedRevision, 1);
  assert.equal(f.store.read(STATE_PATH).revision, 2);
  await rejectsCode(f.service.refresh(UID), "refresh-in-progress");
  assert.equal(f.calls.length, 2);
  f.advance(30000);
  f.provider(async () => profileResponse(false));
  assert.equal((await f.service.refresh(UID)).hasPremiumAccess, false);
  assert.equal(f.store.read(STATE_PATH).completedRevision, 3);
});

test("a failed event is not marked completed and its webhook retry can recover immediately", async () => {
  const f = fixture();
  f.provider(async () => { throw new Error("Simulated network failure"); });
  await rejectsCode(f.service.refresh(UID, { webhook: true, eventHash: "retry-event" }), "provider-unavailable");
  assert.equal(f.store.read(STATE_PATH).lastEventHash, undefined);
  f.provider(async () => profileResponse());
  assert.equal((await f.service.refresh(UID, { webhook: true, eventHash: "retry-event" })).hasPremiumAccess, true);
  assert.equal(f.calls.length, 2);
  assert.equal(f.store.read(STATE_PATH).lastEventHash, "retry-event");
});

test("missing, disabled, and deleted accounts are refused before provider work", async (t) => {
  for (const condition of ["missing-parent", "missing-auth", "disabled", "deletion-pending"]) {
    await t.test(condition, async () => {
      const f = fixture();
      if (condition === "missing-parent") f.store.write(USER_PATH, undefined);
      if (condition === "missing-auth") f.accounts.delete(UID);
      if (condition === "disabled") f.accounts.set(UID, { uid: UID, disabled: true });
      if (condition === "deletion-pending") f.store.write(USER_PATH, { membershipDeletionPending: true });
      await rejectsCode(f.service.refresh(UID), "account-unavailable");
      assert.equal(f.calls.length, 0);
      assert.equal(f.store.read(STATE_PATH), undefined);
    });
  }
});

test("an Auth service failure does not become free membership or mutate state", async () => {
  const f = fixture();
  f.authFailure(Object.assign(new Error("Auth unavailable"), { code: "auth/internal-error" }));
  await rejectsCode(f.service.refresh(UID), "membership-unavailable");
  assert.equal(f.calls.length, 0);
  assert.equal(f.store.read(STATE_PATH), undefined);
});

test("invalid UIDs cannot create membership state", async () => {
  const f = fixture();
  await rejectsCode(f.service.refresh("not/a/user"), "invalid-user");
  assert.equal(f.calls.length, 0);
  assert.equal(f.store.read(STATE_PATH), undefined);
});

test("account deletion during provider fetch does not recreate deleted private state", async () => {
  const f = fixture();
  const pending = f.pendingProvider();
  const refresh = f.service.refresh(UID);
  const rejected = rejectsCode(refresh, "account-unavailable");
  await pending.started;
  f.store.deleteTree(USER_PATH);
  f.accounts.delete(UID);
  pending.resolve(profileResponse());
  await rejected;
  assert.equal(f.store.read(USER_PATH), undefined);
  assert.equal(f.store.read(STATE_PATH), undefined);
});

test("beginDeletion fences an ordinary in-flight callback while preserving legacy fields", async () => {
  const f = fixture();
  const pending = f.pendingProvider();
  const refresh = f.service.refresh(UID);
  const rejected = rejectsCode(refresh, "account-unavailable");
  await pending.started;
  await f.service.beginDeletion(UID);
  assert.deepEqual(f.store.read(USER_PATH), {
    displayName: "Test", isPremium: false, membershipDeletionPending: true,
  });
  pending.resolve(profileResponse());
  await rejected;
  assert.equal(f.store.read(STATE_PATH).snapshot, undefined);
  await rejectsCode(f.service.refresh(UID), "account-unavailable");
  assert.equal(f.calls.length, 1);
});

test("a deletion fence written between transaction read and commit forces a retry and rejection", async () => {
  const f = fixture();
  const pending = f.pendingProvider();
  const refresh = f.service.refresh(UID);
  const rejected = rejectsCode(refresh, "account-unavailable");
  await pending.started;
  f.store.interceptNextCommit(async () => {
    await f.service.beginDeletion(UID);
  });
  pending.resolve(profileResponse());
  await rejected;
  assert.equal(f.store.retries, 1);
  assert.equal(f.store.read(STATE_PATH).snapshot, undefined);
});

test("beginDeletion is idempotent and does not recreate a missing parent", async () => {
  const f = fixture();
  await f.service.beginDeletion(UID);
  await f.service.beginDeletion(UID);
  assert.equal(f.store.read(USER_PATH).membershipDeletionPending, true);
  f.store.deleteTree(USER_PATH);
  await f.service.beginDeletion(UID);
  assert.equal(f.store.read(USER_PATH), undefined);
  assert.equal(f.store.read(STATE_PATH), undefined);
});

test("cached event handling still checks the account deletion fence", async () => {
  const f = fixture();
  await f.service.refresh(UID, { webhook: true, eventHash: "event-one" });
  await f.service.beginDeletion(UID);
  await rejectsCode(f.service.refresh(UID, { webhook: true, eventHash: "event-one" }), "account-unavailable");
  assert.equal(f.calls.length, 1);
});

test("Auth deleted or disabled during the provider fetch cannot receive a new snapshot", async (t) => {
  for (const condition of ["deleted", "disabled"]) {
    await t.test(condition, async () => {
      const f = fixture();
      const pending = f.pendingProvider();
      const refresh = f.service.refresh(UID);
      const rejected = rejectsCode(refresh, "account-unavailable");
      await pending.started;
      if (condition === "deleted") f.accounts.delete(UID);
      else f.accounts.set(UID, { uid: UID, disabled: true });
      pending.resolve(profileResponse());
      await rejected;
      assert.equal(f.store.read(STATE_PATH).snapshot, undefined);
    });
  }
});

test("a recreated state document cannot reuse a previous in-flight refresh token", async () => {
  const f = fixture();
  const pending = f.pendingProvider();
  const older = f.service.refresh(UID, { webhook: true });
  await pending.started;
  const oldToken = f.store.read(STATE_PATH).refreshToken;
  f.store.write(STATE_PATH, undefined);
  f.provider(async () => profileResponse(false));
  const newer = await f.service.refresh(UID, { webhook: true });
  assert.equal(f.store.read(STATE_PATH).revision, 1);
  assert.notEqual(f.store.read(STATE_PATH).refreshToken, oldToken);
  pending.resolve(profileResponse(true));
  assert.deepEqual(await older, newer);
  assert.equal((await older).hasPremiumAccess, false);
});
