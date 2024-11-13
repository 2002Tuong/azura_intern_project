package com.calltheme.app.ui.pickavatar

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
import com.screentheme.app.R
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.databinding.FragmentPickAvatarBinding
import com.screentheme.app.models.AvatarModel
import org.koin.android.ext.android.inject

class PickAvatarFragment : BaseFragment() {

    companion object {
        const val REQUEST_PICKUP_AVATAR_KEY = "REQUEST_PICKUP_AVATAR_KEY"
    }

    lateinit var binding: FragmentPickAvatarBinding

    private val pickAvatarViewModel: PickAvatarViewModel by inject()

    private var yourAvatarAdapter =
        PickYourAvatarAdapter(showHeader = true, itemAvatarRes = R.layout.item_layout_pick_your_avatar)
    private var ourAvatarAdapter =
        PickOurAvatarAdapter(showHeader = false, itemAvatarRes = R.layout.item_layout_pick_our_avatar)
    private var pickedAvatar: AvatarModel? = null
    private var isBannerShowed = false
    private var isDestinationChanged = false
    override fun getViewBinding(): ViewBinding {
        binding = FragmentPickAvatarBinding.inflate(layoutInflater)
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated() {
        pickAvatarViewModel.pickedAvatarLiveData.postValue(null)

        binding.yourAvatarRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.yourAvatarRecyclerView.adapter = yourAvatarAdapter
        yourAvatarAdapter.setCallBack { position, pickedAvatar ->
            if (position == 0) {
                val mainActivity = activity as BaseActivity
                mainActivity.pickImageFromGallery { selectedImageUri ->
                    val newAvatar = AvatarModel(avatar = selectedImageUri.toString())
                    pickAvatarViewModel.addAvatar(avatar = newAvatar)
                }
            } else if (pickedAvatar != null) {
                this.pickedAvatar = pickedAvatar
                pickAvatarViewModel.pickedAvatarLiveData.postValue(pickedAvatar)
            }
            ourAvatarAdapter.deSelect()
        }

        yourAvatarAdapter.onRemoveItem {
            pickAvatarViewModel.removeAvatar(it)
        }

        binding.avatarRecyclerView.layoutManager = GridLayoutManager(context, 4)
        binding.avatarRecyclerView.adapter = ourAvatarAdapter

        ourAvatarAdapter.setCallBack { position, avatar ->

            if (avatar != null) {
                pickedAvatar = avatar
                pickAvatarViewModel.pickedAvatarLiveData.postValue(avatar)
            }
            yourAvatarAdapter.deSelect()
        }

        binding.goBackButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.confirmButton.setOnClickListener {
            if (pickedAvatar != null) {
                setFragmentResult(REQUEST_PICKUP_AVATAR_KEY, bundleOf("avatar" to pickedAvatar))
                findNavController().navigateUp()
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
        val avatarsZZZ =
            themeManager.getResources(AppRemoteConfig.callThemeConfigs(), AvatarModel::class.java)

        ourAvatarAdapter.updateItems(avatarsZZZ as ArrayList<AvatarModel>)

        pickAvatarViewModel.yourAvatars.observe(this) {
            yourAvatarAdapter.updateItems(it)
        }

        pickAvatarViewModel.pickedAvatarLiveData.observe(this) {
            if (it != null) {
                binding.confirmButton.visibility = View.VISIBLE
            } else {
                binding.confirmButton.visibility = View.INVISIBLE
            }
        }
    }


}