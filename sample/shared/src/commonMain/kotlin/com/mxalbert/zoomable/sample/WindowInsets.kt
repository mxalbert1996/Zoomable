package com.mxalbert.zoomable.sample

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable

@get:Composable
@get:NonRestartableComposable
internal expect val WindowInsets.Companion.safeDrawing: WindowInsets
