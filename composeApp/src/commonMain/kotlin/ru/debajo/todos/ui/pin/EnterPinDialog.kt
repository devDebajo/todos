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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ru.debajo.todos.strings.R

@Composable
fun EnterPin3Dialog(
    pin1: TextFieldValue,
    pin2: TextFieldValue,
    pin3: TextFieldValue,
    pin1Placeholder: String = R.strings.oldPin,
    pin2Placeholder: String = R.strings.newPin,
    pin3Placeholder: String = R.strings.confirmNewPin,
    text: String = R.strings.newPinCode,
    isError: Boolean = false,
    visible: Boolean = true,
    onPin1Changed: (TextFieldValue) -> Unit,
    onPin2Changed: (TextFieldValue) -> Unit,
    onPin3Changed: (TextFieldValue) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (visible) {
        val savePinButtonEnabled = pin1.text.isNotEmpty() && pin2.text.isNotEmpty() && pin3.text.isNotEmpty()
        val second = remember { FocusRequester() }
        val third = remember { FocusRequester() }
        AlertDialog(
            title = { Text(text) },
            text = {
                Column {
                    TextField(
                        placeholder = { Text(pin1Placeholder) },
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
                        keyboardActions = remember(second) {
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
                        placeholder = { Text(pin2Placeholder) },
                        value = pin2,
                        onValueChange = onPin2Changed,
                        isError = isError,
                        visualTransformation = remember { PasswordVisualTransformation() },
                        keyboardOptions = remember {
                            KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                autoCorrect = false,
                                imeAction = ImeAction.Next,
                            )
                        },
                        keyboardActions = remember(third) {
                            KeyboardActions(
                                onNext = {
                                    third.requestFocus()
                                }
                            )
                        }
                    )

                    Spacer(Modifier.size(8.dp))

                    TextField(
                        modifier = Modifier.focusRequester(third),
                        placeholder = { Text(pin3Placeholder) },
                        value = pin3,
                        onValueChange = onPin3Changed,
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
                    Text(R.strings.cancel)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    enabled = savePinButtonEnabled,
                ) {
                    Text(R.strings.save)
                }
            },
            onDismissRequest = onCancel,
        )
    }
}

@Composable
fun EnterPin2Dialog(
    pin1: TextFieldValue,
    pin2: TextFieldValue,
    pin1Placeholder: String = R.strings.pin,
    pin2Placeholder: String = R.strings.confirmPin,
    text: String = R.strings.newPinCode,
    isError: Boolean = false,
    visible: Boolean = true,
    onPin1Changed: (TextFieldValue) -> Unit,
    onPin2Changed: (TextFieldValue) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (visible) {
        val savePinButtonEnabled = pin1.text.isNotEmpty() && pin2.text.isNotEmpty()
        val second = remember { FocusRequester() }
        AlertDialog(
            title = { Text(text) },
            text = {
                Column {
                    TextField(
                        placeholder = { Text(pin1Placeholder) },
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
                        keyboardActions = remember(second) {
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
                        placeholder = { Text(pin2Placeholder) },
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
                    Text(R.strings.cancel)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    enabled = savePinButtonEnabled,
                ) {
                    Text(R.strings.save)
                }
            },
            onDismissRequest = onCancel,
        )
    }
}

@Composable
fun EnterPin1Dialog(
    pin: TextFieldValue,
    text: String = R.strings.pinCode,
    placeholder: String = R.strings.pin,
    isError: Boolean = false,
    visible: Boolean = true,
    onPinChanged: (TextFieldValue) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (visible) {
        val savePinButtonEnabled = pin.text.isNotEmpty()
        val requester = remember { FocusRequester() }
        LaunchedEffect(requester) { requester.requestFocus() }
        AlertDialog(
            title = { Text(text) },
            text = {
                TextField(
                    modifier = Modifier.focusRequester(requester),
                    placeholder = { Text(placeholder) },
                    value = pin,
                    onValueChange = onPinChanged,
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
            },
            dismissButton = {
                TextButton(onClick = onCancel) {
                    Text(R.strings.cancel)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    enabled = savePinButtonEnabled,
                ) {
                    Text(R.strings.save)
                }
            },
            onDismissRequest = onCancel,
        )
    }
}
