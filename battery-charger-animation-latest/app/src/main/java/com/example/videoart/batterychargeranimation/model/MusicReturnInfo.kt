package com.example.videoart.batterychargeranimation.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class MusicReturnInfo(
    val audioFilePath: String,
    var outFilePath: String = "",
    val startOffset: Int,
    val length: Int,
    val fileId: String = "",
    val fileName: String = ""
) : Serializable