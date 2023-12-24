package ru.debajo.todos.ui.todolist.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import ru.debajo.todos.domain.AllTodosGroup
import ru.debajo.todos.domain.GroupId
import ru.debajo.todos.domain.TodoGroup
import ru.debajo.todos.domain.TodoItem

@Immutable
data class TodoListState(
    val groups: List<TodoGroup> = listOf(AllTodosGroup),
    val selectedGroupId: GroupId = AllTodosGroup.id,
    val textFieldState: TextFieldValue = TextFieldValue(""),
    val newGroupName: TextFieldValue = TextFieldValue(""),
    val newGroupDialogVisible: Boolean = false,
    val currentDeletingGroup: TodoGroup? = null,
    val savingToFile: Boolean = false,
    val todoItemContextMenuState: TodoItemContextMenuState? = null,
    val currentRenamingGroup: TodoGroup? = null,
    val currentRenamingGroupName: TextFieldValue = TextFieldValue(""),
) {
    private val selectedGroup: Int = groups.indexOfFirst { it.id == selectedGroupId }.takeIf { it >= 0 } ?: 0

    fun canMoveCurrentGroupLeft(): Boolean {
        if (!currentGroup.editable) {
            return false
        }
        return selectedGroup > 0
    }

    fun canMoveCurrentGroupRight(): Boolean {
        if (!currentGroup.editable) {
            return false
        }
        return groups.getOrNull(selectedGroup + 1)?.editable == true
    }

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