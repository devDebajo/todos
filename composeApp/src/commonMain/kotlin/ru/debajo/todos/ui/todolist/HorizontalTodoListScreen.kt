package ru.debajo.todos.ui.todolist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.debajo.todos.domain.GroupId
import ru.debajo.todos.domain.TodoGroup
import ru.debajo.todos.ui.todolist.model.TodoListNews
import ru.debajo.todos.ui.todolist.model.TodoListState

private const val LeftPaneWeight: Float = 0.25f

@Composable
internal fun HorizontalTodoListScreen(viewModel: TodoListViewModel) {
    val state by viewModel.state.collectAsState()
    val groupsLazyListState = rememberLazyListState()

    LaunchedEffect(groupsLazyListState, viewModel) {
        viewModel.news.collect { news ->
            when (news) {
                is TodoListNews.ScrollToGroup -> groupsLazyListState.animateScrollToItem(news.index)
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxHeight().weight(LeftPaneWeight)) {
            val haptic = LocalHapticFeedback.current
            GroupsSpace(
                modifier = Modifier.matchParentSize(),
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
        }
        Box(modifier = Modifier.fillMaxHeight().weight(1f - LeftPaneWeight)) {
            Column(modifier = Modifier.matchParentSize()) {
                TodoListScreenToolbar(viewModel)
                TodoListScreenListWithTypePanel(viewModel)
            }
        }
    }

    TodoListScreenDialogs(viewModel)
}

@Composable
private fun GroupsSpace(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    state: TodoListState,
    onGroupClick: (GroupId) -> Unit,
    onNewGroupClick: () -> Unit,
) {
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        LazyColumn(
            state = lazyListState,
            contentPadding = remember { PaddingValues(top = 16.dp, bottom = 100.dp) },
        ) {
            items(
                count = state.groups.size,
                key = { state.groups[it].id.id.toString() },
                itemContent = { index ->
                    val group = state.groups[index]
                    HorizontalGroup(
                        group = group,
                        selected = state.currentGroup.id == group.id,
                        onClick = { onGroupClick(it.id) },
                    )
                }
            )
        }

        FloatingActionButton(
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            onClick = onNewGroupClick,
            content = {
                Icon(
                    contentDescription = null,
                    imageVector = Icons.Default.Add,
                )
            }
        )
    }
}

private val HorizontalGroupColor: Color = Color.White.copy(alpha = 0.1f)

@Composable
private fun HorizontalGroup(
    group: TodoGroup,
    selected: Boolean,
    onClick: (TodoGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable { onClick(group) }
            .background(if (selected) HorizontalGroupColor else Color.Transparent)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = group.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
