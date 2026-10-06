# Tarot request v2 — server deployed, signed-in smoke test pending

## Deployment checkpoint — 2026-10-06
- Successfully updated only `generateTarotReading` in `astrosea-3de22`, `europe-west1`.
- Syntax checks and all 59 server tests passed before deployment.
- Live unauthenticated request returned the expected HTTP 401 / `UNAUTHENTICATED`; no AI generation or user quota was consumed by this check.
- No device was connected. Signed-in legacy/v2 generation remains unverified on the live server; test a fresh reading before distributing the Android build.
- Firebase CLI reported a successful function update, then exited with an artifact cleanup-policy warning/error. Deployment succeeded; no cleanup policy was changed. Retained build artifacts may incur storage charges.

## Scope
- Android sends `{schemaVersion: 2, spreadId, language, cardIds}`. IDs are ordered by card position.
- Server validates known spreads, exact card count, unique known cards, and `tr`/`en`.
- The existing Turkish/English instructions, model, sampling values, 2048-token cap, output headings, and `{reading: string}` response remain unchanged.
- Empty, truncated, refused, or non-text completions fail instead of being saved. The existing quota release path runs on failure. No automatic retries.
- Existing Firestore interpretations, membership, ads, and UI are not migrated.

## Files to maintain
- `reading-service.js`: request validation, settings, prompt assembly, provider response checks.
- `reading-prompts.js`: original English instruction and system messages.
- `data/reading-catalog.json`: reviewed server copy of existing card references and spread templates; loaded once per server instance.
- `scripts/export-reading-catalog.ps1`: emits the catalog as JSON from Android assets and localized resources. After intentionally changing those sources, regenerate/review this checked-in file. It does not write files or deploy.
- Tests detect drift against the source card meanings and Turkish templates.

## Compatibility and release order
1. Keep the current live Firebase deployment unchanged until publishing is explicitly requested.
2. Publish only the updated `generateTarotReading` callable first, to the existing project/region. No secrets or payment settings need changing.
3. Smoke-test a legacy request and a v2 request using a designated test account (successful real AI requests still use quota/provider resources).
4. Only then install/distribute the new Android build. **The new client cannot talk to the old server protocol; do not upload this AAB before the server update.**
5. Retain v2 server support when rolling the Android client back. Rolling the server back to pre-v2 breaks clients already upgraded.

`SETTINGS.allowLegacyRequests` is intentionally true during migration. Old authenticated clients still send free-form prompts subject to the existing size and quota limits. V2 never falls back to that path. This is a compatibility exception, not full removal of the old API trust boundary. Disable legacy support only after old clients are retired, with a deliberate app-update plan.

## Verification
Run `npm run check` and `npm test` in `functions`, and Android debug/release compilation and unit tests. No test sends a real Groq request. A live smoke test remains necessary after deployment. The unchanged token ceiling can still truncate lengthy generations; now those are reported as failures, not shown as complete readings. Increasing output length is a separate decision.
