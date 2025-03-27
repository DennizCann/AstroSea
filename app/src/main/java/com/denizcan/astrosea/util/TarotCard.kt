package com.denizcan.astrosea.util

data class TarotCard(
    val id: String,
    val name: String,
    val number: Int,
    val type: String,
    val suit: String = "", // Minor Arcana kartları için takım bilgisi
    val imageResName: String,
    val uprightMeaning: String,
    val reversedMeaning: String,
    val description: String,
    val keywords: List<String>
) 