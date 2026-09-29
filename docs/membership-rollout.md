# Membership migration — AAB preparation handoff

## Automatic tester eligibility (2026-09-30)

- User requested all 15 current Play Beta Test emails receive free premium. Exact
  lowercase/trimmed SHA-256 email matches are stored in server-only `betaTesters`.
  Raw tester addresses are not in source control or the Android app.
- `getMembership` now checks fresh Firebase Auth email verification and the private
  list, provisions a missing Adapty profile, and grants `beta_premium` once for 30 days.
  For new grants the period starts on the first eligible membership check. Existing
  active OR expired beta grants are preserved, not extended by this change.
- Each record binds to one UID and uses a transaction lease and fixed retry dates.
  Login, retries, expiry and delete/re-register do not restart a completed grant.
  Unlisted, unverified, disabled or deletion-pending accounts cannot activate it.
- This does not create Firebase accounts, charge money, start store subscriptions,
  alter paid access, change the Play test list or publish an AAB.
- The 15-address eligibility list is configured and verified. This does NOT mean
  all 15 have already activated: users must sign in with that exact verified email
  on an app version using `getMembership` (version 25 code).
- 50 backend tests and 19 local Firestore rule checks pass, including denial of
  client reads/writes to the new beta list. Real-device login remains user smoke test.
- `getMembership` deployed successfully in europe-west1. CLI again reported the
  pre-existing Artifact Registry cleanup-policy warning after successful deployment;
  no image deletion/retention-policy change was made.
- This supersedes the manual-email collection step below for the selected 15.

## Latest implementation (2026-09-29)

- Android version is now 25 / 1.0.24. No AAB was built or submitted.
- Android reads `getMembership` for access and displays separate beta, sandbox and
  paid rights with provider expiry dates in Turkish/English. No locally calculated
  7/30/365-day expiry and no premium grant/revoke writes remain in purchase/restore.
- Active access bypasses the purchase launch. Unknown membership blocks checkout
  with a retry message, rather than incorrectly advertising a new subscription.
- Purchase/restore operations serialize Adapty logout/identify and recheck Firebase UID.
  Both build variants use real store billing; debug no longer simulates premium grants.
  This does NOT make any ordinary Play account a license tester.
- The reading screen and upgrade reminders use the same verified response.
- `generateTarotReading` was updated in production: quotas now use server-verified
  membership (3 free / 25 premium, including beta and valid sandbox) rather than
  public `isPremium`. Existing daily reset behavior was not changed.
- 44 backend tests and 6 new Android membership tests passed; debug/release compile
  checks passed. Real-device purchase/restore/account-switch testing remains pending.
- User explicitly approved 30-day, non-renewing `beta_premium` grants for verified
  app accounts matching the selected Play Beta Test list. The level is created.
  Of 15 tester emails, only 2 match existing verified, enabled Firebase accounts.
  Both received exactly 30 days, ending 2026-10-29 around 19:52 UTC (22:52 Istanbul).
  Other 13 have NOT received access; do not infer identity from similar names.
  Existing paid subscriptions and other access levels were verified unchanged.
  Real grant webhooks automatically saved matching active private Firebase snapshots
  for both accounts. No purchase was made, and these grants do not auto-convert to paid.
- New Firestore rules deployed successfully after 17 local emulator checks. Client
  legacy-premium changes, deletion-marker changes, direct account deletion and private
  membership reads/writes are denied; ordinary profile/notification writes remain.
  Old version 23 billing legacy writes are intentionally no longer supported. Testers
  must use the new version for beta display/access, not try purchases in the old app.
- Play currently serves 23 / 1.0.22; version 24 is still unsubmitted. Neither was
  published/removed here. Build and upload ONLY version 25 / 1.0.24 for this change.
- Play license testing was checked read-only: Beta Test (15) is selected; other
  lists are unselected. Correct device Play account and test payment sheet still
  need verification before any test purchase; app beta access requires none.

## Before distributing the new AAB

