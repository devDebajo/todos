package ru.debajo.todos.ui.pin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.debajo.todos.common.BlockingLoaderDialog

const val PinSize: Int = 4

@Composable
internal fun PinScreen(viewModel: PinViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            PinDots(
                count = PinSize,
                selectedCount = state.pin.length,
                isError = state.isError
            )
        }
        PinPad(
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

    BlockingLoaderDialog(state.showBlockingLoader)
}
