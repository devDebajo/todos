package ru.debajo.todos.ui.fileconfig

sealed interface FileConfigNews {
    class Toast(val text: String) : FileConfigNews
}
