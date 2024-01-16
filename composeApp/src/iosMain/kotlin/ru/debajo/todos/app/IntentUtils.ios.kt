package ru.debajo.todos.app

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

internal actual fun openUrl(url: String) {
    val nsUrl = NSURL.URLWithString(url) ?: return
    UIApplication.sharedApplication.openURL(nsUrl)
}

internal actual fun sendEmail(email: String) {
}