package ru.debajo.todos.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import ru.debajo.todos.common.viewModelFromDi
import ru.debajo.todos.di.getFromDi
import ru.debajo.todos.ui.fileconfig.FileConfigScreen2
import ru.debajo.todos.ui.fileconfig.FileConfigViewModel2
import ru.debajo.todos.ui.newpin.NewPinScreen
import ru.debajo.todos.ui.newpin.NewPinViewModel
import ru.debajo.todos.ui.onboarding.OnboardingScreen
import ru.debajo.todos.ui.onboarding.OnboardingViewModel
import ru.debajo.todos.ui.pin.PinScreen
import ru.debajo.todos.ui.pin.PinViewModel
import ru.debajo.todos.ui.splash.SplashScreen
import ru.debajo.todos.ui.splash.SplashViewModel
import ru.debajo.todos.ui.todolist.HorizontalTodoListScreen
import ru.debajo.todos.ui.todolist.TodoListScreen
import ru.debajo.todos.ui.todolist.TodoListViewModel

sealed interface AppScreen : Screen {

    data object Splash : AppScreen {
        @Composable
        override fun Content() {
            val viewModel = rememberScreenModel { getFromDi<SplashViewModel>() }
            LaunchedEffect(viewModel) { viewModel.init() }
            SplashScreen(viewModel)
        }
    }

    data object Onboarding : AppScreen {
        @Composable
        override fun Content() {
            val viewModel = rememberScreenModel { getFromDi<OnboardingViewModel>() }
            OnboardingScreen(viewModel)
        }
    }

    data object Pin : AppScreen {
        @Composable
        override fun Content() {
            val viewModel = rememberScreenModel { getFromDi<PinViewModel>() }
            LaunchedEffect(viewModel) { viewModel.init() }
            PinScreen(viewModel)
        }
    }

    data object NewPin : AppScreen {
        @Composable
        override fun Content() {
            val viewModel = rememberScreenModel { getFromDi<NewPinViewModel>() }
            NewPinScreen(viewModel)
        }
    }

    data object SelectFile : AppScreen {
        @Composable
        override fun Content() {
//            val viewModel = rememberScreenModel { getFromDi<FileConfigViewModel>() }
//            LaunchedEffect(viewModel) { viewModel.init() }
//            FileConfigScreen(viewModel)

            FileConfigScreen2(viewModelFromDi<FileConfigViewModel2>())
        }
    }

    data object List : AppScreen {
        @Composable
        override fun Content() {
            val viewModel = rememberScreenModel { getFromDi<TodoListViewModel>() }
            LaunchedEffect(viewModel) { viewModel.init() }
            if (isHorizontalOrientation) {
                HorizontalTodoListScreen(viewModel)
            } else {
                TodoListScreen(viewModel)
            }
        }
    }
}