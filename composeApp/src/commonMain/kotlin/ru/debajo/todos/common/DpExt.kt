package ru.debajo.todos.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
internal fun Dp.roundToPx(): Int {
    return with(LocalDensity.current) {
        roundToPx()
    }
}

@Composable
internal fun Int.toDp(): Dp {
    return with(LocalDensity.current) {
        toDp()
    }
}
