package ru.debajo.todos.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

fun Offset.toIntOffset(): IntOffset {
    return IntOffset(x = x.roundToInt(), y = y.roundToInt())
}

@Composable
fun IntOffset.toDpOffset(): DpOffset {
    return with(LocalDensity.current) {
        DpOffset(x = x.toDp(), y = y.toDp())
    }
}
