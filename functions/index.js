const { setGlobalOptions, logger } = require("firebase-functions/v2");
const { HttpsError, onCall } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getFirestore } = require("firebase-admin/firestore");

initializeApp();

// Belgium is a Tier 1 region and keeps the service close to Turkish users.
setGlobalOptions({ region: "europe-west1", maxInstances: 10 });

const db = getFirestore();
const auth = getAuth();
const groqApiKey = defineSecret("GROQ_API_KEY");

const GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
const GROQ_MODEL = "openai/gpt-oss-120b";
const MAX_PROMPT_LENGTH = 14000;
const FREE_DAILY_LIMIT = 3;
const PREMIUM_DAILY_LIMIT = 25;

function startOfTodayUtc() {
  return new Date().toISOString().slice(0, 10);
}

async function reserveReadingQuota(uid) {
  const userRef = db.collection("users").doc(uid);
  const quotaRef = userRef.collection("private").doc("ai_quota");
  const today = startOfTodayUtc();

  return db.runTransaction(async (transaction) => {
    const [userSnapshot, quotaSnapshot] = await Promise.all([
      transaction.get(userRef),
      transaction.get(quotaRef),
    ]);
    const isPremium = userSnapshot.get("isPremium") === true;
    const limit = isPremium ? PREMIUM_DAILY_LIMIT : FREE_DAILY_LIMIT;
    const isSameDay = quotaSnapshot.exists && quotaSnapshot.get("date") === today;
    const used = isSameDay ? Number(quotaSnapshot.get("count") || 0) : 0;

    if (used >= limit) {
      throw new HttpsError(
        "resource-exhausted",
        "Daily interpretation limit reached. Please try again tomorrow."
      );
    }

    transaction.set(quotaRef, { date: today, count: used + 1, updatedAt: new Date() });
    return { isPremium, remaining: limit - used - 1 };
  });
}

/**
 * Generates a tarot interpretation without ever sending the Groq credential to Android.
 * The client may submit only an authenticated, size-limited prompt. A per-user server-side
 * quota bounds potential abuse; the quota document is nested below the user and is therefore
 * removed by deleteAccount.
 */
exports.generateTarotReading = onCall(
  {
    secrets: [groqApiKey],
    timeoutSeconds: 90,
    memory: "256MiB",
  },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) {
      throw new HttpsError("unauthenticated", "You must be signed in to request an interpretation.");
    }

    const prompt = request.data?.prompt;
    const isTurkish = request.data?.isTurkish === true;
    if (typeof prompt !== "string" || prompt.trim().length === 0 || prompt.length > MAX_PROMPT_LENGTH) {
      throw new HttpsError("invalid-argument", "Invalid interpretation request.");
    }

    await reserveReadingQuota(uid);

    const systemMessage = isTurkish
      ? "Sen profesyonel bir Türk tarot yorumcususun. MUTLAKA ve SADECE Türkçe yanıt ver. Kart isimlerini Türkçe karşılıklarıyla yaz; akıcı, anlaşılır ve etkileyici Türkçe kullan."
      : "You are a professional English tarot reader. Respond ONLY in natural, fluent English. Use standard English tarot card names and write an original reading for this specific spread.";

    let groqResponse;
    try {
      groqResponse = await fetch(GROQ_URL, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${groqApiKey.value()}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          model: GROQ_MODEL,
          messages: [
            { role: "system", content: systemMessage },
            { role: "user", content: prompt },
          ],
          temperature: isTurkish ? 0.65 : 0.72,
          max_tokens: 2048,
          top_p: 0.9,
          stream: false,
        }),
      });
    } catch (error) {
      logger.error("Groq network request failed", { code: error?.code ?? "unknown" });
      throw new HttpsError("unavailable", "Interpretation service is temporarily unavailable.");
    }

    if (!groqResponse.ok) {
      logger.error("Groq request failed", { status: groqResponse.status });
      if (groqResponse.status === 429) {
        throw new HttpsError("resource-exhausted", "Interpretation service is busy. Please try again shortly.");
      }
      throw new HttpsError("internal", "Interpretation could not be generated.");
    }

    const payload = await groqResponse.json();
    const reading = payload?.choices?.[0]?.message?.content;
    if (typeof reading !== "string" || reading.trim().length === 0) {
      logger.error("Groq returned an empty interpretation");
      throw new HttpsError("internal", "Interpretation could not be generated.");
    }

    return { reading: reading.trim() };
  }
);

/**
 * Permanently deletes the signed-in user's AstroSea data and Firebase Auth account.
 * The Admin SDK's recursive delete also removes every nested subcollection, including
 * the notification history stored under users/{uid}/notifications.
 */
exports.deleteAccount = onCall(
  {
    timeoutSeconds: 120,
    memory: "256MiB",
  },
  async (request) => {
    const uid = request.auth?.uid;
    if (!uid) {
      throw new HttpsError("unauthenticated", "You must be signed in to delete an account.");
    }

    try {
      await db.recursiveDelete(db.collection("users").doc(uid));
      await auth.deleteUser(uid);

      // Do not log uid, email, or other personal data.
      logger.info("Account deletion completed");
      return { deleted: true };
    } catch (error) {
      logger.error("Account deletion failed", { code: error?.code ?? "unknown" });
      throw new HttpsError("internal", "The account could not be deleted. Please try again.");
    }
  }
);
