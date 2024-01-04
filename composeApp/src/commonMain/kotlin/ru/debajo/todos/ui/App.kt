package ru.debajo.todos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import ru.debajo.todos.ui.theme.AppTheme

@Composable
internal fun App() {
    AppTheme(systemIsDark = true) {
        val mediator = LocalNavigatorMediator.current
        Navigator(AppScreen.Splash) {
            val navigator = LocalNavigator.current
            LaunchedEffect(mediator, navigator) {
                mediator.observeNavigate { navigate ->
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
