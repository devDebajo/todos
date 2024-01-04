package ru.debajo.todos.ui.onboarding

import androidx.compose.runtime.Stable
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.launch
import ru.debajo.todos.auth.AppSecurityManager
import ru.debajo.todos.ui.AppScreen
import ru.debajo.todos.ui.NavigatorMediator

@Stable
class OnboardingViewModel(
    private val securityManager: AppSecurityManager,
    private val navigatorMediator: NavigatorMediator,
) : StateScreenModel<OnboardingState>(OnboardingState()) {

    fun onPinClick() {
        updateState { copy(weakAuthTypeWarningDialogVisible = false) }
        screenModelScope.launch {
            navigatorMediator.navigate(AppScreen.NewPin)
        }
    }

    fun onWeakClick(force: Boolean) {
        if (force) {
            updateState {
                copy(weakAuthTypeWarningDialogVisible = false)
            }
            screenModelScope.launch {
                securityManager.configureWeakAuthType()
                navigatorMediator.replaceAll(AppScreen.SelectFile)
            }
        } else {
            updateState {
                copy(weakAuthTypeWarningDialogVisible = true)
            }
        }
    }

    private inline fun updateState(block: OnboardingState.() -> OnboardingState) {
        mutableState.value = mutableState.value.block()
    }
}
