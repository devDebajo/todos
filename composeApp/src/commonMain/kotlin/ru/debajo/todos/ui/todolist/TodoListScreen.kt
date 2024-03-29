package ru.debajo.todos.ui.todolist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import ru.debajo.todos.app.isHorizontalOrientation
import ru.debajo.todos.app.openUrl
import ru.debajo.todos.common.BlockingLoaderDialog
import ru.debajo.todos.common.PopupDialog
import ru.debajo.todos.common.PopupItem
import ru.debajo.todos.common.ScreenToolbar
import ru.debajo.todos.common.calculatePopupPosition
import ru.debajo.todos.common.contextClickable
import ru.debajo.todos.common.formatDateTime
import ru.debajo.todos.common.formatKmp
import ru.debajo.todos.common.roundToPx
import ru.debajo.todos.common.toDp
import ru.debajo.todos.domain.GroupId
import ru.debajo.todos.domain.TodoItem
import ru.debajo.todos.strings.R
import ru.debajo.todos.ui.todolist.model.TodoItemAction
import ru.debajo.todos.ui.todolist.model.TodoListNews
import ru.debajo.todos.ui.todolist.model.TodoListState

@Composable
internal fun TodoListScreen(viewModel: TodoListViewModel) {
    val state by viewModel.state.collectAsState()
    val groupsLazyListState = rememberLazyListState()

    LaunchedEffect(groupsLazyListState, viewModel) {
        viewModel.news.collect { news ->
            when (news) {
                is TodoListNews.ScrollToGroup -> groupsLazyListState.animateScrollToItem(news.index)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TodoListScreenToolbar(viewModel)
        val haptic = LocalHapticFeedback.current
        GroupsSpace(
            state = state,
            lazyListState = groupsLazyListState,
            onGroupClick = { id ->
                viewModel.selectGroup(id)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
            onNewGroupClick = {
                viewModel.onNewGroupClick()
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            },
        )
        TodoListScreenListWithTypePanel(viewModel)
    }

    TodoListScreenDialogs(viewModel)
}

@Composable
internal fun ColumnScope.TodoListScreenListWithTypePanel(viewModel: TodoListViewModel) {
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current
    TodosListWithPlaceholder(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        state = state,
        onContextClick = { item, coordinates, itemOffset ->
            viewModel.onItemContextClick(item, coordinates.calculatePopupPosition(itemOffset))
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

@Composable
internal fun TodoListScreenToolbar(viewModel: TodoListViewModel) {
    val state by viewModel.state.collectAsState()
    ScreenToolbar(
        title = state.currentFileName,
        navigationButton = {
            IconButton({ viewModel.closeFile() }) {
                Icon(
                    contentDescription = null,
                    imageVector = Icons.Default.Close,
                )
            }
        },
        menuButton = {
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
    )
}

@Composable
internal fun TodoListScreenDialogs(viewModel: TodoListViewModel) {
    val state by viewModel.state.collectAsState()
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
    BlockingLoaderDialog(state.isBlockingLoading)
}

@Composable
private fun ContextItemPopup(
    state: TodoListState,
    onTodoAction: (TodoItem, TodoItemAction) -> Unit,
    onHide: () -> Unit,
) {
    val todoItemContextMenuState = state.todoItemContextMenuState
    PopupDialog(
        visible = todoItemContextMenuState?.visible == true,
        position = todoItemContextMenuState?.position ?: IntOffset.Zero,
        onHide = onHide,
    ) {
        if (todoItemContextMenuState != null) {
            PopupItem(
                modifier = Modifier.widthIn(min = 100.dp),
                text = if (todoItemContextMenuState.item.done) R.strings.undone else R.strings.done,
                onClick = { onTodoAction(todoItemContextMenuState.item, TodoItemAction.Archive) }
            )

            PopupItem(
                modifier = Modifier.widthIn(min = 100.dp),
                text = R.strings.edit,
                onClick = { onTodoAction(todoItemContextMenuState.item, TodoItemAction.Edit) }
            )

            val clipboardManager = LocalClipboardManager.current
            PopupItem(
                modifier = Modifier.widthIn(min = 100.dp),
                text = R.strings.copy,
                onClick = {
                    clipboardManager.setText(AnnotatedString(todoItemContextMenuState.item.text))
                    onTodoAction(todoItemContextMenuState.item, TodoItemAction.Copy)
                }
            )

            PopupItem(
                modifier = Modifier.widthIn(min = 100.dp),
                text = R.strings.delete,
                onClick = { onTodoAction(todoItemContextMenuState.item, TodoItemAction.Delete) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupsSpace(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    state: TodoListState,
    onGroupClick: (GroupId) -> Unit,
    onNewGroupClick: () -> Unit,
) {
    LazyRow(
        modifier = modifier,
        state = lazyListState,
        contentPadding = remember { PaddingValues(horizontal = 16.dp) },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(
            count = state.groups.size,
            key = { state.groups[it].id.id.toString() },
            itemContent = { index ->
                val group = state.groups[index]
                FilterChip(
                    selected = state.currentGroup.id == group.id,
                    shape = RoundedCornerShape(12.dp),
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
    onContextClick: (TodoItem, LayoutCoordinates, Offset) -> Unit,
) {
    Box(modifier = modifier) {
        if (state.isEmpty) {
            Text(
                text = R.strings.emptyTodoList,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TodosList(
    modifier: Modifier = Modifier,
    state: TodoListState,
    onContextClick: (TodoItem, LayoutCoordinates, Offset) -> Unit,
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
                key = { state.actual[it].id.id.toString() },
                contentType = { "todo" },
                itemContent = { index ->
                    DismissableTodoCard(
                        item = state.actual[index],
                        modifier = Modifier.fillMaxWidth().animateItemPlacement(),
                        onContextClick = onContextClick
                    )
                }
            )

            if (state.done.isNotEmpty()) {
                item {
                    Text(
                        modifier = Modifier.fillMaxWidth().animateItemPlacement(),
                        text = R.strings.doneDivider
                    )
                }
            }

            items(
                count = state.done.size,
                key = { state.done[it].id.id.toString() },
                contentType = { "todo" },
                itemContent = { index ->
                    DismissableTodoCard(
                        item = state.done[index],
                        onContextClick = onContextClick,
                        modifier = Modifier.fillMaxWidth().alpha(0.5f).animateItemPlacement(),
                    )
                }
            )
        }
    )
}

private val itemShape: Shape = RoundedCornerShape(14.dp)

@Composable
private fun DismissableTodoCard(
    modifier: Modifier = Modifier,
    onContextClick: (TodoItem, LayoutCoordinates, Offset) -> Unit,
    item: TodoItem,
) {
    var position by remember { mutableStateOf<LayoutCoordinates?>(null) }
    TodoCard(
        item = item,
        onContextClick = { itemOffset -> position?.let { onContextClick(item, it, itemOffset) } },
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
                        Text(R.strings.enterTodo)
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


@OptIn(ExperimentalTextApi::class)
@Composable
private fun TodoCard(
    item: TodoItem,
    onContextClick: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(itemShape)
            .contextClickable(onSecondaryClick = onContextClick)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 14.dp, horizontal = 12.dp)
    ) {
        val colors = MaterialTheme.colorScheme
        val text = rememberTextWithUrls(item.text, colors.tertiary)
        val style = remember {
            TextStyle(
                fontSize = 16.sp,
                color = colors.onSurfaceVariant
            )
        }
        if (text.hasUrls) {
            ClickableText(
                text = text.text,
                style = style,
                onClick = { index ->
                    text.text.getUrlAnnotations(index, index)
                        .firstOrNull()
                        ?.let { openUrl(it.item.url) }
                }
            )
        } else {
            Text(text = text.text, style = style)
        }
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
            R.strings.todoCreatedAt.formatKmp(item.createTimestamp.formatDateTime())
        } else {
            R.strings.editedAt.formatKmp(item.updateTimestamp.formatDateTime())
        }
    }
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
                PopupItem(
                    modifier = Modifier.widthIn(min = columnWidth.toDp()),
                    text = R.strings.renameFolder,
                    onClick = {
                        onRenameClick()
                        popupVisible = false
                    }
                )

                PopupItem(
                    modifier = Modifier.widthIn(min = columnWidth.toDp()),
                    text = R.strings.deleteFolder,
                    onClick = {
                        onDeleteClick()
                        popupVisible = false
                    }
                )
                if (canMoveLeft) {
                    PopupItem(
                        modifier = Modifier.widthIn(min = columnWidth.toDp()),
                        text = if (isHorizontalOrientation) R.strings.moveFolderUp else R.strings.moveFolderLeft,
                        onClick = onMoveLeftClick
                    )
                }
                if (canMoveRight) {
                    PopupItem(
                        modifier = Modifier.widthIn(min = columnWidth.toDp()),
                        text = if (isHorizontalOrientation) R.strings.moveFolderDown else R.strings.moveFolderRight,
                        onClick = onMoveRightClick
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberTextWithUrls(text: String, urlColor: Color): TextWithUrls {
    return remember(text, urlColor) {
        formatText(text, urlColor)
    }
}

private data class TextWithUrls(
    val text: AnnotatedString,
    val hasUrls: Boolean,
)

@OptIn(ExperimentalTextApi::class)
private fun formatText(text: String, urlColor: Color): TextWithUrls {
    if (text.isEmpty()) {
        return TextWithUrls(AnnotatedString(""), false)
    }

    val matches = URL_REGEX.findAll(text).toList()
    if (matches.isEmpty()) {
        return TextWithUrls(AnnotatedString(text), false)
    }
    val annotatedString = buildAnnotatedString {
        append(text)
        for (match in matches) {
            addStyle(SpanStyle(urlColor), match.range.first, match.range.last + 1)
            addUrlAnnotation(UrlAnnotation(match.value), match.range.first, match.range.last + 1)
        }
    }
    return TextWithUrls(annotatedString, true)
}

private val URL_REGEX: Regex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)".toRegex(RegexOption.IGNORE_CASE)
