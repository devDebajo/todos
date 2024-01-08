package ru.debajo.todos.ui.todolist

import java.awt.Desktop
import java.net.URI

actual fun openUrl(url: String) {
    Desktop.getDesktop().browse(URI(url))
}
