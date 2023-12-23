@file:OptIn(ExperimentalMaterial3Api::class)

package ru.debajo.todos.ui.todolist

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import ru.debajo.todos.common.ellipsize
import ru.debajo.todos.domain.TodoItem
import ru.debajo.todos.ui.todolist.model.TodoItemAction
import ru.debajo.todos.ui.todolist.model.TodoListNews
import ru.debajo.todos.ui.todolist.model.TodoListState

val LocalNews: ProvidableCompositionLocal<Flow<TodoListNews>> = staticCompositionLocalOf { error("") }

@Composable
fun TodoListScreen(viewModel: TodoListViewModel) {
    CompositionLocalProvider(
        LocalNews provides remember(viewModel) { viewModel.news }
    ) {
        val state by viewModel.state.collectAsState()
        Column(modifier = Modifier.fillMaxSize()) {
            Box {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 16.dp),
                        text = "// TODO",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (state.currentGroup.editable) {
                        TextButton(onClick = { viewModel.onDeleteGroup(state.currentGroup) }) {
                            Text("Delete folder")
                        }
                    }
                }
                if (state.savingToFile) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
            GroupsSpace(
                state = state,
                onGroupClick = { index -> viewModel.selectGroup(index) },
                onNewGroupClick = { viewModel.onNewGroupClick() },
            )
            TodosListWithPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = state,
                onTodoAction = { item, action -> viewModel.onTodoAction(item, action) },
            )
            EditSpace(
                state = state,
                onTextChanged = { viewModel.updateCurrentTodo(it) },
                onSaveClick = { viewModel.saveCurrentTodo() },
            )
        }

        NewGroupDialog(
            state = state,
            onNameChanged = { viewModel.updateCurrentGroup(it) },
            onHide = { viewModel.hideNewGroupDialog() },
            onConfirm = { viewModel.saveNewGroup() }
        )
        DeleteGroupDialog(
            state = state,
            onConfirm = { withTodos -> viewModel.deleteGroup(withTodos = withTodos) },
            onHide = { viewModel.hideDeleteGroupDialog() }
        )
        DeleteTodoItemDialog(
            state = state,
            onConfirm = { viewModel.deleteItem() },
            onHide = { viewModel.hideTodoItemDialog() }
        )
    }
}

@Composable
private fun GroupsSpace(
    modifier: Modifier = Modifier,
    state: TodoListState,
    onGroupClick: (Int) -> Unit,
    onNewGroupClick: () -> Unit,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = remember { PaddingValues(horizontal = 16.dp) },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(
            count = state.groups.size,
            key = { state.groups[it].id.id },
            itemContent = { index ->
                val group = state.groups[index]
                FilterChip(
                    selected = state.currentGroup.id == group.id,
                    onClick = { onGroupClick(index) },
                    label = { Text(text = group.name) }
                )
            }
        )

        item {
            IconButton(onClick = onNewGroupClick) {
                Icon(
                    contentDescription = null,
                    imageVector = Icons.Default.Add,
                )
            }
        }
    }
}

@Composable
private fun TodosListWithPlaceholder(
    modifier: Modifier = Modifier,
    state: TodoListState,
    onTodoAction: (TodoItem, TodoItemAction) -> Unit,
) {
    Box(modifier = modifier) {
        if (state.isEmpty) {
            Text(
                text = "Empty",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            TodosList(
                modifier = Modifier.fillMaxSize(),
                state = state,
                onTodoAction = onTodoAction,
            )
        }
    }
}

@Composable
private fun TodosList(
    modifier: Modifier = Modifier,
    state: TodoListState,
    onTodoAction: (TodoItem, TodoItemAction) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = remember {
            PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp,
            )
        },
        content = {
            items(
                count = state.actual.size,
                key = { state.actual[it].id.id },
                contentType = { "todo" },
                itemContent = { index ->
                    DismissableTodoCard(
                        item = state.actual[index],
                        modifier = Modifier.fillMaxWidth(),
                        focus = state.currentEditingItem?.id == state.actual[index].id,
                        onTodoAction = onTodoAction
                    )
                }
            )

            if (state.done.isNotEmpty()) {
                item {
                    Text(text = "Done")
                }
            }

            items(
                count = state.done.size,
                key = { state.done[it].id.id },
                contentType = { "todo" },
                itemContent = { index ->
                    DismissableTodoCard(
                        item = state.done[index],
                        modifier = Modifier.fillMaxWidth(),
                        focus = state.currentEditingItem?.id == state.done[index].id,
                        onTodoAction = onTodoAction
                    )
                }
            )
        }
    )
}

