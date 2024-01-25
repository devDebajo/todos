package ru.debajo.todos.ui.todolist

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

internal object HotkeyListenerImpl : HotkeyListener {
    override val hotkeys: Flow<HotkeyListener.Hotkey> = emptyFlow()
}
