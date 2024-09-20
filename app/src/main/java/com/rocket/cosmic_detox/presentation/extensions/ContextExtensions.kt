package com.rocket.cosmic_detox.presentation.extensions

import android.content.Context

fun Context.isAppInstalled(packageId: String): Boolean {
    return try {
        packageManager.getApplicationInfo(packageId, 0)
        true
    } catch (e: Exception) {
        false
    }
}