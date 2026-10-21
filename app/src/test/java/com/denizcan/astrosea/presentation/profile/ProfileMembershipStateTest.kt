package com.denizcan.astrosea.presentation.profile

import com.denizcan.astrosea.billing.MembershipLevel
import com.denizcan.astrosea.billing.MembershipState
import org.junit.Assert.*
import org.junit.Test

class ProfileMembershipStateTest {
    private val premium = MembershipState("review", true, listOf(
        MembershipLevel("manual", "unknown", null, null, null, null, true)
    ))

    @Test fun profileResetKeepsCurrentVerifiedMembership() {
        val state = ProfileState().withMembershipFor("review", premium)
        assertEquals(premium, state.membership)
        assertTrue(state.profileData.isPremium)
    }

    @Test fun equalRefreshRestoresMembershipWithoutAnotherFlowEmission() {
        val cached = premium.copy()
        assertEquals(premium, cached)
        val reset = ProfileState().withMembershipFor("review", cached)
        assertTrue(reset.membership.verified)
        assertTrue(reset.membership.hasAccess)
    }

    @Test fun switchingAccountNeverCopiesPreviousPremiumAccess() {
        val oldState = ProfileState().withMembershipFor("review", premium)
        val next = oldState.withMembershipFor("another-user", premium)
        assertFalse(next.membership.verified)
        assertFalse(next.profileData.isPremium)
        assertEquals("another-user", next.membership.uid)
    }

    @Test fun signedOutAndFailedVerificationNeverGrantPremium() {
        assertFalse(ProfileState().withMembershipFor(null, premium).profileData.isPremium)
        val failed = MembershipState("review")
        val state = ProfileState().withMembershipFor("review", premium)
            .withMembershipFor("review", failed)
        assertFalse(state.membership.verified)
        assertFalse(state.profileData.isPremium)
    }

    @Test fun verifiedFreeMemberDoesNotShowVerificationFailure() {
        val state = ProfileState().withMembershipFor("free", MembershipState("free", true))
        assertTrue(state.membership.verified)
        assertFalse(state.profileData.isPremium)
    }
}
