package ru.debajo.todos.app

import HotkeyDetector
import KeyEventHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.aakira.napier.Napier
import java.awt.Dimension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.context.startKoin
import org.koin.dsl.module
import ru.debajo.todos.common.isDebug
import ru.debajo.todos.di.CommonModule
import ru.debajo.todos.di.JvmModule
import ru.debajo.todos.di.inject
import ru.debajo.todos.strings.R
import ru.debajo.todos.ui.theme.AppTheme

internal class JvmApplication : CoroutineScope by CoroutineScope(SupervisorJob()) {

    private val commonApplication: CommonApplication by inject()
    private val keyEventHandler: KeyEventHandler by inject()
    private val quitHelper: QuitHelper by inject()
    private val logger: ComposeAntilog by lazy { ComposeAntilog() }
    private val hotkeyDetector: HotkeyDetector by inject()

    fun run() {
        onCreate()
        application {
            if (isDebug) {
                DebugLogsWindow()
            }

            Window(
                title = if (isDebug) "${R.strings.appName} (debug)" else R.strings.appName,
                state = rememberWindowState(width = 800.dp, height = 600.dp),
                onCloseRequest = { quitHelper.onCloseRequest { exitApplication() } },
                onKeyEvent = { event -> keyEventHandler.onKeyEvent(event) }
            ) {
                window.minimumSize = remember { Dimension(350, 600) }
                LifecycleListener()
                AppTheme {
                    commonApplication.Content()
                }
            }
        }
    }

    @Composable
    private fun DebugLogsWindow() {
        Window(
            title = "Logs",
            state = rememberWindowState(width = 400.dp, height = 600.dp),
            onCloseRequest = { },
        ) {
            window.minimumSize = remember { Dimension(350, 600) }
            LazyColumn(Modifier.fillMaxSize()) {
                val logs = logger.logs
                items(
                    count = logger.logs.size
                ) {
                    Text(logs[it])
                }
            }
        }
    }


    private fun onCreate() {
        initDi()
        initLog()
        commonApplication.onCreate()
        quitHelper.init()
        keyEventHandler.register(hotkeyDetector)
    }

    private fun initLog() {
        if (isDebug) {
            Napier.base(logger)
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
}

