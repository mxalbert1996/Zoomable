package com.mxalbert.zoomable.sample

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIApplication
import platform.UIKit.UIEdgeInsets

@OptIn(ExperimentalForeignApi::class)
fun MainViewController() = ComposeUIViewController {
    val insets = UIApplication.sharedApplication.keyWindow
        ?.safeAreaInsets?.useContents { toWindowInsets() }
        ?: LocalWindowInsets.current
    CompositionLocalProvider(LocalWindowInsets provides insets) {
        ZoomableApp()
    }
}

private fun UIEdgeInsets.toWindowInsets(): WindowInsets = WindowInsets(
    left = left.dp,
    top = top.dp,
    right = right.dp,
    bottom = bottom.dp
)
