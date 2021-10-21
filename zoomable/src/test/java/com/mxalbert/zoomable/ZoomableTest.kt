package com.mxalbert.zoomable

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.anyChangeConsumed
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.google.common.truth.Truth.assertThat
import org.junit.Test

@Suppress("TestFunctionName")
class ZoomableTest {

    @Test
    fun `ZoomableState default params`() {
        val state = ZoomableState()
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
            initialScale = 2f,
            initialTranslationX = 100f,
            initialTranslationY = 100f
        )
        assertThat(state.scale).isEqualTo(2f)
        assertThat(state.translationX).isEqualTo(100f)
        assertThat(state.translationY).isEqualTo(100f)
    }

    @Test
    fun `Vertical swipe not consumed when dismissGestureEnabled is false`() {
        testTapAndDrag { scope ->
            with(scope) {
                val verticalSwipe = down().moveBy(Offset(0f, 18f))
                assertThat(state.translationX).isEqualTo(0f)
                assertThat(state.translationY).isEqualTo(0f)
                assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(0f)
                assertThat(state.dismissDragOffsetY).isEqualTo(0f)
                assertThat(verticalSwipe.anyChangeConsumed()).isFalse()
                verticalSwipe.up()
            }
        }
    }

    @Test
    fun `Horizontal swipe not consumed when dismissGestureEnabled is false`() {
        testTapAndDrag { scope ->
            with(scope) {
                val horizontalSwipe = down().moveBy(Offset(18f, 0f))
                assertThat(state.translationX).isEqualTo(0f)
                assertThat(state.translationY).isEqualTo(0f)
                assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(0f)
                assertThat(state.dismissDragOffsetY).isEqualTo(0f)
                assertThat(horizontalSwipe.anyChangeConsumed()).isFalse()
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
                var verticalSwipe = down().moveBy(Offset(0f, 18f))
                assertThat(state.translationX).isEqualTo(0f)
                assertThat(state.translationY).isEqualTo(0f)
                assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(0f)
                assertThat(state.dismissDragOffsetY).isEqualTo(0f)
                assertThat(verticalSwipe.consumed.positionChange).isTrue()
                verticalSwipe = verticalSwipe.moveBy(Offset(0f, 150f))
                assertThat(state.translationX).isEqualTo(0f)
                assertThat(state.translationY).isEqualTo(0f)
                assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(150f)
                assertThat(state.dismissDragOffsetY).isEqualTo(size.height / DismissDragResistanceFactor)
                assertThat(verticalSwipe.consumed.positionChange).isTrue()
                verticalSwipe = verticalSwipe.moveBy(Offset(0f, dismissThreshold - 150f))
                assertThat(state.translationX).isEqualTo(0f)
                assertThat(state.translationY).isEqualTo(0f)
                assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(dismissThreshold)
                assertThat(verticalSwipe.consumed.positionChange).isTrue()
                verticalSwipe.up()
                assertThat(dismissed).isFalse()
            }
        }
    }

    @Test
    fun `Vertical swipe to dismiss`() {
        testTapAndDrag { scope ->
            with(scope) {
                dismissGestureEnabled.value = true
                val dismissThreshold = size.height * DismissDragThreshold
                val verticalSwipe = down().moveBy(Offset(0f, 18f))
                    .moveBy(Offset(0f, dismissThreshold + 1f))
                assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(dismissThreshold + 1f)
                assertThat(verticalSwipe.consumed.positionChange).isTrue()
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
                val horizontalSwipe = down().moveBy(Offset(18f, 0f))
                assertThat(state.scale).isEqualTo(ZoomableDefaults.MinScale)
                assertThat(state.translationX).isEqualTo(0f)
                assertThat(state.translationY).isEqualTo(0f)
                assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(0f)
                assertThat(horizontalSwipe.anyChangeConsumed()).isFalse()
                horizontalSwipe.up()
            }
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
                assertThat(secondTap.consumed.downChange).isTrue()
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
                assertThat(secondTap.consumed.downChange).isTrue()
            }
        }
    }

    @Test
    fun Dragging() {
        testTapAndDrag { scope ->
            with(scope) {
                val center = size.toSize().center
                doubleTap(center)

                var drag = down().moveBy(Offset(0f, 18f))
                assertThat(state.translationX).isEqualTo(0f)
                assertThat(state.translationY).isEqualTo(0f)
                assertThat(drag.consumed.positionChange).isTrue()
                drag = drag.moveBy(Offset(50f, 50f))
                assertThat(state.translationX).isEqualTo(50f)
                assertThat(state.translationY).isEqualTo(50f)
                assertThat(drag.consumed.positionChange).isTrue()
                drag = drag.moveBy(Offset(50f, 50f))
                assertThat(state.translationX).isEqualTo(50f)
                assertThat(state.translationY).isEqualTo(50f)
                assertThat(drag.consumed.positionChange).isTrue()
                drag = drag.moveBy(Offset(-100f, -100f))
                assertThat(state.translationX).isEqualTo(-50f)
                assertThat(state.translationY).isEqualTo(-50f)
                assertThat(drag.consumed.positionChange).isTrue()
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
                assertThat(secondTap.consumed.downChange).isTrue()
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

    private fun testTapAndDrag(
        block: suspend SuspendingGestureTestUtil.(TestTapAndDragScope) -> Unit
    ) {
        val size = IntSize(100, 100)
        val scope = TestTapAndDragScope(
            state = ZoomableState().apply {
                this.size = size
                this.childSize = size.toSize()
            },
            size = size,
            dismissGestureEnabled = mutableStateOf(false),
            dismissed = false
        )
        SuspendingGestureTestUtil(width = size.width, height = size.height) {
            detectTapAndDragGestures(scope.state, scope.dismissGestureEnabled) {
                scope.dismissed = true
                false
            }
        }.executeInComposition {
            block(scope)
        }
    }

    private class TestTapAndDragScope(
        val state: ZoomableState,
        val size: IntSize,
        val dismissGestureEnabled: MutableState<Boolean>,
        var dismissed: Boolean
    )

}
