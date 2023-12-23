package ru.debajo.todos.domain

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant

@Immutable
data class TodoItem(
    val id: TodoId,
    val text: String,
    val createTimestamp: Instant,
    val updateTimestamp: Instant,
    val done: Boolean,
)
