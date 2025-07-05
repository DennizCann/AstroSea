package com.denizcan.astrosea.model

data class TarotCard(
    val id: String,
    val name: String,
    val turkishName: String? = null,
    val number: String? = null,
    val type: String,
    val suit: String? = null,
    val imageResName: String,
    val meaningUpright: String,
    val meaningReversed: String,
    val description: String,
    val keywords: List<String>,
    val zodiacSigns: String?,
    val predictions: List<String>?
) 