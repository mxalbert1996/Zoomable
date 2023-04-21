package com.mxalbert.zoomable.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@Composable
fun ZoomableApp() {
    MaterialTheme(colors = darkColors()) {
        Surface(color = MaterialTheme.colors.background) {
            Sample()
        }
    }
}

@Composable
private fun Sample(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        ZoomableImagePage(
            onDismiss = {
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar("Dismissed.")
                }
                false
            }
        ) {
            AsyncZoomableImage(url = Images[0])
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Snackbar(snackbarData = it)
        }
    }
}
