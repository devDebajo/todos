package ru.debajo.todos.common

import com.russhwolf.settings.BuildConfig

actual val isDebug: Boolean
    get() = BuildConfig.DEBUG