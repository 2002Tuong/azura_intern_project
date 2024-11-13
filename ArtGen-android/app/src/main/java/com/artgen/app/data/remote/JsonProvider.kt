package com.artgen.app.data.remote

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

object JsonProvider {

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        prettyPrint = true
        isLenient = true
        explicitNulls = false
        ignoreUnknownKeys = true
        useAlternativeNames = false
    }
}
