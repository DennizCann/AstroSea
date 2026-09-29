"use strict";

// Run manually from the repository root:
//   node functions/scripts/check-membership-live.js --uid EXISTING_FIREBASE_UID
// Add --sync-existing only to refresh that account's private membership snapshot.
// No sign-in, identity creation, purchases, access grants, or deletion are performed.
// Requires the existing Firebase CLI login and access to this project's secrets.

const PROJECT = "astrosea-3de22";
const REGION = "europe-west1";
const BASE_URL = `https://${REGION}-${PROJECT}.cloudfunctions.net`;
const PROFILE_URL = "https://api.adapty.io/api/v2/server-side-api/profile/";
const { isDeepStrictEqual } = require("node:util");
const { fetchProfile, membershipView, validUid } = require("../membership");

let stage = "arguments";
class CheckError extends Error {}
function check(condition, message) {
  if (!condition) throw new CheckError(message);
}
function report(message) {
  process.stdout.write(`${message}\n`);
}

function parseArgs() {
  const args = process.argv.slice(2);
  if (args.length === 1 && args[0] === "--help") {
    report("Usage: node functions/scripts/check-membership-live.js --uid EXISTING_FIREBASE_UID [--sync-existing]");
    report("Default: read-only provider/Firestore checks and non-mutating endpoint probes.");
    report("--sync-existing: send one authenticated invalidation webhook for the supplied existing account; verify its private snapshot and unchanged public parent.");
    return null;
  }
  let uid;
  let sync = false;
  let beta = false;
  for (let i = 0; i < args.length; i++) {
    if (args[i] === "--uid" && uid === undefined) uid = args[++i];
    else if (args[i] === "--sync-existing" && !sync) sync = true;
    else if (args[i] === "--expect-beta" && !beta) beta = true;
    else throw new CheckError("Invalid arguments. Use --help; never supply secrets on the command line.");
  }
  check(validUid(uid), "Supply one valid existing Firebase UID with --uid.");
  return { uid, sync, beta };
}

function loadCli() {
  // Do this BEFORE importing auth, requireAuth, apiv2, or Secret Manager. Never
  // import the CLI entry point: it can install file logging and error handlers.
  check(!process.env.NODE_DEBUG && !process.env.NODE_DEBUG_NATIVE,
    "Start a fresh process without Node debug logging.");
  delete process.env.DEBUG;
  delete process.env.IS_FIREBASE_CLI;
  const logging = require("firebase-tools/lib/logger");
  logging.logger.silent = true;
  logging.logger.clear();
  for (const level of ["log", "debug", "info", "warn", "error", "verbose", "silly", "http"]) {
    logging.logger[level] = () => logging.logger;
  }
  logging.useFileLogger = () => undefined;
  logging.useConsoleLoggers = () => undefined;

  // The CLI supports endpoint overrides. Refuse them instead of sending saved
  // credentials to an unexpected origin. Use the saved login, not token flags.
  for (const key of ["FIREBASE_TOKEN", "FIREBASE_TOKEN_URL", "FIREBASE_GOOGLE_URL",
    "CLOUD_SECRET_MANAGER_URL", "FIREBASE_CLIENT_ID", "FIREBASE_CLIENT_SECRET"]) {
    check(!process.env[key], "Remove Firebase authentication/endpoint overrides before running this check.");
  }
  const { getGlobalDefaultAccount } = require("firebase-tools/lib/auth");
  const { requireAuth } = require("firebase-tools/lib/requireAuth");
  const { Client } = require("firebase-tools/lib/apiv2");
  const { accessSecretVersion } = require("firebase-tools/lib/gcp/secretManager");
  return { getGlobalDefaultAccount, requireAuth, Client, accessSecretVersion };
}

