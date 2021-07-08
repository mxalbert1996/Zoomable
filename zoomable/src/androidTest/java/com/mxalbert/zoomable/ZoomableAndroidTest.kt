package com.mxalbert.zoomable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ZoomableAndroidTest {

    @get:Rule
    val rule = createComposeRule()

    @OptIn(ExperimentalFoundationApi::class)
    @Test
    fun panGestureTest() {
        rule.setContent {
            Zoomable(modifier = Modifier.semantics { testTag = "Zoomable" }) {
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

        rule.onNodeWithTag("Zoomable").apply {
            val content = rule.onNodeWithContentDescription("Content")

            performGesture { doubleClick(position = Offset.Zero) }
            rule.waitForIdle()
            content.assertLeftPositionInRootIsEqualTo(0.dp)

            performGesture { swipeLeft() }
            rule.waitForIdle()
            content.assertLeftPositionInRootIsEqualTo(-getUnclippedBoundsInRoot().width)
        }
    }

}
