package ru.debajo.todos.app

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Stable
internal interface AppLifecycle {
    val state: StateFlow<State>

    val isPaused: Boolean
        get() = state.value == State.Paused

    val isResumed: Boolean
        get() = state.value == State.Resumed

    enum class State { Paused, Resumed }
}

@Stable
internal class AppLifecycleMutable : AppLifecycle {
    private val _state: MutableStateFlow<AppLifecycle.State> = MutableStateFlow(AppLifecycle.State.Paused)
    override val state: StateFlow<AppLifecycle.State> = _state.asStateFlow()

    fun updateState(state: AppLifecycle.State) {
        _state.value = state
    }
}
