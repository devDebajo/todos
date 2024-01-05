package ru.debajo.todos.app

import androidx.compose.runtime.Composable

enum class AppOrientation { Vertical, Horizontal }

@get:Composable
expect val currentOrientation: AppOrientation

val isHorizontalOrientation: Boolean
    @Composable
    get() = currentOrientation == AppOrientation.Horizontal
