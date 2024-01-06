package ru.debajo.todos.ui.newpin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.debajo.todos.ui.pin.ActionType
import ru.debajo.todos.ui.pin.PinDots
import ru.debajo.todos.ui.pin.PinPad
import ru.debajo.todos.ui.pin.PinSize

@Composable
fun NewPinScreen(viewModel: NewPinViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (state.usePin1) {
                    Text("Input PIN")
                } else {
                    Text("Confirm PIN")
                }
                Spacer(Modifier.size(14.dp))
                PinDots(
                    count = PinSize,
                    selectedCount = state.currentPin.length,
                    isError = state.isError,
                )
            }

            PinPad(
                modifier = Modifier.align(Alignment.BottomCenter),
                onActionClick = { action ->
                    when (action) {
                        ActionType.Backspace -> viewModel.backspace()
                        ActionType.Biometric, ActionType.None -> Unit
                    }
                },
                onNumberClick = { viewModel.onButtonClick(it) },
                actionType = state.actionType,
            )
        }
    }

    UseBiometricDialog(
        state = state,
        onConfirm = { viewModel.onConfirmBiometric() },
        onCancel = { viewModel.onCancelBiometric() },
    )
}

@Composable
private fun UseBiometricDialog(
    state: NewPinState,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (state.biometricDialogVisible) {
        AlertDialog(
            title = { Text("Use biometric?") },
            text = { Text("Enable biometric to authentication the app?") },
            dismissButton = {
                TextButton(onClick = onCancel) {
                    Text("Disable")
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Enable")
                }
            },
            onDismissRequest = {}
        )
    }
}
