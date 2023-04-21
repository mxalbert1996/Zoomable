package com.mxalbert.zoomable.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.mxalbert.zoomable.sample.ZoomableApp

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Zoomable"
    ) {
        ZoomableApp()
    }
}
