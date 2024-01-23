package ru.debajo.todos.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import ru.debajo.todos.data.db.FileSession
import ru.debajo.todos.data.storage.DatabaseSnapshotSaver
import ru.debajo.todos.di.getFromDi
import ru.debajo.todos.ui.LocalNavigatorMediator
import ru.debajo.todos.ui.security.SecuredScreenManager
import ru.debajo.todos.ui.theme.AppTheme

// TODO https://github.com/jordond/materialkolor
internal object CommonApplication {

    fun onCreate() {
        val fileSession = getFromDi<FileSession>()
        val databaseSnapshotSaver = getFromDi<DatabaseSnapshotSaver>()
        fileSession.addOnUpdateListener { databaseSnapshotSaver.onUpdate() }
    }

    @Composable
    fun Content() {
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
        }
    }
}

