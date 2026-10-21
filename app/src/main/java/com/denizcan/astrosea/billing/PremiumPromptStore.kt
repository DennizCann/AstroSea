package com.denizcan.astrosea.billing

import android.content.Context

/** Device-local, per-account reminder history; never a source of Premium access. */
internal class PremiumPromptStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("premium_prompt", Context.MODE_PRIVATE)

    fun history(uid: String) = PremiumPromptHistory(
        preferences.getLong("$uid.shown", 0),
        preferences.getLong("$uid.dismissed", 0)
    )

    fun recordShown(uid: String) {
        preferences.edit().putLong("$uid.shown", System.currentTimeMillis()).apply()
    }

    fun recordDismissed(uid: String) {
        preferences.edit().putLong("$uid.dismissed", System.currentTimeMillis()).apply()
    }
}
