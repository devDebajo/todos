package ru.debajo.todos.ui.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinScreen(viewModel: PinViewModel) {
    val state by viewModel.state.collectAsState()

    Box(Modifier.fillMaxSize().padding(bottom = 50.dp)) {
        Column(Modifier.align(Alignment.BottomCenter)) {
            Row {
                PinButton(1) { viewModel.onButtonClick(it) }
                Spacer(Modifier.size(buttonOffset))
                PinButton(2) { viewModel.onButtonClick(it) }
                Spacer(Modifier.size(buttonOffset))
                PinButton(3) { viewModel.onButtonClick(it) }
            }
            Spacer(Modifier.size(buttonOffset))
            Row {
                PinButton(4) { viewModel.onButtonClick(it) }
                Spacer(Modifier.size(buttonOffset))
                PinButton(5) { viewModel.onButtonClick(it) }
                Spacer(Modifier.size(buttonOffset))
                PinButton(6) { viewModel.onButtonClick(it) }
            }
            Spacer(Modifier.size(buttonOffset))
            Row {
                PinButton(7) { viewModel.onButtonClick(it) }
                Spacer(Modifier.size(buttonOffset))
                PinButton(8) { viewModel.onButtonClick(it) }
                Spacer(Modifier.size(buttonOffset))
                PinButton(9) { viewModel.onButtonClick(it) }
            }
            Spacer(Modifier.size(buttonOffset))
            Row {
                Box(Modifier.size(buttonSize))
                Spacer(Modifier.size(buttonOffset))
                PinButton(0) { viewModel.onButtonClick(it) }
                Spacer(Modifier.size(buttonOffset))
                if (state.showBiometricButton) {
                    BiometricButton { viewModel.showBiometric() }
                } else {
                    Box(Modifier.size(buttonSize))
                }
            }
        }
    }

}

private val buttonOffset: Dp = 12.dp
private val buttonSize: Dp = 80.dp
private val buttonColor: Color = Color.White.copy(alpha = 0.1f)

@Composable
private fun PinButton(
    number: Int,
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(buttonSize)
            .clip(CircleShape)
            .clickable { onClick(number) }
            .background(buttonColor)
    ) {
        Text(
            text = number.toString(),
            fontSize = 32.sp,
        )
    }
}

@Composable
private fun BiometricButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(buttonSize)
            .clip(CircleShape)
            .clickable { onClick() }
            .background(buttonColor)
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = null
        )
    }
}
