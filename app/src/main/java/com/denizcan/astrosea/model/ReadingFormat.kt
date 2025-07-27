package com.denizcan.astrosea.model

data class ReadingFormats(
    val readingFormats: Map<String, ReadingFormat>,
    val commonTemplate: CommonTemplate
)

data class ReadingFormat(
    val id: String,
    val name: String,
    val cardCount: Int,
    val positions: List<Position>,
    val basePrompt: String,
    val focusAreas: List<String>,
    val specialInstructions: String
)

data class Position(
    val id: String,
    val name: String,
    val description: String
)

data class CommonTemplate(
    val role: String,
    val instructions: String
) 