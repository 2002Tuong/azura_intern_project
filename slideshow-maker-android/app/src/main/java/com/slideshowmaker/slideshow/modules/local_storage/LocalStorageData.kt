package com.slideshowmaker.slideshow.modules.local_storage

import androidx.lifecycle.MutableLiveData
import com.slideshowmaker.slideshow.data.models.AudioInfo
import com.slideshowmaker.slideshow.data.models.MediaInfo
import com.slideshowmaker.slideshow.data.models.enum.MediaType

interface LocalStorageData {
    val audioInfoResponse:MutableLiveData<ArrayList<AudioInfo>>
    val mediaDataResponse:MutableLiveData<ArrayList<MediaInfo>>

    fun getAllAudio()
    fun getAllMedia(mediaKind: MediaType)
}