package moe.tlaster.zoomable

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ZoomableUnitTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test(expected = IllegalArgumentException::class)
    fun rememberZoomableState_minScale_negative() {
        composeTestRule.setContent {
            rememberZoomableState(minScale = -1f)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rememberZoomableState_minScale_greater_than_maxScale() {
        composeTestRule.setContent {
            rememberZoomableState(minScale = 4f, maxScale = 1f)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rememberZoomableState_minScale_equals_maxScale() {
        composeTestRule.setContent {
            rememberZoomableState(minScale = 1f, maxScale = 1f)
        }
    }
}
