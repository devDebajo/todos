package ru.debajo.todos.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.flow.MutableSharedFlow
import ru.debajo.todos.app.AppScreen
import ru.debajo.todos.ui.security.SecuredScreenManager

@Stable
internal class NavigatorMediator(
    private val securedScreenManager: SecuredScreenManager,
) {

    private val flow: MutableSharedFlow<Command> = MutableSharedFlow()

    suspend fun navigate(screen: AppScreen) {
        flow.emit(Command.Navigate(screen, replaceAll = false))
    }

    suspend fun replaceAll(screen: AppScreen) {
        flow.emit(Command.Navigate(screen, replaceAll = true))
    }

    suspend fun connect(navigatorGetter: () -> Navigator?) {
        flow.collect { command ->
            when (command) {
                is Command.Navigate -> {
                    securedScreenManager.setScreenSecured(command.screen.securedByDefault)
                    if (command.replaceAll) {
                        navigatorGetter()?.replaceAll(command.screen)
                    } else {
                        navigatorGetter()?.push(command.screen)
                    }
                }

                is Command.Back -> {
                    navigatorGetter()?.pop()
                    (navigatorGetter()?.lastItemOrNull as? AppScreen)?.let {
                        securedScreenManager.setScreenSecured(it.securedByDefault)
                    }
                }
            }
        }
    }

    suspend fun back() {
        flow.emit(Command.Back)
    }

    sealed interface Command {
        class Navigate(val screen: AppScreen, val replaceAll: Boolean) : Command
        data object Back : Command
    }
}

internal val LocalNavigatorMediator: ProvidableCompositionLocal<NavigatorMediator> = staticCompositionLocalOf { error("") }
