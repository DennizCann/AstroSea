"use strict";
const { test } = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const vm = require("node:vm");
const readingService = require("../reading-service");

// Exercise the actual callable handler and quota error path without Firebase or paid AI requests.
function harness(providerError) {
  const state = { count: 0, providerCalls: 0 };
  const ref = { collection: () => ref, doc: () => ref };
  const db = { collection: () => ref, runTransaction: async action => action({
    get: async () => ({ exists: true, get: key => key === "date" ? new Date().toISOString().slice(0, 10) : key === "count" ? state.count : false }),
    set: (_ref, value) => { state.count = value.count; },
    update: (_ref, value) => { state.count = value.count; },
  }) };
  class HttpsError extends Error { constructor(code, message) { super(message); this.code = code; } }
  const modules = {
    "firebase-functions/v2": { setGlobalOptions() {}, logger: { error() {}, warn() {} } },
    "firebase-functions/v2/https": { HttpsError, onCall: (_options, handler) => handler, onRequest: (_options, handler) => handler },
    "firebase-functions/params": { defineSecret: () => ({ value: () => "test-only" }) },
    "firebase-admin/app": { initializeApp() {} },
    "firebase-admin/auth": { getAuth: () => ({}) },
    "firebase-admin/firestore": { getFirestore: () => db },
    "./membership": { readingLimit: () => 3 },
    "./membership-service": { createMembershipService: () => ({ refresh: async () => ({ hasPremiumAccess: false }) }) },
    "./beta-service": { createBetaService: () => ({}) },
    "./reading-service": { prepareReading: readingService.prepareReading, requestReading: async () => {
      state.providerCalls++;
      if (providerError) throw Object.assign(new Error("Simulated"), { code: providerError });
      return "Unchanged reading";
    } },
  };
  const context = { exports: {}, require: name => { if (!(name in modules)) throw Error(name); return modules[name]; } };
  vm.runInNewContext(fs.readFileSync(require.resolve("../index.js"), "utf8"), context);
  return { state, call: context.exports.generateTarotReading };
}

test("handler validates before reserving quota or calling provider", async () => {
  const { state, call } = harness();
  await assert.rejects(call({ auth: { uid: "test" }, data: { schemaVersion: 2 } }), { code: "invalid-argument" });
  await assert.rejects(call({ data: {} }), { code: "unauthenticated" });
  assert.deepEqual(state, { count: 0, providerCalls: 0 });
});

test("handler releases quota for incomplete and failed replies", async () => {
  for (const error of ["incomplete-response", "provider-busy", "provider-failed", "network-failed"]) {
    const { state, call } = harness(error);
    await assert.rejects(call({ auth: { uid: "test" }, data: { prompt: "Legacy request", isTurkish: true } }));
    assert.equal(state.count, 0);
    assert.equal(state.providerCalls, 1);
  }
});

test("successful v2 response retains existing reading field and consumes one allowance", async () => {
  const { state, call } = harness();
  const result = await call({ auth: { uid: "test" }, data: {
    schemaVersion: 2, spreadId: "single_card_reading", language: "en", cardIds: ["fool"],
  } });
  assert.equal(result.reading, "Unchanged reading");
  assert.equal(state.count, 1);
  assert.equal(state.providerCalls, 1);
});
