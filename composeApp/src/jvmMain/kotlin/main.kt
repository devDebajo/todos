import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import java.awt.Dimension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.debajo.todos.app.App
import ru.debajo.todos.app.AppLifecycle
import ru.debajo.todos.app.AppLifecycleMutable
import ru.debajo.todos.common.isDebug
import ru.debajo.todos.data.storage.DatabaseSnapshotSaver
import ru.debajo.todos.data.storage.DatabaseSnapshotWorker
import ru.debajo.todos.di.CommonModule
import ru.debajo.todos.di.JvmModule
import ru.debajo.todos.di.getFromDi
import ru.debajo.todos.ui.LocalNavigatorMediator
import ru.debajo.todos.ui.NavigatorMediator

fun main() {
    initDi()
    initLog()
    val savingJob = startProcess()

    val navigatorMediator = getFromDi<NavigatorMediator>()
    val databaseSnapshotSaver = getFromDi<DatabaseSnapshotSaver>()
    val scope = getFromDi<CoroutineScope>()
    application {
        Window(
            title = "// TODO",
            state = rememberWindowState(width = 800.dp, height = 600.dp),
            onCloseRequest = {
                savingJob.cancel()
                scope.launch {
                    databaseSnapshotSaver.save(ignorePaused = true)
                    exitApplication()
                }
            }
        ) {
            window.minimumSize = Dimension(350, 600)
            CompositionLocalProvider(
                LocalNavigatorMediator provides remember { navigatorMediator }
            ) {
                LifecycleListener()
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

private fun startProcess(): Job {
    val scope = getFromDi<CoroutineScope>()
    val databaseSnapshotWorker = getFromDi<DatabaseSnapshotWorker>()
    return scope.launch { databaseSnapshotWorker.doWork() }
}

@Composable
private fun LifecycleListener() {
    val appLifecycleMutable = remember { getFromDi<AppLifecycleMutable>() }
    val windowInfo = LocalWindowInfo.current

    LaunchedEffect(appLifecycleMutable, windowInfo) {
        snapshotFlow { windowInfo.isWindowFocused }.collect { isWindowFocused ->
            if (isWindowFocused) {
                appLifecycleMutable.updateState(AppLifecycle.State.Resumed)
            } else {
                appLifecycleMutable.updateState(AppLifecycle.State.Paused)
            }
        }
    }
}
