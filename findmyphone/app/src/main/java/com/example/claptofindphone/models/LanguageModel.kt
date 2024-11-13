package com.example.claptofindphone.models

import androidx.annotation.Keep

@Keep
class LanguageModel {
    var languageCode: String
    var languageName: Int
    var idResIcon = 0
    var isChosen: Boolean

    constructor(code: String, name: Int, isChoose: Boolean) {
        this.languageCode = code
        this.languageName = name
        this.isChosen = isChoose
    }

    constructor(code: String, name: Int, idIcon: Int, isChoose: Boolean) {
        this.languageCode = code
        this.languageName = name
        this.idResIcon = idIcon
        this.isChosen = isChoose
    }
}
