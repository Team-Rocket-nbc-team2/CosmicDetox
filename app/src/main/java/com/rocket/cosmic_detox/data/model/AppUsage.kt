package com.rocket.cosmic_detox.data.model

import android.graphics.Bitmap
import android.graphics.drawable.Drawable

data class AppUsage(
    val packageId: String = "",
    val appName: String = "",
    val appIcon: Bitmap? = null,
    val usageTime: Long = 0,
    val usagePercentage: Int = 0
)
