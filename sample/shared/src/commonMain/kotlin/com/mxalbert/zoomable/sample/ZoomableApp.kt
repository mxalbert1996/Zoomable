package com.mxalbert.zoomable.sample

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mxalbert.zoomable.OverZoomConfig
import com.mxalbert.zoomable.Zoomable
import com.mxalbert.zoomable.rememberZoomableState
import kotlinx.coroutines.launch

@Composable
internal fun ZoomableApp(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    MaterialTheme(colors = darkColors()) {
        Surface(color = MaterialTheme.colors.background) {
            Box(modifier = modifier.fillMaxSize()) {
                content()

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing
                                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                        )
                ) {
                    Snackbar(snackbarData = it)
                }
            }
        }
    }
}

@Composable
internal fun ZoomableImagePage(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    image: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    var enabled by rememberSaveable { mutableStateOf(true) }
    var overZoom by rememberSaveable { mutableStateOf(false) }
    var fadeOut by rememberSaveable { mutableStateOf(false) }
    var isOverlayVisible by rememberSaveable { mutableStateOf(true) }
    val state = rememberZoomableState(
        minScale = if (overZoom) 0.5f else 1f,
        maxScale = if (overZoom) 6f else 4f,
        overZoomConfig = if (overZoom) OverZoomConfig(1f, 4f) else null
    )
    Box(modifier = modifier) {
        Zoomable(
            modifier = Modifier.graphicsLayer {
                clip = true
                alpha = if (fadeOut) 1 - state.dismissDragProgress else 1f
            },
            state = state,
            enabled = enabled,
            onTap = { isOverlayVisible = !isOverlayVisible },
            dismissGestureEnabled = true,
            onDismiss = {
                scope.launch {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar("Dismissed.")
                }
                false
            },
            content = image
        )

        AnimatedVisibility(
            visible = isOverlayVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        ) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colors.surface.copy(alpha = 0.3f))
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing
                            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Checkbox(
                        text = "Enable",
                        checked = enabled,
                        onCheckedChange = { enabled = it }
                    )
                    Checkbox(
                        text = "Enable over-zoom",
                        checked = overZoom,
                        onCheckedChange = { overZoom = it }
                    )
                    Checkbox(
                        text = "Enable fade-out when dismissed",
                        checked = fadeOut,
                        onCheckedChange = { fadeOut = it }
                    )
                }
                Button(onClick = {
                    scope.launch {
                        state.animateTranslateTo(Offset.Zero)
                    }
                }) {
                    Text(text = "Center")
                }
            }
        }
    }
}

@Composable
private fun Checkbox(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            modifier = Modifier.padding(2.dp)
        )
        Text(text = text, modifier = Modifier.padding(2.dp))
    }
}
