package ru.debajo.todos.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

actual val currentOrientation: AppOrientation
    @Composable
    get() {
        return if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            AppOrientation.Horizontal
        } else {
            AppOrientation.Vertical
        }
    }
