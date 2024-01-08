package ru.debajo.todos.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset

@OptIn(ExperimentalFoundationApi::class)
actual fun Modifier.contextClickable(
    enabled: Boolean,
    onPrimaryClick: (Offset) -> Unit,
    onSecondaryClick: (Offset) -> Unit,
): Modifier {
    return composed {
        val interactionSource = remember { MutableInteractionSource() }
        var position: Offset? by remember { mutableStateOf(null) }
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect {
                when (it) {
                    is PressInteraction.Release -> position = it.press.pressPosition
                    is PressInteraction.Press -> position = it.pressPosition
                }
            }
        }
        combinedClickable(
            enabled = enabled,
            onLongClick = { onSecondaryClick(position ?: Offset.Zero) },
            onClick = { onPrimaryClick(position ?: Offset.Zero) },
            indication = LocalIndication.current,
            interactionSource = interactionSource
        )
    }
}
