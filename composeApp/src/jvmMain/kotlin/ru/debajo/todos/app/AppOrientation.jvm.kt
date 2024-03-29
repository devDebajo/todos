package ru.debajo.todos.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo

@OptIn(ExperimentalComposeUiApi::class)
internal actual val currentOrientation: AppOrientation
    @Composable
    get() {
        val containerSize = LocalWindowInfo.current.containerSize
        return if (containerSize.width > containerSize.height) {
            AppOrientation.Horizontal
        } else {
            AppOrientation.Vertical
        }
    }
