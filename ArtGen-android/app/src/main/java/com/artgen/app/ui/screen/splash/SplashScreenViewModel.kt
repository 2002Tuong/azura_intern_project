package com.artgen.app.ui.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artgen.app.ads.AdsInitializer
import com.artgen.app.ads.NativeAdsManager
import com.artgen.app.ads.OpenAdsManager
import com.artgen.app.data.local.AppDataStore
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.data.remote.repository.ArtGenRepository
import com.artgen.app.tracking.TrackingManager
import com.artgen.app.ui.screen.navigation.MainRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class SplashScreenViewModel(
    private val remoteConfig: RemoteConfig,
    private val dataStore: AppDataStore,
    private val adsInitializer: AdsInitializer,
    private val openAdsManager: OpenAdsManager,
    private val nativeAdsManager: NativeAdsManager,
    private val repository: ArtGenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> get() = _uiState

    init {
        loadData()
        viewModelScope.launch {
            dataStore.isPurchasedFlow
                .distinctUntilChanged()
                .collectLatest { isPurchased ->
                    _uiState.update { it.copy(isPurchased = isPurchased) }
                }
        }
//        viewModelScope.launch {
//            repository.clearTempData()
//        }
        TrackingManager.logSplashScreenLaunch()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            withTimeoutOrNull(10_000) { remoteConfig.waitRemoteConfigLoaded() }
            if (isAdsAvailable()) {
                withTimeoutOrNull(30_000L) { adsInitializer.waitUntilAdsInitialized() }
                val succeed = loadOpenInterAd()
                if (succeed) {
                    _uiState.update { it.copy(isLoading = false, isLoadingAds = true) }
                    delay(800L)
                } else {
                    _uiState.update { it.copy(isLoading = false, isLoadingAds = false) }
                }
                showOpenInterAd()
                if (dataStore.isLanguageSelected){
                    nativeAdsManager.loadStylePickerNativeAd(false)
                }
            }
            _uiState.update {
                it.copy(
                    nextScreenRoute = if (dataStore.isLanguageSelected) {
                        MainRoute.STYLE_PICKER
                    } else {
                        MainRoute.LANGUAGE_SELECTION
                    }
                )
            }
        }
    }

    private suspend fun loadOpenInterAd(): Boolean {
        val startTime = System.currentTimeMillis()
        val succeed = withTimeoutOrNull(20_000) {
            openAdsManager.loadInterSplashAd()
        } == true
        val loadTime = System.currentTimeMillis() - startTime
        if (loadTime < MIN_LOAD_TIME) {
            delay(MIN_LOAD_TIME - loadTime)
        }
        return succeed
    }

    private fun showOpenInterAd() {
        viewModelScope.launch {
            openAdsManager.showInterSplashAdIfAvailable()
        }
    }

    private fun isAdsAvailable(): Boolean {
        return !dataStore.isPurchased && !remoteConfig.offAllAds()
    }

    data class UiState(
        val isLoading: Boolean = false,
        val isLoadingAds: Boolean = false,
        val nextScreenRoute: String? = null,
        val isPurchased: Boolean = false
    )

    companion object {
        private const val MIN_LOAD_TIME = 3000L
    }
}
