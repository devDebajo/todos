package ru.debajo.todos.app

import KeyEventHandler
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.debajo.todos.di.CommonModule
import ru.debajo.todos.di.JvmModule
import ru.debajo.todos.di.inject
import ru.debajo.todos.strings.R

internal class JvmApplication : CoroutineScope by CoroutineScope(SupervisorJob()) {

    private val commonApplication: CommonApplication by inject()
    private val keyEventHandler: KeyEventHandler by inject()
    private val quitHelper: QuitHelper by inject()

    fun run() {
        onCreate()
        application {
            Window(
                title = R.strings.appName,
                state = rememberWindowState(width = 800.dp, height = 600.dp),
                onCloseRequest = { quitHelper.onCloseRequest { exitApplication() } },
                onKeyEvent = { event -> keyEventHandler.onKeyEvent(event) }
            ) {
                window.minimumSize = remember { Dimension(350, 600) }
                LifecycleListener()
                commonApplication.Content()
            }
        }
    }

    private fun onCreate() {
        initDi()
        commonApplication.onCreate()
        quitHelper.init()
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
}