@Composable
private fun DismissableTodoCard(
    modifier: Modifier = Modifier,
    onTodoAction: (TodoItem, TodoItemAction) -> Unit,
    item: TodoItem,
    focus: Boolean,
) {
    val dismissState = rememberDismissState(confirmValueChange = {
        val action = when (it) {
            DismissValue.Default -> null
            DismissValue.DismissedToEnd -> TodoItemAction.Delete
            DismissValue.DismissedToStart -> TodoItemAction.Archive
        }
        if (action != null) {
            onTodoAction(item, action)
        }
        true
    })

    val news = LocalNews.current
    LaunchedEffect(dismissState, news) {
        news.collect { news ->
            when (news) {
                TodoListNews.ResetSwipeToDismiss -> dismissState.snapTo(DismissValue.Default)
            }
        }
    }

    SwipeToDismiss(
        state = dismissState,
        background = {
            Spacer(modifier = Modifier.size(10.dp))
            Icon(
                modifier = Modifier.align(Alignment.CenterVertically),
                imageVector = Icons.Default.Delete,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                modifier = Modifier.align(Alignment.CenterVertically),
                imageVector = Icons.Default.Done,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.size(10.dp))
        },
        modifier = modifier,
        dismissContent = {
            TodoCard(
                item = item,
                onClick = { onTodoAction(item, TodoItemAction.Edit) },
                modifier = Modifier.fillMaxWidth(),
                focus = focus,
            )
        },
    )
}

@Composable
private fun EditSpace(
    modifier: Modifier = Modifier,
    state: TodoListState,
    onTextChanged: (TextFieldValue) -> Unit,
    onSaveClick: () -> Unit,
) {
    Row(modifier = modifier.padding(10.dp)) {
        BasicTextField(
            modifier = Modifier.weight(1f),
            value = state.textFieldState,
            keyboardOptions = remember {
                KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                )
            },
            onValueChange = onTextChanged,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
            decorationBox = { content ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .height(46.dp)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (state.textFieldState.text.isEmpty()) {
                        Text("Enter TODO")
                    }
                    content()
                }
            }
        )

        Spacer(modifier = Modifier.size(6.dp))

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = state.textFieldState.text.isNotEmpty(), onClick = onSaveClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun NewGroupDialog(
    state: TodoListState,
    onNameChanged: (TextFieldValue) -> Unit,
    onHide: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (state.newGroupDialogVisible) {
        AlertDialog(
            title = { Text("Create folder") },
            text = {
                TextField(
                    placeholder = { Text("Folder name") },
                    value = state.newGroupName,
                    onValueChange = onNameChanged,
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
}

@Composable
private fun DeleteGroupDialog(
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
private fun DeleteTodoItemDialog(
    state: TodoListState,
    onConfirm: () -> Unit,
    onHide: () -> Unit,
) {
    val currentDeletingItem = state.currentDeletingItem
    if (currentDeletingItem != null) {
        AlertDialog(
            title = { Text("Delete TODO?") },
            text = {
                Text("Are you sure to delete TODO ${currentDeletingItem.text.ellipsize(10)}?")
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
private fun TodoCard(
    item: TodoItem,
    onClick: (TodoItem) -> Unit,
    focus: Boolean,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(targetValue = if (focus) 1.05f else 1f, label = "scale")
    Card(modifier = modifier.scale(scale), onClick = { onClick(item) }) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
        ) {
            Text(item.text)
            Spacer(Modifier.size(3.dp))
            Text(
                text = formatDate(item),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun formatDate(item: TodoItem): String {
    return remember(item.createTimestamp, item.updateTimestamp) {
        if (item.createTimestamp == item.updateTimestamp) {
            "Created ${item.createTimestamp.format()}"
        } else {
            "Edited ${item.updateTimestamp.format()}"
        }
    }
}

private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

private fun Instant.format(): String {
    return formatter.format(toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime())
}
