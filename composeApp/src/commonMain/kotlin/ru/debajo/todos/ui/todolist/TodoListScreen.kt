package ru.debajo.todos.ui.todolist

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import java.time.format.DateTimeFormatter
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import ru.debajo.todos.common.contextClickable
import ru.debajo.todos.common.roundToPx
import ru.debajo.todos.common.toDp
import ru.debajo.todos.common.toIntOffset
import ru.debajo.todos.domain.GroupId
import ru.debajo.todos.domain.TodoItem
import ru.debajo.todos.ui.todolist.model.TodoItemAction
import ru.debajo.todos.ui.todolist.model.TodoListState

@Composable
fun TodoListScreen(viewModel: TodoListViewModel) {
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
                    GroupMenu(
                        canMoveLeft = remember(state) { state.canMoveCurrentGroupLeft() },
                        canMoveRight = remember(state) { state.canMoveCurrentGroupRight() },
                        onRenameClick = { viewModel.onRenameCurrentGroupClick() },
                        onDeleteClick = { viewModel.onDeleteCurrentGroupClick() },
                        onMoveLeftClick = { viewModel.moveCurrentGroupLeft() },
                        onMoveRightClick = { viewModel.moveCurrentGroupRight() }
                    )
                }
            }
            if (state.savingToFile) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
        val haptic = LocalHapticFeedback.current
        GroupsSpace(
            state = state,
            onGroupClick = { id ->
                viewModel.selectGroup(id)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            onNewGroupClick = {
                viewModel.onNewGroupClick()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
        )
        TodosListWithPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = state,
            onContextClick = { item, coordinates ->
                viewModel.onItemContextClick(item, coordinates.positionInRoot().toIntOffset())
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
        )
        EditSpace(
            state = state,
            onTextChanged = { viewModel.updateCurrentTodo(it) },
            onSaveClick = {
                viewModel.saveCurrentTodo()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
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
        onHide = { viewModel.hideDeleteTodoItemDialog() }
    )
    RenameGroupDialog(
        state = state,
        onNameChanged = { viewModel.updateCurrentGroupName(it) },
        onHide = { viewModel.hideRenameGroupDialog() },
        onConfirm = { viewModel.renameCurrentGroup() }
    )
    ContextItemPopup(
        state = state,
        onTodoAction = { item, action -> viewModel.onTodoAction(item, action) },
        onHide = { viewModel.hideContextPopup() }
    )
    UpdateItemTextDialog(
        state = state,
        onNameChanged = { viewModel.onUpdateItemTextChanged(it) },
        onHide = { viewModel.hideUpdateItemTextDialog() },
        onConfirm = { viewModel.updateItemText() }
    )
}

@Composable
private fun ContextItemPopup(
    state: TodoListState,
    onTodoAction: (TodoItem, TodoItemAction) -> Unit,
    onHide: () -> Unit,
) {
    val todoItemContextMenuState = state.todoItemContextMenuState
    if (todoItemContextMenuState?.visible == true) {
        Popup(
            offset = remember(todoItemContextMenuState.item) { todoItemContextMenuState.position + IntOffset(50, 50) },
            onDismissRequest = onHide
        ) {
            Column(
                modifier = Modifier
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(14.dp))
                    .width(100.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    onClick = { onTodoAction(todoItemContextMenuState.item, TodoItemAction.Archive) }
                ) {
                    if (todoItemContextMenuState.item.done) {
                        Text("Undone")
                    } else {
                        Text("Done")
                    }
                }

                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    onClick = { onTodoAction(todoItemContextMenuState.item, TodoItemAction.Edit) }
                ) {
                    Text("Edit")
                }

                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    onClick = { onTodoAction(todoItemContextMenuState.item, TodoItemAction.Delete) }
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupsSpace(
    modifier: Modifier = Modifier,
    state: TodoListState,
    onGroupClick: (GroupId) -> Unit,
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
                    onClick = { onGroupClick(group.id) },
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
    onContextClick: (TodoItem, LayoutCoordinates) -> Unit,
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
                onContextClick = onContextClick,
            )
        }
    }
}

@Composable
private fun TodosList(
    modifier: Modifier = Modifier,
    state: TodoListState,
    onContextClick: (TodoItem, LayoutCoordinates) -> Unit,
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
                        onContextClick = onContextClick
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
                        onContextClick = onContextClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            )
        }
    )
}

private val itemShape: Shape = RoundedCornerShape(12.dp)

@Composable
private fun DismissableTodoCard(
    modifier: Modifier = Modifier,
    onContextClick: (TodoItem, LayoutCoordinates) -> Unit,
    item: TodoItem,
) {
    var position by remember { mutableStateOf<LayoutCoordinates?>(null) }
    TodoCard(
        item = item,
        onContextClick = { position?.let { onContextClick(item, it) } },
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { position = it }
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
private fun TodoCard(
    item: TodoItem,
    onContextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(itemShape)
            .contextClickable(onClick = onContextClick)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        Text(item.text)
        Spacer(Modifier.size(3.dp))
        Text(
            text = formatDate(item),
            fontSize = 10.sp
        )
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

@Composable
private fun GroupMenu(
    canMoveLeft: Boolean,
    canMoveRight: Boolean,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMoveLeftClick: () -> Unit,
    onMoveRightClick: () -> Unit,
) {
    var popupVisible by remember { mutableStateOf(false) }
    IconButton({ popupVisible = true }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = null,
        )
    }
    if (popupVisible) {
        Popup(
            alignment = Alignment.TopEnd,
            offset = IntOffset(x = 0, y = 60.dp.roundToPx()),
            onDismissRequest = { popupVisible = false }
        ) {
            var columnWidth by remember { mutableIntStateOf(0) }
            Column(
                modifier = Modifier
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(14.dp))
                    .onSizeChanged { columnWidth = it.width }
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            ) {
                TextButton(
                    modifier = Modifier.widthIn(min = columnWidth.toDp()),
                    shape = RectangleShape,
                    onClick = {
                        onRenameClick()
                        popupVisible = false
                    }
                ) {
                    Text("Rename folder")
                }

                TextButton(
                    modifier = Modifier.widthIn(min = columnWidth.toDp()),
                    shape = RectangleShape,
                    onClick = {
                        onDeleteClick()
                        popupVisible = false
                    }
                ) {
                    Text("Delete folder")
                }
                if (canMoveLeft) {
                    TextButton(
                        modifier = Modifier.widthIn(min = columnWidth.toDp()),
                        shape = RectangleShape,
                        onClick = onMoveLeftClick
                    ) {
                        Text("Move left")
                    }
                }
                if (canMoveRight) {
                    TextButton(
                        modifier = Modifier.widthIn(min = columnWidth.toDp()),
                        shape = RectangleShape,
                        onClick = onMoveRightClick
                    ) {
                        Text("Move right")
                    }
                }
            }
        }
    }
}
