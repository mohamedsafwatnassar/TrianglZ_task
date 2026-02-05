package com.trianglz.chatapp.presentation.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility object for formatting dates and times
 */
object DateTimeFormatter {

    private const val MINUTE_MILLIS = 60_000L
    private const val HOUR_MILLIS = 3600_000L
    private const val DAY_MILLIS = 86400_000L

    /**
     * Format timestamp to relative or absolute time
     * - "Just now" for < 1 minute
     * - "Xm ago" for < 1 hour
     * - "HH:mm" for same day
     * - "MMM dd, HH:mm" for older
     */
    fun formatMessageTimestamp(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < MINUTE_MILLIS -> "Just now"
            diff < HOUR_MILLIS -> "${diff / MINUTE_MILLIS}m ago"
            diff < DAY_MILLIS -> {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
            }
            else -> {
                SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }

    /**
     * Format timestamp to full date and time
     */
    fun formatFullDateTime(timestamp: Long): String {
        return SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            .format(Date(timestamp))
    }

    /**
     * Format timestamp to time only
     */
    fun formatTimeOnly(timestamp: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(Date(timestamp))
    }

    /**
     * Format timestamp to date only
     */
    fun formatDateOnly(timestamp: Long): String {
        return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            .format(Date(timestamp))
    }
}