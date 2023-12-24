package ru.debajo.todos.ui.todolist.model

sealed interface TodoListNews {
    class ScrollToGroup(val index: Int) : TodoListNews
}
