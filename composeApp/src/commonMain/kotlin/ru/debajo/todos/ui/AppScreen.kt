package ru.debajo.todos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import ru.debajo.todos.di.getFromDi
import ru.debajo.todos.ui.fileconfig.FileConfigScreen
import ru.debajo.todos.ui.fileconfig.FileConfigViewModel
import ru.debajo.todos.ui.fileconfig.model.FileConfigNews
import ru.debajo.todos.ui.todolist.TodoListScreen
import ru.debajo.todos.ui.todolist.TodoListViewModel

sealed interface AppScreen : Screen {
    data object SelectFile : AppScreen {
        @Composable
        override fun Content() {
            val viewModel = rememberScreenModel { getFromDi<FileConfigViewModel>() }
            LaunchedEffect(viewModel) { viewModel.init() }

            val navigator = LocalNavigator.current
            LaunchedEffect(viewModel, navigator) {
                viewModel.news.collect { news ->
                    when (news) {
                        is FileConfigNews.NavigateToList -> navigator?.push(List)
                    }
                }
            }

            FileConfigScreen(viewModel)
        }
    }

    data object List : AppScreen {
        @Composable
        override fun Content() {
            val viewModel = rememberScreenModel { getFromDi<TodoListViewModel>() }
            LaunchedEffect(viewModel) { viewModel.init() }
            TodoListScreen(viewModel)
        }
    }
}
