package ru.debajo.todos.data.db.model

import kotlinx.datetime.Instant
import ru.debajo.todos.common.UUID

data class DbTodoItem(
    val id: UUID,
    val text: String,
    val createTimestamp: Instant,
    val updateTimestamp: Instant,
    val done: Boolean,
)
