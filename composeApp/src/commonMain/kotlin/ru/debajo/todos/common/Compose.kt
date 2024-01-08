package ru.debajo.todos.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BlockingLoaderDialog(show: Boolean) {
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

fun LayoutCoordinates.calculatePopupPosition(itemOffset: Offset): IntOffset {
    return positionInRoot().toIntOffset() + itemOffset.toIntOffset()
}

@Composable
fun PopupDialog(
    visible: Boolean = false,
    position: IntOffset = IntOffset.Zero,
    onHide: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
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
                var minWidth by remember { mutableStateOf(0) }
                Column(
                    content = content,
                    modifier = Modifier
                        .offset { position }
                        .shadow(elevation = 10.dp, shape = RoundedCornerShape(14.dp))
                        .widthIn(min = minWidth.toDp())
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .onSizeChanged { size -> minWidth = size.width }
                )
            }
        }
    }
}

@Composable
fun PopupItem(
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
