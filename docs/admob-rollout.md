# AstroSea AdMob release checklist

## Configured on 2026-10-26

- Android AdMob app: AstroSea (not yet linked to its public store listing).
- App ID: `ca-app-pub-8919976378865435~8641792715`.
- Rewarded unit: `AstroSea_Yorum_Odullu`.
- Rewarded unit ID: `ca-app-pub-8919976378865435/5824057683`.
- Reward: one interpretation, only after the Google SDK reward callback.
- Published European regulations message: `AstroSea - Reklam Gizlilik Tercihleri`, English + Turkish, with a reject option.
- Published US state regulations message: `AstroSea - ABD Gizlilik Tercihleri`,
  English (en-US), targeting all current and future supported US states.
- Privacy URL: https://dennizcann.github.io/AstroSea/privacy-policy.html

## Agreed next five steps

1. Complete regional AdMob settings and check Play Data safety declarations.
   European and US messages are published; Data safety review is in progress.
   Production has no countries selected yet (verified 2026-10-26); do not change
   distribution countries without the developer's choice.
2. Commit/push changes to GitHub, including the updated public privacy page.
3. Build a new AAB and test ads and consent on a physical phone.
4. Submit the production release to Google for review.
5. After publication, link the store listing to AdMob and complete app
   verification / app-ads.txt requirements.

## App behavior

- Debug uses Google's rewarded demo unit; billing testing stays independent.
- Release uses the AstroSea rewarded unit. Never click live ads for testing.
- Google UMP updates consent at Activity startup. No ads are requested until
  UMP permits requests and Mobile Ads initialization has finished.
- The Profile page exposes ad privacy choices when UMP requires it.
- Changing privacy choices invalidates previously loaded ads.
- Ads older than 55 minutes are reloaded. If a request is still loading, the
  show action waits for it rather than failing immediately.
- Previously unlocked readings and Premium access keep their existing behavior.

## Before public release

1. Commit/push these changes so GitHub Pages publishes the updated privacy
   policy in `docs/privacy-policy.html`; verify the public page includes AdMob.
2. Test a debug build on a real device: initial consent if required, reject and
   accept choices, reopening privacy choices, completing an ad, closing an ad
   before earning the reward, and failed/offline loading. A failed or skipped ad
   must not consume a daily unlock.
3. For testing a release build, register the physical device as a test device
   in AdMob first (Android emulators are test devices automatically).
4. Review Play Console Data safety / ads declarations against the Mobile Ads
   SDK disclosure and the updated policy. Configure any additional regional
   messages appropriate to the actual distribution countries. European and US
   state messages are configured; this is not a worldwide legal certification.
5. Build and submit a new version. No AAB has been uploaded by this change.
6. Once publicly listed, link the Play Store listing in AdMob, complete the
   app-ads.txt / developer-site verification presented there, and await app
   readiness approval. Account approval is not app approval.

## References

- https://developers.google.com/admob/android/privacy
- https://developers.google.com/admob/android/test-ads
- https://support.google.com/admob/answer/9989980
- https://developers.google.com/admob/android/privacy/play-data-disclosure
