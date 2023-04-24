package com.mxalbert.zoomable.sample

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.useContents
import platform.UIKit.UIApplication
import platform.UIKit.UIEdgeInsets

fun MainViewController() = ComposeUIViewController {
    // This doesn't update after orientation changes
    // TODO: Make this observable after it's supported by Compose
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
