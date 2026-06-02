# AstroSea

AstroSea, tarot ve astroloji odaklı, Kotlin + Jetpack Compose ile geliştirilmiş bir Android uygulamasıdır.  
Proje; kişiselleştirilmiş içerik deneyimi, hesap yönetimi, bildirim merkezi ve premium üyelik akışını üretim seviyesinde bir mobil ürün yaklaşımıyla birleştirir.

## Product Snapshot

- Platform: Android
- Durum: Production-ready yayın akışı
- Mimari yaklaşım: Modüler ekran yapısı + ViewModel tabanlı state yönetimi
- Gelir modeli: Play Billing abonelik altyapısı

## Neler Sunuyor?

- Google ile giriş ve kullanıcı profili yönetimi
- Günlük tarot akışı ve çoklu açılım senaryoları
- Burç ve doğum haritası ekranları
- Uygulama içi bildirim merkezi (okunmamış durum göstergesi ile)
- Premium paywall ve abonelik yönetimi
- Firestore tabanlı kullanıcı ve içerik verisi akışı

## Tech Stack

- Kotlin
- Jetpack Compose (Material 3)
- Navigation Compose
- Firebase (Auth, Firestore, Realtime DB, Analytics)
- Google Sign-In
- Google Play Billing + Adapty
- WorkManager

## Mühendislik Notları

- Mobil üretim süreçlerine uygun versioning (`versionCode` artışı) ve release akışı
- Store compliance için gizlilik politikası, hesap silme ve veri güvenliği deklarasyonları
- Bildirim tarafında veri saklama penceresi (son 7 gün görüntüleme, eski kayıt temizliği)
- UI/UX tarafında sade ve odaklı ana akış tasarımı

## Proje Yapısı (Kısa)

```text
app/src/main/java/com/denizcan/astrosea/
  presentation/   -> Compose ekranları
  auth/           -> Kimlik doğrulama
  billing/        -> Premium/abonelik
  notifications/  -> Bildirim planlama ve yönetim
  navigation/     -> Uygulama route yapısı
```

## Privacy & Compliance

- Privacy Policy: `docs/privacy-policy.html`
- Account Deletion: `docs/account-deletion.html`

Public Pages:

- `https://dennizcann.github.io/AstroSea/privacy-policy.html`
- `https://dennizcann.github.io/AstroSea/account-deletion.html`

## Contact

**astrosea777@gmail.com**

