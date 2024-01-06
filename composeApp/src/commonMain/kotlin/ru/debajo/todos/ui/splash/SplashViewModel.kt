package ru.debajo.todos.ui.splash

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import ru.debajo.todos.app.AppScreen
import ru.debajo.todos.auth.AppSecurityManager
import ru.debajo.todos.auth.AuthType
import ru.debajo.todos.common.BaseNewsLessViewModel
import ru.debajo.todos.ui.NavigatorMediator

@Stable
class SplashViewModel(
    private val navigatorMediator: NavigatorMediator,
    private val securityManager: AppSecurityManager,
) : BaseNewsLessViewModel<Unit>(Unit) {

    override fun onLaunch() {
        screenModelScope.launch {
            when (securityManager.getAuthType()) {
                AuthType.NotConfigured -> navigatorMediator.replaceAll(AppScreen.Onboarding)
                AuthType.Weak -> navigatorMediator.replaceAll(AppScreen.SelectFile)
                AuthType.Pin, AuthType.Biometric -> {
                    if (securityManager.isAuthorized()) {
                        navigatorMediator.replaceAll(AppScreen.SelectFile)
                    } else {
                        navigatorMediator.replaceAll(AppScreen.Pin)
                    }
                }
            }
        }
    }
}
