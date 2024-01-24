package ru.debajo.todos.app

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class AppUiLifecycle {

    private val activityExist: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val hasUi: StateFlow<Boolean> = activityExist.asStateFlow()

    fun onCreate() {
        activityExist.value = true
    }

    fun onDestroy() {
        activityExist.value = false
    }
}
