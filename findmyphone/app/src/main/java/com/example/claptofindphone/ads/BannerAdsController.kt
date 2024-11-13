package com.example.claptofindphone.ads

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.lifecycle.MutableLiveData
import com.ads.control.admob.AppOpenManager
import com.example.claptofindphone.AdsUnitId
import com.example.claptofindphone.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

class BannerAdsController(
    private val context: Context
) {
    val bannerAdsHowToUse: MutableLiveData<AdView?> = MutableLiveData()
    //val bannerHowToUseLoading: MutableLiveData<Boolean> = MutableLiveData()

    val bannerAdsSelectSound: MutableLiveData<AdView?> = MutableLiveData()
    //val bannerSelectSoundLoading: MutableLiveData<Boolean> = MutableLiveData()

    val bannerAdsFindPhone: MutableLiveData<AdView?> = MutableLiveData()
    //val bannerFindPhoneLoading: MutableLiveData<AdView?> = MutableLiveData()

    val bannerAdsSetting: MutableLiveData<AdView?> = MutableLiveData()
    //val bannerSettingLoading: MutableLiveData<AdView?> = MutableLiveData()

    val bannerAdsLanguage: MutableLiveData<AdView?> = MutableLiveData()

    val bannerAdsOnboard: MutableLiveData<AdView?> = MutableLiveData()

    val bannerAdsPermission: MutableLiveData<AdView?> = MutableLiveData()

    val bannerAdsSoundActive: MutableLiveData<AdView?> = MutableLiveData()

    fun loadBanner(placement: BannerPlacement, reload: Boolean = false) {
        val placementOldAdView = when(placement) {
            BannerPlacement.HOW_TO_USE -> bannerAdsHowToUse
            BannerPlacement.SELECT_SOUND -> bannerAdsSelectSound
            BannerPlacement.FIND_PHONE -> bannerAdsFindPhone
            BannerPlacement.SETTING -> bannerAdsSetting
            BannerPlacement.LANGUAGE ->bannerAdsLanguage
            BannerPlacement.ONBOARDING -> bannerAdsOnboard
            BannerPlacement.PERMISSION -> bannerAdsPermission
            BannerPlacement.ACTIVE_SOUND -> bannerAdsSoundActive
        }

        if(placementOldAdView.value != null && reload) {
            placementOldAdView.value!!.loadAd(AdRequest.Builder().build())
            return
        }

        if(placementOldAdView.value == null && !reload) {
            val newAdView = AdView(context).apply {
                setAdSize(this@BannerAdsController.getAdSize())
                adUnitId = AdsUnitId.banner
                loadAd(AdRequest.Builder().build())
                id = ViewCompat.generateViewId()
                adListener = object  : AdListener() {
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        if (BuildConfig.FLAVOR == "dev") {
                            Toast.makeText(
                                context,
                                "Banner ads ${placement.name} has been loaded",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        Log.d("banner", "onAdLoaded ${placement.name}")
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        loadBanner(placement, true)
                        //AppOpenManager.getInstance().disableAppResume()
                    }
                }
            }

            when(placement) {
                BannerPlacement.HOW_TO_USE -> bannerAdsHowToUse
                BannerPlacement.SELECT_SOUND -> bannerAdsSelectSound
                BannerPlacement.FIND_PHONE -> bannerAdsFindPhone
                BannerPlacement.SETTING -> bannerAdsSetting
                BannerPlacement.LANGUAGE -> bannerAdsLanguage
                BannerPlacement.ONBOARDING -> bannerAdsOnboard
                BannerPlacement.PERMISSION -> bannerAdsPermission
                BannerPlacement.ACTIVE_SOUND -> bannerAdsSoundActive
            }.postValue(newAdView)
        }
    }


    private fun getAdSize(): AdSize {
        val displayInfo = context.resources.displayMetrics
        val widthOfAd = displayInfo.widthPixels / displayInfo.density
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
            context, widthOfAd.toInt()
        )
    }

    enum class BannerPlacement {
        HOW_TO_USE, SELECT_SOUND, FIND_PHONE, SETTING, LANGUAGE, ONBOARDING,PERMISSION, ACTIVE_SOUND
    }
}