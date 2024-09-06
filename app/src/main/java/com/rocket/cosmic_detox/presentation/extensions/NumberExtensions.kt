package com.rocket.cosmic_detox.presentation.extensions

import android.content.Context
import java.math.BigDecimal

// dp 값을 px로 변환
fun Int.dpToPx(context: Context): Int {
    val density = context.resources.displayMetrics.density
    return (this * density).toInt()
}

fun BigDecimal.toHours(): Long {
    return this.toLong() / 3600
}

fun BigDecimal.toMinutes(): Long {
    return (this.toLong() % 3600) / 60
}

fun BigDecimal.toSeconds(): Long {
    return this.toLong() % 60
}

fun Long.fromSecondsToMinutes(): Long {
    return this / 60
}

fun Long.fromSecondsToHours(): Long {
    return this / 3600
}

fun Long.fromMinutesToSeconds(): Long {
    return this * 60
}

// ms -> s
fun BigDecimal.fromMillisecondsToSeconds(): BigDecimal {
    return this.divide(BigDecimal(1000))
}