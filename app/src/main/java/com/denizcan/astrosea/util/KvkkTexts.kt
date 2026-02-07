package com.denizcan.astrosea.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

/**
 * KVKK (Kişisel Verilerin Korunması Kanunu) metinleri
 * Cihaz diline göre Türkçe veya İngilizce metin döndürür
 */
object KvkkTexts {
    
    @Composable
    fun getTitle(): String {
        return if (isDeviceTurkish()) {
            "ASTROSEA KİŞİSEL VERİLERİN KORUNMASI VE İŞLENMESİ AYDINLATMA METNİ"
        } else {
            "ASTROSEA PRIVACY POLICY AND CLARIFICATION TEXT"
        }
    }
    
    @Composable
    fun getShortTitle(): String {
        return if (isDeviceTurkish()) {
            "KVKK Aydınlatma Metni"
        } else {
            "Privacy Policy"
        }
    }
    
    @Composable
    fun getConsentText(): String {
        return if (isDeviceTurkish()) {
            "KVKK Aydınlatma Metni'ni okudum ve kabul ediyorum"
        } else {
            "I have read and accept the Privacy Policy"
        }
    }
    
    @Composable
    fun getFullText(): String {
        return if (isDeviceTurkish()) TURKISH_TEXT else ENGLISH_TEXT
    }
    
    @Composable
    private fun isDeviceTurkish(): Boolean {
        val locale = LocalConfiguration.current.locales[0]
        return locale.language == "tr"
    }
    
    // Non-composable version for use outside Compose
    fun isDeviceTurkishLocale(): Boolean {
        return Locale.getDefault().language == "tr"
    }
    
    fun getFullTextNonComposable(): String {
        return if (isDeviceTurkishLocale()) TURKISH_TEXT else ENGLISH_TEXT
    }
    
    fun getTitleNonComposable(): String {
        return if (isDeviceTurkishLocale()) {
            "ASTROSEA KİŞİSEL VERİLERİN KORUNMASI VE İŞLENMESİ AYDINLATMA METNİ"
        } else {
            "ASTROSEA PRIVACY POLICY AND CLARIFICATION TEXT"
        }
    }
    
