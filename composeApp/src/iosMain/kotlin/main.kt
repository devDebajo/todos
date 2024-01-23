import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import ru.debajo.todos.app.CommonApplication

fun MainViewController(): UIViewController = ComposeUIViewController { CommonApplication.Content() }
