package com.denizcan.astrosea.notifications

import org.junit.Assert.*
import org.junit.Test

class DailyReminderPolicyTest {
    private val today = "2026-10-05"

    @Test fun fiveSlotsAreEvenlySpaced() {
        assertEquals(listOf(10, 13, 16, 19, 22), DailyReminderPolicy.hours)
        assertTrue(DailyReminderPolicy.hours.zipWithNext().all { (a, b) -> b - a == 3 })
    }

    @Test fun onlyCurrentSlotMayDeliver() {
        for (hour in 0..23) {
            val allowed = DailyReminderPolicy.hours.indices.filter {
                DailyReminderPolicy.isCurrentSlot(it, today, today, hour)
            }
            if (hour < 10) assertTrue(allowed.isEmpty())
            else assertEquals(listOf(DailyReminderPolicy.hours.indexOfLast { it <= hour }), allowed)
        }
    }

    @Test fun oldDatesAndInvalidSlotsAreIgnored() {
        assertFalse(DailyReminderPolicy.isCurrentSlot(0, "2026-10-04", today, 10))
        assertFalse(DailyReminderPolicy.isCurrentSlot(-1, today, today, 10))
        assertFalse(DailyReminderPolicy.isCurrentSlot(5, today, today, 22))
    }

    @Test fun firstCardDoesNotCountAsCompletedReading() {
        assertFalse(DailyReminderPolicy.completed(today, today, listOf(true, false, false)))
        assertFalse(DailyReminderPolicy.completed(today, today, emptyList()))
        assertFalse(DailyReminderPolicy.completed(today, today, listOf(true)))
        assertTrue(DailyReminderPolicy.completed(today, today, listOf(true, true, true)))
        assertFalse(DailyReminderPolicy.completed("2026-10-04", today, listOf(true, true, true)))
    }
}
