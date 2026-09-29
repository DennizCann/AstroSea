const { setGlobalOptions, logger } = require("firebase-functions/v2");
const { HttpsError, onCall, onRequest } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getFirestore } = require("firebase-admin/firestore");
const { authorized, parseWebhook, readingLimit } = require("./membership");
const { createMembershipService } = require("./membership-service");
const { createBetaService } = require("./beta-service");

initializeApp();

// Belgium is a Tier 1 region and keeps the service close to Turkish users.
setGlobalOptions({ region: "europe-west1", maxInstances: 10 });

const db = getFirestore();
const auth = getAuth();
const groqApiKey = defineSecret("GROQ_API_KEY");
const adaptySecretKey = defineSecret("ADAPTY_SECRET_KEY");
const adaptyWebhookSecret = defineSecret("ADAPTY_WEBHOOK_SECRET");
const membershipService = createMembershipService({ db, auth, secret: () => adaptySecretKey.value() });
const betaService = createBetaService({ db, auth, secret: () => adaptySecretKey.value() });

// Verified tester eligibility comes only from the server-managed beta list.
exports.getMembership = onCall(
  { secrets: [adaptySecretKey], timeoutSeconds: 60, memory: "256MiB", maxInstances: 2 },
  async (request) => {
    if (!request.auth?.uid) throw new HttpsError("unauthenticated", "Sign in to check membership.");
    try {
      const granted = await betaService.ensure(request.auth.uid);
      return await membershipService.refresh(request.auth.uid, { webhook: granted });
    } catch (error) {
      if (error?.code === "account-unavailable") throw new HttpsError("failed-precondition", "Account unavailable.");
      // Do not log credentials, UIDs, provider payloads, or treat network errors as free access.
      logger.warn("Membership refresh unavailable");
      throw new HttpsError("unavailable", "Membership could not be verified. Please try again shortly.");
    }
  }
);

exports.adaptyWebhook = onRequest(
  { secrets: [adaptySecretKey, adaptyWebhookSecret], timeoutSeconds: 15, memory: "256MiB", maxInstances: 2, invoker: "public" },
  async (request, response) => {
    if (request.method !== "POST") return response.status(405).send("Method not allowed");
    if (!authorized(request.get("authorization"), adaptyWebhookSecret.value())) return response.status(401).send("Unauthorized");
    if ((request.rawBody?.length ?? 0) > 65536) return response.status(413).send("Payload too large");
    try {
      const event = parseWebhook(request.body);
      if (event.verification || event.ignored) return response.status(200).json({ received: true });
      await membershipService.refresh(event.uid, { webhook: true, eventHash: event.eventHash });
      return response.status(200).json({ received: true });
    } catch (error) {
      if (error?.code === "invalid-event") return response.status(400).send("Invalid event");
      if (error?.code === "account-unavailable") return response.status(200).json({ received: true });
      logger.warn("Membership webhook refresh failed; retry required");
      // Only acknowledge after a durable write. Adapty retries 5xx responses.
      return response.status(503).send("Retry later");
    }
  }
);

const GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
const GROQ_MODEL = "openai/gpt-oss-120b";
const MAX_PROMPT_LENGTH = 14000;

function startOfTodayUtc() {
  return new Date().toISOString().slice(0, 10);
}

async function reserveReadingQuota(uid) {
  // The client-writable legacy isPremium field is never an authority.
  let membership;
  try {
    membership = await membershipService.refresh(uid);
  } catch {
    throw new HttpsError("unavailable", "Membership could not be verified. Please try again shortly.");
  }
  const userRef = db.collection("users").doc(uid);
  const quotaRef = userRef.collection("private").doc("ai_quota");
  const today = startOfTodayUtc();

  return db.runTransaction(async (transaction) => {
    const [userSnapshot, quotaSnapshot] = await Promise.all([
      transaction.get(userRef),
      transaction.get(quotaRef),
    ]);
    if (!userSnapshot.exists || userSnapshot.get("membershipDeletionPending") === true) {
      throw new HttpsError("failed-precondition", "Account unavailable.");
    }
    const isPremium = membership.hasPremiumAccess === true;
    const limit = readingLimit(membership);
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

async function releaseReadingQuota(uid) {
  const quotaRef = db.collection("users").doc(uid).collection("private").doc("ai_quota");
  const today = startOfTodayUtc();
  await db.runTransaction(async (transaction) => {
    const quotaSnapshot = await transaction.get(quotaRef);
    const isSameDay = quotaSnapshot.exists && quotaSnapshot.get("date") === today;
    const used = isSameDay ? Number(quotaSnapshot.get("count") || 0) : 0;
    if (used > 0) {
      transaction.update(quotaRef, { count: used - 1, updatedAt: new Date() });
    }
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
    secrets: [groqApiKey, adaptySecretKey],
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

    let quotaReserved = false;
    try {
      await reserveReadingQuota(uid);
      quotaReserved = true;

    const systemMessage = isTurkish
      ? "Sen profesyonel bir Türk tarot yorumcususun. MUTLAKA ve SADECE Türkçe yanıt ver. Kart isimlerini Türkçe karşılıklarıyla yaz; akıcı, anlaşılır ve etkileyici Türkçe kullan."
      : "You are a professional English tarot reader. Respond ONLY in natural, fluent English. Use standard English tarot card names and write an original reading for this specific spread.";

      const groqResponse = await fetch(GROQ_URL, {
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
    } catch (error) {
      if (quotaReserved) {
        await releaseReadingQuota(uid).catch((releaseError) => {
          logger.error("Failed to release interpretation quota", { code: releaseError?.code ?? "unknown" });
        });
      }
      if (error instanceof HttpsError) throw error;
      logger.error("Groq network request failed", { code: error?.code ?? "unknown" });
      throw new HttpsError("unavailable", "Interpretation service is temporarily unavailable.");
    }
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
      await membershipService.beginDeletion(uid);
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
