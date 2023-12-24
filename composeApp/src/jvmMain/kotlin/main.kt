import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import java.awt.Dimension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.debajo.todos.common.isDebug
import ru.debajo.todos.data.storage.DatabaseSnapshotWorker
import ru.debajo.todos.di.CommonModule
import ru.debajo.todos.di.JvmModule
import ru.debajo.todos.di.getFromDi
import ru.debajo.todos.ui.App
import ru.debajo.todos.ui.LocalNavigatorMediator
import ru.debajo.todos.ui.NavigatorMediator

fun main() {
    initDi()
    initLog()
    startProcess()

    val navigatorMediator = getFromDi<NavigatorMediator>()
    application {
        Window(
            title = "TODOs",
            state = rememberWindowState(width = 800.dp, height = 600.dp),
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(350, 600)
            CompositionLocalProvider(
                LocalNavigatorMediator provides remember { navigatorMediator }
            ) {
                App()
            }
        }
    }
}

private fun initDi() {
    startKoin {
        modules(
            module {
                single<CoroutineScope> { CoroutineScope(SupervisorJob()) }
            },
            JvmModule,
            CommonModule
        )
    }
}

private fun initLog() {
    if (isDebug) {
        Napier.base(DebugAntilog())
    }
}

private fun startProcess() {
    val scope = getFromDi<CoroutineScope>()
    val databaseSnapshotWorker = getFromDi<DatabaseSnapshotWorker>()
    scope.launch { databaseSnapshotWorker.doWork() }
}