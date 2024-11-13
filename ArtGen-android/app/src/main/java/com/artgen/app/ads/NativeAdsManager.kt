package com.artgen.app.ads

import android.content.Context
import com.artgen.app.R
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.log.Logger
import com.artgen.app.tracking.TrackingManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class NativeAdsManager @Inject constructor(
    private val context: Context,
    private val remoteConfig: RemoteConfig,
    private val appOpenAdsManager: OpenAdsManager
) {
    private val _onboardingNativeAd: MutableStateFlow<NativeAdWrapper?> = MutableStateFlow(null)
    val onboardingNativeAd: StateFlow<NativeAdWrapper?> get() = _onboardingNativeAd
    private val _onboardingNativeAd2: MutableStateFlow<NativeAdWrapper?> = MutableStateFlow(null)
    val onboardingNativeAd2: StateFlow<NativeAdWrapper?> get() = _onboardingNativeAd2
    private val _onboardingNativeAd3: MutableStateFlow<NativeAdWrapper?> = MutableStateFlow(null)
    val onboardingNativeAd3: StateFlow<NativeAdWrapper?> get() = _onboardingNativeAd3

    private val _languageNativeAd: MutableStateFlow<NativeAdWrapper?> = MutableStateFlow(null)
    val languageNativeAd: StateFlow<NativeAdWrapper?> get() = _languageNativeAd

    private val _languageSettingNativeAd: MutableStateFlow<NativeAdWrapper?> =
        MutableStateFlow(null)
    val languageSettingNativeAd: StateFlow<NativeAdWrapper?> get() = _languageSettingNativeAd

    private val _stylePickerNativeAd: MutableStateFlow<NativeAdWrapper?> = MutableStateFlow(null)
    val stylePickerNativeAd: StateFlow<NativeAdWrapper?> get() = _stylePickerNativeAd

    private val _imagePickerNativeAd: MutableStateFlow<NativeAdWrapper?> = MutableStateFlow(null)
    val imagePickerNativeAd: StateFlow<NativeAdWrapper?> get() = _imagePickerNativeAd

    private val _allStyleNativeAd: MutableStateFlow<NativeAdWrapper?> = MutableStateFlow(null)
    val allStyleNativeAd: StateFlow<NativeAdWrapper?> get() = _allStyleNativeAd

    private val _allDoneNativeAd: MutableStateFlow<NativeAdWrapper?> = MutableStateFlow(null)
    val allDoneNativeAd: StateFlow<NativeAdWrapper?> get() = _allDoneNativeAd

    private val _resultNativeAd: MutableStateFlow<NativeAdWrapper?> = MutableStateFlow(null)
    val resultNativeAd: StateFlow<NativeAdWrapper?> = _resultNativeAd

    private var isProUser: Boolean = false

    val onboardingNativeAds = arrayOf<NativeAdWrapper?>(null, null, null)

    fun setProUser(isPro: Boolean) {
        this.isProUser = isPro
    }

    fun loadOnboardingNativeAd(page: Int = 0) {
        if (onboardingNativeAds.filterNotNull().size == 3) {
            return
        }

        if(isProUser) {
            destroyOnboardingNativeAd()
            return
        }

        TrackingManager.logStartLoadOnboardingNativeAdEvent()
        val startTime = System.currentTimeMillis()
        if (_onboardingNativeAd.value == null) {
            _onboardingNativeAd.tryEmit(NativeAdWrapper(page, null, LoadState.LOADING))
        } else if (_onboardingNativeAd2.value == null) {
            _onboardingNativeAd2.tryEmit(NativeAdWrapper(page, null, LoadState.LOADING))
        } else if (_onboardingNativeAd3.value == null) {
            _onboardingNativeAd3.tryEmit(NativeAdWrapper(page, null, LoadState.LOADING))

        }
        loadAd(R.string.native_onboarding, {
            if (_onboardingNativeAd3.value?.loadState == LoadState.LOADING) {
                _onboardingNativeAd3.tryEmit(NativeAdWrapper(page, null, LoadState.FAILED))
            } else if (_onboardingNativeAd2.value?.loadState == LoadState.LOADING) {
                _onboardingNativeAd2.tryEmit(NativeAdWrapper(page, null, LoadState.FAILED))
            } else if (_onboardingNativeAd.value?.loadState == LoadState.LOADING) {
                _onboardingNativeAd.tryEmit(NativeAdWrapper(page, null, LoadState.FAILED))

            }
        }) {
            if (!isProUser) {
                if (_onboardingNativeAd.value?.loadState == LoadState.LOADING) {
                    _onboardingNativeAd.tryEmit(NativeAdWrapper(0, it, LoadState.SUCCESS))
                } else if (_onboardingNativeAd2.value?.loadState == LoadState.LOADING) {
                    _onboardingNativeAd2.tryEmit(NativeAdWrapper(1, it, LoadState.SUCCESS))
                } else {
                    _onboardingNativeAd3.tryEmit(NativeAdWrapper(2, it, LoadState.SUCCESS))
                }
            } else {
                destroyOnboardingNativeAd()
            }
            val loadTime = System.currentTimeMillis() - startTime
            TrackingManager.logLoadOnboardingNativeAdSuccessEvent(loadTime)
        }
    }

    fun loadLanguageNativeAd(reload: Boolean = false) {
        if (languageNativeAd.value != null && !reload) {
            return
        }

        if(isProUser) {
            destroyLanguageNativeAd()
            return
        }

        TrackingManager.logStartLoadLanguageNativeAdEvent()
        val startTime = System.currentTimeMillis()
        _languageNativeAd.tryEmit(
            NativeAdWrapper(
                System.currentTimeMillis(),
                null,
                LoadState.LOADING
            )
        )
        loadAd(R.string.native_language, {
            _languageNativeAd.tryEmit(
                NativeAdWrapper(
                    System.currentTimeMillis(),
                    null,
                    LoadState.FAILED
                )
            )
        }) {
            if (!isProUser) {
                _languageNativeAd.tryEmit(
                    NativeAdWrapper(
                        System.currentTimeMillis(),
                        it,
                        LoadState.SUCCESS
                    )
                )
            } else {
                destroyLanguageNativeAd()
            }
            val loadTime = System.currentTimeMillis() - startTime
            TrackingManager.logLoadLanguageNativeAdSuccessEvent(loadTime)
        }
    }

    fun loadLanguageSettingNativeAd() {

        if (remoteConfig.offAllAds() || isProUser) {
            destroyLanguageSettingNativeAd()
            return
        }

        if (languageSettingNativeAd.value != null) {
            return
        }
        _languageSettingNativeAd.tryEmit(
            NativeAdWrapper(
                System.currentTimeMillis(),
                null,
                LoadState.LOADING
            )
        )

        TrackingManager.logStartLoadLanguageSettingNativeAdEvent()
        loadAd(R.string.native_language, {
            _languageSettingNativeAd.tryEmit(
                NativeAdWrapper(
                    System.currentTimeMillis(),
                    null,
                    LoadState.FAILED
                )
            )
        }) {
            if (!isProUser) {
                _languageSettingNativeAd.tryEmit(
                    NativeAdWrapper(
                        System.currentTimeMillis(),
                        it,
                        LoadState.SUCCESS
                    )
                )
            } else {
                destroyLanguageSettingNativeAd()
            }
        }
    }

    fun loadStylePickerNativeAd(reload: Boolean) {
        if ((stylePickerNativeAd.value != null && !reload) || remoteConfig.offAllAds()) {
            return
        }

        if(isProUser) {
            destroyStylePickerNativeAd()
            return
        }

        TrackingManager.logStartLoadStylePickerNativeAdEvent()
        val startTime = System.currentTimeMillis()
        _stylePickerNativeAd.tryEmit(
            NativeAdWrapper(
                System.currentTimeMillis(),
                null,
                LoadState.LOADING
            )
        )
        loadAd(R.string.native_style, {
            _stylePickerNativeAd.tryEmit(
                NativeAdWrapper(
                    System.currentTimeMillis(),
                    null,
                    LoadState.FAILED
                )
            )
        }) {

            if (!isProUser) {
                _stylePickerNativeAd.tryEmit(
                    NativeAdWrapper(
                        System.currentTimeMillis(),
                        it,
                        LoadState.SUCCESS
                    )
                )
            } else {
                destroyStylePickerNativeAd()
            }
            val loadTime = System.currentTimeMillis() - startTime
            TrackingManager.logLoadStylePickerNativeAdSuccessEvent(loadTime)
        }
    }

    fun loadImagePickerNativeAd(reload: Boolean) {
        if (remoteConfig.offAllAds() || isProUser) {
            destroyImagePickerNativeAd()
            return
        }

        if (imagePickerNativeAd.value != null && !reload) {
            return
        }

        TrackingManager.logStartLoadImagePickerNativeAdEvent()
        val startTime = System.currentTimeMillis()
        _imagePickerNativeAd.tryEmit(NativeAdWrapper(System.currentTimeMillis(), null))
        loadAd(R.string.native_select_photo, {
            _imagePickerNativeAd.tryEmit(
                NativeAdWrapper(
                    System.currentTimeMillis(),
                    null,
                    LoadState.FAILED
                )
            )

        }) {

            if (!isProUser) {
                _imagePickerNativeAd.tryEmit(
                    NativeAdWrapper(
                        System.currentTimeMillis(),
                        it,
                        LoadState.SUCCESS
                    )
                )
            } else {
                destroyImagePickerNativeAd()
            }
            val loadTime = System.currentTimeMillis() - startTime
            TrackingManager.logLoadImagePickerNativeAdSuccessEvent(loadTime)
        }
    }

    fun loadAllStyleNativeAd(reload: Boolean) {

        if (remoteConfig.offAllAds() || isProUser) {
            destroyAllStyleNativeAd()
            return
        }

        if (allStyleNativeAd.value != null && !reload) {
            return
        }

        TrackingManager.logStartLoadAllStyleNativeAdEvent()
        val startTime = System.currentTimeMillis()
        _allStyleNativeAd.tryEmit(NativeAdWrapper(startTime, null, LoadState.LOADING))
        loadAd(R.string.native_all_styles, {
            _allStyleNativeAd.tryEmit(NativeAdWrapper(startTime, null, LoadState.FAILED))

        }) {

            if (!isProUser) {
                _allStyleNativeAd.tryEmit(
                    NativeAdWrapper(
                        System.currentTimeMillis(),
                        it,
                        LoadState.SUCCESS
                    )
                )
            } else {
                destroyAllStyleNativeAd()
            }
            val loadTime = System.currentTimeMillis() - startTime
            TrackingManager.logLoadAllStyleNativeAdSuccessEvent(loadTime)
        }
    }

    fun loadAllDoneNativeAd(reload: Boolean = false) {

        if (remoteConfig.offAllAds() || isProUser) {
            destroyAllDoneNativeAd()
            return
        }

        if (allDoneNativeAd.value != null && !reload) {
            return
        }

        TrackingManager.logStartLoadAllDoneNativeAdEvent()
        val startTime = System.currentTimeMillis()
        _allDoneNativeAd.tryEmit(NativeAdWrapper(startTime, null, LoadState.LOADING))
        loadAd(R.string.native_all_done, {
            _allDoneNativeAd.tryEmit(NativeAdWrapper(startTime, null, LoadState.FAILED))

        }) {
            if (!isProUser) {
                _allDoneNativeAd.tryEmit(NativeAdWrapper(startTime, it, LoadState.SUCCESS))
            } else {
                destroyAllDoneNativeAd()
            }
            val loadTime = System.currentTimeMillis() - startTime
            TrackingManager.logLoadAllDoneNativeAdSuccessEvent(loadTime)
        }
    }

    fun loadResultNativeAd(reload: Boolean) {

        if (remoteConfig.offAllAds() || isProUser) {
            destroyResultNativeAd()
            return
        }

        if (resultNativeAd.value != null && !reload) {
            return
        }

        TrackingManager.logStartLoadResultNativeAdEvent()
        val startTime = System.currentTimeMillis()
        _resultNativeAd.tryEmit(NativeAdWrapper(startTime, null, LoadState.LOADING))
        loadAd(R.string.native_result, {
            _resultNativeAd.tryEmit(NativeAdWrapper(startTime, null, LoadState.FAILED))
        }) {
            if(!isProUser) {
                _resultNativeAd.tryEmit(NativeAdWrapper(startTime, it, LoadState.SUCCESS))
            } else {
                destroyResultNativeAd()
            }
            val loadTime = System.currentTimeMillis() - startTime
            TrackingManager.logLoadResultNativeAdSuccessEvent(loadTime)
        }
    }

    private fun loadAds(adUnitResId: Int, numOfAds: Int, onSuccess: (NativeAd) -> Unit) {
        if (isProUser || remoteConfig.offAllAds()) {
            return
        }
        val unitId = context.getString(adUnitResId)
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val nativeAdLoader = AdLoader.Builder(context, unitId)
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setVideoOptions(videoOptions)
                    .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
                    .build()
            )
            .withAdListener(
                object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Logger.e(Throwable("onAdFailedToLoad: ${loadAdError.message}"))
                        when (adUnitResId) {
                            R.string.native_language -> TrackingManager.logLoadNativeAdFailed(
                                "load_native_language_ad_failed",
                                loadAdError.code
                            )

                            R.string.native_onboarding -> TrackingManager.logLoadNativeAdFailed(
                                "load_native_onboarding_ad_failed",
                                loadAdError.code
                            )

                            R.string.native_style -> TrackingManager.logLoadNativeAdFailed(
                                "load_native_style_ad_failed",
                                loadAdError.code
                            )

                            R.string.native_all_styles -> TrackingManager.logLoadNativeAdFailed(
                                "load_native_all_styles_ad_failed",
                                loadAdError.code
                            )

                            R.string.native_select_photo -> TrackingManager.logLoadNativeAdFailed(
                                "load_native_photo_ad_failed",
                                loadAdError.code
                            )

                            R.string.native_all_done -> TrackingManager.logLoadNativeAdFailed(
                                "load_native_all_done_ad_failed",
                                loadAdError.code
                            )
                        }
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        when (adUnitResId) {
                            R.string.native_language -> TrackingManager.logNativeAdImpression(
                                "show_native_language_ad_success"
                            )

                            R.string.native_onboarding -> TrackingManager.logNativeAdImpression(
                                "show_native_onboarding_ad_success"
                            )

                            R.string.native_style -> TrackingManager.logNativeAdImpression(
                                "show_native_style_ad_success"
                            )

                            R.string.native_all_styles -> TrackingManager.logNativeAdImpression(
                                "show_native_all_styles_ad_success"
                            )

                            R.string.native_select_photo -> TrackingManager.logNativeAdImpression(
                                "show_native_select_photo_ad_success"
                            )

                            R.string.native_all_done -> TrackingManager.logNativeAdImpression(
                                "show_native_all_done_ad_success"
                            )
                        }
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        appOpenAdsManager.setShouldShowAppOpenAds(false)
                    }
                }
            )
            .forNativeAd {
                onSuccess(it)
                Logger.d("forNativeAd $it - $unitId")
            }
            .build()
        nativeAdLoader.loadAds(AdRequest.Builder().build(), numOfAds)
    }

    private fun loadAd(adUnitResId: Int, onFailed: () -> Unit, onSuccess: (NativeAd) -> Unit) {
        if (isProUser || remoteConfig.offAllAds()) {
            return
        }
        val unitId = context.getString(adUnitResId)
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        val nativeAdLoader = AdLoader.Builder(context, unitId)
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setVideoOptions(videoOptions)
                    .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
                    .build()
            )
            .withAdListener(
                object : AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Logger.e(Throwable("onAdFailedToLoad: ${loadAdError.message}"))
                        onFailed()
                        when (adUnitResId) {
                            R.string.native_language -> TrackingManager.logLoadNativeAdFailed(
                                "load_native_language_ad_failed",
                                loadAdError.code
                            )

                            R.string.native_onboarding -> TrackingManager.logLoadNativeAdFailed(
                                "load_native_onboarding_ad_failed",
                                loadAdError.code
                            )

                            R.string.native_style -> TrackingManager.logLoadNativeAdFailed(
                                "load_native_style_ad_failed",
                                loadAdError.code
                            )

                            R.string.native_all_styles -> TrackingManager.logLoadNativeAdFailed(
                                "load_native_all_styles_ad_failed",
                                loadAdError.code
                            )

                            R.string.native_select_photo -> TrackingManager.logLoadNativeAdFailed(
                                "load_native_photo_ad_failed",
                                loadAdError.code
                            )

                            R.string.native_all_done -> TrackingManager.logLoadNativeAdFailed(
                                "load_native_all_done_ad_failed",
                                loadAdError.code
                            )
                        }
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        when (adUnitResId) {
                            R.string.native_language -> TrackingManager.logNativeAdImpression(
                                "show_native_language_ad_success"
                            )

                            R.string.native_onboarding -> TrackingManager.logNativeAdImpression(
                                "show_native_onboarding_ad_success"
                            )

                            R.string.native_style -> TrackingManager.logNativeAdImpression(
                                "show_native_style_ad_success"
                            )

                            R.string.native_all_styles -> TrackingManager.logNativeAdImpression(
                                "show_native_all_styles_ad_success"
                            )

                            R.string.native_select_photo -> TrackingManager.logNativeAdImpression(
                                "show_native_select_photo_ad_success"
                            )

                            R.string.native_all_done -> TrackingManager.logNativeAdImpression(
                                "show_native_all_done_ad_success"
                            )
                        }
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        if (adUnitResId == R.string.native_language) {
                            loadLanguageNativeAd(reload = true)
                        }
                        appOpenAdsManager.setShouldShowAppOpenAds(false)
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()

                    }
                }
            )
            .forNativeAd {
                onSuccess(it)
                Logger.d("forNativeAd $it - $unitId")
            }
            .build()
        nativeAdLoader.loadAd(AdRequest.Builder().build())
    }

    fun onDestroy() {
        destroyOnboardingNativeAd()
        destroyLanguageNativeAd()
        destroyLanguageSettingNativeAd()
        destroyStylePickerNativeAd()
        destroyImagePickerNativeAd()
        destroyAllStyleNativeAd()
        destroyAllDoneNativeAd()
        destroyResultNativeAd()
    }

    private fun destroyOnboardingNativeAd() {
        onboardingNativeAd.value?.destroy()
        onboardingNativeAds.forEach { it?.destroy() }
        _onboardingNativeAd.tryEmit(null)
    }

    private fun destroyLanguageNativeAd() {
        languageNativeAd.value?.destroy()
        _languageNativeAd.tryEmit(null)
    }

    private fun destroyLanguageSettingNativeAd() {
        languageSettingNativeAd.value?.destroy()
        _languageSettingNativeAd.tryEmit(null)
    }

    fun destroyStylePickerNativeAd() {
        stylePickerNativeAd.value?.destroy()
        _stylePickerNativeAd.tryEmit(null)
    }

    private fun destroyImagePickerNativeAd() {
        imagePickerNativeAd.value?.destroy()
        _imagePickerNativeAd.tryEmit(null)
    }

    private fun destroyAllStyleNativeAd() {
        allStyleNativeAd.value?.destroy()
        _allStyleNativeAd.tryEmit(null)
    }

    private fun destroyAllDoneNativeAd() {
        allDoneNativeAd.value?.destroy()
        _allDoneNativeAd.tryEmit(null)
    }

    private fun destroyResultNativeAd() {
        resultNativeAd.value?.destroy()
        _resultNativeAd.tryEmit(null)
    }

}
