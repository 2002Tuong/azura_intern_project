package com.wifi.wificharger.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Language(
    val flagResId: Int,
    val languageName: String = "",
    var languageCode: String,
    var languageNameId: Int
) : Parcelable
