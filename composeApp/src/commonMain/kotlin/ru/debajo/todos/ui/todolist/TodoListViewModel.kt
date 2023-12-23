package ru.debajo.todos.ui.todolist

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.TextFieldValue
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.debajo.todos.data.storage.DatabaseSnapshotSaver
import ru.debajo.todos.domain.TodoGroup
import ru.debajo.todos.domain.TodoItem
import ru.debajo.todos.domain.TodoItemUseCase
import ru.debajo.todos.ui.todolist.model.TodoItemAction
import ru.debajo.todos.ui.todolist.model.TodoListNews
import ru.debajo.todos.ui.todolist.model.TodoListState

@Stable
class TodoListViewModel(
    private val todoItemUseCase: TodoItemUseCase,
    private val databaseSnapshotSaver: DatabaseSnapshotSaver,
) : StateScreenModel<TodoListState>(TodoListState()) {

    private val _news: MutableSharedFlow<TodoListNews> = MutableSharedFlow()
    val news: Flow<TodoListNews> = _news.asSharedFlow()

    fun init() {
        screenModelScope.launch {
            todoItemUseCase.observeGroups().collect { groups ->
                updateState {
                    copy(groups = groups)
                }
            }
        }

        screenModelScope.launch {
            databaseSnapshotSaver.saving.collect { savingToFile ->
                updateState {
                    copy(savingToFile = savingToFile)
                }
            }
        }
    }

    fun saveCurrentTodo() {
        val state = state.value
        val text = state.textFieldState.text.trim()
        if (text.isEmpty()) {
            return
        }

        val currentEditingItem = state.currentEditingItem
        updateState {
            copy(
                textFieldState = TextFieldValue(""),
                currentEditingItem = null
            )
        }

        screenModelScope.launch {
            if (currentEditingItem != null) {
                todoItemUseCase.updateTodo(currentEditingItem.id, text)
            } else {
                todoItemUseCase.createTodo(text, state.currentGroup.id)
            }
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
                todoItemUseCase.createGroup(name)
                hideNewGroupDialog()
            }
        }
    }

    fun selectGroup(index: Int) {
        updateState {
            copy(selectedGroup = index)
        }
    }

    fun onDeleteGroup(group: TodoGroup) {
        updateState {
            copy(currentDeletingGroup = group)
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

    fun hideTodoItemDialog() {
        screenModelScope.launch {
            updateState {
                copy(currentDeletingItem = null)
            }
            _news.emit(TodoListNews.ResetSwipeToDismiss)
        }
    }

    fun deleteItem() {
        val item = state.value.currentDeletingItem ?: return
        screenModelScope.launch {
            todoItemUseCase.delete(item.id)
            hideTodoItemDialog()
        }
    }

    fun onTodoAction(item: TodoItem, action: TodoItemAction) {
        screenModelScope.launch {
            when (action) {
                TodoItemAction.Delete -> updateState {
                    copy(currentDeletingItem = item)
                }

                TodoItemAction.Archive -> todoItemUseCase.updateDone(item.id, !item.done)

                TodoItemAction.Edit -> updateState {
                    if (item.id == this.currentEditingItem?.id) {
                        copy(
                            currentEditingItem = null,
                            textFieldState = TextFieldValue(""),
                        )
                    } else {
                        copy(
                            currentEditingItem = item,
                            textFieldState = TextFieldValue(item.text),
                        )
                    }
                }

            }
        }
    }

    private inline fun updateState(block: TodoListState.() -> TodoListState) {
        mutableState.value = mutableState.value.block()
    }
}
