package ru.debajo.todos.common

import org.jetbrains.compose.components.resources.BuildConfig

actual val isDebug: Boolean
    get() = BuildConfig.DEBUG