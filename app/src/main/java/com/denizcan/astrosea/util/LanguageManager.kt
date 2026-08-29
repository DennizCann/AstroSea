package com.denizcan.astrosea.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Uygulama dili yönetimi.
 * İlk açılışta cihaz dili Türkçe ise Türkçe, değilse İngilizce seçilir.
 * Kullanıcı tercihi SharedPreferences'ta saklanır ve cihaz dilinden bağımsız uygulanır.
 */
object LanguageManager {

    const val TURKISH = "tr"
    const val ENGLISH = "en"

    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "app_language"

    private var appContext: Context? = null

    /** Application.onCreate içinde bir kez çağrılır; Context olmayan yerler doğru dili okur. */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_LANGUAGE, null)
        if (saved == TURKISH || saved == ENGLISH) return saved
        // İlk açılış: cihaz diline göre varsayılanı kaydet
        val initial = if (Locale.getDefault().language == "tr") TURKISH else ENGLISH
        prefs.edit().putString(KEY_LANGUAGE, initial).apply()
        return initial
    }

    fun setLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    fun isTurkish(context: Context): Boolean = getLanguage(context) == TURKISH

    /** Context olmayan yerler (ViewModel, servis vb.) için kayıtlı uygulama dilini okur. */
    fun isAppTurkish(): Boolean {
        val ctx = appContext ?: return Locale.getDefault().language == "tr"
        return isTurkish(ctx)
    }

    /**
     * Context'i seçili dile göre sarar. Activity ve Application'ın
     * attachBaseContext'inde çağrılır.
     */
    fun wrap(context: Context): Context {
        val locale = Locale(getLanguage(context))
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
