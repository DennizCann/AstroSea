package com.denizcan.astrosea.util

import java.util.Locale

data class ReadingBlock(val text: String, val isHeading: Boolean = false)
data class ReadingPresentation(
    val overview: List<ReadingBlock>,
    val details: List<ReadingBlock> = emptyList()
) {
    val hasDetails: Boolean get() = details.isNotEmpty()
}

private val markdownHeading = Regex("^\\s{0,3}#{1,6}\\s*(\\S.*?)\\s*$")
private val summaryTitles = setOf("genel yorum", "genel yorum ve tavsiye", "overall reading")

private fun cleanHeading(text: String): String =
    text.trim().replace(Regex("\\s+#+$"), "").removeSurrounding("**").trim()

private fun isSummary(text: String): Boolean =
    text.trim().trimEnd(':').lowercase(Locale.ROOT).replace("\u0307", "") in summaryTitles

/** Presentation only: never overwrite the original response or guess where a summary starts. */
fun readingPresentation(text: String, allowCollapse: Boolean = true): ReadingPresentation {
    val blocks = mutableListOf<ReadingBlock>()
    val body = StringBuilder()
    fun flushBody() {
        blocks += readingParagraphs(body.toString()).map { ReadingBlock(it) }
        body.clear()
    }
    text.replace("\r\n", "\n").lineSequence().forEach { line ->
        val heading = markdownHeading.matchEntire(line)?.groupValues?.get(1)?.let(::cleanHeading)
        val plainSummary = cleanHeading(line).takeIf(::isSummary)
        if (heading != null || plainSummary != null) {
            flushBody()
            blocks += ReadingBlock(heading ?: plainSummary!!, isHeading = true)
        } else {
            body.append(line).append('\n')
        }
    }
    flushBody()

    val full = ReadingPresentation(blocks)
    if (!allowCollapse || blocks.firstOrNull()?.isHeading != true) return full
    val headings = blocks.indices.filter { blocks[it].isHeading }
    val summaries = headings.filter { isSummary(blocks[it].text) }
    if (summaries.size != 1) return full
    val summary = summaries.single()
    // Only rearrange a complete, unambiguous final summary after card sections.
    if (summary == 0 || summary != headings.last() || summary == blocks.lastIndex) return full
    if (headings.zipWithNext().any { (a, b) -> b == a + 1 }) return full
    if (headings.filter { it < summary }.any { ':' !in blocks[it].text }) return full
    return ReadingPresentation(overview = blocks.drop(summary), details = blocks.take(summary))
}
