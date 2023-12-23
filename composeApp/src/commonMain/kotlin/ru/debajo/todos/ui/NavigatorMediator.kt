package ru.debajo.todos.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableSharedFlow

@Stable
class NavigatorMediator {

    private val flow: MutableSharedFlow<AppScreen> = MutableSharedFlow<AppScreen>()

    suspend fun navigate(screen: AppScreen) {
        flow.emit(screen)
    }

    suspend fun observeNavigate(callback: (AppScreen) -> Unit) {
        flow.collect(callback)
    }
}

val LocalNavigatorMediator: ProvidableCompositionLocal<NavigatorMediator> = staticCompositionLocalOf { error("") }
