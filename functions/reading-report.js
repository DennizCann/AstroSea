"use strict";

const { createHash } = require("node:crypto");
const { HttpsError } = require("firebase-functions/v2/https");
const { validUid } = require("./membership");

const MAX_REPORT_BYTES = 64 * 1024;
const DAILY_REPORT_LIMIT = 10;

function createReadingReportHandler(db, clock = () => new Date()) {
  return async (request) => {
    const uid = request.auth?.uid;
    if (!validUid(uid)) throw new HttpsError("unauthenticated", "Sign in to report a reading.");

    const data = request.data;
    if (!data || typeof data !== "object" || Object.keys(data).length !== 1 ||
        typeof data.reading !== "string" || !data.reading.trim() ||
        Buffer.byteLength(data.reading, "utf8") > MAX_REPORT_BYTES) {
      throw new HttpsError("invalid-argument", "Invalid reading report.");
    }
    const reading = data.reading.trim();
    const reportId = createHash("sha256").update(reading).digest("hex");
    const createdAt = clock();
    const date = createdAt.toISOString().slice(0, 10);
    const userRef = db.collection("users").doc(uid);
    const quotaRef = userRef.collection("private").doc("reading_report_quota");
    const reportRef = quotaRef.collection("readingReports").doc(reportId);

    return db.runTransaction(async (transaction) => {
      const [user, report, quota] = await Promise.all([
        transaction.get(userRef), transaction.get(reportRef), transaction.get(quotaRef),
      ]);
      if (!user.exists || user.get("membershipDeletionPending") === true) {
        throw new HttpsError("failed-precondition", "Account unavailable.");
      }
      if (report.exists) return { reported: true };

      const count = quota.get("date") === date ? quota.get("count") : 0;
      if (count >= DAILY_REPORT_LIMIT) {
        throw new HttpsError("resource-exhausted", "Daily reading report limit reached.");
      }
      // The parent user's deletion also removes these private moderation records.
      transaction.set(quotaRef, { date, count: count + 1 });
      transaction.create(reportRef, { reading, createdAt });
      return { reported: true };
    });
  };
}

module.exports = { createReadingReportHandler };
