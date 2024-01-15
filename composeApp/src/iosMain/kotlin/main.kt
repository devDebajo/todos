import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import ru.debajo.todos.app.App

fun MainViewController(): UIViewController = ComposeUIViewController { App() }
