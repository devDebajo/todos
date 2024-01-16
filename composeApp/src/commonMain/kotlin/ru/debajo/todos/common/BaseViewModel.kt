package ru.debajo.todos.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ru.debajo.todos.di.getFromDi

typealias BaseNewsLessViewModel<S> = BaseViewModel<S, Unit>

abstract class BaseViewModel<S, N>(initialState: S) : StateScreenModel<S>(initialState) {

    private val mutableNews: MutableSharedFlow<N> = MutableSharedFlow()
    val news: Flow<N> = mutableNews.asSharedFlow()

    open fun onLaunch() = Unit

    protected suspend fun sendNews(news: N) {
        mutableNews.emit(news)
    }

    protected fun updateState(block: S.() -> S) {
        var success: Boolean
        do {
            val currentState = mutableState.value
            val newState = currentState.block()
            success = mutableState.compareAndSet(currentState, newState)
        } while (!success)
    }
}

@Composable
internal inline fun <reified T : BaseViewModel<*, *>> Screen.viewModelFromDi(tag: String? = null): T {
    val viewModel = rememberScreenModel(tag) { getFromDi<T>() }
    LaunchedEffect(viewModel) { viewModel.onLaunch() }
    return viewModel
}
