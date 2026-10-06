package com.denizcan.astrosea.util

/** A small versioned request. Card order is the spread's position order. */
fun tarotReadingRequest(spreadId: String, language: String, cardIds: List<String>, expectedCount: Int): Map<String, Any> {
    require(spreadId.isNotBlank() && language in setOf("tr", "en"))
    require(expectedCount in 1..9 && cardIds.size == expectedCount)
    require(cardIds.all { it.isNotBlank() } && cardIds.distinct().size == cardIds.size)
    return mapOf("schemaVersion" to 2, "spreadId" to spreadId, "language" to language, "cardIds" to cardIds.toList())
}
