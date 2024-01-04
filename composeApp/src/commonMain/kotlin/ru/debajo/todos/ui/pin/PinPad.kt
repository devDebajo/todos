package ru.debajo.todos.ui.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ActionType { Biometric, Backspace, None }

@Composable
fun PinPad(
    onNumberClick: (Int) -> Unit,
    onActionClick: (ActionType) -> Unit = {},
    modifier: Modifier = Modifier,
    actionType: ActionType = ActionType.None,
) {
    Column(modifier = modifier) {
        Row {
            PinButton(1, onClick = onNumberClick)
            Spacer(Modifier.size(buttonOffset))
            PinButton(2, onClick = onNumberClick)
            Spacer(Modifier.size(buttonOffset))
            PinButton(3, onClick = onNumberClick)
        }
        Spacer(Modifier.size(buttonOffset))
        Row {
            PinButton(4, onClick = onNumberClick)
            Spacer(Modifier.size(buttonOffset))
            PinButton(5, onClick = onNumberClick)
            Spacer(Modifier.size(buttonOffset))
            PinButton(6, onClick = onNumberClick)
        }
        Spacer(Modifier.size(buttonOffset))
        Row {
            PinButton(7, onClick = onNumberClick)
            Spacer(Modifier.size(buttonOffset))
            PinButton(8, onClick = onNumberClick)
            Spacer(Modifier.size(buttonOffset))
            PinButton(9, onClick = onNumberClick)
        }
        Spacer(Modifier.size(buttonOffset))
        Row {
            Box(Modifier.size(buttonSize))
            Spacer(Modifier.size(buttonOffset))
            PinButton(0, onClick = onNumberClick)
            Spacer(Modifier.size(buttonOffset))
            when (actionType) {
                ActionType.Biometric -> IconButton(
                    onClick = { onActionClick(ActionType.Biometric) },
                    imageVector = Icons.Default.Fingerprint
                )

                ActionType.Backspace -> IconButton(
                    onClick = { onActionClick(ActionType.Backspace) },
                    imageVector = Icons.Default.Backspace
                )

                ActionType.None -> Box(Modifier.size(buttonSize))
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
private fun IconButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(buttonSize)
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null
        )
    }
}
