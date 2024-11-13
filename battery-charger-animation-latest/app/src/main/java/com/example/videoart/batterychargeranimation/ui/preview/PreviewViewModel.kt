package com.example.videoart.batterychargeranimation.ui.preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.videoart.batterychargeranimation.helper.FileHelper
import com.slideshowmaker.slideshow.data.remote.AnimationServerInterface
import com.slideshowmaker.slideshow.data.remote.DownloadState
import com.slideshowmaker.slideshow.data.remote.saveFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PreviewViewModel(
    val service: AnimationServerInterface
) : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState
    fun downloadSound(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val soundPath = FileHelper.createTempAudioFile("sound")
            service.downloadLargeFile(url)
                .saveFile(soundPath)
                .collectLatest {downState ->
                    if(downState == DownloadState.Finished) {
                        _uiState.update {
                            it.copy(
                                downloadSoundState = DownloadState.Finished,
                                soundPath = soundPath
                            )
                        }
                    } else if(downState == DownloadState.Failed()) {
                        _uiState.update {
                            it.copy(
                                downloadSoundState = DownloadState.Failed(),
                                soundPath = ""
                            )
                        }
                    }
                }
        }
    }

    fun downloadFont(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val fontPath = FileHelper.createTempAudioFile("font")
            service.downloadLargeFile(url)
                .saveFile(fontPath).collectLatest { downState ->
                    if(downState == DownloadState.Finished) {
                        _uiState.update {
                            it.copy(
                                downloadFontState = DownloadState.Finished,
                                fontPath = fontPath
                            )
                        }
                    }else if(downState == DownloadState.Failed()) {
                        _uiState.update {
                            it.copy(
                                downloadFontState = DownloadState.Failed(),
                                fontPath = ""
                            )
                        }
                    }
                }
        }
    }

    fun downloadAnimation(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val animationPath = FileHelper.createTempAudioFile("animation")
            service.downloadLargeFile(url)
                .saveFile(animationPath).collectLatest { downState ->
                    if(downState == DownloadState.Finished) {
                        _uiState.update {
                            it.copy(
                                downloadAnimationState = DownloadState.Finished,
                                animationPath = animationPath
                            )
                        }
                    }else if(downState == DownloadState.Failed()) {
                        _uiState.update {
                            it.copy(
                                downloadAnimationState = DownloadState.Failed(),
                                animationPath = ""
                            )
                        }
                    }
                }
        }
    }

    fun previewHasShow(isShow: Boolean) {
        _uiState.update { it.copy(previewShow = isShow) }
    }

    data class UiState(
        val previewShow: Boolean = false,
        val downloadSoundState: DownloadState = DownloadState.Downloading(0),
        val downloadFontState: DownloadState = DownloadState.Downloading(0),
        val downloadAnimationState: DownloadState = DownloadState.Downloading(0),
        val soundPath: String ="",
        val fontPath: String ="",
        val animationPath: String="",
    )
}