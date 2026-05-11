package com.vortexa.navigation

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.LocalCompatNavigationEventDispatcherOwner
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEvent
import kotlin.math.abs
import kotlin.math.min

@OptIn(ExperimentalComposeUiApi::class, InternalComposeUiApi::class)
@Composable
internal actual fun WideBackGestureLayer(
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    val dispatcherOwner = LocalCompatNavigationEventDispatcherOwner.current
    val input = remember { DirectNavigationEventInput() }

    DisposableEffect(dispatcherOwner, input) {
        val dispatcher = dispatcherOwner?.navigationEventDispatcher
        if (dispatcher != null) {
            dispatcher.addInput(input)
        }
        onDispose {
            dispatcher?.removeInput(input)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .wideBackGestureInput(
                enabled = enabled && dispatcherOwner != null,
                input = input,
                density = LocalDensity.current,
            )
    ) {
        content()
    }
}

private fun Modifier.wideBackGestureInput(
    enabled: Boolean,
    input: DirectNavigationEventInput,
    density: Density,
): Modifier {
    if (!enabled) return this

    return pointerInput(input, density) {
        val nativeEdgeReservationPx = with(density) { 8.dp.toPx() }
        val maxStartEdgePx = with(density) { 96.dp.toPx() }
        val lockSlopPx = with(density) { 12.dp.toPx() }

        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val startEdgeWidth = min(size.width * 0.2f, maxStartEdgePx)
            if (
                down.position.x <= nativeEdgeReservationPx ||
                down.position.x > startEdgeWidth
            ) {
                return@awaitEachGesture
            }

            val pointerId = down.id
            val startPosition = down.position
            val velocityTracker = VelocityTracker()
            var backEventStarted = false
            var completedOrCancelled = false

            velocityTracker.addPosition(down.uptimeMillis, down.position)

            try {
                while (true) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == pointerId } ?: continue

                    velocityTracker.addPosition(change.uptimeMillis, change.position)

                    if (change.changedToUpIgnoreConsumed()) {
                        if (backEventStarted) {
                            val progress = backProgress(
                                dx = change.position.x - startPosition.x,
                                width = size.width,
                            )
                            val velocityX = velocityTracker.calculateVelocity().x
                            if (progress >= 0.33f || velocityX >= 900f) {
                                input.backCompleted()
                            } else {
                                input.backCancelled()
                            }
                            completedOrCancelled = true
                        }
                        break
                    }

                    val offset = change.position - startPosition
                    if (!backEventStarted) {
                        if (shouldStartWideBack(offset, lockSlopPx)) {
                            backEventStarted = true
                            input.backStarted(startPosition.toBackEvent(progress = 0f))
                        } else if (shouldIgnoreWideBack(offset, lockSlopPx)) {
                            break
                        } else {
                            continue
                        }
                    }

                    val progress = backProgress(offset.x, size.width)
                    input.backProgressed(change.position.toBackEvent(progress = progress))
                    change.consume()
                }
            } finally {
                if (backEventStarted && !completedOrCancelled) {
                    input.backCancelled()
                }
            }
        }
    }
}

private fun shouldStartWideBack(offset: Offset, lockSlopPx: Float): Boolean {
    val dx = offset.x
    val dy = offset.y
    return dx > lockSlopPx && dx > abs(dy) * 1.25f
}

private fun shouldIgnoreWideBack(offset: Offset, lockSlopPx: Float): Boolean {
    val dx = offset.x
    val dy = offset.y
    return abs(dy) > lockSlopPx && abs(dy) >= abs(dx)
}

private fun backProgress(dx: Float, width: Int): Float {
    if (width <= 0) return 0f
    return (dx / width).coerceIn(0f, 1f)
}

private fun Offset.toBackEvent(progress: Float): NavigationEvent =
    NavigationEvent(
        swipeEdge = NavigationEvent.EDGE_LEFT,
        progress = progress,
        touchX = x,
        touchY = y,
    )
