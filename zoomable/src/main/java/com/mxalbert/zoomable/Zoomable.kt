package com.mxalbert.zoomable

import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A zoomable layout that supports zooming in and out, dragging, double tap and dismiss gesture.
 *
 * @param modifier The modifier to apply to this layout.
 * @param state The state object to be used to control or observe the state.
 * @param enabled Controls the enabled state. When false, all gestures will be ignored.
 * @param dismissGestureEnabled Whether to enable dismiss gesture detection.
 * @param onDismiss Will be called when dismiss gesture is detected. Should return a boolean
 * indicating whether the dismiss request is handled.
 * @param content a block which describes the content.
 */
@Composable
fun Zoomable(
    modifier: Modifier = Modifier,
    state: ZoomableState = rememberZoomableState(),
    enabled: Boolean = true,
    dismissGestureEnabled: Boolean = false,
    onDismiss: () -> Boolean = { false },
    content: @Composable () -> Unit
) {
    val dismissGestureEnabledState = rememberUpdatedState(dismissGestureEnabled)
    val gesturesModifier = if (!enabled) Modifier else {
        LaunchedEffect(state.isGestureInProgress, state.overZoomConfig) {
            if (!state.isGestureInProgress) {
                val range = state.overZoomConfig?.range
                if (range?.contains(state.scale) == false) {
                    state.animateScaleTo(state.scale.coerceIn(range))
                }
            }
        }

        Modifier.pointerInput(state) {
            detectZoomableGestures(
                state = state,
                dismissGestureEnabled = dismissGestureEnabledState,
                onDismiss = onDismiss
            )
        }
    }

    Box(
        modifier = modifier
            .then(gesturesModifier)
            .layout { measurable, constraints ->
                val width = constraints.maxWidth
                val height = constraints.maxHeight
                val placeable = measurable.measure(
                    Constraints(
                        maxWidth = (width * state.scale).roundToInt(),
                        maxHeight = (height * state.scale).roundToInt()
                    )
                )
                state.size = IntSize(width, height)
                state.childSize = Size(
                    placeable.width / state.scale,
                    placeable.height / state.scale
                )
                layout(width, height) {
                    placeable.placeWithLayer(
                        state.translationX.roundToInt() - state.boundOffset.x,
                        state.translationY.roundToInt() - state.boundOffset.y
                                + state.dismissDragOffsetY.roundToInt()
                    )
                }
            }
    ) {
        content()
    }
}

internal suspend fun PointerInputScope.detectZoomableGestures(
    state: ZoomableState,
    dismissGestureEnabled: State<Boolean>,
    onDismiss: () -> Boolean
): Unit = coroutineScope {
    launch {
        detectTapGestures(
            onDoubleTap = { offset ->
                launch {
                    val isZooming = state.isZooming
                    val targetScale = if (isZooming) state.minSnapScale else state.doubleTapScale
                    state.animateScaleTo(
                        targetScale = targetScale,
                        targetTranslation = if (isZooming) {
                            Offset.Zero
                        } else {
                            state.calculateTargetTranslation(offset) * targetScale
                        }
                    )
                }
            }
        )
    }
    launch {
        detectTransformGestures(
            onGestureStart = { state.onGestureStart() },
            onGesture = { centroid, pan, zoom ->
                if (state.dismissDragAbsoluteOffsetY == 0f) {
                    launch {
                        state.onTransform(centroid, pan, zoom)
                    }
                }
            },
            onGestureEnd = { state.onTransformEnd() }
        )
    }
    launch {
        detectDragGestures(
            state = state,
            dismissGestureEnabled = dismissGestureEnabled,
            startDragImmediately = { state.isGestureInProgress },
            onDragStart = {
                state.onGestureStart()
                state.addPosition(it.uptimeMillis, it.position)
            },
            onDrag = { change, dragAmount ->
                if (state.isZooming) {
                    launch {
                        state.onDrag(dragAmount)
                        state.addPosition(change.uptimeMillis, change.position)
                    }
                } else {
                    state.onDismissDrag(dragAmount.y)
                }
            },
            onDragCancel = {
                if (state.isZooming) {
                    state.resetTracking()
                } else {
                    launch {
                        state.onDismissDragEnd()
                    }
                }
            },
            onDragEnd = {
                launch {
                    if (state.isZooming) {
                        state.onDragEnd()
                    } else {
                        if (!(state.shouldDismiss && onDismiss())) {
                            state.onDismissDragEnd()
                        }
                    }
                }
            }
        )
    }
}

