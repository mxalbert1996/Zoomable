package com.mxalbert.zoomable

import androidx.compose.animation.SplineBasedFloatDecayAnimationSpec
import androidx.compose.animation.core.generateDecayAnimationSpec
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.delay
import org.junit.Test

@Suppress("TestFunctionName")
class ZoomableTest {

    @Test
    fun `ZoomableState default params`() {
        val state = ZoomableState(decayAnimationSpec)
        assertThat(state.minScale).isEqualTo(ZoomableDefaults.MinScale)
        assertThat(state.minScale).isEqualTo(ZoomableDefaults.MinScale)
        assertThat(state.maxScale).isEqualTo(ZoomableDefaults.MaxScale)
        assertThat(state.doubleTapScale).isEqualTo(ZoomableDefaults.DoubleTapScale)
        assertThat(state.scale).isEqualTo(ZoomableDefaults.MinScale)
        assertThat(state.translationX).isEqualTo(0f)
        assertThat(state.translationY).isEqualTo(0f)
    }

    @Test
    fun `ZoomableState constructor params`() {
        val state = ZoomableState(
            decayAnimationSpec,
            initialScale = 2f,
            initialTranslationX = 100f,
            initialTranslationY = 100f
        )
        assertThat(state.scale).isEqualTo(2f)
        assertThat(state.translationX).isEqualTo(100f)
        assertThat(state.translationY).isEqualTo(100f)
    }

    @Test
    fun `Translation not reset after stored`() {
        val state = ZoomableState(
            decayAnimationSpec,
            initialScale = 2f,
            initialTranslationX = 100f,
            initialTranslationY = 100f
        )
        state.size = IntSize(200, 200)
        state.childSize = Size(0f, 0f)
        assertThat(state.translationX).isEqualTo(100f)
        assertThat(state.translationY).isEqualTo(100f)
    }

    @Test
    fun `Single tap`() {
        testTapAndDrag { scope ->
            with(scope) {
                down().up()
                delay(doubleTapTimeoutMillis + 1)
                assertThat(tapped).isTrue()
            }
        }
    }

    @Test
    fun `Vertical swipe not consumed when dismissGestureEnabled is false`() {
        testTapAndDrag { scope ->
            with(scope) {
                val verticalSwipe = down().moveBy(Offset(0f, touchSlop))
                assertThat(state.translationX).isEqualTo(0f)
                assertThat(state.translationY).isEqualTo(0f)
                assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(0f)
                assertThat(state.dismissDragOffsetY).isEqualTo(0f)
                assertThat(verticalSwipe.isConsumed).isFalse()
                verticalSwipe.up()
            }
        }
    }

    @Test
    fun `Horizontal swipe not consumed when dismissGestureEnabled is false`() {
        testTapAndDrag { scope ->
            with(scope) {
                val horizontalSwipe = down().moveBy(Offset(touchSlop, 0f))
                assertThat(state.translationX).isEqualTo(0f)
                assertThat(state.translationY).isEqualTo(0f)
                assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(0f)
                assertThat(state.dismissDragOffsetY).isEqualTo(0f)
                assertThat(horizontalSwipe.isConsumed).isFalse()
                horizontalSwipe.up()
            }
        }
    }

    @Test
    fun `Vertical swipe consumed when dismissGestureEnabled is true`() {
        testTapAndDrag { scope ->
            with(scope) {
                dismissGestureEnabled.value = true
                val dismissThreshold = size.height * DismissDragThreshold
                var verticalSwipe = down().moveBy(Offset(0f, touchSlop))
                assertThat(state.translationX).isEqualTo(0f)
                assertThat(state.translationY).isEqualTo(0f)
                assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(0f)
                assertThat(state.dismissDragProgress).isEqualTo(0f)
                assertThat(state.dismissDragOffsetY).isEqualTo(0f)
                assertThat(verticalSwipe.isConsumed).isTrue()
                verticalSwipe = verticalSwipe.moveBy(Offset(0f, 150f))
                assertThat(state.translationX).isEqualTo(0f)
                assertThat(state.translationY).isEqualTo(0f)
                assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(150f)
                assertThat(state.dismissDragOffsetY).isEqualTo(size.height / DismissDragResistanceFactor)
                assertThat(verticalSwipe.isConsumed).isTrue()
                verticalSwipe = verticalSwipe.moveBy(Offset(0f, dismissThreshold - 150f))
                assertThat(state.translationX).isEqualTo(0f)
                assertThat(state.translationY).isEqualTo(0f)
                assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(dismissThreshold)
                assertThat(state.dismissDragProgress).isEqualTo(1f)
                assertThat(verticalSwipe.isConsumed).isTrue()
                verticalSwipe.up()
                assertThat(dismissed).isFalse()
            }
        }
    }

