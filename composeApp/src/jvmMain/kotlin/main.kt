import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.get
import ru.debajo.todos.data.storage.DatabaseSnapshotWorker
import ru.debajo.todos.di.CommonModule
import ru.debajo.todos.di.JvmModule
import ru.debajo.todos.ui.App

fun main() {
    initDi()
    startProcess()

    application {
        Window(
            title = "TODOs",
            state = rememberWindowState(width = 800.dp, height = 600.dp),
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(350, 600)
            App()
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

private fun startProcess() {
    val scope = get<CoroutineScope>(CoroutineScope::class.java)
    val databaseSnapshotWorker = get<DatabaseSnapshotWorker>(DatabaseSnapshotWorker::class.java)
    scope.launch { databaseSnapshotWorker.doWork() }
}