1. On a device, sign into one of the two granted accounts. Refresh Profile and confirm
   the free beta label, October 29 expiry and AI access without entering checkout.
   This authenticated device smoke test has NOT been performed by the agent.
2. Create the signed release AAB for version 25 / 1.0.24 and upload that artifact.
3. For remaining testers, obtain the actual app sign-in email and verify it matches
   the approved Play list before granting. Do not tell all 15 that access is already free.
4. Any actual Billing test still requires a correctly configured Google license-test
   account and a Google test-payment sheet. Beta grants themselves require no purchase.

Operator grant tool: `functions/scripts/grant-beta.js UID --approved-30-days`.
Only run after exact tester-email matching and explicit authorization; the tool checks
verified/enabled Auth, existing profile, finite expiry and unchanged subscriptions.
Live verification: `check-membership-live.js --uid UID --expect-beta` checks the actual
automatic webhook snapshot; it does not grant access or charge money.

The following notes retain the earlier staged rollout history; client/AI migration
steps in that historical checklist are now implemented as described above.

## Verified on 2026-09-29

- Firebase project: `astrosea-3de22`; Functions region: `europe-west1`.
- Adapty Google Play RTDN is active; a real refund is reflected as expired access.
- Store products weekly/monthly/yearly all grant `premium`.
- Only the `premium` access level exists. No `beta_premium` grants have been made.
- Adapty custom webhook is ON. Production and sandbox both use
  `https://europe-west1-astrosea-3de22.cloudfunctions.net/adaptyWebhook`.
  The exact authorization secret is stored in Google Secret Manager, not this document.
  Access Level Updated and subscription lifecycle events are enabled; optional user
  attributes, attribution and Play purchase tokens remain disabled.
- `ADAPTY_SECRET_KEY` and `ADAPTY_WEBHOOK_SECRET` version 1 are enabled in Secret Manager.
- `getMembership`, `adaptyWebhook`, and the updated `deleteAccount` are deployed.
  `generateTarotReading`, Firestore rules, Android release and membership grants are unchanged.
- Live Firestore rules allow an authenticated owner to access `users/{uid}` and
  `users/{uid}/notifications/{id}`. Other subcollections are implicitly denied.
  Rules must be rechecked at deployment; a recursive owner-write rule would invalidate
  the new server-only design.

## Deployed additive backend

- `getMembership`: authenticated callable, derives UID only from Firebase Auth and
  checks that the Auth account and parent Firestore profile still exist.
- `adaptyWebhook`: authenticated POST receiver. Uses Adapty events only as invalidation
  signals and fetches current state from the fixed Adapty Server API v2 endpoint.
- Verified, minimal entitlement state is stored under
  `users/{uid}/private/membershipState`; clients cannot read or write it directly.
- `premium` and future `beta_premium` access levels are retained separately. Expiration
  is evaluated at read time, including cached results. Valid sandbox grants intentionally
  unlock test functionality; `hasSandboxAccess` and per-level environment distinguish this
  from real store subscriptions. `hasPremiumAccess` is NOT proof of payment.
- A transaction revision prevents an older in-flight fetch overwriting a newer one.
  A unique token prevents revision reuse after document recreation. Firebase Auth is
  checked again after the provider fetch, before committing the response.
  Provider failures preserve the prior verified snapshot and return an error, not free access.
- The last successfully processed event ID is hashed for duplicate retry suppression;
  older duplicate events are harmless current-state refetches. Event business timestamps
  are not used to order membership updates.
- Account deletion marks the parent as pending before recursively deleting data.
  Membership transactions stop when this flag is set or the parent is absent.
  This is an ordinary concurrency fence, not a replacement for final write restrictions:
  the legacy rules still let a user modify their own parent document.
  Before authoritative activation, prevent parent deletion/recreation and clearing this
  field through client rules (or implement a durable server-only lifecycle fence). Do not
  claim complete deletion-race protection under the legacy rules.

## Validation and remaining activation work

