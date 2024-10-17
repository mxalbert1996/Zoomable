package com.github.trueddd

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.mxalbert.zoomable.sample.ZoomableApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(
        title = "Zoomable",
        canvasElementId = "canvas",
    ) {
        ZoomableApp()
    }
}
