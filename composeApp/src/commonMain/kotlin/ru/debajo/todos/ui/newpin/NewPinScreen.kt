package ru.debajo.todos.ui.newpin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.debajo.todos.strings.R
import ru.debajo.todos.ui.pin.ActionType
import ru.debajo.todos.ui.pin.PinDots
import ru.debajo.todos.ui.pin.PinPad
import ru.debajo.todos.ui.pin.PinSize

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun NewPinScreen(viewModel: NewPinViewModel) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState { 2 }
    val usePin1AsState = rememberUpdatedState(state.usePin1)
    LaunchedEffect(pagerState) {
        snapshotFlow { usePin1AsState.value }.collect { usePin1 ->
            if (usePin1) {
                pagerState.animateScrollToPage(0)
            } else {
                pagerState.animateScrollToPage(1)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
            ) { index ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (index == 0) {
                        Text(R.strings.inputPin)
                    } else {
                        Text(R.strings.confirmPin)
                    }
                    Spacer(Modifier.size(14.dp))
                    PinDots(
                        count = PinSize,
                        selectedCount = if (index == 0) state.pin1.length else state.pin2.length,
                        isError = state.isError,
                    )
                }
            }
        }

        PinPad(
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
            title = { Text(R.strings.useBiometricDialogTitle) },
            text = { Text(R.strings.useBiometricDialogText) },
            dismissButton = {
                TextButton(onClick = onCancel) {
                    Text(R.strings.disable)
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(R.strings.enable)
                }
            },
            onDismissRequest = {}
        )
    }
}