Completed: both secrets were saved, the three scoped functions were deployed, and the
Adapty settings remained ON with both URLs and matching authorization values after reload.
The first Adapty verification returned 401 immediately after deployment; direct verification
with the stored secret returned 200, and the subsequent Adapty save succeeded without
loosening authentication. The cause of the initial transient failure is not established.

Live checks passed against an existing refunded account: Adapty profile schema accepted,
no active premium/sandbox access, unauthenticated callable/webhook rejected with 401,
GET rejected with 405, authenticated empty verification returned 200, and an authenticated
invalidation refreshed the server-only snapshot. All public parent fields and its update
time were unchanged. This was a synthetic invalidation, NOT a real sandbox purchase or
an end-to-end Android billing test. No new payment, refund, grant or deletion was performed.
All 40 unit tests and syntax checks passed.

Deployment reported successful functions but exited with a separate Artifact Registry
cleanup-policy warning. No old build images were deleted; retention setup remains pending.

The checklist below still applies; items 1 and 3 are completed, items 2 and 4 are partially
validated (real sandbox lifecycle and authenticated Android callable checks remain).

1. Store `ADAPTY_SECRET_KEY` and an independently generated, high-entropy
   `ADAPTY_WEBHOOK_SECRET` (at least 32 characters) in Google Secret Manager.
   Never put them in Android, Firestore documents, source control, logs or chat.
   Approve the exact credential transfer/creation before doing it through browser UI.
2. Check the Adapty API key permissions, returned profile schema and actual dates against
   a known account. Test 404 vs service failure and sandbox response isolation.
3. Deploy the new endpoints **and updated deleteAccount together**, after reviewing the
   deletion fence. Configure Adapty's production AND sandbox webhook endpoint URLs and
   exact authorization values; enable Access Level Updated and subscription events.
   Keep sending optional user attributes/device data disabled when not required.
4. Validate authenticated empty verification, reject wrong secrets, send a sandbox
   lifecycle event, and verify the protected stored snapshot. Do not claim live completion
   based only on unit tests or a successful deployment.
5. Migrate Android to the verified membership response, including logout/identify ordering,
   actual dates, renewal/cancellation labels and Turkish/English text. Remove all client
   writes to premium fields. Do not use build type to identify Google sandbox purchases.
6. Migrate AI quota access checks to the canonical state. Public legacy `isPremium` is
   STILL trusted by the existing AI function in this additive phase; that is not fixed yet.
   Align daily-reset semantics separately.
7. Restrict legacy premium/lifecycle fields in Firestore rules after testing compatibility
   with the old release; keep profile, notification and account-deletion flows working.
8. Only then create/assign finite `beta_premium` rights to verified tester Firebase UIDs.
   Confirm the duration and start policy first. A Play tester email is not automatically
   the same as the app's authenticated account. Do not grant anonymous profiles.
9. Test paid+beta coexistence, refund, cancellation, expired access, restore, account switch,
   fresh install, provider outage and deletion races. Submit only the final Android bundle.

## Unchanged intentionally

No membership grants, rules, quotas, purchases or releases have been changed. Existing
public profile fields remain legacy and are not authoritative for the new endpoints.
Do not deploy all functions blindly during this staged migration.

## Verification commands

Run `npm run check` and `npm test` in `functions`. Tests use fake accounts and provider
responses; they do not make purchases, contact Adapty or write to production Firebase.

For a manually authorized live check, run
`node functions/scripts/check-membership-live.js --uid EXISTING_FIREBASE_UID`.
The optional `--sync-existing` flag refreshes only that existing inactive account's private
membership snapshot and verifies that its public parent remains unchanged. Credentials
stay in process memory and diagnostic output excludes secrets and account identifiers.

## References

- https://adapty.io/docs/api-adapty/operations/getProfile
- https://adapty.io/docs/api-adapty/operations/grantAccessLevel
- https://adapty.io/docs/set-up-webhook-integration
- https://adapty.io/docs/webhook-event-types-and-fields
