package com.mxalbert.zoomable.sample

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
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
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import kotlinx.coroutines.launch

@Composable
fun ZoomableApp() {
    MaterialTheme(colors = darkColors()) {
        Surface(color = MaterialTheme.colors.background) {
            Sample()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Sample(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        HorizontalPager(pageCount = Images.size) { index ->
            ZoomableImagePage(
                onDismiss = {
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar("Dismissed.")
                    }
                    false
                }
            ) {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Images[index])
                        .size(Size.ORIGINAL)
                        .build()
                )
                if (painter.state is AsyncImagePainter.State.Success) {
                    val size = painter.intrinsicSize
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .aspectRatio(size.width / size.height)
                            .fillMaxSize()
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            Snackbar(snackbarData = it)
        }
    }
}