    @Test
    fun `Dismiss frag progress is always positive`() {
        testTapAndDrag { scope ->
            with(scope) {
                dismissGestureEnabled.value = true
                val dismissThreshold = size.height * DismissDragThreshold
                var verticalSwipe = down().moveBy(Offset(0f, -touchSlop))
                assertThat(state.dismissDragProgress).isEqualTo(0f)
                verticalSwipe = verticalSwipe.moveBy(Offset(0f, -dismissThreshold))
                assertThat(state.dismissDragProgress).isEqualTo(1f)
                verticalSwipe.up()
            }
        }
    }

    @Test
    fun `Vertical swipe to dismiss`() {
        testTapAndDrag { scope ->
            with(scope) {
                dismissGestureEnabled.value = true
                val dismissThreshold = size.height * DismissDragThreshold
                val verticalSwipe = down().moveBy(Offset(0f, touchSlop))
                    .moveBy(Offset(0f, dismissThreshold + 1f))
                assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(dismissThreshold + 1f)
                assertThat(verticalSwipe.isConsumed).isTrue()
                verticalSwipe.up()
                assertThat(dismissed).isTrue()
            }
        }
    }

    @Test
    fun `Horizontal swipe not consumed when dismissGestureEnabled is true`() {
        testTapAndDrag { scope ->
            with(scope) {
                dismissGestureEnabled.value = true
                val horizontalSwipe = down().moveBy(Offset(touchSlop, 0f))
                assertThat(state.scale).isEqualTo(ZoomableDefaults.MinScale)
                assertThat(state.translationX).isEqualTo(0f)
                assertThat(state.translationY).isEqualTo(0f)
                assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(0f)
                assertThat(horizontalSwipe.isConsumed).isFalse()
                horizontalSwipe.up()
            }
        }
    }

    @Test
    fun `Horizontal swipe not consumed at edge when zoomed`() {
        testTapAndDrag {
            doubleTap()
            val horizontalSwipe = down().moveBy(Offset(touchSlop, 0f))
            assertThat(horizontalSwipe.isConsumed).isFalse()
            horizontalSwipe.up()
        }
    }

    @Test
    fun `Double tap at the center to zoom in without translation change`() {
        testTapAndDrag { scope ->
            with(scope) {
                val center = size.toSize().center
                val secondTap = doubleTap(center)
                assertThat(state.scale).isEqualTo(ZoomableDefaults.DoubleTapScale)
                assertThat(state.translationX).isEqualTo(0f)
                assertThat(state.translationY).isEqualTo(0f)
                assertThat(secondTap.isConsumed).isTrue()
            }
        }
    }

    @Test
    fun `Double tap at the top left to zoom in with translation change`() {
        testTapAndDrag { scope ->
            with(scope) {
                val secondTap = doubleTap()
                assertThat(state.scale).isEqualTo(ZoomableDefaults.DoubleTapScale)
                assertThat(state.translationX).isEqualTo(50f)
                assertThat(state.translationY).isEqualTo(50f)
                assertThat(secondTap.isConsumed).isTrue()
            }
        }
    }

    @Test
    fun Dragging() {
        testTapAndDrag { scope ->
            with(scope) {
                val center = size.toSize().center
                doubleTap(center)

                var drag = down().moveBy(Offset(0f, touchSlop))
                assertThat(state.translationX).isEqualTo(0f)
                assertThat(state.translationY).isEqualTo(0f)
                assertThat(drag.isConsumed).isTrue()
                drag = drag.moveBy(Offset(50f, 50f))
                assertThat(state.translationX).isEqualTo(50f)
                assertThat(state.translationY).isEqualTo(50f)
                assertThat(drag.isConsumed).isTrue()
                drag = drag.moveBy(Offset(50f, 50f))
                assertThat(state.translationX).isEqualTo(50f)
                assertThat(state.translationY).isEqualTo(50f)
                assertThat(drag.isConsumed).isTrue()
                drag = drag.moveBy(Offset(-100f, -100f))
                assertThat(state.translationX).isEqualTo(-50f)
                assertThat(state.translationY).isEqualTo(-50f)
                assertThat(drag.isConsumed).isTrue()
                drag.up()
            }
        }
    }

