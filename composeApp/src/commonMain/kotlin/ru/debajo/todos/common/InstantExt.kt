package ru.debajo.todos.common

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// format HH:mm dd-MM-yyyy
fun Instant.formatDateTime(): String {
    val localDateTime: LocalDateTime = toLocalDateTime(TimeZone.currentSystemDefault())
    val date = "${localDateTime.dayOfMonth.toStringMin2Signs()}-${localDateTime.monthNumber.toStringMin2Signs()}-${localDateTime.year}"
    val time = "${localDateTime.hour.toStringMin2Signs()}:${localDateTime.minute.toStringMin2Signs()}"
    return "$time $date"
}

// format HH:mm:ss dd-MM-yyyy
fun Instant.formatDateTimeWithSeconds(): String {
    val localDateTime: LocalDateTime = toLocalDateTime(TimeZone.currentSystemDefault())
    val date = "${localDateTime.dayOfMonth.toStringMin2Signs()}-${localDateTime.monthNumber.toStringMin2Signs()}-${localDateTime.year}"
    val time = "${localDateTime.hour.toStringMin2Signs()}:${localDateTime.minute.toStringMin2Signs()}:${localDateTime.second.toStringMin2Signs()}"
    return "$time $date"
}

private fun Int.toStringMin2Signs(): String {
    val result = toString()
    return if (result.length < 2) "0$result" else result
}
