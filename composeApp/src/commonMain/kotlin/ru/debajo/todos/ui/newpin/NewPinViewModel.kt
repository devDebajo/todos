package ru.debajo.todos.ui.newpin

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.StateScreenModel
import ru.debajo.todos.auth.AppSecurityManager
import ru.debajo.todos.ui.NavigatorMediator

@Stable
class NewPinViewModel(
    private val securityManager: AppSecurityManager,
    private val navigatorMediator: NavigatorMediator,
) : StateScreenModel<NewPinState>(NewPinState()) {

    fun onButtonClick(symbol: Int) {

    }

    fun backspace() {

    }
}
