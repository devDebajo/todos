package ru.debajo.todos.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Spacer(Modifier.size(20.dp))
        Text(
            text = "Welcome",
            fontSize = 36.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.size(30.dp))
        Text(
            text = "Before you begin, you need to configure data protection in the application",
            fontSize = 20.sp,
        )
        Spacer(Modifier.weight(1f))

        Text(
            text = "Select your preferred security type",
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.size(10.dp))

        Row(Modifier.align(Alignment.CenterHorizontally)) {
            Button({ viewModel.onPinClick() }) {
                Text("PIN-code")
            }
            Spacer(Modifier.size(10.dp))
            OutlinedButton({ viewModel.onWeakClick(force = false) }) {
                Text("Do not secure data")
            }
        }
        Spacer(Modifier.size(30.dp))
    }

    WeakAuthTypeWarningDialog(
        state = state,
        onUsePin = { viewModel.onPinClick() },
        onDisable = { viewModel.onWeakClick(force = true) }
    )

    EnterPinDialog(
        state = state,
        onPinChanged = { viewModel.onPinChanged(it) },
        onPinConfirmationChanged = { viewModel.onPinConfirmationChanged(it) },
        onCancel = { viewModel.onCancelPin() },
        onConfirm = { viewModel.onConfirmPin() },
    )

    UseBiometricDialog(
        state = state,
        onConfirm = { viewModel.onConfirmBiometric() },
        onCancel = { viewModel.onCancelBiometric() },
    )
}

@Composable
private fun EnterPinDialog(
    state: OnboardingState,
    onPinChanged: (TextFieldValue) -> Unit,
    onPinConfirmationChanged: (TextFieldValue) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (state.enterPinDialogVisible) {
        val second = remember { FocusRequester() }
        AlertDialog(
            title = { Text("New PIN-code") },
            text = {
                Column {
                    TextField(
                        placeholder = { Text("PIN") },
                        value = state.pin,
                        onValueChange = onPinChanged,
                        isError = state.enterPinDialogError,
                        visualTransformation = remember { PasswordVisualTransformation() },
                        keyboardOptions = remember {
                            KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                autoCorrect = false,
                                imeAction = ImeAction.Next,
                            )
                        },
                        keyboardActions = remember(onConfirm) {
                            KeyboardActions(
                                onNext = {
                                    second.requestFocus()
                                }
                            )
                        }
                    )

                    Spacer(Modifier.size(8.dp))

                    TextField(
                        modifier = Modifier.focusRequester(second),
                        placeholder = { Text("Confirm PIN") },
                        value = state.pinConfirmation,
                        onValueChange = onPinConfirmationChanged,
                        isError = state.enterPinDialogError,
                        visualTransformation = remember { PasswordVisualTransformation() },
                        keyboardOptions = remember {
                            KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                autoCorrect = false,
                                imeAction = ImeAction.Done,
                            )
                        },
                        keyboardActions = remember(onConfirm) {
                            KeyboardActions(
                                onDone = {
                                    if (state.savePinButtonEnabled) {
                                        onConfirm()
                                    }
                                }
                            )
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    enabled = state.savePinButtonEnabled,
                ) {
                    Text("Save")
                }
            },
            onDismissRequest = onCancel,
        )
    }
}

@Composable
private fun WeakAuthTypeWarningDialog(
    state: OnboardingState,
    onDisable: () -> Unit,
    onUsePin: () -> Unit,
) {
    if (state.weakAuthTypeWarningDialogVisible) {
        AlertDialog(
            title = { Text("Do not use encryption?") },
            text = { Text("Are you sure to disable encryption?") },
            dismissButton = {
                TextButton(onClick = onDisable) {
                    Text("Disable")
                }
            },
            confirmButton = {
                TextButton(onClick = onUsePin) {
                    Text("Use pin")
                }
            },
            onDismissRequest = {}
        )
    }
}

@Composable
private fun UseBiometricDialog(
    state: OnboardingState,
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
