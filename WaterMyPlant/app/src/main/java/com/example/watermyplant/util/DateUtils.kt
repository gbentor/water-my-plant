package com.example.watermyplant.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {
    fun formatDate(instant: Instant?): String {
        if (instant == null) return "Never"
        val dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    }
} 