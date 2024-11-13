package com.calltheme.app.ui.pickbackground

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.ads.control.admob.Admob
import com.calltheme.app.ui.activity.BaseActivity
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.utils.BannerAdsHelpers
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.databinding.FragmentPickBackgroundBinding
import com.screentheme.app.models.BackgroundModel
import org.koin.android.ext.android.inject

class PickBackgroundFragment : BaseFragment() {

    companion object {
        const val REQUEST_PICKUP_BACKGROUND_KEY = "REQUEST_PICKUP_BACKGROUND_KEY"
    }

    private lateinit var binding: FragmentPickBackgroundBinding

    private val pickBackgroundViewModel: PickBackgroundViewModel by inject()

    private var yourBackgroundAdapter = PickYourBackgroundAdapter()
    private var ourBackgroundAdapter = PickOurBackgroundAdapter()
    private var pickedBackground: BackgroundModel? = null
    private var isBannerShowed = false
    private var isDestinationChanged = false
    override fun getViewBinding(): ViewBinding {
        binding = FragmentPickBackgroundBinding.inflate(layoutInflater)
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated() {
        pickBackgroundViewModel.pickedBackgroundLiveData.postValue(null)

        binding.apply {
            yourBackgroundRecyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            yourBackgroundRecyclerView.adapter = yourBackgroundAdapter
            yourBackgroundAdapter.setCallBack { position, pickedBackground ->
                if (position == 0) {
                    val mainActivity = activity as BaseActivity
                    mainActivity.pickImageFromGallery { selectedImageUri ->
                        val newBackground = BackgroundModel(background = selectedImageUri.toString())
                        pickBackgroundViewModel.addBackground(newBackground)
                    }
                    isDestinationChanged = true
                } else if (pickedBackground != null) {
                    this@PickBackgroundFragment.pickedBackground = pickedBackground
                    pickBackgroundViewModel.pickedBackgroundLiveData.postValue(pickedBackground)
                }
                ourBackgroundAdapter.deSelect()
            }
            yourBackgroundAdapter.onRemoveItem {
                pickBackgroundViewModel.removeBackground(it)
            }

            ourBackgroundRecyclerView.isNestedScrollingEnabled = true
            ourBackgroundRecyclerView.setHasFixedSize(false)
            ourBackgroundRecyclerView.layoutManager = GridLayoutManager(context, 2)
            ourBackgroundRecyclerView.adapter = ourBackgroundAdapter

            ourBackgroundAdapter.setCallBack { position, background ->
                isDestinationChanged = true
                if (background != null) {
                    pickedBackground = background
                    pickBackgroundViewModel.pickedBackgroundLiveData.postValue(background)
                }
                yourBackgroundAdapter.deSelect()
            }

            goBackButton.setOnClickListener {
                findNavController().popBackStack()
            }

            confirmButton.setOnClickListener {
                if (pickedBackground != null) {
                    setFragmentResult(
                        REQUEST_PICKUP_BACKGROUND_KEY,
                        bundleOf("background" to pickedBackground)
                    )

                    findNavController().navigateUp()
                }
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
    override fun registerObservers() {
        val backgrounds =
            themeManager.getResources(AppRemoteConfig.callThemeConfigs(), BackgroundModel::class.java) as ArrayList<BackgroundModel>
        ourBackgroundAdapter.updateItems(backgrounds)

        pickBackgroundViewModel.yourBackgrounds.observe(this) {
            yourBackgroundAdapter.updateItems(it)
        }

        pickBackgroundViewModel.pickedBackgroundLiveData.observe(this) {
            if (it != null) {
                binding.confirmButton.visibility = View.VISIBLE
            } else {
                binding.confirmButton.visibility = View.INVISIBLE
            }
        }
    }


}