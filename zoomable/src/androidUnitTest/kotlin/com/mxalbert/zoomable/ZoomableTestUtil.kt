package com.mxalbert.zoomable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

internal class ZoomableUiTestScope(
    viewConfiguration: ViewConfiguration,
    val size: IntSize,
    dismissGestureEnabled: Boolean
) : ViewConfiguration by viewConfiguration {

    lateinit var state: ZoomableState
    lateinit var zoomable: SemanticsNodeInteraction
    lateinit var content: SemanticsNodeInteraction

    var dismissGestureEnabled: Boolean by mutableStateOf(dismissGestureEnabled)

    lateinit var lastPointerInputChange: PointerInputChange

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

    fun performTouchInput(
        block: TouchInjectionScope.() -> Unit
    ): SemanticsNodeInteraction = zoomable.performTouchInput(block)
}

internal fun ComposeContentTestRule.testZoomable(
    viewConfiguration: TestViewConfiguration = TestViewConfiguration(),
    size: IntSize = IntSize(100, 100),
    overZoom: Boolean = false,
    dismissGestureEnabled: Boolean = false,
    block: ZoomableUiTestScope.() -> Unit
) {
    val scope = ZoomableUiTestScope(
        viewConfiguration = viewConfiguration,
        size = size,
        dismissGestureEnabled = dismissGestureEnabled,
    )

    setContent {
        CompositionLocalProvider(
            LocalDensity provides Density(1f, 1f),
            LocalViewConfiguration provides viewConfiguration
        ) {
            Zoomable(
                state = rememberZoomableState(
                    minScale = if (overZoom) {
                        ZoomableDefaults.MinScale / 2
                    } else {
                        ZoomableDefaults.MinScale
                    },
                    maxScale = if (overZoom) {
                        ZoomableDefaults.MaxScale * 1.5f
                    } else {
                        ZoomableDefaults.MaxScale
                    },
                    overZoomConfig = if (overZoom) {
                        OverZoomConfig(ZoomableDefaults.MinScale, ZoomableDefaults.MaxScale)
                    } else {
                        null
                    }
                ).also { scope.state = it },
                onTap = { scope.onTap() },
                dismissGestureEnabled = scope.dismissGestureEnabled,
                onDismiss = {
                    scope.onDismiss()
                    false
                },
                modifier = Modifier
                    .size(width = size.width.dp, height = size.height.dp)
                    .semantics { testTag = "Zoomable" }
                    .pointerInput(Unit) {
                        @Suppress("ReturnFromAwaitPointerEventScope")
                        awaitPointerEventScope {
                            while (true) {
                                scope.lastPointerInputChange = awaitPointerEvent().changes.single()
                            }
                        }
                    }
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

    scope.zoomable = onNodeWithTag("Zoomable")
    scope.content = onNodeWithContentDescription("Content")
    block(scope)
}
