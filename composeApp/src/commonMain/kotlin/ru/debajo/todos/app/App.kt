package ru.debajo.todos.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ru.debajo.todos.common.BlockingLoaderDialog
import ru.debajo.todos.common.isDebug
import ru.debajo.todos.data.db.FileSession
import ru.debajo.todos.data.storage.DatabaseSnapshotSaver
import ru.debajo.todos.data.storage.DatabaseSnapshotWorker
import ru.debajo.todos.di.getFromDi
import ru.debajo.todos.di.inject
import ru.debajo.todos.ui.LocalNavigatorMediator
import ru.debajo.todos.ui.NavigatorMediator
import ru.debajo.todos.ui.security.SecuredScreenManager
import ru.debajo.todos.ui.theme.AppTheme

// TODO https://github.com/jordond/materialkolor
internal class CommonApplication {

    private val navigatorMediator: NavigatorMediator by inject()
    private val databaseSnapshotSaver: DatabaseSnapshotSaver by inject()
    private var saveWorkerJob: Job? = null
    private val terminating: MutableState<Boolean> = mutableStateOf(false)

    fun onCreate() {
        initLog()
        listenDb()
        saveWorkerJob = startSaveWorker()
    }

    suspend fun onTerminate() {
        terminating.value = true
        saveWorkerJob?.cancel()
        databaseSnapshotSaver.save()
    }

    fun onPause() = Unit

    fun onResume() = Unit

    @Composable
    fun Content() {
        CompositionLocalProvider(
            LocalNavigatorMediator provides remember { navigatorMediator }
        ) {
            AppTheme {
                val securedScreenManager = remember { getFromDi<SecuredScreenManager>() }
                val mediator = LocalNavigatorMediator.current
                Navigator(AppScreen.Splash) {
                    val navigator = LocalNavigator.current
                    LaunchedEffect(mediator, navigator) {
                        mediator.observeNavigate { navigate ->
                            securedScreenManager.setScreenSecured(navigate.screen.securedByDefault)
                            if (navigate.replaceAll) {
                                navigator?.replaceAll(navigate.screen)
                            } else {
                                navigator?.push(navigate.screen)
                            }
                        }
                    }

                    CurrentScreen()
                }

                BlockingLoaderDialog(terminating.value)
            }
        }
    }

    private fun listenDb() {
        val fileSession = getFromDi<FileSession>()
        val databaseSnapshotSaver = getFromDi<DatabaseSnapshotSaver>()
        fileSession.addOnUpdateListener { databaseSnapshotSaver.onUpdate() }
    }

    private fun startSaveWorker(): Job {
        val scope = getFromDi<CoroutineScope>()
        val databaseSnapshotWorker = getFromDi<DatabaseSnapshotWorker>()
        return scope.launch { databaseSnapshotWorker.doWork() }
    }

    private fun initLog() {
        if (isDebug) {
            Napier.base(DebugAntilog())
        }
    }
}
