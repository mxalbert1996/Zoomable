package com.mxalbert.zoomable.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.mxalbert.zoomable.OverZoomConfig
import com.mxalbert.zoomable.Zoomable
import com.mxalbert.zoomable.rememberZoomableState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MaterialTheme(colors = darkColors()) {
                Surface(color = MaterialTheme.colors.background) {
                    Sample()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Sample(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        HorizontalPager(pageCount = images.size) { index ->
            var enabled by rememberSaveable { mutableStateOf(true) }
            var overZoom by rememberSaveable { mutableStateOf(false) }
            var fadeOut by rememberSaveable { mutableStateOf(false) }
            var isOverlayVisible by rememberSaveable { mutableStateOf(true) }
            val state = rememberZoomableState(
                minScale = if (overZoom) 0.5f else 1f,
                maxScale = if (overZoom) 6f else 4f,
                overZoomConfig = if (overZoom) OverZoomConfig(1f, 4f) else null
            )
            Box {
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
                    }
                ) {
                    val painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(images[index])
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

                AnimatedVisibility(
                    visible = isOverlayVisible,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                ) {
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colors.surface.copy(alpha = 0.3f))
                            .statusBarsPadding()
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

private val images = arrayOf(
    "https://images.unsplash.com/photo-1623325780558-ef088d1e973b?w=2000&h=2000",
    "https://images.unsplash.com/photo-1623267258448-46f7eaeebbc1?w=2000&h=2000",
    "https://images.unsplash.com/photo-1581291518857-4e27b48ff24e?w=2000&h=2000",
    "https://images.unsplash.com/photo-1623396149135-94380a57f670?w=2000&h=2000"
)
