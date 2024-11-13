package com.slideshowmaker.slideshow.ui.select_music

import android.media.MediaMetadataRetriever
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.models.MusicReturnInfo
import com.slideshowmaker.slideshow.data.remote.DownloadState
import com.slideshowmaker.slideshow.data.remote.VideoMakerServerInterface
import com.slideshowmaker.slideshow.data.remote.saveFile
import com.slideshowmaker.slideshow.ui.music_picker.MusicPickerViewModel
import com.slideshowmaker.slideshow.utils.FileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class SelectMusicViewModel(private val resClient: VideoMakerServerInterface) : ViewModel() {

    val _downloadMusicsState = MutableStateFlow(emptyList<MusicPickerViewModel.DownloadMusic>())
    val _loadingState = MutableStateFlow<LoadingState>(LoadingState.None)
    val _downloadingProgressState = MutableStateFlow<Int?>(null)
    val errorMessageState = MutableStateFlow<Pair<Int, Int>?>(null)

    val downloadedMusic = MutableStateFlow(emptyList<MusicPickerViewModel.DownloadMusic>())
    var hasDownloadedFinished = false

    init {
        getDownloadedMusics()
    }

    fun getDownloadedMusics() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                downloadedMusic.value = FileHelper.listDownloadedAudio()
                    .map {
                        MusicPickerViewModel.DownloadMusic(
                            it.first,
                            it.second,
                            100,
                            true,
                            false,
                            it.third
                        )
                    }
            }
        }
    }

    fun downloadMusic(fileUrl: String, fileId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                Timber.d("Downloading ${fileUrl} ${fileId}")
                val curDownloadAudioList = _downloadMusicsState.value
                if (curDownloadAudioList.none { it.id == fileUrl }) {
                    val filePath = FileHelper.createTempAudioFile(fileId)
                    try {
                        resClient.downloadLargeFile(fileUrl)
                            .saveFile(filePath)
                            .collect { state ->
                                val currentValue = _downloadMusicsState.value.toMutableList()
                                currentValue.removeAll { it.id == fileId }
                                when (state) {
                                    is DownloadState.Downloading -> {
                                        if (_loadingState.value != LoadingState.Loading) {
                                            _loadingState.value = LoadingState.Loading
                                        }
                                        Timber.d("Downloading ${state.progress}")
                                        _downloadingProgressState.value = state.progress
                                        val audioItem = MusicPickerViewModel.DownloadMusic(
                                            fileId,
                                            filePath,
                                            state.progress,
                                            false,
                                            true
                                        )
                                        currentValue.add(audioItem)

                                    }

                                    is DownloadState.Finished -> {
                                        _downloadingProgressState.value = null
                                        _loadingState.value = LoadingState.Completed
                                        Timber.d("Downloaded")
                                        val mediaMetadataRetriever = MediaMetadataRetriever()
                                        mediaMetadataRetriever.setDataSource(filePath)
                                        val durationString =
                                            mediaMetadataRetriever.extractMetadata(
                                                MediaMetadataRetriever.METADATA_KEY_DURATION
                                            )
                                                ?: "0"
                                        val audioItem = MusicPickerViewModel.DownloadMusic(
                                            fileId,
                                            filePath,
                                            100,
                                            true,
                                            false,
                                            (durationString.toLong() / 1000).toInt()
                                        )
                                        currentValue.add(audioItem)
                                        hasDownloadedFinished = true
                                    }

                                    is DownloadState.Failed -> {
                                        _downloadingProgressState.value = null
                                        _loadingState.value = LoadingState.None
                                        Timber.e("Failed to download", state.error)
                                        val audioItem = MusicPickerViewModel.DownloadMusic(
                                            fileId.orEmpty(),
                                            filePath,
                                            0,
                                            false,
                                            false
                                        )
                                        currentValue.add(audioItem)
                                        errorMessageState.value = Pair(
                                            R.string.popup_title_unable_to_download,
                                            R.string.popup_message_unable_to_download
                                        )
                                    }
                                }
                                currentValue.removeAll { music -> downloadedMusic.value.any { it.id == music.id } }
                                currentValue.addAll(downloadedMusic.value)
                                _downloadMusicsState.value = currentValue
                            }
                    } catch (exception: Exception) {
                        errorMessageState.value = Pair(
                            R.string.popup_title_unable_to_download,
                            R.string.popup_message_unable_to_download
                        )
                        FileHelper.deleteFile(arrayListOf(filePath))
                    }

                }

            }
        }
    }

    fun clearError() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                errorMessageState.value = null
            }
        }
    }

    fun addSelectedMusic(musicReturnData: MusicReturnInfo) {
        val newAudioList = downloadedMusic.value.toMutableList()
        newAudioList.add(
            0, MusicPickerViewModel.DownloadMusic(
                musicReturnData.fileId,
                musicReturnData.outFilePath,
                100,
                true,
                false,
                musicReturnData.length,
            )
        )
        downloadedMusic.value = newAudioList.distinctBy { it.id }
    }

    sealed class LoadingState {
        object Loading : LoadingState()
        object Completed : LoadingState()
        object None : LoadingState()
    }
}