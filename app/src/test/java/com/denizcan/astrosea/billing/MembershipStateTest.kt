package com.denizcan.astrosea.billing

import org.junit.Assert.*
import org.junit.Test

class MembershipStateTest {
    private fun level(source: String = "beta", expiry: String? = "2099-01-01T00:00:00.000Z") =
        MembershipLevel(source, "unknown", null, null, expiry, null, true)

    @Test fun unverifiedStateNeverGrantsAccess() {
        assertFalse(MembershipState("user", false, listOf(level())).hasAccess)
    }

    @Test fun expiryIsRecheckedWithoutAnotherServerResponse() {
        assertFalse(MembershipState("user", true, listOf(level(expiry = "2000-01-01T00:00:00.000Z"))).hasAccess)
        assertFalse(MembershipState("user", true, listOf(level(expiry = "bad date"))).hasAccess)
    }

    @Test fun betaExpiryDoesNotRemovePaidAccess() {
        val state = MembershipState("user", true, listOf(
            level(expiry = "2000-01-01T00:00:00.000Z"), level(source = "subscription")))
        assertTrue(state.hasAccess)
        assertEquals("subscription", state.activeLevels.single().source)
    }

    @Test fun bothRightsRemainSeparate() {
        assertEquals(2, MembershipState("user", true, listOf(level(), level("subscription"))).activeLevels.size)
    }

    @Test fun serverInactiveAndSignedOutHaveNoAccess() {
        assertFalse(MembershipState().hasAccess)
        assertFalse(MembershipState("user", true, listOf(level().copy(isActive = false))).hasAccess)
    }

    @Test fun utcTimestampIsParsedIndependentlyOfDeviceTimezone() {
        assertEquals(0L, membershipTime("1970-01-01T00:00:00.000Z"))
    }
}
