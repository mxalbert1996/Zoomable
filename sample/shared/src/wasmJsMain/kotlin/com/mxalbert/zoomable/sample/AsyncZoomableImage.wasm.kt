package com.mxalbert.zoomable.sample

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

internal actual val httpClient = HttpClient(Js)
