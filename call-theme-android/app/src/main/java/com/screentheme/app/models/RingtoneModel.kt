package com.screentheme.app.models

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class RingtoneModel(
    val title: String, val uri: Uri
) : Parcelable
