package com.bloodpressure.app.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodpressure.app.ads.AdsInitializer
import com.bloodpressure.app.ads.OpenAdsManager
import com.bloodpressure.app.data.local.AppDataStore
import com.bloodpressure.app.data.remote.RemoteConfig
import com.bloodpressure.app.screen.MainRoute
import com.bloodpressure.app.tracking.TrackingManager
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
    private val openAdsManager: OpenAdsManager
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
        TrackingManager.logSplashScreenLaunch()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isOnboardingCompleted = dataStore.isOnboardingShown
                )
            }
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
            }
            _uiState.update {
                it.copy(nextScreenRoute = getNextRoute())
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
            openAdsManager.showInterSplashAdIfAvailable(isSplash = true)
        }
    }

    private fun isAdsAvailable(): Boolean {
        return !dataStore.isPurchased && !remoteConfig.offAllAds()
    }

    private fun getNextRoute(): String {
        val isLanguageSelected = dataStore.isLanguageSelected
        val isOnboardingShown = dataStore.isOnboardingShown
        return when {
            !isLanguageSelected -> MainRoute.LANGUAGE_SELECTION
            !isOnboardingShown -> MainRoute.ONBOARDING
            else -> MainRoute.HOME
        }
    }

    data class UiState(
        val isLoading: Boolean = false,
        val isLoadingAds: Boolean = false,
        val nextScreenRoute: String? = null,
        val isPurchased: Boolean = false,
        val isOnboardingCompleted: Boolean = false
    )

    companion object {
        private const val MIN_LOAD_TIME = 3000L
    }
}
