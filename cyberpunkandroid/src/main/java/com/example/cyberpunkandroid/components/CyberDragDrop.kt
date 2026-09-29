package com.example.cyberpunkandroid.components

import androidx.compose.foundation.gestures.detectDragGestures
import com.example.cyberpunkandroid.effects.cyberSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Unadorned state holder for tracking tactical drag-and-drop interactions.
 */
class CyberDragDropState<T> {
    var isDragging by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var draggedData by mutableStateOf<T?>(null)
}

/**
 * CompositionLocal to provide drag-and-drop context down the tree.
 */
val LocalCyberDragDropState = compositionLocalOf<CyberDragDropState<Any>> {
    error("No CyberDragDropState provided. Wrap your UI in CyberDragDropProvider.")
}

/**
 * Provider container that establishes the scope for tactical sorting and reallocation.
 * Adds no layout node, so it has no accessibility label of its own; label the draggable items instead.
 */
@Composable
fun CyberDragDropProvider(
    content: @Composable () -> Unit
) {
    val state = remember { CyberDragDropState<Any>() }
    CompositionLocalProvider(
        LocalCyberDragDropState provides state,
        content = content
    )
}

/**
 * Modifier applied to draggable modules. Automatically translates visual position while dragging
 * and updates the [LocalCyberDragDropState].
 */
fun <T : Any> Modifier.cyberDraggable(
    data: T,
    snapBackOnRelease: Boolean = false,
    appendedA11y: String? = null,
    customA11y: String? = null,
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {}
): Modifier = this.cyberSemantics("CyberDraggable", appendedA11y, customA11y).composed {
    val state = LocalCyberDragDropState.current
    var offset by remember { mutableStateOf(Offset.Zero) }

    this
        .graphicsLayer {
            translationX = offset.x
            translationY = offset.y
        }
        .pointerInput(data) {
            detectDragGestures(
                onDragStart = { startOffset ->
                    state.isDragging = true
                    state.dragPosition = startOffset
                    state.draggedData = data
                    onDragStart()
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    offset += dragAmount
                    state.dragPosition += dragAmount
                },
                onDragEnd = {
                    state.isDragging = false
                    state.draggedData = null
                    if (snapBackOnRelease) {
                        offset = Offset.Zero
                    }
                    onDragEnd()
                },
                onDragCancel = {
                    state.isDragging = false
                    state.draggedData = null
                    if (snapBackOnRelease) {
                        offset = Offset.Zero
                    }
                    onDragEnd()
                }
            )
        }
}
