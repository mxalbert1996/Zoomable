package com.mxalbert.zoomable.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.*
import androidx.compose.runtime.*
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
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
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
                ProvideWindowInsets {
                    Surface(color = MaterialTheme.colors.background) {
                        Sample(onDismiss = {
                            Toast.makeText(this, "Dismiss", Toast.LENGTH_SHORT).show()
                        })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun Sample(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalPager(count = images.size, modifier = modifier) { index ->
        var enabled by remember { mutableStateOf(true) }
        var overZoom by remember { mutableStateOf(false) }
        var fadeOut by remember { mutableStateOf(false) }
        var isOverlayVisible by remember { mutableStateOf(true) }
        val state = rememberZoomableState(
            minScale = if (overZoom) 0.5f else 1f,
            maxScale = if (overZoom) 6f else 4f,
            overZoomConfig = if (overZoom) OverZoomConfig(1f, 4f) else null
        )
        Box {
            Zoomable(
                modifier = if (!fadeOut) Modifier else
                    Modifier.graphicsLayer { alpha = 1 - state.dismissDragProgress },
                state = state,
                enabled = enabled,
                onTap = { isOverlayVisible = !isOverlayVisible },
                dismissGestureEnabled = true,
                onDismiss = {
                    onDismiss()
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
                val scope = rememberCoroutineScope()
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colors.surface.copy(alpha = 0.3f))
                        .statusBarsPadding()
                        .padding(16.dp),
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
}

@Composable
private fun Checkbox(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.toggleable(
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
