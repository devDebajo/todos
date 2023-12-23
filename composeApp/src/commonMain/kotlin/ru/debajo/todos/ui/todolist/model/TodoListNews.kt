package ru.debajo.todos.ui.todolist.model

sealed interface TodoListNews {
    data object ResetSwipeToDismiss : TodoListNews
}
