package com.denizcan.astrosea.util

import org.junit.Assert.*
import org.junit.Test

class PerformanceHelpersTest {
    @Test fun largeImagesAreSampledBeforeAllocation() {
        assertEquals(8, bitmapSampleSize(4500, 4500, 512))
        assertEquals(4, bitmapSampleSize(1500, 2550, 512))
        assertEquals(512 to 512, boundedImageSize(4500, 4500, 512))
        assertEquals(301 to 512, boundedImageSize(1500, 2550, 512))
    }

    @Test fun backgroundsKeepAspectRatioAndSmallImagesAreNotUpscaled() {
        assertEquals(1244 to 1600, boundedImageSize(1792, 2304, 1600))
        assertEquals(48 to 24, boundedImageSize(48, 24, 512))
        assertEquals(1, bitmapSampleSize(48, 24, 512))
    }

    @Test fun blankReadingsAndParagraphBoundaries() {
        assertTrue(readingParagraphs(" \n\n ").isEmpty())
        assertEquals(listOf("İlk paragraf", "Second paragraph"), readingParagraphs("İlk paragraf\r\n\r\nSecond paragraph"))
    }

    @Test fun veryLongReadingKeepsAllWordsInBoundedChunks() {
        val text = (1..4000).joinToString(" ") { "sözcük$it" }
        val parts = readingParagraphs(text)
        assertTrue(parts.size > 1)
        assertTrue(parts.all { it.length <= 1200 })
        assertEquals(text, parts.joinToString(" "))
    }

    @Test fun longUnbrokenTextAndEmojiAreNotLost() {
        val text = "a".repeat(1199) + "🔮" + "b".repeat(2400)
        val parts = readingParagraphs(text)
        assertEquals(text, parts.joinToString(""))
        assertTrue(parts.all { it.length <= 1200 })
        assertTrue(parts.none { Character.isHighSurrogate(it.last()) || Character.isLowSurrogate(it.first()) })
    }
}
