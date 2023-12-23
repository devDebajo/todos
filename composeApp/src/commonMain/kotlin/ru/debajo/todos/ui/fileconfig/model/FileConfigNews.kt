package ru.debajo.todos.ui.fileconfig.model

sealed interface FileConfigNews {
    data object NavigateToList : FileConfigNews
}
