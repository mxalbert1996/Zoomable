package com.mxalbert.zoomable.sample

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image

private val imageCache: MutableMap<String, ImageBitmap> = HashMap()

internal expect val httpClient: HttpClient

private suspend fun loadImage(url: String): ImageBitmap? {
    imageCache[url]?.let { return it }
    return withContext(Dispatchers.Default) {
        runCatching {
            Image.makeFromEncoded(httpClient.get(url).readBytes()).toComposeImageBitmap()
        }.getOrNull()?.also { imageCache[url] = it }
    }
}

@Composable
internal fun AsyncZoomableImage(url: String) {
    val painter by produceState<Painter?>(null, url) {
        value = null
        loadImage(url)?.let { value = BitmapPainter(it) }
    }
    painter?.let {
        val size = it.intrinsicSize
        Image(
            painter = it,
            contentDescription = null,
            modifier = Modifier
                .aspectRatio(size.width / size.height)
                .fillMaxSize()
        )
    }
}
