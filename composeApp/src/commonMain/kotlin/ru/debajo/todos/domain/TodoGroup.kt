package ru.debajo.todos.domain

import androidx.compose.runtime.Immutable
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

val AllTodosGroupId: GroupId = GroupId("AllTodosGroup")
val OtherTodosGroupId: GroupId = GroupId("OtherTodosGroup")

val AllTodosGroup: TodoGroup = TodoGroup(
    id = AllTodosGroupId,
    name = R.strings.allTodosGroupName,
    actualTodos = emptyList(),
    doneTodos = emptyList(),
    editable = false,
)

fun GroupId.isSyntheticGroup(): Boolean = this == AllTodosGroupId || this == OtherTodosGroupId
