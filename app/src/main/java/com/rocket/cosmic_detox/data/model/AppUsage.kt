package com.rocket.cosmic_detox.data.model

import android.graphics.drawable.Drawable

data class AppUsage(
    val packageId: String = "",
    val appName: String = "",
    val appIcon: Drawable? = null,
    val usageTime: Long = 0,
)
