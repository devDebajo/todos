package ru.debajo.todos.common

import ru.debajo.todos.BuildConfig

internal actual val isDebug: Boolean
    get() = BuildConfig.DEBUG