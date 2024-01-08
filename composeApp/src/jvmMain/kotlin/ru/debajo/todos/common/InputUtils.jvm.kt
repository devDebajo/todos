package ru.debajo.todos.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.onClick
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEvent

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

        val matcher = remember { MyMatcher { position = it } }
        onClick(
            enabled = enabled,
            matcher = matcher,
            onClick = { onSecondaryClick(position ?: Offset.Zero) },
        ).clickable(
            enabled = enabled,
            onClick = { onPrimaryClick(position ?: Offset.Zero) },
            indication = LocalIndication.current,
            interactionSource = interactionSource,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private class MyMatcher(private val offsetListener: (Offset?) -> Unit) : PointerMatcher {

    private val secondaryMatcher: PointerMatcher = PointerMatcher.mouse(PointerButton.Secondary)

    @ExperimentalFoundationApi
    override fun matches(event: PointerEvent): Boolean {
        val matches = secondaryMatcher.matches(event)
        if (matches) {
            offsetListener(event.changes.lastOrNull()?.position)
        }
        return matches
    }
}
