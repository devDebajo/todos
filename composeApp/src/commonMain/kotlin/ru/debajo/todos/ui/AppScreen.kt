package ru.debajo.todos.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import ru.debajo.todos.ui.fileconfig.FileConfigScreen
import ru.debajo.todos.ui.fileconfig.FileConfigViewModel

sealed interface AppScreen : Screen {
    data object SelectFile : AppScreen {
        @Composable
        override fun Content() {
            val viewModel = rememberScreenModel { FileConfigViewModel() }
            FileConfigScreen(viewModel)
        }
    }
}
