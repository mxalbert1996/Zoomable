package com.mxalbert.zoomable.sample

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.compositionLocalOf

internal val LocalWindowInsets = compositionLocalOf {
    WindowInsets(0, 0, 0, 0)
}

internal actual val WindowInsets.Companion.safeDrawing: WindowInsets
    @Composable
    @NonRestartableComposable
    get() = LocalWindowInsets.current
