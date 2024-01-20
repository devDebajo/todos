package ru.debajo.todos.data.db.model

import ru.debajo.todos.common.UUID

data class DbTodoGroupToItemLink(
    val groupId: UUID,
    val todoId: UUID,
)