function decode(value) {
  check(value && typeof value === "object", "Private snapshot has an invalid Firestore value.");
  if ("nullValue" in value) return null;
  if ("stringValue" in value) return value.stringValue;
  if ("booleanValue" in value) return value.booleanValue;
  if ("integerValue" in value) {
    const number = Number(value.integerValue);
    check(Number.isSafeInteger(number), "Private snapshot has an invalid integer.");
    return number;
  }
  if ("doubleValue" in value) {
    check(Number.isFinite(value.doubleValue), "Private snapshot has an invalid number.");
    return value.doubleValue;
  }
  if ("arrayValue" in value) return (value.arrayValue.values ?? []).map(decode);
  if ("mapValue" in value) {
    return Object.fromEntries(Object.entries(value.mapValue.fields ?? {}).map(([key, entry]) => [key, decode(entry)]));
  }
  throw new CheckError("Private snapshot has an unsupported Firestore value.");
}

async function main() {
  const args = parseArgs();
  if (!args) return;
  stage = "CLI authentication";
  const cli = loadCli();
  const account = cli.getGlobalDefaultAccount();
  check(account?.user && account?.tokens?.refresh_token, "An existing Firebase CLI login is required.");
  await cli.requireAuth({ project: PROJECT, user: account.user, tokens: account.tokens, nonInteractive: true }, true);
  const firestore = new cli.Client({ urlPrefix: "https://firestore.googleapis.com", apiVersion: "v1" });
  const privatePath = `/projects/${PROJECT}/databases/(default)/documents/users/${encodeURIComponent(args.uid)}/private/membershipState`;
  const parentPath = `/projects/${PROJECT}/databases/(default)/documents/users/${encodeURIComponent(args.uid)}`;
  async function readDocument(path) {
    const response = await firestore.get(path, {
      resolveOnHTTPError: true, retries: 0, timeout: 15000,
      skipLog: { body: true, queryParams: true, resBody: true },
    });
    if (response.status === 404) return null;
    check(response.status === 200, `Firestore read failed (HTTP ${response.status}).`);
    return response.body;
  }

  stage = "existing public account";
  const parentBefore = await readDocument(parentPath);
  check(parentBefore, "The supplied account has no existing public Firestore parent.");
  check(parentBefore.fields?.membershipDeletionPending?.booleanValue !== true, "The supplied account is pending deletion.");
  report("PASS existing public account found");

  stage = "secret access";
  // Secret values and API payloads stay in process memory. The normal CLI OAuth
  // helper may refresh its existing local credential cache; no new files are made.
  const providerSecret = await cli.accessSecretVersion(PROJECT, "ADAPTY_SECRET_KEY", "latest");
  const webhookSecret = await cli.accessSecretVersion(PROJECT, "ADAPTY_WEBHOOK_SECRET", "latest");
  report(`Webhook secret format: length=${webhookSecret.length}, trimmedLength=${webhookSecret.trim().length}, hex96=${/^[a-f0-9]{96}$/i.test(webhookSecret)}`);

  stage = "read-only Adapty profile validation";
  let profileExists = false;
  const snapshot = await fetchProfile(args.uid, providerSecret, async (url, options) => {
    check(url === PROFILE_URL && options.method === "GET", "Unexpected provider operation blocked.");
    const response = await fetch(url, { ...options, redirect: "error" });
    profileExists = response.status === 200;
    return response;
  });
  check(profileExists, "An existing Adapty profile is required; a missing profile does not validate a refund.");
  const view = membershipView(snapshot);
  report(`PASS existing Adapty profile schema; hasPremiumAccess=${view.hasPremiumAccess}; hasSandboxAccess=${view.hasSandboxAccess}`);
  check(args.beta ? view.hasPremiumAccess && snapshot.accessLevels.some(x => x.source === "beta") : !view.hasPremiumAccess,
    "The provider membership does not match the expected beta/inactive state.");

  async function probe(name, endpoint, options, expectedStatus, received = false) {
    stage = name;
    const response = await fetch(`${BASE_URL}/${endpoint}`, {
      ...options, redirect: "error", signal: AbortSignal.timeout(25000),
    });
    check(response.status === expectedStatus, `${name}: expected HTTP ${expectedStatus}, received HTTP ${response.status}.`);
    if (received) {
      const body = await response.json();
      check(body?.received === true, `${name}: unexpected acknowledgement.`);
    } else {
      await response.body?.cancel();
    }
    report(`PASS ${name} (HTTP ${response.status})`);
  }

  await probe("unauthenticated getMembership", "getMembership", {
    method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ data: {} }),
  }, 401);
  await probe("unauthenticated webhook", "adaptyWebhook", {
    method: "POST", headers: { "Content-Type": "application/json" }, body: "{}",
  }, 401);
  await probe("webhook GET", "adaptyWebhook", { method: "GET" }, 405);

  // HTTP clients normalize surrounding whitespace. Diagnose this explicitly:
  // a secret stored with a newline cannot equal the actual Authorization header.
  const headerSecret = webhookSecret.trim();
  check(headerSecret.length >= 32 && !/[\r\n]/.test(headerSecret), "Webhook secret cannot form a valid Authorization header.");
  const headers = { "Content-Type": "application/json", Authorization: headerSecret };
  await probe("authenticated empty webhook", "adaptyWebhook", { method: "POST", headers, body: "{}" }, 200, true);

  if (args.sync) {
    stage = "existing private state read";
    const stateBefore = await readDocument(privatePath);
    const revisionBefore = stateBefore?.fields?.revision ? decode(stateBefore.fields.revision) : 0;
    const startedAt = Date.now();
    let syncFailure;
    try {
      await probe("existing-account snapshot sync", "adaptyWebhook", {
        method: "POST", headers,
        body: JSON.stringify({ event_type: "access_level_updated", customer_user_id: args.uid }),
      }, 200, true);
    } catch (error) {
      syncFailure = error;
    }
    stage = "public parent comparison";
    const parentAfter = await readDocument(parentPath);
    check(isDeepStrictEqual(parentAfter, parentBefore), "Public parent changed during the check; investigate concurrent activity before attributing the change.");
    report("PASS public parent unchanged (all fields and update time)");
    if (syncFailure) throw syncFailure;

    stage = "private snapshot verification";
    const stateAfter = await readDocument(privatePath);
    check(stateAfter?.fields?.snapshot, "No private membership snapshot was saved.");
    const state = Object.fromEntries(Object.entries(stateAfter.fields).map(([key, value]) => [key, decode(value)]));
    check(Number.isSafeInteger(state.revision) && state.revision > revisionBefore && state.completedRevision === state.revision,
      "The private snapshot refresh did not complete at a new revision.");
    check(state.snapshot.schemaVersion === snapshot.schemaVersion && isDeepStrictEqual(state.snapshot.accessLevels, snapshot.accessLevels),
      "Saved membership differs from the read-only provider result; retry after any concurrent provider change.");
    const verifiedAt = Date.parse(state.snapshot.verifiedAt);
    check(Number.isFinite(verifiedAt) && verifiedAt >= startedAt - 5000 && verifiedAt <= Date.now() + 5000,
      "Private snapshot verification time is outside this check.");
    check(membershipView(state.snapshot).hasPremiumAccess === view.hasPremiumAccess, "Saved access differs from provider.");
    report("PASS fresh private snapshot matches provider access");
  } else {
    if (args.beta) {
      const saved = await readDocument(privatePath);
      const stored = saved?.fields?.snapshot && decode(saved.fields.snapshot);
      check(stored && isDeepStrictEqual(stored.accessLevels, snapshot.accessLevels)
        && membershipView(stored).hasPremiumAccess, "Automatic webhook snapshot does not yet match beta access.");
      report("PASS automatic webhook saved matching active beta access");
    }
    stage = "public parent comparison";
    check(isDeepStrictEqual(await readDocument(parentPath), parentBefore), "Public parent changed during the read-only check.");
    report("PASS public parent unchanged; snapshot sync not requested");
  }
  report("PASS live membership checks complete");
}

main().catch((error) => {
  // Never print provider responses, error objects, stacks, account IDs, or keys.
  const detail = error instanceof CheckError ? error.message : "Request failed; raw error details withheld to protect credentials and account data.";
  process.stderr.write(`FAIL ${stage}: ${detail}\n`);
  process.exitCode = 1;
});
