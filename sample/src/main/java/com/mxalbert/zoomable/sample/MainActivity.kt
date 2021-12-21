package com.mxalbert.zoomable.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import coil.size.OriginalSize
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.mxalbert.zoomable.Zoomable
import com.mxalbert.zoomable.rememberZoomableState
import kotlinx.coroutines.launch
import kotlin.math.abs

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

@OptIn(ExperimentalPagerApi::class, ExperimentalCoilApi::class)
@Composable
private fun Sample(onDismiss: () -> Unit) {
    HorizontalPager(count = images.size) { index ->
        val state = rememberZoomableState()
        var enabled by remember { mutableStateOf(true) }
        Box {
            Zoomable(
                state = state,
                enabled = enabled,
                dismissGestureEnabled = true,
                onDismiss = {
                    onDismiss()
                    false
                }
            ) {
                val painter = rememberImagePainter(data = images[index]) { size(OriginalSize) }
                if (painter.state is ImagePainter.State.Success) {
                    val size = painter.intrinsicSize
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .aspectRatio(size.width / size.height)
                            .fillMaxSize()
                            .graphicsLayer { alpha = 1 - abs(state.dismissDragOffset) }
                    )
                }
            }
            val scope = rememberCoroutineScope()
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = enabled, onCheckedChange = { enabled = it })
                Text(text = "Enable")
                Spacer(modifier = Modifier.weight(1f))

                Text(text = abs(state.dismissDragOffset).toString())
                Spacer(modifier = Modifier.weight(1f))

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

private val images = arrayOf(
    "https://images.unsplash.com/photo-1623325780558-ef088d1e973b?w=2000&h=2000",
    "https://images.unsplash.com/photo-1623267258448-46f7eaeebbc1?w=2000&h=2000",
    "https://images.unsplash.com/photo-1581291518857-4e27b48ff24e?w=2000&h=2000",
    "https://images.unsplash.com/photo-1623396149135-94380a57f670?w=2000&h=2000"
)
