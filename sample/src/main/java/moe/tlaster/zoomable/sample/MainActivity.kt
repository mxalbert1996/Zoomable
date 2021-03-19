package moe.tlaster.zoomable.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import moe.tlaster.zoomable.Zoomable
import moe.tlaster.zoomable.rememberZoomableState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Sample()
        }
    }
}

@Composable
private fun Sample() {
    val state = rememberZoomableState(
        minScale = 2f
    )
    Zoomable(state = state) {
        Text(text = "Zoom me!")
    }
}