private suspend fun PointerInputScope.detectDragGestures(
    state: ZoomableState,
    dismissGestureEnabled: State<Boolean>,
    startDragImmediately: () -> Boolean,
    onDragStart: (PointerInputChange) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {},
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit
) {
    forEachGesture {
        awaitPointerEventScope {
            // We have to always call this or we'll get a crash if we do nothing
            val down = awaitFirstDown(requireUnconsumed = false)
            if (state.isZooming || dismissGestureEnabled.value) {
                var overSlop = Offset.Zero
                val drag = if (state.isZooming) {
                    if (startDragImmediately()) down else {
                        awaitTouchSlopOrCancellation(down.id) { change, over ->
                            if (state.isMaxX(change)) {
                                return@awaitTouchSlopOrCancellation
                            }
                            change.consumePositionChange()
                            overSlop = over
                        }
                    }
                } else {
                    awaitVerticalTouchSlopOrCancellation(down.id) { change, over ->
                        change.consumePositionChange()
                        overSlop = Offset(0f, over)
                    }
                }
                if (drag != null) {
                    onDragStart(down)
                    if (overSlop != Offset.Zero) onDrag(drag, overSlop)
                    if (
                        !drag(drag.id) {
                            onDrag(it, it.positionChange())
                            it.consumePositionChange()
                        }
                    ) {
                        onDragCancel()
                    } else {
                        onDragEnd()
                    }
                }
            }
        }
    }
}

/**
 * Simplified version of [androidx.compose.foundation.gestures.detectTransformGestures] which
 * awaits two pointer downs (instead of one) and starts immediately without considering touch slop.
 */
private suspend fun PointerInputScope.detectTransformGestures(
    onGestureStart: () -> Unit = {},
    onGestureEnd: () -> Unit = {},
    onGesture: (centroid: Offset, pan: Offset, zoom: Float) -> Unit
) {
    forEachGesture {
        awaitPointerEventScope {
            awaitTwoDowns(requireUnconsumed = false)
            onGestureStart()
            do {
                val event = awaitPointerEvent()
                val canceled = event.changes.fastAny { it.positionChangeConsumed() }
                if (!canceled) {
                    val zoomChange = event.calculateZoom()
                    val panChange = event.calculatePan()
                    val centroid = event.calculateCentroid(useCurrent = false)
                    if (zoomChange != 1f || panChange != Offset.Zero) {
                        onGesture(centroid, panChange, zoomChange)
                    }
                    event.changes.fastForEach {
                        if (it.positionChanged()) {
                            it.consumeAllChanges()
                        }
                    }
                }
            } while (!canceled && event.changes.fastAny { it.pressed })
            onGestureEnd()
        }
    }
}

private suspend fun AwaitPointerEventScope.awaitTwoDowns(requireUnconsumed: Boolean = true) {
    var event: PointerEvent
    var firstDown: PointerId? = null
    do {
        event = awaitPointerEvent()
        var downPointers = if (firstDown != null) 1 else 0
        event.changes.fastForEach {
            val isDown =
                if (requireUnconsumed) it.changedToDown() else it.changedToDownIgnoreConsumed()
            val isUp =
                if (requireUnconsumed) it.changedToUp() else it.changedToUpIgnoreConsumed()
            if (isUp && firstDown == it.id) {
                firstDown = null
                downPointers -= 1
            }
            if (isDown) {
                firstDown = it.id
                downPointers += 1
            }
        }
        val satisfied = downPointers > 1
    } while (!satisfied)
}
