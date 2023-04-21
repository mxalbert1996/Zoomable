package com.mxalbert.zoomable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ZoomableAndroidTest {

    @get:Rule
    val rule = createComposeRule()

    @OptIn(ExperimentalFoundationApi::class)
    private fun test(block: SemanticsNodeInteraction.(content: SemanticsNodeInteraction) -> Unit) {
        rule.setContent {
            WithTouchSlop(0f) {
                Zoomable(
                    modifier = Modifier
                        .size(300.dp)
                        .semantics { testTag = "Zoomable" },
                    state = rememberZoomableState(doubleTapScale = 2f),
                    dismissGestureEnabled = true
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = "Content"
                    ) {
                        drawRect(
                            Brush.horizontalGradient(colors = listOf(Color.Black, Color.White))
                        )
                    }
                }
            }
        }

        rule.onNodeWithTag("Zoomable").block(rule.onNodeWithContentDescription("Content"))
    }

    @Test
    fun doubleTapGesture() = test { content ->
        performTouchInput { doubleClick(position = Offset.Zero) }
        rule.waitForIdle()
        content
            .assertLeftPositionInRootIsEqualTo(0.dp)
            .assertTopPositionInRootIsEqualTo(0.dp)
            .assertWidthIsEqualTo(getUnclippedBoundsInRoot().width * 2)
            .assertHeightIsEqualTo(getUnclippedBoundsInRoot().height * 2)
    }

    @Test
    fun panGesture() = test { content ->
        performTouchInput { doubleClick(position = Offset.Zero) }
        rule.waitForIdle()

        val distance = with(rule.density) { 10.dp.toPx() }
        performTouchInput {
            down(Offset(centerX, centerY))
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
    fun flingAfterPanGesture() = test { content ->
        performTouchInput { doubleClick(position = Offset.Zero) }
        rule.waitForIdle()

        performTouchInput {
            swipeWithVelocity(
                start = center / 2f,
                end = topLeft,
                endVelocity = 5000f
            )
        }
        val contentBounds = content.getUnclippedBoundsInRoot()
        val containerBounds = getUnclippedBoundsInRoot()
        assertThat(contentBounds.left).isLessThan(-containerBounds.width / 2)
        assertThat(contentBounds.top).isLessThan(-containerBounds.height / 2)
    }

    @Test
    fun swipeToDismissGesture() = test { content ->
        val distance = with(rule.density) { 10.dp.toPx() }
        performTouchInput {
            down(Offset(centerX, centerY))
            moveBy(Offset(0f, distance))
        }

        val top = content.getUnclippedBoundsInRoot().top
        assertThat(top).isGreaterThan(0.dp)
        assertThat(top).isLessThan(10.dp)
    }

}
