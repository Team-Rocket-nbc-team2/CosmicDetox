package com.rocket.cosmic_detox.presentation.extensions

import android.icu.text.DecimalFormat
import android.widget.TextView
import com.rocket.cosmic_detox.R
import org.w3c.dom.Text
import java.math.BigDecimal

fun TextView.setCumulativeTime(time: BigDecimal) {
    text = context.getString(
        R.string.race_format_cumulative_time,
        time.toHours(),
        time.toMinutes()
    )
}

fun BigDecimal.toHours(): Long {
    return this.toLong() / 3600
}

fun BigDecimal.toMinutes(): Long {
    return (this.toLong() % 3600) / 60
}

fun TextView.setPoints(points: BigDecimal) {
    text = context.getString(R.string.race_format_points, points.convertThreeDigitComma())
}

fun BigDecimal.convertThreeDigitComma(): String {
    val decimalFormat = DecimalFormat("#,###")
    return decimalFormat.format(this)
}

fun TextView.setStats(time: BigDecimal, points: BigDecimal) {
    text = context.getString(
        R.string.race_format_stats,
        time.toHours(),
        time.toMinutes(),
        points.convertThreeDigitComma()
    )
}