package ru.debajo.todos.common

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// format yyyy-MM-dd HH:mm
fun Instant.formatDateTime(): String {
    val localDateTime: LocalDateTime = toLocalDateTime(TimeZone.currentSystemDefault())
    val date = "${localDateTime.year}-${localDateTime.monthNumber.toStringMin2Signs()}-${localDateTime.dayOfMonth.toStringMin2Signs()}"
    val time = "${localDateTime.hour.toStringMin2Signs()}:${localDateTime.minute.toStringMin2Signs()}"
    return "$date $time"
}

private fun Int.toStringMin2Signs(): String {
    val result = toString()
    return if (result.length < 2) "0$result" else result
}
