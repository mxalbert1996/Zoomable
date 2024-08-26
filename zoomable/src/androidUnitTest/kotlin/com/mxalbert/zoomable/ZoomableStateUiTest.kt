package com.mxalbert.zoomable

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.swipeWithVelocity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("TestFunctionName")
@RunWith(AndroidJUnit4::class)
class ZoomableStateUiTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `Single tap`() = rule.testZoomable {
        performTouchInput {
            down(Offset.Zero)
            up()
        }
        rule.mainClock.advanceTimeBy(doubleTapTimeoutMillis + 1)
        assertThat(tapped).isTrue()
    }

    @Test
    fun `Vertical swipe not consumed when dismissGestureEnabled is false`() = rule.testZoomable {
        performTouchInput {
            down(Offset.Zero)
            moveBy(Offset(0f, touchSlop))
        }
        assertThat(state.translationX).isEqualTo(0f)
        assertThat(state.translationY).isEqualTo(0f)
        assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(0f)
        assertThat(state.dismissDragOffsetY).isEqualTo(0f)
        assertThat(lastPointerInputChange.isConsumed).isFalse()
        performTouchInput { up() }
    }

    @Test
    fun `Horizontal swipe not consumed when dismissGestureEnabled is false`() = rule.testZoomable {
        performTouchInput {
            down(Offset.Zero)
            moveBy(Offset(touchSlop, 0f))
        }
        assertThat(state.translationX).isEqualTo(0f)
        assertThat(state.translationY).isEqualTo(0f)
        assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(0f)
        assertThat(state.dismissDragOffsetY).isEqualTo(0f)
        assertThat(lastPointerInputChange.isConsumed).isFalse()
        performTouchInput { up() }
    }

    @Test
    fun `Vertical swipe consumed when dismissGestureEnabled is true`() = rule.testZoomable {
        dismissGestureEnabled = true
        val dismissThreshold = size.height * DismissDragThreshold
        performTouchInput {
            down(Offset.Zero)
            moveBy(Offset(0f, touchSlop))
        }
        assertThat(state.translationX).isEqualTo(0f)
        assertThat(state.translationY).isEqualTo(0f)
        assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(0f)
        assertThat(state.dismissDragProgress).isEqualTo(0f)
        assertThat(state.dismissDragOffsetY).isEqualTo(0f)
        assertThat(lastPointerInputChange.isConsumed).isTrue()
        performTouchInput {
            moveBy(Offset(0f, 150f))
        }
        assertThat(state.translationX).isEqualTo(0f)
        assertThat(state.translationY).isEqualTo(0f)
        assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(150f)
        assertThat(state.dismissDragOffsetY).isEqualTo(size.height / DismissDragResistanceFactor)
        assertThat(lastPointerInputChange.isConsumed).isTrue()
        performTouchInput {
            moveBy(Offset(0f, dismissThreshold - 150f))
        }
        assertThat(state.translationX).isEqualTo(0f)
        assertThat(state.translationY).isEqualTo(0f)
        assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(dismissThreshold)
        assertThat(state.dismissDragProgress).isEqualTo(1f)
        assertThat(lastPointerInputChange.isConsumed).isTrue()
        performTouchInput { up() }
        assertThat(dismissed).isFalse()
    }

    @Test
    fun `Dismiss drag progress is always positive`() = rule.testZoomable {
        dismissGestureEnabled = true
        val dismissThreshold = size.height * DismissDragThreshold
        performTouchInput {
            down(Offset.Zero)
            moveBy(Offset(0f, -touchSlop))
        }
        assertThat(state.dismissDragProgress).isEqualTo(0f)
        performTouchInput {
            moveBy(Offset(0f, -dismissThreshold))
        }
        assertThat(state.dismissDragProgress).isEqualTo(1f)
        performTouchInput { up() }
    }

    @Test
    fun `Vertical swipe to dismiss`() = rule.testZoomable {
        dismissGestureEnabled = true
        val dismissThreshold = size.height * DismissDragThreshold
        performTouchInput {
            down(Offset.Zero)
            moveBy(Offset(0f, touchSlop))
            moveBy(Offset(0f, dismissThreshold + 1f))
        }
        assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(dismissThreshold + 1f)
        assertThat(lastPointerInputChange.isConsumed).isTrue()
        performTouchInput { up() }
        assertThat(dismissed).isTrue()
    }

    @Test
    fun `Horizontal swipe not consumed when dismissGestureEnabled is true`() = rule.testZoomable {
        dismissGestureEnabled = true
        performTouchInput {
            down(Offset.Zero)
            moveBy(Offset(touchSlop, 0f))
        }
        assertThat(state.scale).isEqualTo(ZoomableDefaults.MinScale)
        assertThat(state.translationX).isEqualTo(0f)
        assertThat(state.translationY).isEqualTo(0f)
        assertThat(state.dismissDragAbsoluteOffsetY).isEqualTo(0f)
        assertThat(lastPointerInputChange.isConsumed).isFalse()
        performTouchInput { up() }
    }

    @Test
    fun `Horizontal swipe not consumed at edge when zoomed`() = rule.testZoomable {
        performTouchInput {
            doubleClick(Offset.Zero)
        }
        rule.waitForIdle()
        performTouchInput {
            down(Offset.Zero)
            moveBy(Offset(touchSlop, 0f))
        }
        assertThat(lastPointerInputChange.isConsumed).isFalse()
        performTouchInput { up() }
    }

    @Test
    fun `Double tap at the center to zoom in without translation change`() = rule.testZoomable {
        performTouchInput {
            doubleClick(center)
        }
        rule.waitForIdle()
        assertThat(state.scale).isEqualTo(ZoomableDefaults.DoubleTapScale)
        assertThat(state.translationX).isEqualTo(0f)
        assertThat(state.translationY).isEqualTo(0f)
        assertThat(lastPointerInputChange.isConsumed).isTrue()
    }

    @Test
    fun `Double tap at the top left to zoom in with translation change`() = rule.testZoomable {
        performTouchInput {
            doubleClick(Offset.Zero)
        }
        rule.waitForIdle()
        assertThat(state.scale).isEqualTo(ZoomableDefaults.DoubleTapScale)
        assertThat(state.translationX).isEqualTo(50f)
        assertThat(state.translationY).isEqualTo(50f)
        assertThat(lastPointerInputChange.isConsumed).isTrue()
    }

    @Test
    fun Dragging() = rule.testZoomable {
        performTouchInput {
            doubleClick(center)
        }
        rule.waitForIdle()
        performTouchInput {
            down(Offset.Zero)
            moveBy(Offset(0f, touchSlop))
        }
        assertThat(state.translationX).isEqualTo(0f)
        assertThat(state.translationY).isEqualTo(0f)
        assertThat(lastPointerInputChange.isConsumed).isTrue()
        performTouchInput {
            moveBy(Offset(50f, 50f))
        }
        assertThat(state.translationX).isEqualTo(50f)
        assertThat(state.translationY).isEqualTo(50f)
        assertThat(lastPointerInputChange.isConsumed).isTrue()
        performTouchInput {
            moveBy(Offset(50f, 50f))
        }
        assertThat(state.translationX).isEqualTo(50f)
        assertThat(state.translationY).isEqualTo(50f)
        assertThat(lastPointerInputChange.isConsumed).isTrue()
        performTouchInput {
            moveBy(Offset(-100f, -100f))
        }
        assertThat(state.translationX).isEqualTo(-50f)
        assertThat(state.translationY).isEqualTo(-50f)
        assertThat(lastPointerInputChange.isConsumed).isTrue()
        performTouchInput { up() }
    }

    @Test
    fun `Double tap to zoom out`() = rule.testZoomable {
        performTouchInput {
            doubleClick(Offset.Zero)
        }
        rule.waitForIdle()
        performTouchInput {
            doubleClick(Offset.Zero)
        }
        rule.waitForIdle()
        assertThat(state.scale).isEqualTo(ZoomableDefaults.MinScale)
        assertThat(state.translationX).isEqualTo(0f)
        assertThat(state.translationY).isEqualTo(0f)
        assertThat(lastPointerInputChange.isConsumed).isTrue()
    }

    @Test
    fun `Double tap to zoom out with over-zoom enabled`() = rule.testZoomable(overZoom = true) {
        performTouchInput {
            doubleClick(Offset.Zero)
        }
        rule.waitForIdle()
        performTouchInput {
            doubleClick(Offset.Zero)
        }
        rule.waitForIdle()
        assertThat(state.scale).isEqualTo(ZoomableDefaults.MinScale)
        assertThat(state.translationX).isEqualTo(0f)
        assertThat(state.translationY).isEqualTo(0f)
        assertThat(lastPointerInputChange.isConsumed).isTrue()
    }

    @Test
    fun Fling() = rule.testZoomable {
        performTouchInput {
            doubleClick(Offset.Zero)
        }
        rule.waitForIdle()
        performTouchInput {
            swipeWithVelocity(
                start = bottomRight,
                end = topLeft,
                endVelocity = 5000f
            )
        }
        rule.waitForIdle()
        assertThat(state.translationX).isEqualTo(-size.width / 2)
        assertThat(state.translationY).isEqualTo(-size.height / 2)
    }

    @Test
    fun `Start drag immediately when flinging`() = rule.testZoomable {
        performTouchInput {
            doubleClick(Offset.Zero)
        }
        rule.waitForIdle()
        performTouchInput {
            swipeWithVelocity(
                start = bottomRight,
                end = topLeft,
                endVelocity = 5000f
            )
            advanceEventTime()
            down(Offset.Zero)
        }
        val translationX = state.translationX
        val translationY = state.translationY
        performTouchInput {
            moveBy(Offset(10f, 10f))
        }
        assertThat(state.translationX).isEqualTo(translationX + 10f)
        assertThat(state.translationY).isEqualTo(translationY + 10f)
        performTouchInput { up() }
    }
}
