package ru.debajo.todos.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalWindowInfo
import ru.debajo.todos.di.getFromDi

@Composable
internal fun LifecycleListener() {
    val commonApplication = remember { getFromDi<CommonApplication>() }
    val windowInfo = LocalWindowInfo.current

    LaunchedEffect(commonApplication, windowInfo) {
        snapshotFlow { windowInfo.isWindowFocused }.collect { isWindowFocused ->
            if (isWindowFocused) {
                commonApplication.onResume()
            } else {
                commonApplication.onPause()
            }
        }
    }
}
