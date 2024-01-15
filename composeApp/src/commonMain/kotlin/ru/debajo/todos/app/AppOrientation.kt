package ru.debajo.todos.app

import androidx.compose.runtime.Composable

enum class AppOrientation { Vertical, Horizontal }

@get:Composable
internal expect val currentOrientation: AppOrientation

internal val isHorizontalOrientation: Boolean
    @Composable
    get() = currentOrientation == AppOrientation.Horizontal
