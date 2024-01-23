package ru.debajo.todos.app

import java.awt.Desktop
import java.awt.desktop.QuitEvent
import java.awt.desktop.QuitHandler
import java.awt.desktop.QuitResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class QuitHelper(
    private val scope: CoroutineScope,
    private val commonApplication: CommonApplication,
) : QuitHandler {

    fun init() {
        Desktop.getDesktop().setQuitHandler(this)
    }

    override fun handleQuitRequestWith(quitEvent: QuitEvent, quitResponse: QuitResponse) {
        onCloseRequest { quitResponse.performQuit() }
    }

    fun onCloseRequest(exitApplication: () -> Unit) {
        scope.launch {
            commonApplication.onTerminate()
            exitApplication()
        }
    }
}
