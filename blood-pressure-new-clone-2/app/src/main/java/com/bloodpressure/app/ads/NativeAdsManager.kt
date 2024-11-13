package com.bloodpressure.app.ads

import android.annotation.SuppressLint
import android.content.Context
import com.bloodpressure.app.R
import com.bloodpressure.app.data.remote.RemoteConfig
import com.bloodpressure.app.tracking.TrackingManager
import com.bloodpressure.app.utils.Logger
import com.bloodpressure.app.utils.Toast
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

class NativeAdsManager @Inject constructor(
    private val context: Context,
    private val remoteConfig: RemoteConfig
) {

    private val _trackerNativeAd: MutableStateFlow<NativeAd?> = MutableStateFlow(null)
    val trackerNativeAd: StateFlow<NativeAd?> = _trackerNativeAd

    private val _onboardingNativeAd: MutableStateFlow<NativeAd?> = MutableStateFlow(null)
    val onboardingNativeAd: StateFlow<NativeAd?> get() = _onboardingNativeAd

    private val _onboardingNativeAd1: MutableStateFlow<NativeAd?> = MutableStateFlow(null)
    val onboardingNativeAd1: StateFlow<NativeAd?> get() = _onboardingNativeAd1

    private val _onboardingNativeAd2: MutableStateFlow<NativeAd?> = MutableStateFlow(null)
    val onboardingNativeAd2: StateFlow<NativeAd?> get() = _onboardingNativeAd2

    private val _onboardingNativeAd3: MutableStateFlow<NativeAd?> = MutableStateFlow(null)
    val onboardingNativeAd3: StateFlow<NativeAd?> get() = _onboardingNativeAd3

    private val _featuresNativeAd: Map<NativeAdPlacement, MutableStateFlow<NativeAd?>> = mapOf(
        NativeAdPlacement.NATIVE_BLOOD_PRESSURE to MutableStateFlow(null),
        NativeAdPlacement.NATIVE_HEART_RATE to MutableStateFlow(null),
        NativeAdPlacement.NATIVE_BLOOD_SUGAR to MutableStateFlow(null),
        NativeAdPlacement.NATIVE_BMI to MutableStateFlow(null),
    )
    val featuresNativeAd: Map<NativeAdPlacement, StateFlow<NativeAd?>> get() = _featuresNativeAd

    private val _historyNativeAd: MutableStateFlow<NativeAd?> = MutableStateFlow(null)
    val historyNativeAd: StateFlow<NativeAd?> get() = _historyNativeAd

    private val _infoNativeAd: MutableStateFlow<NativeAd?> = MutableStateFlow(null)
    val infoNativeAd: StateFlow<NativeAd?> get() = _infoNativeAd

    private val _languageNativeAd: MutableStateFlow<NativeAd?> = MutableStateFlow(null)
    val languageNativeAd: StateFlow<NativeAd?> get() = _languageNativeAd

    private val _languageSettingNativeAd: MutableStateFlow<NativeAd?> = MutableStateFlow(null)
    val languageSettingNativeAd: StateFlow<NativeAd?> get() = _languageSettingNativeAd

    private val _addRecordNativeAd: MutableStateFlow<NativeAd?> = MutableStateFlow(null)
    val addRecordNativeAd: StateFlow<NativeAd?> get() = _addRecordNativeAd

    private val _exitAppNativeAd: MutableStateFlow<NativeAd?> = MutableStateFlow(null)
    val exitAppNativeAd: StateFlow<NativeAd?> get() = _exitAppNativeAd

    private var isProUser: Boolean = false
    private var countLoading = 0
    @SuppressLint("StaticFieldLeak")
    private var adLoader: AdLoader? = null
    private var nativeAdLoader: AdLoader? = null

    fun setProUser(isPro: Boolean) {
        this.isProUser = isPro
    }

    private val loadingStateByAds: MutableMap<NativeAdPlacement, Boolean> = mutableMapOf()
    fun loadHomeNativeAd() {
        Timber.d("loading home native ads")
        if (trackerNativeAd.value != null || loadingStateByAds[NativeAdPlacement.NATIVE_HOME] == true) {
            return
        }
        if (remoteConfig.adsConfig.shouldShowHomeNativeAd) {
            loadingStateByAds[NativeAdPlacement.NATIVE_HOME] = true
            loadAd(
                adUnitResId = R.string.native_home,
                placement = NativeAdPlacement.NATIVE_HOME
            ) {
                if (!isProUser) {
                    _trackerNativeAd.tryEmit(it)
                } else {
                    destroyTrackerNativeAd()
                }
            }
        }
    }

//    fun loadOnboardingNativeAd() {
//        if (onboardingNativeAd.value != null) {
//            return
//        }
//        if (remoteConfig.adsConfig.shouldShowOnboardingNativeAd) {
//            TrackingManager.logStartLoadOnboardingNativeAdEvent()
//            val startTime = System.currentTimeMillis()
//            loadAd(R.string.native_onboarding) {
//                if (!isProUser) {
//                    _onboardingNativeAd.tryEmit(it)
//                } else {
//                    destroyOnboardingNativeAd()
//                }
//                val loadTime = System.currentTimeMillis() - startTime
//                TrackingManager.logLoadOnboardingNativeAdSuccessEvent(loadTime)
//            }
//        }
//    }

    fun loadNativeOnboard() {
        if (isProUser || remoteConfig.offAllAds() || !remoteConfig.adsConfig.shouldShowNativeAd) {
            return
        }
        countLoading = 0
        val videoOptions = VideoOptions.Builder()
            .setStartMuted(true)
            .build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
            .build()
        adLoader = AdLoader.Builder(context, context.getString(R.string.native_onboarding))
            .forNativeAd { nativeAd ->
                nativeAd.setOnPaidEventListener { value ->
                    TrackingManager.logAdRevenue(
                        nativeAd.responseInfo?.mediationAdapterClassName.orEmpty(),
                        value.valueMicros / 1_000_000.0, emptyMap()
                    )
                }
                when (countLoading) {
                    0 -> {
                        Logger.d("loadNativeOnboard: $countLoading")
                        if (!isProUser) {
                            _onboardingNativeAd1.tryEmit(nativeAd)
                        } else {
                            destroyOnboardNativeAd(_onboardingNativeAd1, onboardingNativeAd1)
                        }
                    }

                    1 -> {
                        Logger.d("loadNativeOnboard: $countLoading")
                        if (!isProUser) {
                            _onboardingNativeAd2.tryEmit(nativeAd)
                        } else {
                            destroyOnboardNativeAd(_onboardingNativeAd2, onboardingNativeAd2)
                        }
                    }

                    2 -> {
                        Logger.d("loadNativeOnboard: $countLoading")
                        if (!isProUser) {
                            _onboardingNativeAd3.tryEmit(nativeAd)
                        } else {
                            destroyOnboardNativeAd(_onboardingNativeAd3, onboardingNativeAd3)
                        }
                    }
                }
                if (adLoader?.isLoading == true) countLoading++
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Logger.e(Throwable("onAdFailedToLoad: ${loadAdError.message}"))
                }
            })
            .withNativeAdOptions(adOptions)
            .build()
        adLoader?.loadAds(AdRequest.Builder().build(), 3)
    }

    fun loadFeatureNativeAd() {
        if (
            remoteConfig.adsConfig.shouldShowHearRateNativeAd
            && featuresNativeAd[NativeAdPlacement.NATIVE_HEART_RATE]?.value == null
            && loadingStateByAds[NativeAdPlacement.NATIVE_HEART_RATE] != true
        ) {
            loadingStateByAds[NativeAdPlacement.NATIVE_HEART_RATE] = true
            loadAd(
                adUnitResId = R.string.native_heart_rate,
                placement = NativeAdPlacement.NATIVE_HEART_RATE,
            ) {
                if (!isProUser) {
                    _featuresNativeAd[NativeAdPlacement.NATIVE_HEART_RATE]?.tryEmit(it)
                } else {
                    featuresNativeAd[NativeAdPlacement.NATIVE_HEART_RATE]?.value?.destroy()
                    _featuresNativeAd[NativeAdPlacement.NATIVE_HEART_RATE]?.tryEmit(null)
                }
            }
        }

        if (remoteConfig.adsConfig.shouldShowPressureNativeAd && featuresNativeAd[NativeAdPlacement.NATIVE_BLOOD_PRESSURE]?.value == null
            && loadingStateByAds[NativeAdPlacement.NATIVE_BLOOD_PRESSURE] != true) {
            loadingStateByAds[NativeAdPlacement.NATIVE_BLOOD_PRESSURE] = true
            loadAd(
                adUnitResId = R.string.native_blood_pressure,
                placement = NativeAdPlacement.NATIVE_BLOOD_PRESSURE,
            ) {
                if (!isProUser) {
                    _featuresNativeAd[NativeAdPlacement.NATIVE_BLOOD_PRESSURE]?.tryEmit(it)
                } else {
                    featuresNativeAd[NativeAdPlacement.NATIVE_BLOOD_PRESSURE]?.value?.destroy()
                    _featuresNativeAd[NativeAdPlacement.NATIVE_BLOOD_PRESSURE]?.tryEmit(null)
                }

            }
        }

        if (remoteConfig.adsConfig.shouldShowBmiNativeAd && featuresNativeAd[NativeAdPlacement.NATIVE_BMI]?.value == null
            && loadingStateByAds[NativeAdPlacement.NATIVE_BMI] != true) {
            loadingStateByAds[NativeAdPlacement.NATIVE_BMI] = true
            loadAd(
                adUnitResId = R.string.native_bmi,
                placement = NativeAdPlacement.NATIVE_BMI,
            ) {
                if (!isProUser) {
                    _featuresNativeAd[NativeAdPlacement.NATIVE_BMI]?.tryEmit(it)
                } else {
                    featuresNativeAd[NativeAdPlacement.NATIVE_BMI]?.value?.destroy()
                    _featuresNativeAd[NativeAdPlacement.NATIVE_BMI]?.tryEmit(null)
                }

            }
        }

        if (remoteConfig.adsConfig.shouldShowBloodSugarNativeAd && featuresNativeAd[NativeAdPlacement.NATIVE_BLOOD_SUGAR]?.value == null
            && loadingStateByAds[NativeAdPlacement.NATIVE_BLOOD_SUGAR] != true) {
            loadingStateByAds[NativeAdPlacement.NATIVE_BLOOD_SUGAR] = true
            loadAd(
                adUnitResId = R.string.native_blood_sugar,
                placement = NativeAdPlacement.NATIVE_BLOOD_SUGAR,
            ) {
                if (!isProUser) {
                    _featuresNativeAd[NativeAdPlacement.NATIVE_BLOOD_SUGAR]?.tryEmit(it)
                } else {
                    featuresNativeAd[NativeAdPlacement.NATIVE_BLOOD_SUGAR]?.value?.destroy()
                    _featuresNativeAd[NativeAdPlacement.NATIVE_BLOOD_SUGAR]?.tryEmit(null)
                }

            }
        }
    }

    fun loadHistoryNativeAd() {
        if (historyNativeAd.value != null || loadingStateByAds[NativeAdPlacement.NATIVE_HISTORY] == true) {
            return
        }
        if (remoteConfig.adsConfig.shouldShowHistoryNativeAd) {
            loadingStateByAds[NativeAdPlacement.NATIVE_HISTORY] = true
            loadAd(R.string.native_history, NativeAdPlacement.NATIVE_HISTORY) {
                if (!isProUser) {
                    _historyNativeAd.tryEmit(it)
                } else {
                    destroyHistoryNativeAd()
                }
            }
        }
    }

    fun loadInfoNativeAd() {
        if (infoNativeAd.value != null || loadingStateByAds[NativeAdPlacement.NATIVE_INFO] == true) {
            return
        }
        if (remoteConfig.adsConfig.shouldShowInfoNativeAd) {
            loadingStateByAds[NativeAdPlacement.NATIVE_INFO] = true
            loadAd(R.string.native_info, NativeAdPlacement.NATIVE_INFO) {
                if (!isProUser) {
                    _infoNativeAd.tryEmit(it)
                } else {
                    destroyInfoNativeAd()
                }
            }
        }
    }

    fun loadLanguageNativeAd() {
        if (languageNativeAd.value != null || loadingStateByAds[NativeAdPlacement.NATIVE_LANGUAGE] == true) {
            return
        }

        if (remoteConfig.adsConfig.shouldShowLanguageNativeAd) {
            loadingStateByAds[NativeAdPlacement.NATIVE_LANGUAGE] = true
            TrackingManager.logStartLoadLanguageNativeAdEvent()
            val startTime = System.currentTimeMillis()
            loadAd(
                R.string.native_language,
                NativeAdPlacement.NATIVE_LANGUAGE
            ) {
                if (!isProUser) {
                    _languageNativeAd.tryEmit(it)
                } else {
                    destroyLanguageNativeAd()
                }
                val loadTime = System.currentTimeMillis() - startTime
                TrackingManager.logLoadLanguageNativeAdSuccessEvent(loadTime)
            }
        }
    }

    fun loadLanguageSettingNativeAd() {
        if (languageSettingNativeAd.value != null || loadingStateByAds[NativeAdPlacement.NATIVE_LANGUAGE_SETTING] == true) {
            return
        }
        if (remoteConfig.adsConfig.shouldShowLanguageSettingNativeAd) {
            loadingStateByAds[NativeAdPlacement.NATIVE_LANGUAGE_SETTING] = true
            loadAd(R.string.native_language_setting, NativeAdPlacement.NATIVE_LANGUAGE_SETTING) {
                if (!isProUser) {
                    _languageSettingNativeAd.tryEmit(it)
                } else {
                    destroyLanguageSettingNativeAd()
                }
            }
        }
    }

    fun loadAddRecordNativeAd() {
        if (addRecordNativeAd.value != null || loadingStateByAds[NativeAdPlacement.NATIVE_EDIT] == true) {
            return
        }
        if (remoteConfig.adsConfig.shouldShowAddRecordNativeAd) {
            loadingStateByAds[NativeAdPlacement.NATIVE_EDIT] = true
            loadAd(R.string.native_edit, NativeAdPlacement.NATIVE_EDIT) {
                if (!isProUser) {
                    _addRecordNativeAd.tryEmit(it)
                } else {
                    destroyAddRecordNativeAd()
                }
            }
        }
    }

    fun loadExitAppNativeAd() {
        if (exitAppNativeAd.value != null || loadingStateByAds[NativeAdPlacement.NATIVE_EXIT_APP] == true) {
            return
        }
        if (remoteConfig.adsConfig.shouldShowExitAppNativeAd) {
            loadingStateByAds[NativeAdPlacement.NATIVE_EXIT_APP] = true
            loadAd(R.string.native_exit_app, NativeAdPlacement.NATIVE_EXIT_APP) {
                if (!isProUser) {
                    _exitAppNativeAd.tryEmit(it)
                } else {
                    destroyExitAppNativeAd()
                }
            }
        }
    }

    private fun loadAd(
        adUnitResId: Int,
        placement: NativeAdPlacement,
        onCancel: () -> Unit = {},
        onSuccess: (NativeAd) -> Unit
    ) {
        if (isProUser || remoteConfig.offAllAds() || !remoteConfig.adsConfig.shouldShowNativeAd) {
            onCancel.invoke()
            return
        }
        val unitId = context.getString(adUnitResId)
        val videoOptions = VideoOptions.Builder().setStartMuted(true).build()
        nativeAdLoader = AdLoader.Builder(context, unitId)
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setVideoOptions(videoOptions)
                    .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_LANDSCAPE)
                    .build()
            )
            .withAdListener(
                object : AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        loadingStateByAds[placement] = false
                        Toast.show(context, "Native loaded")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        loadingStateByAds[placement] = false
                        Logger.e(Throwable("onAdFailedToLoad: ${loadAdError.message}"))
                        Toast.show(context, "Native loaded fail")
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        nativeAdLoader?.loadAd(AdRequest.Builder().build())
                    }
                }
            )
            .forNativeAd {
                loadingStateByAds[placement] = false
                it.setOnPaidEventListener { value ->
                    TrackingManager.logAdRevenue(
                        it.responseInfo?.mediationAdapterClassName.orEmpty(),
                        value.valueMicros / 1_000_000.0, emptyMap()
                    )
                }
                onSuccess(it)
                Logger.d("forNativeAd $it - $unitId")
            }
            .build()
        nativeAdLoader?.loadAd(AdRequest.Builder().build())
    }

    fun onDestroy() {
        destroyTrackerNativeAd()
        destroyOnboardNativeAd(_onboardingNativeAd1, onboardingNativeAd1)
        destroyOnboardNativeAd(_onboardingNativeAd2, onboardingNativeAd2)
        destroyOnboardNativeAd(_onboardingNativeAd3, onboardingNativeAd3)
        destroyHistoryNativeAd()
        destroyInfoNativeAd()
        destroyLanguageNativeAd()
        destroyLanguageSettingNativeAd()
        destroyAddRecordNativeAd()
        destroyExitAppNativeAd()
        destroyFeatureNativeAd()
    }

    private fun destroyTrackerNativeAd() {
        trackerNativeAd.value?.destroy()
        _trackerNativeAd.tryEmit(null)
    }

    private fun destroyOnboardingNativeAd() {
        onboardingNativeAd.value?.destroy()
        _onboardingNativeAd.tryEmit(null)
    }

    private fun destroyOnboardNativeAd(
        localOnboardingNativeAd: MutableStateFlow<NativeAd?>,
        onboardingNativeAd: StateFlow<NativeAd?>
    ) {
        onboardingNativeAd.value?.destroy()
        localOnboardingNativeAd.tryEmit(null)
    }

    private fun destroyHistoryNativeAd() {
        historyNativeAd.value?.destroy()
        _historyNativeAd.tryEmit(null)
    }

    private fun destroyInfoNativeAd() {
        infoNativeAd.value?.destroy()
        _infoNativeAd.tryEmit(null)
    }

    private fun destroyLanguageNativeAd() {
        languageNativeAd.value?.destroy()
        _languageNativeAd.tryEmit(null)
    }

    private fun destroyLanguageSettingNativeAd() {
        languageSettingNativeAd.value?.destroy()
        _languageSettingNativeAd.tryEmit(null)
    }

    private fun destroyAddRecordNativeAd() {
        addRecordNativeAd.value?.destroy()
        _addRecordNativeAd.tryEmit(null)
    }

    private fun destroyExitAppNativeAd() {
        exitAppNativeAd.value?.destroy()
        _exitAppNativeAd.tryEmit(null)
    }

    private fun destroyFeatureNativeAd() {
        _featuresNativeAd.values.forEach {
            it.value?.destroy()
            it.tryEmit(null)
        }
    }
}
enum class NativeAdPlacement {
    NATIVE_LANGUAGE,
    NATIVE_HOME,
    NATIVE_HEART_RATE,
    NATIVE_BLOOD_PRESSURE,
    NATIVE_BMI,
    NATIVE_BLOOD_SUGAR,
    NATIVE_HISTORY,
    NATIVE_INFO,
    NATIVE_EDIT,
    NATIVE_EXIT_APP,
    NATIVE_LANGUAGE_SETTING
}
