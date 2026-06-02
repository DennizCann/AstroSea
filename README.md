# AstroSea

AstroSea, tarot ve astroloji odaklı bir Android uygulamasıdır.  
Uygulama; günlük açılımlar, farklı tarot okuma türleri, profil bazlı kişiselleştirme, bildirim sistemi ve premium üyelik akışını bir araya getirir.

## Öne Çıkan Özellikler

- Google ile giriş ve kullanıcı hesabı yönetimi
- Günlük tarot kartı akışı ve farklı okuma kategorileri
- Burç yorumları ve doğum haritası ekranları
- Uygulama içi bildirim merkezi (okunmamış durum takibi)
- Premium üyelik/paywall akışı (Play Billing + Adapty)
- Firebase tabanlı kullanıcı ve içerik verisi yönetimi

## Teknik Stack

- **Dil:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Mimari:** ViewModel + StateFlow tabanlı ekran yönetimi
- **Navigation:** Navigation Compose
- **Backend/Servisler:** Firebase Auth, Firestore, Realtime Database, Analytics
- **Kimlik Doğrulama:** Google Sign-In
- **Abonelik:** Google Play Billing + Adapty SDK
- **Arka Plan İşleri:** WorkManager

## Proje Yapısı (Özet)

```text
app/
  src/main/java/com/denizcan/astrosea/
    presentation/      -> Compose ekranları ve UI logic
    auth/              -> Kimlik doğrulama akışları
    billing/           -> Premium/abonelik yönetimi
    notifications/     -> Bildirim zamanlama ve receiver katmanı
    navigation/        -> Route ve ekran yönlendirmeleri
```

## Kurulum

### Gereksinimler

- Android Studio (güncel stable sürüm)
- JDK 11+
- Android SDK (minSdk 24, target/compileSdk 35)

### 1) Projeyi Klonla

```bash
git clone https://github.com/DennizCann/AstroSea.git
cd AstroSea
```

### 2) Yerel Ayarlar

`local.properties` içinde gerekli anahtarları tanımla:

```properties
GROQ_API_KEY=YOUR_KEY
```

`app/google-services.json` dosyasının mevcut ve doğru Firebase projesine bağlı olduğundan emin ol.

### 3) Çalıştırma

- Android Studio ile projeyi aç
- Gradle sync tamamlandıktan sonra `app` modülünü çalıştır

## Build

Debug build:

```bash
./gradlew :app:assembleDebug
```

Release bundle:

```bash
./gradlew :app:bundleRelease
```

> Not: Play Console yüklemelerinde `versionCode` her sürümde artırılmalıdır.

## Gizlilik ve Hesap Silme

- Gizlilik Politikası: `docs/privacy-policy.html`
- Hesap Silme Talebi: `docs/account-deletion.html`

GitHub Pages aktifse:

- `https://dennizcann.github.io/AstroSea/privacy-policy.html`
- `https://dennizcann.github.io/AstroSea/account-deletion.html`

## Durum

Proje aktif geliştirme altındadır.  
Kapalı test, Play Console süreçleri ve yayın hazırlıkları düzenli olarak güncellenmektedir.

---

İletişim: **astrosea777@gmail.com**

