package ru.debajo.todos.ui.todolist

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import java.awt.Desktop
import java.net.URI
import ru.debajo.todos.common.toIntOffset

actual fun LayoutCoordinates.calculatePopupPosition(itemOffset: Offset): IntOffset {
    return positionInRoot().toIntOffset() + itemOffset.toIntOffset()
}

actual fun openUrl(url: String) {
    Desktop.getDesktop().browse(URI(url))
}
