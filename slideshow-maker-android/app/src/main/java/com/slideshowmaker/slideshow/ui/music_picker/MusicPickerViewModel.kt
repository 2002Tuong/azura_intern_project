package com.slideshowmaker.slideshow.ui.music_picker

import android.media.MediaMetadataRetriever
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.RemoteConfigRepository
import com.slideshowmaker.slideshow.data.remote.DownloadState
import com.slideshowmaker.slideshow.data.remote.VideoMakerServerInterface
import com.slideshowmaker.slideshow.data.remote.saveFile
import com.slideshowmaker.slideshow.data.response.ItemAudio
import com.slideshowmaker.slideshow.utils.FileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MusicPickerViewModel(private val resClient: VideoMakerServerInterface) : ViewModel() {

    val musics: StateFlow<List<ItemAudio>>
        get() = _audioState
    private val _audioState = MutableStateFlow(emptyList<ItemAudio>())
    val _downloadAudioState = MutableStateFlow(emptyList<DownloadMusic>())
    val errorMessagState = MutableStateFlow<Pair<Int, Int>?>(null)

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _audioState.value = RemoteConfigRepository.audioConfigs.orEmpty()
                _downloadAudioState.value = FileHelper.listDownloadedAudio()
                    .map { DownloadMusic(it.first, it.second, 100, true, false, it.third) }
            }
        }
    }

    fun downloadMusic(music: ItemAudio) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Timber.d("Downloading ${music.audioUrl}")
                val curDownloadAudioList = _downloadAudioState.value
                if (curDownloadAudioList.none { it.id == music.id }) {
                    val filePath = FileHelper.createTempAudioFile(music.id.orEmpty())
                    resClient.downloadLargeFile(music.audioUrl.orEmpty())
                        .saveFile(filePath)
                        .collect { state ->
                            val currentValue = _downloadAudioState.value.toMutableList()
                            currentValue.removeAll { it.id == music.id }
                            if (state is DownloadState.Downloading) {
                                Timber.d("Downloading ${state.progress}")
                                val audio = DownloadMusic(
                                    music.id.orEmpty(),
                                    filePath,
                                    state.progress,
                                    false,
                                    true
                                )
                                currentValue.add(audio)

                            } else if (state is DownloadState.Finished) {
                                Timber.d("Downloaded")
                                val mediaMetadataRetriever = MediaMetadataRetriever()
                                mediaMetadataRetriever.setDataSource(filePath)
                                val durationStr =
                                    mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                        ?: "0"
                                val item = DownloadMusic(
                                    music.id.orEmpty(),
                                    filePath,
                                    100,
                                    true,
                                    false,
                                    (durationStr.toLong() / 1000).toInt()
                                )
                                currentValue.add(item)
                            } else if (state is DownloadState.Failed) {
                                Timber.e("Failed to download", state.error)
                                val item = DownloadMusic(
                                    music.id.orEmpty(),
                                    filePath,
                                    0,
                                    false,
                                    false
                                )
                                currentValue.add(item)
                                errorMessagState.value = Pair(
                                    R.string.popup_title_unable_to_download,
                                    R.string.popup_message_unable_to_download
                                )
                            }
                            _downloadAudioState.value = currentValue
                        }
                }

            }
        }
    }

    data class DownloadMusic(
        val id: String,
        val filePath: String,
        val progress: Int,
        val downloaded: Boolean,
        val downloading: Boolean = false,
        val duration: Int = 0
    )

}