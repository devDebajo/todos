package ru.debajo.todos.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

fun Offset.toIntOffset(): IntOffset {
    return IntOffset(x = x.roundToInt(), y = y.roundToInt())
}

