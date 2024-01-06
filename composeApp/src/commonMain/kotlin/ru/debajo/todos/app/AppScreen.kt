package ru.debajo.todos.app

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import ru.debajo.todos.common.viewModelFromDi
import ru.debajo.todos.ui.fileconfig.FileConfigScreen
import ru.debajo.todos.ui.newpin.NewPinScreen
import ru.debajo.todos.ui.onboarding.OnboardingScreen
import ru.debajo.todos.ui.pin.PinScreen
import ru.debajo.todos.ui.splash.SplashScreen
import ru.debajo.todos.ui.todolist.HorizontalTodoListScreen
import ru.debajo.todos.ui.todolist.TodoListScreen
import ru.debajo.todos.ui.todolist.TodoListViewModel

sealed interface AppScreen : Screen {

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
        @Composable
        override fun Content() {
            PinScreen(viewModelFromDi())
        }
    }

    data object NewPin : AppScreen {
        @Composable
        override fun Content() {
            NewPinScreen(viewModelFromDi())
        }
    }

    data object SelectFile : AppScreen {
        @Composable
        override fun Content() {
            FileConfigScreen(viewModelFromDi())
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
