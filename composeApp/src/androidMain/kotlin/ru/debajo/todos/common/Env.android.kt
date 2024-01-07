package ru.debajo.todos.common

import ru.debajo.todos.BuildConfig

actual val isDebug: Boolean
    get() = BuildConfig.DEBUG