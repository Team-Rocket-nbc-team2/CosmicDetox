package com.rocket.cosmic_detox.data.model

data class MyInfo(
    val uid: String,
    val name: String,
    val dailyTime: Long,
    val totalTime: Long,
    val totalDay: Long,
    val isWithdrawn: Boolean,
    val trophies: List<Trophy>,
    val apps: List<AllowedApp>
)
