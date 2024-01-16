package ru.debajo.todos.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun BlockingLoaderDialog(show: Boolean) {
    if (show) {
        AlertDialog(onDismissRequest = { /** could not close dialog **/ }) {
            Box(Modifier.fillMaxSize()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .align(Alignment.Center)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

internal fun LayoutCoordinates.calculatePopupPosition(itemOffset: Offset): IntOffset {
    return positionInRoot().toIntOffset() + itemOffset.toIntOffset()
}

@Composable
internal fun PopupDialog(
    visible: Boolean = false,
    position: IntOffset = IntOffset.Zero,
    onHide: () -> Unit,
    content: @Composable () -> Unit,
) {
    var containerSize: IntSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        onClick = onHide,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            ) {
                var popupSize: IntSize by remember { mutableStateOf(IntSize.Zero) }
                SameSizeColumn(
                    content = content,
                    modifier = Modifier
                        .offset { calculateOffset(position, popupSize, containerSize) }
                        .shadow(elevation = 10.dp, shape = RoundedCornerShape(14.dp))
                        .widthIn(min = popupSize.width.toDp())
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .onSizeChanged { popupSize = it }
                )
            }
        }
    }
}

@Composable
private fun SameSizeColumn(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    SubcomposeLayout(modifier = modifier) { constraints ->
        var width = 0
        var height = 0

        val measurables1StPass = subcompose(0, content)
        for (measurable in measurables1StPass) {
            val placeable = measurable.measure(constraints)
            width = max(width, placeable.width)
            height += placeable.height
        }

        val newConstraints = constraints.copy(minWidth = width, maxWidth = width)
        val measurables2StPass = subcompose(1, content)
        val placeables = measurables2StPass.map { it.measure(newConstraints) }

        layout(width, height) {
            var y = 0
            for (placeable in placeables) {
                placeable.placeRelative(x = 0, y = y)
                y += placeable.height
            }
        }
    }
}

private fun calculateOffset(position: IntOffset, popupSize: IntSize, containerSize: IntSize): IntOffset {
    var x = position.x
    var y = position.y

    if (x + popupSize.width > containerSize.width) {
        x = containerSize.width - popupSize.width
    }

    if (y + popupSize.height > containerSize.height) {
        y = containerSize.height - popupSize.height
    }

    return IntOffset(x, y)
}

@Composable
internal fun PopupItem(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .height(46.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
    ) {
        Text(text = text)
    }
}

@Composable
internal fun ScreenToolbar(
    title: String,
    navigationButton: @Composable (() -> Unit)? = null,
    menuButton: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.size(4.dp))
        if (navigationButton != null) {
            navigationButton()
            Spacer(Modifier.size(8.dp))
        }
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
        )
        Spacer(modifier = Modifier.weight(1f))
        if (menuButton != null) {
            menuButton()
        }
    }
}