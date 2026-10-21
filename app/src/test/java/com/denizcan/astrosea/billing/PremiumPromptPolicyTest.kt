package com.denizcan.astrosea.billing

import org.junit.Assert.*
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.TimeZone

class PremiumPromptPolicyTest {
    private val zone = TimeZone.getTimeZone("UTC")
    private val free = MembershipState("free", true)
    private fun at(value: String) = SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
        .apply { timeZone = zone }.parse(value)!!.time
    private val now = at("2026-10-15 18:00")

    @Test fun verifiedFreeUserWithPricesIsEligible() {
        assertTrue(PremiumPromptPolicy.canShow("free", free, true, PremiumPromptHistory(), now, zone))
    }

    @Test fun bothPaidAndGrantedPremiumAreExcluded() {
        for (source in listOf("subscription", "beta", "manual", "sandbox")) {
            val premium = free.copy(levels = listOf(MembershipLevel(source, "unknown", null, null, null, null, true)))
            assertFalse(PremiumPromptPolicy.canShow("free", premium, true, PremiumPromptHistory(), now, zone))
        }
    }

    @Test fun unknownMembershipAndMissingPricesAreExcluded() {
        assertFalse(PremiumPromptPolicy.canShow("free", free.copy(verified = false), true, PremiumPromptHistory(), now, zone))
        assertFalse(PremiumPromptPolicy.canShow("free", free, false, PremiumPromptHistory(), now, zone))
    }

    @Test fun signedOutAndAnotherAccountsMembershipAreExcluded() {
        assertFalse(PremiumPromptPolicy.canShow(null, free, true, PremiumPromptHistory(), now, zone))
        assertFalse(PremiumPromptPolicy.canShow("other", free, true, PremiumPromptHistory(), now, zone))
    }

    @Test fun crossingMidnightDoesNotResetRolling24Hours() {
        val shown = at("2026-10-14 23:59")
        assertFalse(PremiumPromptPolicy.cooldownPassed(PremiumPromptHistory(shown), now, zone))
    }

    @Test fun closingOrManuallyViewingSuppressesAnotherAutomaticPrompt() {
        assertFalse(PremiumPromptPolicy.cooldownPassed(PremiumPromptHistory(shownAt = now - 1000), now, zone))
        assertFalse(PremiumPromptPolicy.cooldownPassed(PremiumPromptHistory(dismissedAt = now - 1000), now, zone))
    }

    @Test fun eligibleAgainAfter24HoursOnAnotherDay() {
        assertTrue(PremiumPromptPolicy.cooldownPassed(PremiumPromptHistory(at("2026-10-14 18:00")), now, zone))
    }

    @Test fun clockRollbackCannotBypassCooldown() {
        assertFalse(PremiumPromptPolicy.cooldownPassed(PremiumPromptHistory(shownAt = now + 1000), now, zone))
    }

    @Test fun sameDayIsBlockedEvenWhenDaylightSavingMakesItLongerThan24Hours() {
        val dstZone = TimeZone.getTimeZone("America/New_York")
        // DST ends on Nov 1, 2026; both timestamps are on that same 25-hour local day.
        assertFalse(PremiumPromptPolicy.cooldownPassed(
            PremiumPromptHistory(at("2026-11-01 04:01")), at("2026-11-02 04:30"), dstZone))
    }
}
