package com.dimitrije.hiddengems.model

data class Gem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val imageUrl: String? = null,
    val createdBy: String = "",
    val timestamp: Long = System.currentTimeMillis()
)