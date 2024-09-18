package com.rocket.cosmic_detox.data.model

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckedApp(
    val packageId: String,
    val appName: String,
    val appIcon: Bitmap,
    val limitedTime: Long,
    val isChecked: Boolean = false
) : Parcelable
