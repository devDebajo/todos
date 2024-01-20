package ru.debajo.todos.ui.todolist

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import ru.debajo.todos.app.AppScreen
import ru.debajo.todos.common.BaseViewModel
import ru.debajo.todos.data.db.FileSessionManager
import ru.debajo.todos.data.preferences.Preferences
import ru.debajo.todos.data.storage.DatabaseSnapshotSaver
import ru.debajo.todos.data.storage.StorageFileManager
import ru.debajo.todos.data.storage.codec.FileCodecHelper
import ru.debajo.todos.data.storage.model.StorageFile
import ru.debajo.todos.domain.GroupId
import ru.debajo.todos.domain.TodoGroup
import ru.debajo.todos.domain.TodoItem
import ru.debajo.todos.domain.TodoItemUseCase
import ru.debajo.todos.ui.NavigatorMediator
import ru.debajo.todos.ui.security.SecuredScreenManager
import ru.debajo.todos.ui.todolist.model.TodoItemAction
import ru.debajo.todos.ui.todolist.model.TodoItemContextMenuState
import ru.debajo.todos.ui.todolist.model.TodoListNews
import ru.debajo.todos.ui.todolist.model.TodoListState

@Stable
internal class TodoListViewModel(
    private val fileSessionManager: FileSessionManager,
    private val databaseSnapshotSaver: DatabaseSnapshotSaver,
    private val storageFileManager: StorageFileManager,
    private val fileCodecHelper: FileCodecHelper,
    private val todoItemUseCase: TodoItemUseCase,
    private val preferences: Preferences,
    private val navigatorMediator: NavigatorMediator,
    private val securedScreenManager: SecuredScreenManager,
) : BaseViewModel<TodoListState, TodoListNews>(TodoListState()) {

    private val onCloseListener: (StorageFile) -> Unit = {
        screenModelScope.launch {
            navigatorMediator.replaceAll(AppScreen.SelectFile())
        }
    }

    override fun onLaunch() {
        fileSessionManager.fileSession.addOnCloseListener(onCloseListener)
        screenModelScope.launch {
            var first = true
            todoItemUseCase.observeGroups().collect { groups ->
                if (first) {
                    first = false
                    val (groupId, groupIndex) = findInitialGroupId(groups)
                    sendNews(TodoListNews.ScrollToGroup(groupIndex))
                    updateState {
                        copy(
                            groups = groups,
                            selectedGroupId = groupId,
                        )
                    }
                } else {
                    updateState { copy(groups = groups) }
                }
            }
        }
        screenModelScope.launch {
            val currentFile = storageFileManager.awaitCurrentFile()
            val encrypted = fileCodecHelper.isEncrypted(currentFile)
            securedScreenManager.setScreenSecured(encrypted)
            updateState {
                copy(currentFileName = currentFile.nameWithExtension)
            }
        }
    }

    override fun onDispose() {
        super.onDispose()
        fileSessionManager.fileSession.removeOnCloseListener(onCloseListener)
    }

    fun saveCurrentTodo() {
        val state = state.value
        val text = state.textFieldState.text.trim()
        if (text.isEmpty()) {
            return
        }

        updateState {
            copy(textFieldState = TextFieldValue(""))
        }

        screenModelScope.launch {
            todoItemUseCase.createTodo(text, state.currentGroup.id)
        }
    }

    fun updateCurrentTodo(value: TextFieldValue) {
        updateState {
            copy(textFieldState = value)
        }
    }

    fun updateCurrentGroup(value: TextFieldValue) {
        updateState {
            copy(newGroupName = value)
        }
    }

    fun hideNewGroupDialog() {
        updateState {
            copy(
                newGroupName = TextFieldValue(""),
                newGroupDialogVisible = false,
            )
        }
    }

    fun onNewGroupClick() {
        updateState {
            copy(newGroupDialogVisible = true)
        }
    }

    fun saveNewGroup() {
        val name = state.value.newGroupName.text
        if (name.isNotEmpty()) {
            screenModelScope.launch {
                val newGroup = todoItemUseCase.createGroup(name)
                hideNewGroupDialog()
                selectGroup(newGroup.id)
            }
        }
    }

    fun selectGroup(id: GroupId) {
        updateState {
            copy(selectedGroupId = id)
        }
        screenModelScope.launch {
            preferences.putString(LastGroupIdKey, id.id.toString())
        }
    }

    fun onDeleteCurrentGroupClick() {
        updateState {
            copy(currentDeletingGroup = currentGroup)
        }
    }

    fun onRenameCurrentGroupClick() {
        updateState {
            copy(
                currentRenamingGroup = currentGroup,
                currentRenamingGroupName = TextFieldValue(currentGroup.name)
            )
        }
    }

    fun hideRenameGroupDialog() {
        updateState {
            copy(
                currentRenamingGroup = null,
                currentRenamingGroupName = TextFieldValue(""),
            )
        }
    }

    fun updateCurrentGroupName(name: TextFieldValue) {
        updateState {
            copy(currentRenamingGroupName = name)
        }
    }

    fun renameCurrentGroup() {
        val currentRenamingGroup = state.value.currentRenamingGroup ?: return
        val newName = state.value.currentRenamingGroupName.text.trim()
        if (newName.isEmpty()) {
            hideRenameGroupDialog()
            return
        }

        screenModelScope.launch {
            todoItemUseCase.renameGroup(currentRenamingGroup.id, newName)
            hideRenameGroupDialog()
        }
    }

    fun hideDeleteGroupDialog() {
        updateState {
            copy(currentDeletingGroup = null)
        }
    }

    fun deleteGroup(withTodos: Boolean) {
        val currentDeletingGroup = state.value.currentDeletingGroup ?: return
        screenModelScope.launch {
            todoItemUseCase.deleteGroup(currentDeletingGroup.id, withTodos)
            hideDeleteGroupDialog()
        }
    }

    fun hideDeleteTodoItemDialog() {
        hideContextPopup()
    }

    fun deleteItem() {
        val item = state.value.todoItemContextMenuState?.item ?: return
        screenModelScope.launch {
            todoItemUseCase.delete(item.id)
            hideContextPopup()
        }
    }

    fun onTodoAction(item: TodoItem, action: TodoItemAction) {
        screenModelScope.launch {
            when (action) {
                TodoItemAction.Delete -> updateState {
                    copy(
                        todoItemContextMenuState = todoItemContextMenuState?.copy(
                            showDeleteDialog = true,
                            visible = false,
                        )
                    )
                }

                TodoItemAction.Archive -> {
                    todoItemUseCase.updateDone(item.id, !item.done)
                    hideContextPopup()
                }

                TodoItemAction.Edit -> updateState {
                    copy(
                        todoItemContextMenuState = todoItemContextMenuState?.copy(
                            changeTextDialogVisible = true,
                            showDeleteDialog = false,
                            visible = false,
                        )
                    )
                }

                TodoItemAction.Copy -> hideContextPopup()
            }
        }
    }

    fun onItemContextClick(item: TodoItem, coordinates: IntOffset) {
        updateState {
            copy(
                todoItemContextMenuState = TodoItemContextMenuState(
                    item = item,
                    position = coordinates,
                )
            )
        }
    }

    fun hideContextPopup() {
        updateState {
            copy(
                todoItemContextMenuState = todoItemContextMenuState?.copy(
                    visible = false,
                    showDeleteDialog = false,
                    changeTextDialogVisible = false,
                )
            )
        }
    }

    fun onUpdateItemTextChanged(text: TextFieldValue) {
        updateState {
            copy(
                todoItemContextMenuState = todoItemContextMenuState?.copy(
                    changeTextDialogValue = text
                )
            )
        }
    }

    fun hideUpdateItemTextDialog() {
        hideContextPopup()
    }

    fun updateItemText() {
        val todoItemContextMenuState = state.value.todoItemContextMenuState
        if (todoItemContextMenuState == null) {
            hideContextPopup()
            return
        }
        val text = todoItemContextMenuState.changeTextDialogValue.text.trim()
        if (text.isEmpty()) {
            hideContextPopup()
            return
        }

        screenModelScope.launch {
            todoItemUseCase.updateTodo(todoItemContextMenuState.item.id, text)
            hideContextPopup()
        }
    }

    fun moveCurrentGroupLeft() {
        screenModelScope.launch {
            todoItemUseCase.moveLeft(state.value.currentGroup.id)
        }
    }

    fun moveCurrentGroupRight() {
        screenModelScope.launch {
            todoItemUseCase.moveRight(state.value.currentGroup.id)
        }
    }

    fun closeFile() {
        updateState { copy(isBlockingLoading = true) }
        screenModelScope.launch {
            databaseSnapshotSaver.save()
            fileSessionManager.close()
        }
    }

    private suspend fun findInitialGroupId(groups: List<TodoGroup>): Pair<GroupId, Int> {
        val lastId = preferences.getString(LastGroupIdKey)
        for (index in groups.indices) {
            val group = groups[index]
            if (group.id.id.toString() == lastId) {
                return group.id to index
            }
        }
        return groups.first().id to 0
    }

    private companion object {
        const val LastGroupIdKey: String = "LastGroupId"
    }
}
