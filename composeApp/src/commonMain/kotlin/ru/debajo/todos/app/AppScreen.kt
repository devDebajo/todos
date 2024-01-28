package ru.debajo.todos.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.screen.Screen
import ru.debajo.todos.common.viewModelFromDi
import ru.debajo.todos.ui.fileconfig.FileConfigScreen
import ru.debajo.todos.ui.fileconfig.FileConfigViewModel
import ru.debajo.todos.ui.newpin.NewPinScreen
import ru.debajo.todos.ui.onboarding.OnboardingScreen
import ru.debajo.todos.ui.pin.PinScreen
import ru.debajo.todos.ui.settings.SettingsScreen
import ru.debajo.todos.ui.splash.SplashScreen
import ru.debajo.todos.ui.todolist.HorizontalTodoListScreen
import ru.debajo.todos.ui.todolist.TodoListScreen
import ru.debajo.todos.ui.todolist.TodoListViewModel

internal sealed interface AppScreen : Screen {

    val securedByDefault: Boolean
        get() = false

    data object Splash : AppScreen {
        @Composable
        override fun Content() {
            SplashScreen(viewModelFromDi())
        }
    }

    data object Onboarding : AppScreen {
        @Composable
        override fun Content() {
            OnboardingScreen(viewModelFromDi())
        }
    }

    data object Pin : AppScreen {
        override val securedByDefault: Boolean = true

        @Composable
        override fun Content() {
            PinScreen(viewModelFromDi())
        }
    }

    data object NewPin : AppScreen {
        override val securedByDefault: Boolean = true

        @Composable
        override fun Content() {
            NewPinScreen(viewModelFromDi())
        }
    }

    class SelectFile(private val autoOpen: Boolean = false) : AppScreen {
        @Composable
        override fun Content() {
            val viewModel = viewModelFromDi<FileConfigViewModel>()
            LaunchedEffect(viewModel) {
                if (autoOpen) {
                    viewModel.tryToAutoOpen()
                }
            }
            FileConfigScreen(viewModelFromDi())
        }
    }

    data object Settings : AppScreen {
        @Composable
        override fun Content() {
            SettingsScreen(viewModelFromDi())
        }
    }

    data object List : AppScreen {
        @Composable
        override fun Content() {
            val viewModel = viewModelFromDi<TodoListViewModel>()
            if (isHorizontalOrientation) {
                HorizontalTodoListScreen(viewModel)
            } else {
                TodoListScreen(viewModel)
            }
        }
    }
}
