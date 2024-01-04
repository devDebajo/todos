package ru.debajo.todos.ui.onboarding

import androidx.compose.runtime.Immutable

@Immutable
data class OnboardingState(
    val weakAuthTypeWarningDialogVisible: Boolean = false,
    val biometricDialogVisible: Boolean = false,
)