    private const val TURKISH_TEXT = """
ASTROSEA KİŞİSEL VERİLERİN KORUNMASI VE İŞLENMESİ AYDINLATMA METNİ

Son Güncelleme Tarihi: 07.02.2026

1. Veri Sorumlusu Kimdir?

6698 sayılı Kişisel Verilerin Korunması Kanunu ("Kanun") uyarınca, AstroSea ("Geliştirici" veya "AstroSea") olarak, veri sorumlusu sıfatıyla hareket etmekteyiz.

AstroSea mobil uygulaması, bireysel bir girişim olarak geliştirilmiş olup; kullanıcılarımızın ("İlgili Kişi") gizliliğine ve kişisel verilerinin güvenliğine en üst düzeyde önem vermekteyiz. Bu aydınlatma metni, AstroSea mobil uygulamasını ("Uygulama") kullanımınız sırasında elde edilen kişisel verilerinizin toplanma şekli, işlenme amaçları, hukuki sebepleri ve haklarınız konusunda sizi şeffaf bir şekilde bilgilendirmek amacıyla hazırlanmıştır.

2. Hangi Kişisel Verilerinizi İşliyoruz?

Uygulama üzerinden sunduğumuz astroloji ve tarot rehberliği hizmetlerinin doğası gereği, aşağıdaki kategorilerde yer alan kişisel verileriniz işlenmektedir:

• Kimlik Bilgileri: Adınız, soyadınız (veya uygulamada kullandığınız takma ad).

• İletişim Bilgileri: E-posta adresiniz (Hesap doğrulama, şifre sıfırlama ve iletişim için).

• Astrolojik İşlem Verileri: Doğum tarihiniz (gün/ay/yıl), doğum saatiniz ve doğum yeriniz (Şehir/Ülke).

Not: Bu veriler, doğum haritanızın (Natal Chart) matematiksel olarak hesaplanması ve size özel burç yorumlarının oluşturulabilmesi için teknik bir zorunluluktur.

• İşlem Güvenliği Bilgileri: IP adresiniz, cihaz kimlik bilgileri (Device ID), Firebase üzerindeki işlem kayıtları (Loglar), parola bilgileriniz (hashlenmiş/şifrelenmiş olarak).

• Görsel ve İşitsel Kayıtlar: (Opsiyonel) Tarot açılımları için yüklediğiniz fotoğraflar veya profil fotoğrafınız.

• Pazarlama Verileri: (Sadece açık rızanız olması halinde) Size özel kampanya veya duyuruların iletilmesi amacıyla işlenen veriler.

3. Kişisel Verilerinizi Hangi Amaçlarla İşliyoruz?

Toplanan kişisel verileriniz, Kanun'un 4., 5. ve 6. maddelerinde belirtilen kişisel veri işleme şartları dahilinde, aşağıdaki amaçlarla işlenmektedir:

• Hizmetin İfası: Astrolojik haritaların çıkarılması, günlük burç yorumlarının iletilmesi ve tarot analizlerinin yapılması.

• Üyelik İşlemleri: Kullanıcı kaydının açılması, Firebase Authentication altyapısı üzerinden "Verification" (doğrulama) mailinin gönderilmesi ve hesap güvenliğinin sağlanması.

• Müşteri Destek: Sorun bildirimlerinizin alınması ve çözümlenmesi.

• Uygulama Geliştirme: Uygulama içi hataların tespiti (Crashlytics vb.), performansın iyileştirilmesi ve kullanıcı deneyiminin (UX) geliştirilmesi.

• Yasal Yükümlülükler: 5651 sayılı Kanun ve ilgili mevzuat gereği log kayıtlarının tutulması.

4. Kişisel Verilerin Aktarılması

Kişisel verileriniz, Kanun'un 8. ve 9. maddelerinde belirtilen şartlara uygun olarak; bir şirket bünyesinde değil, güvenli bulut hizmet sağlayıcıları üzerinde saklanmaktadır:

• Teknik Altyapı Sağlayıcıları (Veri İşleyenler): Uygulamanın veritabanı, kimlik doğrulama ve sunucu hizmetleri Google Firebase (Google LLC) altyapısı üzerinde sağlanmaktadır. Verileriniz, Google'ın sağladığı yüksek güvenlik standartlarına sahip sunucularda şifreli olarak saklanmaktadır.

Önemli Not: Google sunucularının yurt dışında bulunması sebebiyle, verileriniz teknik zorunluluk gereği yurt dışına aktarılmaktadır. Bu işlem için uygulama kayıt ekranında "Açık Rızanız" talep edilmektedir.

• Yetkili Kamu Kurumları: Hukuki uyuşmazlıklarda veya yasal bir zorunluluk halinde (Örn: Savcılık talebi) adli makamlarla paylaşılabilir.

• Ödeme Hizmet Sağlayıcıları: Uygulama içi satın alma yapmanız durumunda, ödeme işlemleri doğrudan Google Play Store veya Apple App Store ödeme sistemleri üzerinden gerçekleşir. Geliştirici olarak kredi kartı bilgilerinize erişimimiz ve saklama yetkimiz yoktur.

5. Kişisel Veri Toplamanın Yöntemi ve Hukuki Sebebi

Kişisel verileriniz; AstroSea mobil uygulaması aracılığıyla, elektronik ortamda, tamamen veya kısmen otomatik yollarla toplanmaktadır.

Bu veriler, Kanun'un 5. maddesinde belirtilen aşağıdaki hukuki sebeplere dayanılarak işlenmektedir:

• Sözleşmenin Kurulması ve İfası: Astroloji ve tarot hizmetinin sunulabilmesi için doğum bilgilerinizin işlenmesi zorunludur.

• Açık Rıza: Sunucuların yurt dışında (Google Firebase) olması sebebiyle verilerin yurt dışına aktarımı için açık rızanız alınmaktadır.

• Meşru Menfaat: Temel haklarınıza zarar vermemek kaydıyla, uygulamanın güvenliğinin sağlanması ve hataların giderilmesi.

6. Veri Sahibi Olarak Haklarınız (Madde 11)

Kanun'un 11. maddesi uyarınca, veri sorumlusu olan Geliştirici'ye başvurarak aşağıdaki haklarınızı kullanabilirsiniz:

• Kişisel verilerinizin işlenip işlenmediğini öğrenme,

• Kişisel verileriniz işlenmişse buna ilişkin bilgi talep etme,

• Kişisel verilerinizin işlenme amacını ve bunların amacına uygun kullanılıp kullanılmadığını öğrenme,

• Yurt içinde veya yurt dışında kişisel verilerinizin aktarıldığı üçüncü kişileri bilme,

• Kişisel verilerinizin eksik veya yanlış işlenmiş olması hâlinde bunların düzeltilmesini isteme,

• Kanun'da öngörülen şartlar çerçevesinde kişisel verilerinizin silinmesini veya yok edilmesini isteme.

7. İletişim ve Başvuru

KVKK kapsamındaki taleplerinizi, kimliğinizi tespit edici gerekli bilgiler ile birlikte aşağıdaki e-posta adresine iletebilirsiniz:

E-posta: kvkk@astrosea.app

Talebiniz, niteliğine göre en kısa sürede ve en geç 30 (otuz) gün içinde ücretsiz olarak sonuçlandırılacaktır.
"""

