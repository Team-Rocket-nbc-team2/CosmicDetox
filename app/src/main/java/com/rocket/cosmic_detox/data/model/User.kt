package com.rocket.cosmic_detox.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val dailyTime: Long = 0,
    val totalTime: Long = 0,
    val totalDay: Long = 0,
    val createdAt: String = "",
    val isWithdrawn: Boolean = false,
    val trophies: List<Trophy> = listOf(),
    val apps: List<AllowedApp> = listOf()
)
