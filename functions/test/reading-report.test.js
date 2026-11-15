"use strict";

const { test } = require("node:test");
const assert = require("node:assert/strict");
const { createReadingReportHandler } = require("../reading-report");

function setup(user = {}) {
  const documents = new Map(user ? [["users/alice", user]] : []);
  const reference = path => ({
    path,
    collection: name => reference(`${path}/${name}`),
    doc: id => reference(`${path}/${id}`),
  });
  const db = {
    collection: reference,
    async runTransaction(callback) {
      const writes = new Map();
      const result = await callback({
        get: async ({ path }) => ({
          exists: documents.has(path),
          get: key => documents.get(path)?.[key],
        }),
        set: ({ path }, data) => writes.set(path, data),
        create: ({ path }, data) => {
          assert.equal(documents.has(path), false);
          writes.set(path, data);
        },
      });
      for (const [path, data] of writes) documents.set(path, data);
      return result;
    },
  };
  let now = new Date("2026-11-15T10:00:00Z");
  return {
    handler: createReadingReportHandler(db, () => now),
    documents,
    nextDay: () => { now = new Date("2026-11-16T10:00:00Z"); },
  };
}

const request = reading => ({ auth: { uid: "alice" }, data: { reading } });
const quotaPath = "users/alice/private/reading_report_quota";

test("a report stores only the reading and date under the signed-in account", async () => {
  const { handler, documents } = setup();
  assert.deepEqual(await handler(request("İncelenecek yorum")), { reported: true });
  assert.deepEqual(documents.get(quotaPath), { date: "2026-11-15", count: 1 });
  const reports = [...documents].filter(([path]) => path.startsWith(`${quotaPath}/readingReports/`));
  assert.equal(reports.length, 1);
  assert.deepEqual(reports[0][1], {
    reading: "İncelenecek yorum",
    createdAt: new Date("2026-11-15T10:00:00Z"),
  });
});

test("retrying the same report does not duplicate it or use another allowance", async () => {
  const { handler, documents } = setup();
  await handler(request("Sample reading"));
  assert.deepEqual(await handler(request("Sample reading")), { reported: true });
  assert.equal(documents.get(quotaPath).count, 1);
  assert.equal(documents.size, 3);
});

test("invalid reports are rejected without writing a document", async () => {
  const { handler, documents } = setup();
  await assert.rejects(handler({ data: { reading: "Sample reading" } }), { code: "unauthenticated" });
  for (const data of [{ reading: " " }, { reading: "ş".repeat(32769) }, { reading: "Sample", email: "extra" }]) {
    await assert.rejects(handler({ ...request("Sample"), data }), { code: "invalid-argument" });
  }
  assert.equal(documents.size, 1);
});

test("a report does not recreate account data after deletion starts", async () => {
  for (const user of [null, { membershipDeletionPending: true }]) {
    const { handler, documents } = setup(user);
    await assert.rejects(handler(request("Sample reading")), { code: "failed-precondition" });
    assert.equal(documents.has(quotaPath), false);
  }
});

test("daily report limiting preserves retries and resets the following day", async () => {
  const { handler, documents, nextDay } = setup();
  for (let i = 0; i < 10; i++) await handler(request(`Reading ${i}`));
  await assert.rejects(handler(request("Another reading")), { code: "resource-exhausted" });
  assert.deepEqual(await handler(request("Reading 0")), { reported: true });
  nextDay();
  await handler(request("Another reading"));
  assert.deepEqual(documents.get(quotaPath), { date: "2026-11-16", count: 1 });
});
