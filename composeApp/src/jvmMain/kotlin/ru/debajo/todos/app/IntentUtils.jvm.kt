package ru.debajo.todos.app

import java.awt.Desktop
import java.net.URI

internal actual fun openUrl(url: String) {
    Desktop.getDesktop().browse(URI(url))
}

internal actual fun sendEmail(email: String) {
    Desktop.getDesktop().mail(URI("mailto:$email"))
}
