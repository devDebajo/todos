package ru.debajo.todos.ui.pin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

const val PinSize: Int = 4

@Composable
fun PinScreen(viewModel: PinViewModel) {
    val state by viewModel.state.collectAsState()

    Box(Modifier.fillMaxSize().padding(bottom = 50.dp)) {
        PinDots(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 180.dp),
            count = PinSize,
            selectedCount = state.pin.length,
            isError = state.isError
        )

        PinPad(
            modifier = Modifier.align(Alignment.BottomCenter),
            onActionClick = { action ->
                when (action) {
                    ActionType.Biometric -> viewModel.showBiometric()
                    ActionType.Backspace -> viewModel.backspace()
                    ActionType.None -> Unit
                }
            },
            onNumberClick = { viewModel.onButtonClick(it) },
            actionType = state.actionType,
        )
    }
}