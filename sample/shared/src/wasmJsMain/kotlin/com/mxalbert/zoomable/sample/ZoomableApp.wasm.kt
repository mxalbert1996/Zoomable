package com.mxalbert.zoomable.sample

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun ZoomableApp() {
    val snackbarHostState = remember { SnackbarHostState() }

    ZoomableApp(snackbarHostState = snackbarHostState) {
        ZoomableImagePage(snackbarHostState = snackbarHostState) {
            AsyncZoomableImage(url = Images[0])
        }
    }
}
