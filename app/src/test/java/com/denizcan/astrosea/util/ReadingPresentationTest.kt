package com.denizcan.astrosea.util

import org.junit.Assert.*
import org.junit.Test

class ReadingPresentationTest {
    private val turkish = "### Düşünce: Güneş\nİlk kartın yorumu.\n\n### His: Ay\nİkinci kartın yorumu.\n\n###Genel Yorum\nOrtak hikâye ve tavsiye."

    @Test fun turkishSummaryComesFirstWithoutChangingBodies() {
        val result = readingPresentation(turkish)
        assertTrue(result.hasDetails)
        assertEquals(listOf("Genel Yorum", "Ortak hikâye ve tavsiye."), result.overview.map { it.text })
        assertEquals(listOf("Düşünce: Güneş", "İlk kartın yorumu.", "His: Ay", "İkinci kartın yorumu."), result.details.map { it.text })
        assertTrue((result.overview + result.details).filter { it.isHeading }.none { it.text.startsWith("#") })
    }

    @Test fun englishAndAlternativeMarkdownHeadingLevels() {
        val result = readingPresentation("## You: The Fool\r\nA new beginning.\r\n###### **Overall Reading** ###\r\nKeep learning.")
        assertTrue(result.hasDetails)
        assertEquals("Overall Reading", result.overview.first().text)
        assertEquals("Keep learning.", result.overview.last().text)
    }

    @Test fun plainSummaryHeadingIsSupported() {
        assertTrue(readingPresentation("### You: Sun\nCard body.\nOverall Reading:\nSummary.").hasDetails)
    }

    @Test fun missingOrEmptySummaryNeverHidesCards() {
        for (text in listOf("### You: Sun\nBody.", "### You: Sun\nBody.\n### Overall Reading")) {
            val result = readingPresentation(text)
            assertFalse(result.hasDetails)
            assertTrue(result.overview.any { it.text == "Body." })
        }
    }

    @Test fun duplicateSummaryAndUnknownSectionsAreNotReordered() {
        val duplicate = turkish + "\n### Genel Yorum\nBaşka sonuç."
        val trailingSection = turkish + "\n### Tavsiye\nEk tavsiye."
        for (text in listOf(duplicate, trailingSection)) {
            val result = readingPresentation(text)
            assertFalse(result.hasDetails)
            assertEquals("Düşünce: Güneş", result.overview.first().text)
            assertTrue(result.overview.last().text.isNotBlank())
        }
    }

    @Test fun preambleAndErrorFallbackRemainVisible() {
        assertFalse(readingPresentation("Hata açıklaması\n" + turkish).hasDetails)
        val result = readingPresentation(turkish, allowCollapse = false)
        assertFalse(result.hasDetails)
        assertEquals("Düşünce: Güneş", result.overview.first().text)
        assertTrue(result.overview.first().isHeading)
    }

    @Test fun summaryAlreadyFirstAndPlainTextStayInOrder() {
        val result = readingPresentation("### Genel Yorum\nÖzet.\n### Kart: Ay\nDetay.")
        assertFalse(result.hasDetails)
        assertEquals("Genel Yorum", result.overview.first().text)
        assertEquals("Detay.", result.overview.last().text)
        assertEquals(listOf(ReadingBlock("Sadece metin; C# ve #etiket aynı kalır.")),
            readingPresentation("Sadece metin; C# ve #etiket aynı kalır.").overview)
    }

    @Test fun longBodiesStayBoundedAndEmptyInputIsSafe() {
        val body = "a".repeat(5000)
        val result = readingPresentation("### Kart: Ay\n$body\n### Genel Yorum\nÖzet.")
        assertTrue(result.hasDetails)
        assertTrue(result.details.filterNot { it.isHeading }.all { it.text.length <= 1200 })
        assertEquals(body, result.details.filterNot { it.isHeading }.joinToString("") { it.text })
        assertTrue(readingPresentation("").overview.isEmpty())
    }

    @Test fun emptyCardSectionsAndUnrecognizedHeadingsDoNotCollapse() {
        assertFalse(readingPresentation("### Kart: Ay\n### Genel Yorum\nÖzet.").hasDetails)
        assertFalse(readingPresentation("### Açıklama\nMetin.\n### Genel Yorum\nÖzet.").hasDetails)
    }
}
