package com.denizcan.astrosea.util

import org.junit.Assert.*
import org.junit.Test

class TarotReadingRequestTest {
    @Test fun sendsOnlyVersionedIdsInPositionOrder() {
        val ids = listOf("fool", "magician", "high_priestess")
        val data = tarotReadingRequest("daily_reading", "tr", ids, 3)
        assertEquals(setOf("schemaVersion", "spreadId", "language", "cardIds"), data.keys)
        assertEquals(2, data["schemaVersion"])
        assertEquals(ids, data["cardIds"])
        assertEquals("en", tarotReadingRequest("single_card_reading", "en", listOf("fool"), 1)["language"])
    }

    @Test fun invalidLocalRequestsFailBeforeNetworkCall() {
        assertThrows(IllegalArgumentException::class.java) { tarotReadingRequest("daily_reading", "tr", listOf("fool"), 3) }
        assertThrows(IllegalArgumentException::class.java) { tarotReadingRequest("daily_reading", "tr", listOf("fool", "fool", "fool"), 3) }
        assertThrows(IllegalArgumentException::class.java) { tarotReadingRequest("single_card_reading", "fr", listOf("fool"), 1) }
        assertThrows(IllegalArgumentException::class.java) { tarotReadingRequest("single_card_reading", "en", listOf(""), 1) }
    }
}
