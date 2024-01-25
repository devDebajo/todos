package ru.debajo.todos.ui.todolist

import kotlinx.coroutines.flow.Flow

interface HotkeyListener {

    val hotkeys: Flow<Hotkey>

    enum class Hotkey {
        CmdS,
        CmdEnter,
        CmdW,
    }
}
