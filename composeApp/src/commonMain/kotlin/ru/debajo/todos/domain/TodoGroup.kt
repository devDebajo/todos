package ru.debajo.todos.domain

import androidx.compose.runtime.Immutable

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
const val AllTodosGroupName: String = "All"
val OtherTodosGroupId: GroupId = GroupId("OtherTodosGroup")

val AllTodosGroup: TodoGroup = TodoGroup(
    id = AllTodosGroupId,
    name = AllTodosGroupName,
    actualTodos = emptyList(),
    doneTodos = emptyList(),
    editable = false,
)

fun GroupId.isSyntheticGroup(): Boolean = this == AllTodosGroupId || this == OtherTodosGroupId
