package com.screentheme.app.models


import androidx.annotation.Keep

@Keep
data class CallContactModel(
    var name: String,
    var photoUri: String,
    var number: String,
    var numberLabel: String
)
