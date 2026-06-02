# AstroSea

For Turkish documentation, see [README.tr.md](README.tr.md).

AstroSea is an Android application built with Kotlin and Jetpack Compose, featuring authentication, subscription management, notifications, personalized content delivery, and AI-powered user experiences.

## Product Snapshot

- Platform: Android
- Status: Production-ready release flow
- Architecture approach: Modular screen structure + ViewModel-based state management
- Monetization model: Play Billing subscription infrastructure

## What It Offers

- Google Sign-In and user profile management
- Daily tarot flow and multiple reading scenarios
- Horoscope and birth chart screens
- In-app notification center (with unread status indicator)
- Premium paywall and subscription management
- Firestore-based user and content data flow

## Tech Stack

- Kotlin
- Jetpack Compose (Material 3)
- Navigation Compose
- Firebase (Auth, Firestore, Realtime DB, Analytics)
- Google Sign-In
- Google Play Billing + Adapty
- WorkManager

## Engineering Notes

- Mobile release versioning with `versionCode` increments
- Store compliance assets: privacy policy, account deletion, and data safety declarations
- Notification retention window optimization (last 7 days in UI + cleanup policy)
- Clean and focused main flow with iterative UX improvements

## Project Structure (Short)

```text
app/src/main/java/com/denizcan/astrosea/
  presentation/   -> Compose screens
  auth/           -> Authentication flows
  billing/        -> Premium/subscription logic
  notifications/  -> Notification scheduling and management
  navigation/     -> Route structure
```

## Privacy & Compliance

- Privacy Policy: `docs/privacy-policy.html`
- Account Deletion: `docs/account-deletion.html`

Public Pages:

- `https://dennizcann.github.io/AstroSea/privacy-policy.html`
- `https://dennizcann.github.io/AstroSea/account-deletion.html`

## Contact

**astrosea777@gmail.com**

