import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import ru.debajo.todos.app.OS
import ru.debajo.todos.app.currentOS
import ru.debajo.todos.ui.todolist.HotkeyListener

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

class HotkeyDetector(
    private val scope: CoroutineScope,
) : KeyEventHandler.Listener, HotkeyListener {

    private val mutableHotkeys: MutableSharedFlow<HotkeyListener.Hotkey> = MutableSharedFlow()
    override val hotkeys: Flow<HotkeyListener.Hotkey> = mutableHotkeys.asSharedFlow()

    private var cmdPressed: Boolean = false

    override fun handle(keyEvent: KeyEvent): Boolean {
        if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isCmd) {
            cmdPressed = true
        }

        if (keyEvent.type == KeyEventType.KeyUp && keyEvent.isCmd) {
            cmdPressed = false
        }

        if (cmdPressed && keyEvent.type == KeyEventType.KeyDown) {
            if (keyEvent.key.keyCode == Key.S.keyCode) {
                scope.launch { mutableHotkeys.emit(HotkeyListener.Hotkey.CmdS) }
                return true
            }

            if (keyEvent.key.keyCode == Key.Enter.keyCode) {
                scope.launch { mutableHotkeys.emit(HotkeyListener.Hotkey.CmdEnter) }
                return true
            }

            if (keyEvent.key.keyCode == Key.W.keyCode) {
                scope.launch { mutableHotkeys.emit(HotkeyListener.Hotkey.CmdW) }
                return true
            }
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
