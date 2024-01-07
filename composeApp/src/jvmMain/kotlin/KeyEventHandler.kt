import androidx.compose.ui.input.key.KeyEvent

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
