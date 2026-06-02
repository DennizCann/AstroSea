# AstroSea

For English documentation, see [README.md](README.md).

AstroSea, Kotlin ve Jetpack Compose ile geliştirilmiş; kimlik doğrulama, abonelik yönetimi, bildirimler, kişiselleştirilmiş içerik sunumu ve yapay zeka destekli kullanıcı deneyimleri içeren bir Android uygulamasıdır.

## Product Snapshot

- Platform: Android
- Durum: Production-ready yayın akışı
- Mimari yaklaşım: Modüler ekran yapısı + ViewModel tabanlı state yönetimi
- Gelir modeli: Play Billing abonelik altyapısı

## Neler Sunuyor

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

- `versionCode` artışına dayalı mobil release versioning
- Store uyumluluğu için gizlilik politikası, hesap silme ve veri güvenliği beyanları
- Bildirim saklama penceresi optimizasyonu (arayüzde son 7 gün + temizleme politikası)
- İteratif UX iyileştirmeleri ile sade ve odaklı ana akış

## Proje Yapısı (Kısa)

```text
app/src/main/java/com/denizcan/astrosea/
  presentation/   -> Compose ekranları
  auth/           -> Kimlik doğrulama akışları
  billing/        -> Premium/abonelik mantığı
  notifications/  -> Bildirim planlama ve yönetim
  navigation/     -> Route yapısı
```

## Gizlilik ve Uyum

- Gizlilik Politikası: `docs/privacy-policy.html`
- Hesap Silme Talebi: `docs/account-deletion.html`

Public Pages:

- `https://dennizcann.github.io/AstroSea/privacy-policy.html`
- `https://dennizcann.github.io/AstroSea/account-deletion.html`

## İletişim

**astrosea777@gmail.com**
