package com.mxalbert.zoomable.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(
        title = "Zoomable",
        canvasElementId = "canvas",
    ) {
        ZoomableApp()
    }
}
