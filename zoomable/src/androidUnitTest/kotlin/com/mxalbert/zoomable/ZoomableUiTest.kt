package com.mxalbert.zoomable

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertLeftPositionInRootIsEqualTo
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.swipeWithVelocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ZoomableUiTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun doubleTapGesture() = rule.testZoomable {
        performTouchInput { doubleClick(Offset.Zero) }
        rule.waitForIdle()
        content
            .assertLeftPositionInRootIsEqualTo(0.dp)
            .assertTopPositionInRootIsEqualTo(0.dp)
            .assertWidthIsEqualTo(zoomable.getUnclippedBoundsInRoot().width * 2)
            .assertHeightIsEqualTo(zoomable.getUnclippedBoundsInRoot().height * 2)
    }

    @Test
    fun panGesture() = rule.testZoomable {
        performTouchInput { doubleClick(Offset.Zero) }
        rule.waitForIdle()

        val distance = 10f
        performTouchInput {
            down(Offset(centerX, centerY))
            moveBy(Offset(-viewConfiguration.touchSlop, 0f))
            moveBy(Offset(-distance, 0f))
        }
        content
            .assertLeftPositionInRootIsEqualTo((-10).dp)
            .assertTopPositionInRootIsEqualTo(0.dp)

        performTouchInput { moveBy(Offset(0f, -distance)) }
        content
            .assertLeftPositionInRootIsEqualTo((-10).dp)
            .assertTopPositionInRootIsEqualTo((-10).dp)

        performTouchInput { moveBy(Offset(-distance, -distance)) }
        content
            .assertLeftPositionInRootIsEqualTo((-20).dp)
            .assertTopPositionInRootIsEqualTo((-20).dp)
    }

    @Test
    fun flingAfterPanGesture() = rule.testZoomable {
        performTouchInput { doubleClick(Offset.Zero) }
        rule.waitForIdle()

        performTouchInput {
            swipeWithVelocity(
                start = bottomRight,
                end = topLeft,
                endVelocity = 5000f
            )
        }
        rule.waitForIdle()
        val contentBounds = content.getUnclippedBoundsInRoot()
        val containerBounds = zoomable.getUnclippedBoundsInRoot()
        assertThat(contentBounds.left).isLessThan(-containerBounds.width / 2)
        assertThat(contentBounds.top).isLessThan(-containerBounds.height / 2)
    }

    @Test
    fun swipeToDismissGesture() = rule.testZoomable(dismissGestureEnabled = true) {
        performTouchInput {
            down(Offset(centerX, centerY))
            moveBy(Offset(0f, viewConfiguration.touchSlop))
            moveBy(Offset(0f, 10f))
        }

        val top = content.getUnclippedBoundsInRoot().top
        assertThat(top).isGreaterThan(0.dp)
        assertThat(top).isLessThan(10.dp)
    }
}
