package ru.debajo.todos.ui.todolist

import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import ru.debajo.todos.common.toIntOffset

actual fun LayoutCoordinates.calculatePopupPosition(): IntOffset {
    return positionInRoot().toIntOffset() + IntOffset(50, 50)
}