    private const val ENGLISH_TEXT = """
ASTROSEA PRIVACY POLICY AND CLARIFICATION TEXT

Last Updated: February 7, 2026

1. Who is the Data Controller?

In accordance with the Law on the Protection of Personal Data No. 6698 ("Law"), we, AstroSea ("Developer" or "AstroSea"), act as the data controller.

The AstroSea mobile application has been developed as an individual initiative; we attach the utmost importance to the privacy of our users ("Data Subject") and the security of their personal data. This clarification text has been prepared to transparently inform you about how your personal data obtained during your use of the AstroSea mobile application ("Application") is collected, the purposes of processing, legal reasons, and your rights.

2. Which Personal Data Do We Process?

Due to the nature of the astrology and tarot guidance services we offer through the Application, your personal data in the following categories is processed:

• Identity Information: Your name, surname (or the nickname you use in the application).

• Contact Information: Your e-mail address (For account verification, password reset, and communication).

• Astrological Transaction Data: Your birth date (day/month/year), birth time, and birth place (City/Country).

Note: This data is a technical necessity for the mathematical calculation of your natal chart and the creation of personalized horoscope comments.

• Transaction Security Information: Your IP address, device identity information (Device ID), transaction records on Firebase (Logs), password information (stored as hashed/encrypted).

• Visual and Audio Records: (Optional) Photos you upload for tarot readings or your profile photo.

• Marketing Data: (Only with your explicit consent) Data processed for the purpose of transmitting campaigns or announcements specific to you.

3. For What Purposes Do We Process Your Personal Data?

Your collected personal data is processed within the scope of personal data processing conditions specified in Articles 4, 5, and 6 of the Law, for the following purposes:

• Performance of the Service: Creating astrological charts, transmitting daily horoscope comments, and performing tarot analyses.

• Membership Transactions: Creating user registration, sending "Verification" emails via Firebase Authentication infrastructure, and ensuring account security.

• Customer Support: Receiving and resolving your problem notifications.

• Application Development: Detecting in-app errors (Crashlytics, etc.), improving performance, and enhancing user experience (UX).

• Legal Obligations: Keeping log records in accordance with Law No. 5651 and relevant legislation.

4. Transfer of Personal Data

Your personal data is stored on secure cloud service providers, not within a corporate entity, in accordance with the conditions specified in Articles 8 and 9 of the Law:

• Technical Infrastructure Providers (Data Processors): The application's database, authentication, and server services are provided on Google Firebase (Google LLC) infrastructure. Your data is stored encrypted on servers with high-security standards provided by Google.

Important Note: Since Google servers are located abroad, your data is transferred abroad due to technical necessity. Your "Explicit Consent" is requested on the application registration screen for this process.

• Authorized Public Institutions: It may be shared with judicial authorities in case of legal disputes or a legal obligation (e.g., Prosecutor's request).

• Payment Service Providers: If you make an in-app purchase, payment transactions are carried out directly through Google Play Store or Apple App Store payment systems. As the Developer, we do not have access to or authority to store your credit card information.

5. Method and Legal Reason for Personal Data Collection

Your personal data is collected electronically, wholly or partially by automated means, via the AstroSea mobile application.

This data is processed based on the following legal reasons specified in Article 5 of the Law:

• Establishment and Performance of the Contract: Processing your birth information is mandatory to provide astrology and tarot services.

• Explicit Consent: Since the servers are located abroad (Google Firebase), your explicit consent is obtained for the transfer of data abroad.

• Legitimate Interest: Ensuring the security of the application and correcting errors, provided that it does not harm your fundamental rights.

6. Your Rights as a Data Owner (Article 11)

In accordance with Article 11 of the Law, you can exercise the following rights by applying to the Developer, who is the data controller:

• To learn whether your personal data is processed,

• To request information if your personal data has been processed,

• To learn the purpose of processing your personal data and whether they are used appropriately for their purpose,

• To know the third parties to whom your personal data is transferred domestically or abroad,

• To request correction of your personal data if it is incomplete or incorrectly processed,

• To request the deletion or destruction of your personal data within the framework of the conditions stipulated in the Law.

7. Contact and Application

You can submit your requests within the scope of KVKK, along with the necessary information to identify your identity, to the following e-mail address:

E-mail: kvkk@astrosea.app

Your request will be concluded free of charge as soon as possible and within 30 (thirty) days at the latest, depending on its nature.
"""
}
