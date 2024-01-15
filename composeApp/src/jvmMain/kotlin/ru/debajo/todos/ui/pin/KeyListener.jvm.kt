package ru.debajo.todos.ui.pin

import KeyEventHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.input.key.KeyEvent
import ru.debajo.todos.di.getFromDi

@Composable
internal actual fun KeyListener(listener: (KeyEvent) -> Boolean) {
    val keyEventHandler = remember { getFromDi<KeyEventHandler>() }
    val listenerLatest = rememberUpdatedState(listener)

    DisposableEffect(keyEventHandler) {
        val keyListener = KeyEventHandler.Listener { listenerLatest.value.invoke(it) }
        keyEventHandler.register(keyListener)
        onDispose {
            keyEventHandler.unregister(keyListener)
        }
    }
}
