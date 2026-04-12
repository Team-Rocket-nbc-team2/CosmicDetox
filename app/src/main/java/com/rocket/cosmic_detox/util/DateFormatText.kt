package com.rocket.cosmic_detox.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object DateFormatText {

    fun getCurrentTime(): String {
        val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val currentDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time
        return formatter.format(currentDate)
    }

    fun getTotalDays(date: String?): Int {
        // createdDate: yyyy.MM.dd HH:mm:ss
        val raw = date?.trim().orEmpty()
        if (raw.isEmpty()) return 1

        val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val createdDate = runCatching { formatter.parse(raw) }.getOrNull() ?: return 1
        val currentDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time
        val diff = currentDate.time - createdDate.time
        return (diff / (24 * 60 * 60 * 1000)).toInt() + 1 // 일 단위로 변환
    }
}
