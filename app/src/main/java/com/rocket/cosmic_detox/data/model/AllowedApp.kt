package com.rocket.cosmic_detox.data.model

data class AllowedApp(
    val packageId: String = "",
    val appName: String = "",
    val limitedTime: Int = 0,
    val isAllowed: Boolean = false
)