    @Test
    fun `Double tap to zoom out`() {
        testTapAndDrag { scope ->
            with(scope) {
                doubleTap()
                val secondTap = doubleTap()
                assertThat(state.scale).isEqualTo(ZoomableDefaults.MinScale)
                assertThat(state.translationX).isEqualTo(0f)
                assertThat(state.translationY).isEqualTo(0f)
                assertThat(secondTap.isConsumed).isTrue()
            }
        }
    }

    @Test
    fun `Double tap to zoom out with over-zoom enabled`() {
        testTapAndDrag(overZoom = true) { scope ->
            with(scope) {
                doubleTap()
                val secondTap = doubleTap()
                assertThat(state.scale).isEqualTo(ZoomableDefaults.MinScale)
                assertThat(state.translationX).isEqualTo(0f)
                assertThat(state.translationY).isEqualTo(0f)
                assertThat(secondTap.isConsumed).isTrue()
            }
        }
    }

    @Test
    fun Fling() {
        testTapAndDrag { scope ->
            with(scope) {
                doubleTap()

                val center = size.toSize().center
                val singleMove = -center / 3f
                down(center)
                    .moveBy(singleMove)
                    .moveBy(singleMove)
                    .moveBy(singleMove)
                    .up(timeDiffMillis = 0)
                advanceTime(1_000_000_000)
                assertThat(state.translationX).isEqualTo(-size.width / 2)
                assertThat(state.translationY).isEqualTo(-size.height / 2)
            }
        }
    }

    @Test
    fun `Start drag immediately when flinging`() {
        testTapAndDrag { scope ->
            with(scope) {
                doubleTap()

                val center = size.toSize().center
                val singleMove = -center / 3f
                down(center)
                    .moveBy(singleMove)
                    .moveBy(singleMove)
                    .moveBy(singleMove)
                    .up(timeDiffMillis = 0)
                advanceTime(16_000_000)  // One frame
                val translationX = state.translationX
                val translationY = state.translationY
                val drag = down().moveBy(Offset(10f, 10f))
                assertThat(state.translationX).isEqualTo(translationX + 10f)
                assertThat(state.translationY).isEqualTo(translationY + 10f)
                drag.up()
            }
        }
    }

    private suspend fun SuspendingGestureTestUtil.doubleTap(
        offset: Offset = Offset.Zero
    ): PointerInputChange {
        down(offset = offset).up()
        val secondTap = down(offset = offset, timeDiffMillis = 50).up()
        advanceTime(1_000_000_000)
        return secondTap
    }

    private val decayAnimationSpec =
        SplineBasedFloatDecayAnimationSpec(Density(1f)).generateDecayAnimationSpec<Float>()

    private fun testTapAndDrag(
        overZoom: Boolean = false,
        block: suspend SuspendingGestureTestUtil.(TestTapAndDragScope) -> Unit
    ) {
        val size = IntSize(100, 100)
        val scope = TestTapAndDragScope(
            state = ZoomableState(decayAnimationSpec).apply {
                if (overZoom) {
                    minScale = ZoomableDefaults.MinScale / 2
                    maxScale = ZoomableDefaults.MaxScale * 1.5f
                    overZoomConfig =
                        OverZoomConfig(ZoomableDefaults.MinScale, ZoomableDefaults.MaxScale)
                }
                this.size = size
                this.childSize = size.toSize()
            },
            size = size,
            dismissGestureEnabled = false
        )
        SuspendingGestureTestUtil(width = size.width, height = size.height) {
            detectZoomableGestures(
                state = scope.state,
                onTap = { scope.onTap() },
                dismissGestureEnabled = scope.dismissGestureEnabled,
                onDismiss = mutableStateOf({
                    scope.onDismiss()
                    false
                })
            )
        }.executeInComposition {
            block(scope)
        }
    }

    private class TestTapAndDragScope(
        val state: ZoomableState,
        val size: IntSize,
        dismissGestureEnabled: Boolean
    ) {
        val dismissGestureEnabled: MutableState<Boolean> = mutableStateOf(dismissGestureEnabled)

        var tapped: Boolean = false
            private set
        var dismissed: Boolean = false
            private set

        fun onTap() {
            tapped = true
        }

        fun onDismiss() {
            dismissed = true
        }
    }

}
