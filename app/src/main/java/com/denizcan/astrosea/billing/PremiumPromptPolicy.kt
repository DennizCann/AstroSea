package com.denizcan.astrosea.billing

import java.util.Calendar
import java.util.TimeZone

internal data class PremiumPromptHistory(val shownAt: Long = 0, val dismissedAt: Long = 0)

internal object PremiumPromptPolicy {
    private const val DAY_MS = 24 * 60 * 60 * 1000L

    fun cooldownPassed(history: PremiumPromptHistory, now: Long, zone: TimeZone = TimeZone.getDefault()): Boolean {
        val last = maxOf(history.shownAt, history.dismissedAt)
        if (last <= 0) return true
        // Clock rollback must not allow an extra prompt.
        if (last > now || now - last < DAY_MS) return false
        fun day(time: Long): Pair<Int, Int> = Calendar.getInstance(zone).run {
            timeInMillis = time
            get(Calendar.YEAR) to get(Calendar.DAY_OF_YEAR)
        }
        return day(last) != day(now)
    }

    fun canShow(
        uid: String?,
        membership: MembershipState,
        productsAvailable: Boolean,
        history: PremiumPromptHistory,
        now: Long,
        zone: TimeZone = TimeZone.getDefault()
    ): Boolean = uid != null && membership.uid == uid && membership.verified &&
        !membership.hasAccess && productsAvailable && cooldownPassed(history, now, zone)
}
