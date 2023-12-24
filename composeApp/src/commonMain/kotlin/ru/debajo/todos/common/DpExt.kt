package ru.debajo.todos.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun Dp.toPx(): Float {
    return with(LocalDensity.current) {
        toPx()
    }
}

@Composable
fun Dp.roundToPx(): Int {
    return with(LocalDensity.current) {
        roundToPx()
    }
}

@Composable
fun Int.toDp(): Dp {
    return with(LocalDensity.current) {
        toDp()
    }
}