package ru.debajo.todos.common

import androidx.compose.ui.text.input.TextFieldValue

fun TextFieldValue.limit(limit: Int): TextFieldValue = copy(text = text.take(limit))
