package ru.debajo.todos.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import ru.debajo.todos.ui.theme.AppTheme

@Composable
internal fun App() {
    AppTheme(systemIsDark = true) {
        Navigator(AppScreen.SelectFile)
    }
}
