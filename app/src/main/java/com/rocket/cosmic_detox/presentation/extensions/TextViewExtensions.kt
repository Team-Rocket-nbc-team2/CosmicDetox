package com.rocket.cosmic_detox.presentation.extensions

import android.icu.text.DecimalFormat
import android.widget.TextView
import com.rocket.cosmic_detox.R
import org.w3c.dom.Text
import java.math.BigDecimal

fun TextView.setCurrentLocation(cumulativeTime: BigDecimal) {
    context.apply {
        text = when {
            cumulativeTime < BigDecimal(6 * 3600) -> getString(R.string.mercury) // 수성: 누적 6시간 (21600초)
            cumulativeTime < BigDecimal(12 * 3600) -> getString(R.string.mars) // 화성: 누적 12시간 (43200초)
            cumulativeTime < BigDecimal(24 * 3600) -> getString(R.string.venus) // 금성: 누적 24시간 (86400초)
            cumulativeTime < BigDecimal(48 * 3600) -> getString(R.string.earth) // 지구: 누적 48시간 (172800초)
            cumulativeTime < BigDecimal(64 * 3600) -> getString(R.string.neptune) // 해왕성: 누적 64시간 (230400초)
            cumulativeTime < BigDecimal(120 * 3600) -> getString(R.string.uranus) // 천왕성: 누적 120시간 (432000초)
            cumulativeTime < BigDecimal(240 * 3600) -> getString(R.string.saturn) // 토성: 누적 240시간 (864000초)
            cumulativeTime < BigDecimal(400 * 3600) -> getString(R.string.jupiter) // 목성: 누적 400시간 (1440000초)
            else -> getString(R.string.sun)// 500시간 이상일 경우 태양 이미지
        }
    }
}

fun TextView.setCumulativeTime(time: BigDecimal, home: Boolean = false) {
    val hours = time.toHours()
    val minutes = time.toMinutes()

    with(context) {
        text = if(home){
            when {
                hours > 1 && minutes == 0L -> hours.toString()
                else -> minutes.toString()
            }
        } else {
            when {
                hours > 1 && minutes > 0 -> getString(R.string.race_format_total_time, hours, minutes)
                hours > 1 && minutes == 0L -> getString(R.string.race_format_total_time_no_minutes, hours)
                hours == 1L && minutes > 0 -> getString(R.string.race_format_total_time_one_hour, minutes)
                hours == 1L && minutes == 0L -> getString(R.string.race_format_total_time_no_minutes_one_hour)
                else -> getString(R.string.race_format_total_time_no_hours, minutes)
            }
        }
    }
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

    with(context) {
        text = when {
            hours > 1 && minutes > 0 -> getString(R.string.race_format_stats, hours, minutes, formattedPoints)
            hours > 1 && minutes == 0L -> getString(R.string.race_format_stats_time_no_minutes, hours, formattedPoints)
            hours == 1L && minutes > 0 -> getString(R.string.race_format_stats_time_one_hour, minutes, formattedPoints)
            hours == 1L && minutes == 0L -> getString(R.string.race_format_stats_time_no_minutes_one_hour, formattedPoints)
            else -> getString(R.string.race_format_stats_time_no_hours, minutes, formattedPoints)
        }
    }
}

fun TextView.setTravelingTime(time: BigDecimal) {
    val hours = time.toHours()
    val minutes = time.toMinutes()
    val seconds = time.toSeconds()

    with(context) {
        text = getString(R.string.home_traveling_time, hours, minutes)
    }
}

fun TextView.setMyDescription(days: Long, time: BigDecimal) {
    val hours = time.toHours()
    val minutes = time.toMinutes()

    with(context) {
        text = when {
            days == 0L -> getString(R.string.format_my_description_no_days)
            hours > 1 && minutes > 0 -> getString(R.string.format_my_description, days, hours, minutes)
            hours > 1 && minutes == 0L -> getString(R.string.format_my_description_no_minutes, days, hours)
            hours == 1L && minutes > 0 -> getString(R.string.format_my_description_one_hour, days, minutes)
            hours == 1L && minutes == 0L -> getString(R.string.format_my_description_no_minutes_one_hour, days)
            else -> getString(R.string.format_my_description_no_hours, days, minutes)
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