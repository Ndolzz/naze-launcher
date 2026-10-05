package com.naze.launcher.gestures

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * A single pointerInput block per gesture family (tap vs vertical drag vs horizontal
 * drag) rather than one giant custom gesture recognizer — keeps conflicts between drag
 * directions predictable and matches how Android's own gesture APIs are meant to be
 * composed. Long-press and double-tap share the tap detector, which Android resolves
 * natively.
 */
fun Modifier.homeGestures(
    bindings: GestureBindings,
    onAction: (GestureAction) -> Unit,
    swipeThresholdPx: Float = 110f
): Modifier = this
    .pointerInput(bindings) {
        detectTapGestures(
            onDoubleTap = { onAction(bindings.doubleTap) },
            onLongPress = { onAction(bindings.longPress) }
        )
    }
    .pointerInput(bindings) {
        var accumulated = 0f
        detectVerticalDragGestures(
            onDragStart = { accumulated = 0f },
            onVerticalDrag = { change, dragAmount ->
                accumulated += dragAmount
                change.consume()
            },
            onDragEnd = {
                when {
                    accumulated <= -swipeThresholdPx -> onAction(bindings.swipeUp)
                    accumulated >= swipeThresholdPx -> onAction(bindings.swipeDown)
                }
            }
        )
    }
    .pointerInput(bindings) {
        var accumulated = 0f
        detectHorizontalDragGestures(
            onDragStart = { accumulated = 0f },
            onHorizontalDrag = { change, dragAmount ->
                accumulated += dragAmount
                change.consume()
            },
            onDragEnd = {
                when {
                    accumulated <= -swipeThresholdPx -> onAction(bindings.swipeLeft)
                    accumulated >= swipeThresholdPx -> onAction(bindings.swipeRight)
                }
            }
        )
    }
