package com.screentheme.app.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

object JsonInstance {

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        prettyPrint = true
        isLenient = true
        explicitNulls = false
        ignoreUnknownKeys = true
        useAlternativeNames = false
    }
}
