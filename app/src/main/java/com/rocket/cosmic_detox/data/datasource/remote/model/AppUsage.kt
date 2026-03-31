package com.rocket.cosmic_detox.data.datasource.remote.model

import android.graphics.Bitmap

data class AppUsage(
    val packageId: String = "",
    val appName: String = "",
    val appIcon: Bitmap? = null,
    val usageTime: Long = 0,
    val usagePercentage: Int = 0
)
