package ru.debajo.todos.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
