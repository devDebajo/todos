package ru.debajo.todos.ui.pin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun EnterPinDialog(
    pin1: TextFieldValue,
    pin2: TextFieldValue,
    isError: Boolean = false,
    visible: Boolean = false,
    onPin1Changed: (TextFieldValue) -> Unit,
    onPin2Changed: (TextFieldValue) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (visible) {
        val savePinButtonEnabled = pin1.text.isNotEmpty() && pin2.text.isNotEmpty()
        val second = remember { FocusRequester() }
        AlertDialog(
            title = { Text("New PIN-code") },
            text = {
                Column {
                    TextField(
                        placeholder = { Text("PIN") },
                        value = pin1,
                        onValueChange = onPin1Changed,
                        isError = isError,
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
                        value = pin2,
                        onValueChange = onPin2Changed,
                        isError = isError,
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
                                    if (savePinButtonEnabled) {
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
                    enabled = savePinButtonEnabled,
                ) {
                    Text("Save")
                }
            },
            onDismissRequest = onCancel,
        )
    }
}
