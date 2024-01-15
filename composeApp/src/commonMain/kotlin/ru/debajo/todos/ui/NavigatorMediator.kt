package ru.debajo.todos.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableSharedFlow
import ru.debajo.todos.app.AppScreen

@Stable
internal class NavigatorMediator {

    private val flow: MutableSharedFlow<Navigate> = MutableSharedFlow()

    suspend fun navigate(screen: AppScreen) {
        flow.emit(Navigate(screen, replaceAll = false))
    }

    suspend fun replaceAll(screen: AppScreen) {
        flow.emit(Navigate(screen, replaceAll = true))
    }

    suspend fun observeNavigate(callback: (Navigate) -> Unit) {
        flow.collect(callback)
    }

    class Navigate(val screen: AppScreen, val replaceAll: Boolean)
}

internal val LocalNavigatorMediator: ProvidableCompositionLocal<NavigatorMediator> = staticCompositionLocalOf { error("") }
