package ru.debajo.todos.ui.todolist

import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import java.awt.Desktop
import java.net.URI
import ru.debajo.todos.common.toIntOffset

actual fun LayoutCoordinates.calculatePopupPosition(): IntOffset {
    return positionInRoot().toIntOffset() + IntOffset(50, 50)
//    val mousePosition = MouseInfo.getPointerInfo().location
//    return IntOffset(mousePosition.x, mousePosition.y)
}

actual fun openUrl(url: String) {
    Desktop.getDesktop().browse(URI(url))
}
