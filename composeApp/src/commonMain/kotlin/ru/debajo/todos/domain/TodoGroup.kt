package ru.debajo.todos.domain

import androidx.compose.runtime.Immutable
import ru.debajo.todos.common.UUID
import ru.debajo.todos.strings.R

@Immutable
data class TodoGroup(
    val id: GroupId,
    val name: String,
    val actualTodos: List<TodoItem>,
    val doneTodos: List<TodoItem>,
    val editable: Boolean,
) {
    val isEmpty: Boolean = actualTodos.isEmpty() && doneTodos.isEmpty()
}

val AllTodosGroupId: GroupId = GroupId(UUID("837fa61a-2b9c-461f-baed-e2c535ec179f"))
val OtherTodosGroupId: GroupId = GroupId(UUID("6a607772-70e8-4e82-b0c7-8735c47416be"))

val AllTodosGroup: TodoGroup = TodoGroup(
    id = AllTodosGroupId,
    name = R.strings.allTodosGroupName,
    actualTodos = emptyList(),
    doneTodos = emptyList(),
    editable = false,
)

fun GroupId.isSyntheticGroup(): Boolean = this == AllTodosGroupId || this == OtherTodosGroupId
