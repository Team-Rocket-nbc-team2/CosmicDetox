package com.rocket.cosmic_detox.data.datasource.remote.model

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InstalledApp(
    val packageId: String,
    val appName: String,
    val appIcon: Bitmap,
    val limitedTime: Long,
    val isChecked: Boolean = false
) : Parcelable
