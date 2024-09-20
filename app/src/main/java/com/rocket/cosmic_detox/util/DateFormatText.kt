package com.rocket.cosmic_detox.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object DateFormatText {

    fun getCurrentTime(): String {
        val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA)
        val currentDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time
        return formatter.format(currentDate)
    }

    fun getTotalDays(date: String): Int {
        // createdDate: yyyy.MM.dd HH:mm:ss
        val formatter = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA)
        val createdDate = formatter.parse(date)
        val currentDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).time
        val diff = currentDate.time - (createdDate?.time ?: 0)
        return (diff / (24 * 60 * 60 * 1000)).toInt() + 1 // 일 단위로 변환
    }
}