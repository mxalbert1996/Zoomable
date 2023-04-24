package com.mxalbert.zoomable.sample

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

internal actual val httpClient = HttpClient(Darwin)
