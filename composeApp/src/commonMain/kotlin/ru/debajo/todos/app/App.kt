package ru.debajo.todos.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import ru.debajo.todos.di.getFromDi
import ru.debajo.todos.ui.LocalNavigatorMediator
import ru.debajo.todos.ui.security.SecuredScreenManager
import ru.debajo.todos.ui.theme.AppTheme

@Composable
internal fun App() {
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

// TODO Чтение файла при первом запуске работает плохо, зависает, или вылетает
// TODO При первом старте фалг открытия последнего файла, как будто true, хотя в UI false
// баг с тем, что в первый запуск список пустой, хотя файл не пустой
// настройки
// TODO https://github.com/jordond/materialkolor
