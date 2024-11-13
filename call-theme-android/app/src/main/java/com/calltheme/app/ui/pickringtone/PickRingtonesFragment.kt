package com.calltheme.app.ui.pickringtone

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.ads.control.admob.Admob
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.utils.BannerAdsHelpers
import com.screentheme.app.databinding.FragmentPickRingtonBinding
import com.screentheme.app.utils.helpers.RingtoneController
import org.koin.android.ext.android.inject

class PickRingtonesFragment : BaseFragment() {

    companion object {
        const val REQUEST_PICKUP_RINGTONE_KEY = "REQUEST_PICKUP_RINGTONE_KEY"
    }

    lateinit var binding: FragmentPickRingtonBinding

    private val pickRingtonesViewModel: PickRingtonesViewModel by inject()
    private val ringToneHelper: RingtoneController by inject()

    private lateinit var adapter: RingtoneAdapter
    private var isBannerShowed = false
    private var isDestinationChanged = false
    override fun getViewBinding(): ViewBinding {
        binding = FragmentPickRingtonBinding.inflate(layoutInflater)
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated() {

        pickRingtonesViewModel.pickedRingtoneLiveData.postValue(null)

        adapter = RingtoneAdapter(ringToneHelper)

        adapter.onItemClicked { ringtoneItem ->
            pickRingtonesViewModel.pickedRingtoneLiveData.postValue(ringtoneItem)
        }

        binding.apply {
            ringtonesRecyclerView.adapter = adapter
            ringtonesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

            goBackButton.setOnClickListener {
                findNavController().popBackStack()
            }

            confirmButton.setOnClickListener {

                if (pickRingtonesViewModel.pickedRingtoneLiveData.value != null) {
                    setFragmentResult(
                        REQUEST_PICKUP_RINGTONE_KEY,
                        bundleOf("ringtone" to pickRingtonesViewModel.pickedRingtoneLiveData.value)
                    )

                    findNavController().navigateUp()
                }

            }
        }

        pickRingtonesViewModel.getListRingtones()
        myActivity?.let { BannerAdsHelpers.requestLoadBannerAds(BannerAdsHelpers.BannerAdPlacement.THEME, it) }
        requestBannerListener()
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun requestBannerListener() {
        BannerAdsHelpers.themeBannerAd.value?.let {
            isBannerShowed = true
            //binding.bannerAds.flShimemr.visibility = View.GONE
            Admob.getInstance().populateUnifiedBannerAdView(
                myActivity,
                it,
                binding.bannerAds.bannerContainer
            )
        }
        BannerAdsHelpers.themeBannerAd.observe(this) { adView ->
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
        BannerAdsHelpers.bannerThemeFailToLoad.observe(this) {
            if (it && !isBannerShowed) {
                binding.frAds.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isDestinationChanged) {
            isDestinationChanged = false
        }
    }

    override fun onStop() {
        myActivity?.let { BannerAdsHelpers.requestLoadBannerAds(BannerAdsHelpers.BannerAdPlacement.THEME, it) }
        super.onStop()
        ringToneHelper.stopRingtone()
    }

    override fun registerObservers() {
        pickRingtonesViewModel.listOfRingtones.observe(this) {
            adapter.updateItems(it)
        }

        pickRingtonesViewModel.pickedRingtoneLiveData.observe(this) { ringtoneItem ->
            if (ringtoneItem != null) {
                binding.confirmButton.visibility = View.VISIBLE
            } else {
                binding.confirmButton.visibility = View.INVISIBLE
            }
        }
    }

    override fun initializeViewModels() {
        super.initializeViewModels()
    }
}