package com.calltheme.app.ui.pickavatar

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewbinding.ViewBinding
import com.ads.control.admob.Admob
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.ui.mydesign.MyThemeAdapter
import com.calltheme.app.utils.AdsUtils
import com.calltheme.app.utils.BannerAdsHelpers
import com.screentheme.app.R
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.databinding.FragmentPickAllAvatarsBinding
import com.screentheme.app.models.AvatarModel
import com.screentheme.app.models.BackgroundModel
import com.screentheme.app.models.CallIconModel
import com.screentheme.app.utils.extensions.FragmentDirections
import org.koin.android.ext.android.inject

class PickAllAvatarsFragment : BaseFragment() {

    companion object {
        const val KEY_AVATARS = 1
        const val KEY_BACKGROUNDS = 2
        const val KEY_CALL_ICONS = 3
    }

    private lateinit var binding: FragmentPickAllAvatarsBinding
    private val pickAllAvatarsViewModel: PickAllAvatarsViewModel by inject()

    private lateinit var adapter: MyThemeAdapter

    private var type = 0
    private var isBannerShowed = false
    private var isDestinationChanged = false
    override fun getViewBinding(): ViewBinding {
        binding = FragmentPickAllAvatarsBinding.inflate(layoutInflater)
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            type = it.getInt("type")
        }
    }

    override fun onViewCreated() {
        adapter = MyThemeAdapter(requireContext())
        myActivity?.let {
            AdsUtils.loadInterChooseAvatar(it)
            AdsUtils.loadInterChooseBackground(it)
            AdsUtils.loadInterChooseIcon(it)
        }


        binding.apply {
            recyclerView.layoutManager = GridLayoutManager(context, 2)
            recyclerView.adapter = adapter

            adapter.setCallBack {
                isDestinationChanged = true
                val args = Bundle()
                args.putParcelable("themeConfig", it)

                val action = FragmentDirections.action(
                    args,
                    R.id.action_navigation_pick_all_avatars_to_navigation_set_call_theme
                )
                try {
                    when (type) {
                        KEY_AVATARS -> {
                            myActivity?.let {
                                AdsUtils.forceShowInterChooseAvatar(it, onNextAction = {
                                    getNavController().navigate(action)
                                })
                            }
                        }
                        KEY_BACKGROUNDS -> {
                            myActivity?.let {
                                AdsUtils.forceShowInterChooseBackground(it, onNextAction = {
                                    getNavController().navigate(action)
                                })
                            }
                        }
                        KEY_CALL_ICONS -> {
                            myActivity?.let {
                                AdsUtils.forceShowInterChooseIcon(it, onNextAction = {
                                    getNavController().navigate(action)
                                })
                            }
                        }
                        else -> getNavController().navigate(action)
                    }


                } catch (exception: IllegalArgumentException) {

                }

            }

            goBackButton.setOnClickListener {
                findNavController().popBackStack()
            }
        }

        myActivity?.let { BannerAdsHelpers.requestLoadBannerAds(BannerAdsHelpers.BannerAdPlacement.CUSTOMIZE, it) }
        requestBannerListener()
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun requestBannerListener() {
        BannerAdsHelpers.customizeBannerAd.value?.let {
            isBannerShowed = true
            //binding.bannerAds.flShimemr.visibility = View.GONE
            Admob.getInstance().populateUnifiedBannerAdView(
                myActivity,
                it,
                binding.bannerAds.bannerContainer
            )
        }
        BannerAdsHelpers.customizeBannerAd.observe(this) { adView ->
            adView?.let {
                isBannerShowed = true
                //binding.bannerAds.flShimemr.visibility = View.GONE
                try {
                    (it.parent as? ViewGroup)?.removeView(it)
                    binding.bannerAds.bannerContainer.removeAllViews()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
                Admob.getInstance().populateUnifiedBannerAdView(
                    myActivity,
                    adView,
                    binding.bannerAds.bannerContainer
                )
            }
        }
        BannerAdsHelpers.bannerCustomizeFailToLoad.observe(this) {
            if (it && !isBannerShowed) {
                binding.frAds.visibility = View.GONE
            }
        }
    }

    override fun onStop() {
        myActivity?.let { BannerAdsHelpers.requestLoadBannerAds(BannerAdsHelpers.BannerAdPlacement.CUSTOMIZE, it) }
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        if (isDestinationChanged) {
            isDestinationChanged = false
        }
    }
    override fun onDestroyView() {
        pickAllAvatarsViewModel.themeConfigListLiveData.postValue(null)
        super.onDestroyView()
    }

    override fun registerObservers() {
        val remoteConfig = AppRemoteConfig.callThemeConfigs()

        when (type) {
            KEY_AVATARS -> {
                binding.navTitle.text = getString(R.string.choose_avatar)
                val avatars = themeManager.getResources(
                    remoteConfig,
                    AvatarModel::class.java
                ) as ArrayList<AvatarModel>
                pickAllAvatarsViewModel.createThemeConfigListFromAvatars(avatars)
            }

            KEY_BACKGROUNDS -> {
                binding.navTitle.text = getString(R.string.choose_background)
                val backgrounds = themeManager.getResources(
                    remoteConfig,
                    BackgroundModel::class.java
                ) as ArrayList<BackgroundModel>
                pickAllAvatarsViewModel.createThemeConfigListFromBackgrounds(backgrounds)
            }

            KEY_CALL_ICONS -> {
                binding.navTitle.text = getString(R.string.choose_call_icon)
                val callIcons = themeManager.getResources(
                    remoteConfig,
                    CallIconModel::class.java
                ) as ArrayList<CallIconModel>
                pickAllAvatarsViewModel.createThemeConfigListFromCallIcons(callIcons)
            }
        }

        pickAllAvatarsViewModel.themeConfigListLiveData.observe(this) { themeConfigList ->

            if (themeConfigList == null) return@observe

            Handler().postDelayed({
                adapter.updateItems(themeConfigList)
            }, 300)
        }
    }

}