package com.mxalbert.zoomable.sample

import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java

internal actual val httpClient = HttpClient(Java)
