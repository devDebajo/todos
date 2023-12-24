package ru.debajo.todos.ui.todolist.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import ru.debajo.todos.domain.AllTodosGroup
import ru.debajo.todos.domain.TodoGroup
import ru.debajo.todos.domain.TodoItem

@Immutable
data class TodoListState(
    val groups: List<TodoGroup> = listOf(AllTodosGroup),
    val selectedGroup: Int = 0,
    val textFieldState: TextFieldValue = TextFieldValue(""),
    val newGroupName: TextFieldValue = TextFieldValue(""),
    val newGroupDialogVisible: Boolean = false,
    val currentDeletingGroup: TodoGroup? = null,
    val savingToFile: Boolean = false,
    val todoItemContextMenuState: TodoItemContextMenuState? = null,
    val currentRenamingGroup: TodoGroup? = null,
    val currentRenamingGroupName: TextFieldValue = TextFieldValue(""),
) {
    val currentGroup: TodoGroup = groups[selectedGroup.coerceIn(groups.indices)]
    val actual: List<TodoItem> = currentGroup.actualTodos
    val done: List<TodoItem> = currentGroup.doneTodos
    val isEmpty: Boolean = groups.all { it.isEmpty }
}

@Immutable
data class TodoItemContextMenuState(
    val item: TodoItem,
    val position: IntOffset,
    val visible: Boolean = true,
    val changeTextDialogVisible: Boolean = false,
    val changeTextDialogValue: TextFieldValue = TextFieldValue(item.text),
    val showDeleteDialog: Boolean = false,
)