package com.slideshowmaker.slideshow.ui.my_video

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.models.MyVideoModel
import com.slideshowmaker.slideshow.utils.MediaHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MyVideoViewModel(val context: Context) : ViewModel() {

    val videosState: MutableStateFlow<List<MyVideoModel>>
        get() = _videosState
    private val _videosState = MutableStateFlow(emptyList<MyVideoModel>())

    val loadingState = MutableStateFlow<Boolean>(false)
    fun getMyVideos() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                loadingState.value = true
                val saveVideoFolder =
                    File("/storage/emulated/0/Movies/${context.getString(R.string.folder_name)}")
                val myVideoDataList = ArrayList<MyVideoModel>()
                if (saveVideoFolder.exists() && saveVideoFolder.isDirectory) {
                    for (item in saveVideoFolder.listFiles().orEmpty()) {
                        try {
                            //MediaUtils.getVideoSize(item.absolutePath)
                            val duration = MediaHelper.getVideoDuration(item.absolutePath)
                            if (item.exists()) myVideoDataList.add(
                                MyVideoModel(
                                    item.absolutePath, item.lastModified(), duration
                                )
                            )

                        } catch (e: java.lang.Exception) {

                        }

                    }
                }
                myVideoDataList.sort()
                _videosState.value = myVideoDataList
                loadingState.value = false
            }
        }
    }
}