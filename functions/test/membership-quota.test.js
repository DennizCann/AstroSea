"use strict";
const { test } = require("node:test");
const assert = require("node:assert/strict");
const { readingLimit, membershipView } = require("../membership");

test("legacy public premium flag cannot select premium quota", () => {
  assert.throws(() => readingLimit({ isPremium: true }));
  assert.equal(readingLimit({ schemaVersion: 1, hasPremiumAccess: false, isPremium: true }), 3);
});

test("verified paid, beta and sandbox access use the same premium limit", () => {
  for (const source of ["subscription", "beta", "sandbox"]) {
    const view = membershipView({ schemaVersion: 1, accessLevels: [
      { source, startsAt: null, expiresAt: "2099-01-01T00:00:00.000Z" },
    ] });
    assert.equal(readingLimit(view), 25);
  }
});

test("expired access falls back to free limit", () => {
  const view = membershipView({ schemaVersion: 1, accessLevels: [
    { source: "beta", startsAt: null, expiresAt: "2000-01-01T00:00:00.000Z" },
  ] });
  assert.equal(readingLimit(view), 3);
});

test("unknown membership is not silently classified as free", () => {
  assert.throws(() => readingLimit(null));
  assert.throws(() => readingLimit({ schemaVersion: 1 }));
});
