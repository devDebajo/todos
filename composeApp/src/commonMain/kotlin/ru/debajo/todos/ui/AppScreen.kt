package ru.debajo.todos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import ru.debajo.todos.di.getFromDi
import ru.debajo.todos.ui.fileconfig.FileConfigScreen
import ru.debajo.todos.ui.fileconfig.FileConfigViewModel

sealed interface AppScreen : Screen {
    data object SelectFile : AppScreen {
        @Composable
        override fun Content() {
            val viewModel = rememberScreenModel { getFromDi<FileConfigViewModel>() }
            LaunchedEffect(viewModel) { viewModel.init() }
            FileConfigScreen(viewModel)
        }
    }
}
