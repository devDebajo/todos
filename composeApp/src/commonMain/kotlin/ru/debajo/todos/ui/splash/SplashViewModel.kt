package ru.debajo.todos.ui.splash

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import ru.debajo.todos.app.AppScreen
import ru.debajo.todos.auth.AppSecurityManager
import ru.debajo.todos.auth.AuthType
import ru.debajo.todos.common.BaseNewsLessViewModel
import ru.debajo.todos.data.db.FileSession
import ru.debajo.todos.ui.NavigatorMediator

@Stable
internal class SplashViewModel(
    private val navigatorMediator: NavigatorMediator,
    private val securityManager: AppSecurityManager,
    private val fileSession: FileSession,
) : BaseNewsLessViewModel<Unit>(Unit) {

    override fun onLaunch() {
        screenModelScope.launch {
            when (securityManager.getAuthType()) {
                AuthType.NotConfigured -> navigatorMediator.replaceAll(AppScreen.Onboarding)
                AuthType.Weak -> navigatorMediator.replaceAll(AppScreen.SelectFile(true))
                AuthType.Pin, AuthType.Biometric -> {
                    if (securityManager.isAuthorized()) {
                        if (fileSession.isOpened) {
                            navigatorMediator.replaceAll(AppScreen.List)
                        } else {
                            navigatorMediator.replaceAll(AppScreen.SelectFile(true))
                        }
                    } else {
                        navigatorMediator.replaceAll(AppScreen.Pin)
                    }
                }
            }
        }
    }
}
