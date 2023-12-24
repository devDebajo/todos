package ru.debajo.todos.ui.todolist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import ru.debajo.todos.common.ellipsize
import ru.debajo.todos.ui.todolist.model.TodoListState

@Composable
internal fun NewGroupDialog(
    state: TodoListState,
    onNameChanged: (TextFieldValue) -> Unit,
    onHide: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (state.newGroupDialogVisible) {
        TextFieldDialog(
            title = "Create folder",
            placeholder = "Folder name",
            value = state.newGroupName,
            onValueChanged = onNameChanged,
            onHide = onHide,
            onConfirm = onConfirm,
        )
    }
}

@Composable
internal fun DeleteGroupDialog(
    state: TodoListState,
    onConfirm: (withTodos: Boolean) -> Unit,
    onHide: () -> Unit,
) {
    val currentDeletingGroup = state.currentDeletingGroup
    if (currentDeletingGroup != null) {
        AlertDialog(
            title = { Text("Delete folder?") },
            text = {
                Text("Are you sure to delete folder ${currentDeletingGroup.name}?")
            },
            confirmButton = {
                Column(horizontalAlignment = Alignment.End) {
                    TextButton(onClick = { onConfirm(false) }) {
                        Text("Delete only folder")
                    }

                    TextButton(onClick = { onConfirm(true) }) {
                        Text("Delete folder with todos")
                    }

                    TextButton(onClick = onHide) {
                        Text("Cancel")
                    }
                }
            },
            onDismissRequest = onHide
        )
    }
}

@Composable
internal fun RenameGroupDialog(
    state: TodoListState,
    onNameChanged: (TextFieldValue) -> Unit,
    onHide: () -> Unit,
    onConfirm: () -> Unit,
) {
    val currentRenamingGroup = state.currentRenamingGroup
    if (currentRenamingGroup != null) {
        TextFieldDialog(
            title = "Rename folder",
            placeholder = "Folder name",
            value = state.currentRenamingGroupName,
            onValueChanged = onNameChanged,
            onHide = onHide,
            onConfirm = onConfirm,
        )
    }
}

@Composable
internal fun UpdateItemTextDialog(
    state: TodoListState,
    onNameChanged: (TextFieldValue) -> Unit,
    onHide: () -> Unit,
    onConfirm: () -> Unit,
) {
    val todoItemContextMenuState = state.todoItemContextMenuState
    if (todoItemContextMenuState?.changeTextDialogVisible == true) {
        TextFieldDialog(
            title = "Update TODO",
            placeholder = "Text",
            value = todoItemContextMenuState.changeTextDialogValue,
            onValueChanged = onNameChanged,
            onHide = onHide,
            onConfirm = onConfirm,
        )
    }
}

@Composable
internal fun DeleteTodoItemDialog(
    state: TodoListState,
    onConfirm: () -> Unit,
    onHide: () -> Unit,
) {
    val todoItemContextMenuState = state.todoItemContextMenuState
    if (todoItemContextMenuState?.showDeleteDialog == true) {
        AlertDialog(
            title = { Text("Delete TODO?") },
            text = {
                Text("Are you sure to delete TODO ${todoItemContextMenuState.item.text.ellipsize(20)}?")
            },
            dismissButton = {
                TextButton(onClick = onHide) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Delete")
                }
            },
            onDismissRequest = onHide
        )
    }
}

@Composable
private fun TextFieldDialog(
    title: String,
    placeholder: String = "",
    value: TextFieldValue,
    onValueChanged: (TextFieldValue) -> Unit,
    onHide: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        title = { Text(title) },
        text = {
            TextField(
                placeholder = { Text(placeholder) },
                value = value,
                onValueChange = onValueChanged,
                keyboardOptions = remember {
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send,
                    )
                },
                keyboardActions = remember(onConfirm) {
                    KeyboardActions(
                        onSend = { onConfirm() }
                    )
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onHide) {
                Text("Cancel")
            }
        },
        onDismissRequest = onHide
    )
}
