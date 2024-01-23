import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import ru.debajo.todos.app.OS
import ru.debajo.todos.app.currentOS

class KeyEventHandler {
    private val listeners: MutableSet<Listener> = mutableSetOf()

    fun onKeyEvent(keyEvent: KeyEvent): Boolean {
        return listeners.any { it.handle(keyEvent) }
    }

    fun register(listener: Listener) {
        listeners.add(listener)
    }

    fun unregister(listener: Listener) {
        listeners.remove(listener)
    }

    fun interface Listener {
        fun handle(keyEvent: KeyEvent): Boolean
    }
}

class SaveDetector(private val callback: () -> Unit) : KeyEventHandler.Listener {

    private var cmdPressed: Boolean = false

    override fun handle(keyEvent: KeyEvent): Boolean {
        if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isCmd) {
            cmdPressed = true
        }

        if (keyEvent.type == KeyEventType.KeyUp && keyEvent.isCmd) {
            cmdPressed = false
        }

        if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key.keyCode == Key.S.keyCode && cmdPressed) {
            callback()
            return true
        }

        return false
    }

    private val KeyEvent.isCmd: Boolean
        get() {
            return when (currentOS) {
                OS.MAC -> key.keyCode == Key.MetaLeft.keyCode || key.keyCode == Key.MetaRight.keyCode
                else -> key.keyCode == Key.CtrlLeft.keyCode || key.keyCode == Key.CtrlRight.keyCode
            }
        }
}