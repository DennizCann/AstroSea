"use strict";
// Explicit operator action; use SHA-256 of exact trimmed/lowercase Play emails.
// Does not start the 30-day period or overwrite an existing grant/UID binding.
const hashes = (process.argv[2] || "").split(",");
if (hashes.length !== 15 || new Set(hashes).size !== 15 || hashes.some(h => !/^[a-f0-9]{64}$/.test(h))) {
  throw new Error("Supply exactly 15 unique SHA-256 email hashes");
}
async function main() {
  if (process.env.NODE_DEBUG || process.env.DEBUG || process.env.FIREBASE_TOKEN) throw new Error("Debug/override refused");
  const logging = require("firebase-tools/lib/logger"); logging.logger.silent = true; logging.logger.clear();
  const account = require("firebase-tools/lib/auth").getGlobalDefaultAccount();
  const project = "astrosea-3de22";
  await require("firebase-tools/lib/requireAuth").requireAuth({ project, user: account.user, tokens: account.tokens, nonInteractive: true }, true);
  const { Client } = require("firebase-tools/lib/apiv2");
  const db = new Client({ urlPrefix: "https://firestore.googleapis.com", apiVersion: "v1" });
  const root = `projects/${project}/databases/(default)/documents`;
  const options = () => ({ skipLog: { body: true, resBody: true }, retries: 0 });
  const writes = hashes.map(hash => ({ update: {
    name: `${root}/betaTesters/${hash}`, fields: { enabled: { booleanValue: true }, days: { integerValue: "30" },
      campaign: { stringValue: "closed-beta-2026-09" } },
  }, updateMask: { fieldPaths: ["enabled", "days", "campaign"] } }));
  await db.post(`/${root}:commit`, { writes }, options());
  for (const hash of hashes) {
    const doc = (await db.get(`/${root}/betaTesters/${hash}`, options())).body;
    if (doc.fields?.enabled?.booleanValue !== true || doc.fields?.days?.integerValue !== "30") throw new Error("Verification failed");
  }
  console.log("PASS 15 approved email hashes enabled for one-time 30-day beta; existing grant fields preserved");
}
main().catch(() => { console.error("Beta list setup failed; inspect before retry"); process.exitCode = 1; });
