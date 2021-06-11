package com.mxalbert.zoomable

import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
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
    val scope = rememberCoroutineScope()
    val gesturesModifier = if (!enabled) Modifier else Modifier
        .pointerInput(Unit) {
            coroutineScope {
                launch {
                    detectTapGestures(onDoubleTap = { offset ->
                        scope.launch {
                            val isZooming = state.isZooming
                            val targetScale =
                                if (isZooming) state.minScale else state.doubleTapScale
                            state.animateScaleTo(
                                targetScale = targetScale,
                                targetTranslation = if (isZooming) {
                                    Offset.Zero
                                } else {
                                    state.calculateTargetTranslation(offset) * targetScale
                                }
                            )
                        }
                    }, onPress = {
                        state.onPress()
                    })
                }
                launch {
                    detectDragGestures(
                        state = state,
                        dismissGestureEnabled = dismissGestureEnabledState,
                        onDrag = { change, dragAmount ->
                            if (state.isZooming) {
                                scope.launch {
                                    state.onDrag(dragAmount)
                                    state.addPosition(
                                        change.uptimeMillis,
                                        change.position
                                    )
                                }
                            } else {
                                state.onDismissDrag(dragAmount.y)
                            }
                        },
                        onDragCancel = {
                            if (state.isZooming) {
                                state.resetTracking()
                            } else {
                                scope.launch {
                                    state.onDismissDragEnd()
                                }
                            }
                        },
                        onDragEnd = {
                            scope.launch {
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
        }
        .transformable(state = rememberTransformableState { zoomChange, panChange, _ ->
            if (state.dismissDragAbsoluteOffsetY == 0f) {
                scope.launch {
                    state.onZoomChange(zoomChange)
                    state.onDrag(panChange)
                }
            }
        })

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
                    placeable.place(
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

private suspend fun PointerInputScope.detectDragGestures(
    state: ZoomableState,
    dismissGestureEnabled: State<Boolean>,
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
                    awaitTouchSlopOrCancellation(down.id) { change, over ->
                        change.consumePositionChange()
                        overSlop = over
                    }
                } else {
                    awaitVerticalTouchSlopOrCancellation(down.id) { change, over ->
                        change.consumePositionChange()
                        overSlop = Offset(0f, over)
                    }
                }
                if (drag != null) {
                    onDrag(drag, overSlop)
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

private fun ZoomableState.calculateTargetTranslation(doubleTapPoint: Offset): Offset =
    (size.toSize().center + Offset(translationX, translationY) - doubleTapPoint) / scale
