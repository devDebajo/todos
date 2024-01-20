package ru.debajo.todos.data.db.model

import ru.debajo.todos.common.UUID

data class DbTodoGroup(
    val id: UUID,
    val name: String,
    val position: Int,
)

