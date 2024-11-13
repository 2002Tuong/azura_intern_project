package com.example.videoart.batterychargeranimation.model

import androidx.annotation.Keep

@Keep
class LanguageModel {
    var code: String
    var name: Int
    var idResIcon = 0
    var isChoose: Boolean

    constructor(code: String, name: Int, isChoose: Boolean) {
        this.code = code
        this.name = name
        this.isChoose = isChoose
    }

    constructor(code: String, name: Int, idIcon: Int, isChoose: Boolean) {
        this.code = code
        this.name = name
        this.idResIcon = idIcon
        this.isChoose = isChoose
    }
}