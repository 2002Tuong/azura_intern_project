package com.screentheme.app.models

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class LanguageModel(
    val flagResId: Int,
    val languageName: String = "",
    var languageCode: String,
    var languageNameId: Int
) : Parcelable
