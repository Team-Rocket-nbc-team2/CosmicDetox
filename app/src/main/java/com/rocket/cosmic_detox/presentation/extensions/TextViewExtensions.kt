package com.rocket.cosmic_detox.presentation.extensions

import android.icu.text.DecimalFormat
import android.widget.TextView
import com.rocket.cosmic_detox.R
import org.w3c.dom.Text
import java.math.BigDecimal

fun TextView.setCumulativeTime(time: BigDecimal) {
    val hours = time.toHours()
    val minutes = time.toMinutes()

    context.run {
        text = when {
            hours > 1 && minutes > 0 -> getString(R.string.race_format_total_time, hours, minutes)
            hours > 1 && minutes == 0L -> getString(R.string.race_format_total_time_no_minutes, hours)
            hours == 1L && minutes > 0 -> getString(R.string.race_format_total_time_one_hour, minutes)
            hours == 1L && minutes == 0L -> getString(R.string.race_format_total_time_no_minutes_one_hour)
            else -> getString(R.string.race_format_total_time_no_hours, minutes)
        }
    }
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

// ms -> s
fun BigDecimal.fromMillisecondsToSeconds(): BigDecimal {
    return this / 1000.toBigDecimal()
}

fun TextView.setPoints(points: BigDecimal) {
    text = context.getString(R.string.race_format_points, points.convertThreeDigitComma())
}

fun BigDecimal.convertThreeDigitComma(): String {
    val decimalFormat = DecimalFormat("#,###")
    return decimalFormat.format(this)
}

fun TextView.setStats(time: BigDecimal, points: BigDecimal) {
    val hours = time.toHours()
    val minutes = time.toMinutes()
    val formattedPoints = points.convertThreeDigitComma()

    context.run {
        text = when {
            hours > 1 && minutes > 0 -> getString(R.string.race_format_stats, hours, minutes, formattedPoints)
            hours > 1 && minutes == 0L -> getString(R.string.race_format_stats_time_no_minutes, hours, formattedPoints)
            hours == 1L && minutes > 0 -> getString(R.string.race_format_stats_time_one_hour, minutes, formattedPoints)
            hours == 1L && minutes == 0L -> getString(R.string.race_format_stats_time_no_minutes_one_hour, formattedPoints)
            else -> getString(R.string.race_format_stats_time_no_hours, minutes, formattedPoints)
        }
    }
}

//fun TextView.setAppUsageTime(time: BigDecimal) {
//    val hours = time.toHours()
//    val minutes = time.toMinutes()
//
//    context.run {
//        text = when {
//            hours > 1 && minutes > 0 -> getString(R.string