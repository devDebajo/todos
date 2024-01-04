package ru.debajo.todos.ui.pin

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PinDots(
    count: Int,
    selectedCount: Int,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        for (index in 0 until count) {
            PinDot(selected = index + 1 <= selectedCount, isError = isError)
        }
    }
}

private val pinDotSize: Dp = 14.dp
private val pinDotColor: Color = Color.White.copy(alpha = 0.7f)
private val pinErrorColor: Color = Color.Red.copy(alpha = 0.7f)

@Composable
private fun PinDot(
    selected: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(if (selected) 1f else 0.5f)
    val color by animateColorAsState(if (isError) pinErrorColor else pinDotColor)
    Box(
        modifier = modifier
            .scale(scale)
            .size(pinDotSize)
            .clip(CircleShape)
            .background(color)
    )
}
