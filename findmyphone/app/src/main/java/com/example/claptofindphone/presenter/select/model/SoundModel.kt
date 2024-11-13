package com.example.claptofindphone.presenter.select.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.example.claptofindphone.presenter.select.SelectFragment
import kotlinx.parcelize.Parcelize

@Parcelize
@Keep
data class SoundModel(
    val soundCategory: SelectFragment.SoundCategory,
    val iconRes: Int,
    var soundNameRes: Int
) : Parcelable
