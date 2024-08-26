package com.mxalbert.zoomable.sample

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ZoomableApp() {
    val snackbarHostState = remember { SnackbarHostState() }

    ZoomableApp(snackbarHostState = snackbarHostState) {
        HorizontalPager(state = rememberPagerState { Images.size }) { index ->
            ZoomableImagePage(snackbarHostState = snackbarHostState) {
                AsyncZoomableImage(url = Images[index])
            }
        }
    }
}
