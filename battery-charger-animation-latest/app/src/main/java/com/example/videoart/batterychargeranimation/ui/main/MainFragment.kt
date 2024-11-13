package com.example.videoart.batterychargeranimation.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.ads.control.admob.Admob
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.example.videoart.batterychargeranimation.MainActivity
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.databinding.FragmentMainBinding
import com.example.videoart.batterychargeranimation.notification.NotificationController
import com.example.videoart.batterychargeranimation.ui.base.BaseFragment
import com.example.videoart.batterychargeranimation.ui.dialog.RatingAppDialog
import com.example.videoart.batterychargeranimation.ui.gallery.GalleryFragment
import com.example.videoart.batterychargeranimation.ui.home.HomeFragment
import com.example.videoart.batterychargeranimation.ui.setting.SettingFragment
import com.example.videoart.batterychargeranimation.utils.AdsUtils
import com.example.videoart.batterychargeranimation.utils.delay
import org.koin.android.ext.android.inject
import javax.inject.Inject


class MainFragment : BaseFragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: MainViewPagerAdapter
    private val notificationController: NotificationController by inject()

    private var listFragment = mutableListOf<Fragment>()
    private val homeFragment = HomeFragment()
    private val galleryFragment = GalleryFragment()
    private val settingFragment = SettingFragment.newInstance(this)

    private var isBannerShowed = false
    private var isDestinationChanged = false

    private val permissionNotificationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if(it) {
                notificationController.showMenuNotification()
            }
        }

    private val myRatingDialog = RatingAppDialog {
        exitAppDialog.show(requireActivity())
    }

    override fun getViewBinding(): ViewBinding {
        binding = FragmentMainBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {
        notificationController.showMenuNotification()
        AppOpenManager.getInstance().enableAppResume()
        AdsUtils.requestNativePreview(requireActivity())
        AdsUtils.requestNativeExit(requireActivity())
        AdsUtils.requestLoadBanner(requireActivity())
        AdsUtils.isCloseAdSplash.observe(viewLifecycleOwner) {
            if(it) {
                checkNotificationPermissionAndroid13()
            }
        }
        listFragment.add(homeFragment)
        listFragment.add(galleryFragment)
        listFragment.add(settingFragment)
        adapter = MainViewPagerAdapter(requireActivity(), listFragment)
        binding.apply {
            homeViewPager.adapter = adapter
            homeViewPager.offscreenPageLimit = 3
            homeViewPager.isSaveEnabled = false
            homeViewPager.isUserInputEnabled = false
            bottomNavView.setOnItemSelectedListener { item ->
                when(item.itemId) {
                    R.id.homeFragment -> {
                        AdsUtils.requestLoadBanner(requireActivity(), true)
                        binding.bottomIndicator.setSelectedIndex(0)
                        homeViewPager.setCurrentItem(0, true)
                    }
                    R.id.galleryFragment -> {
                        AdsUtils.requestLoadBanner(requireActivity(), true)
                        binding.bottomIndicator.setSelectedIndex(1)
                        homeViewPager.setCurrentItem(1, true)
                    }
                    R.id.settingFragment -> {
                        AdsUtils.requestLoadBanner(requireActivity(), true)
                        binding.bottomIndicator.setSelectedIndex(2)
                        homeViewPager.setCurrentItem(2, true)
                    }
                }
                true
            }
        }

        exitAppDialog.onExitApp {
            exitAppDialog.dismiss()
            delay {
                try {
                    requireActivity().finish()
                } catch (exception: Exception) {
                }

            }
        }

        exitAppDialog.onRateApp {
            exitAppDialog.dismiss()
        }

        setUpBanner()
    }

    override fun registerObservers() {

    }

    fun setUpBanner() {
        (requireActivity() as MainActivity).isDestinationChanged.observe(viewLifecycleOwner) { destination ->
            if (destination.id == R.id.languageFragment
                || destination.id == R.id.onboardingFragment
                || destination.id == R.id.splashFragment
            )
                return@observe

            isDestinationChanged = destination.id != R.id.mainFragment
        }
        Log.d("AdsBanner", "banner set up ")
        AdsUtils.bannerLiveData.value?.let {
            Log.d("AdsBanner", "banner set up with ${it}")
            isBannerShowed = true
            Admob.getInstance().populateUnifiedBannerAdView(
                requireActivity(),
                it,
                binding.bannerAds.bannerContainer
            )
        }

        AdsUtils.bannerLiveData.observe(viewLifecycleOwner) { adView ->
            adView?.let {
                isBannerShowed = true
                //binding.bannerAds.flShimemr.visibility = View.GONE
                Log.d("AdsBanner", "banner set up with in observer ${it}")
                try {
                    binding.bannerAds.bannerContainer.removeAllViews()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
                Admob.getInstance().populateUnifiedBannerAdView(
                    requireActivity(),
                    adView,
                    binding.bannerAds.bannerContainer
                )
            }
        }

        AdsUtils.bannerFailToLoad.observe(viewLifecycleOwner) {
            if (it && !isBannerShowed) {
                Log.d("AdsUtil", "banner fail ${it} and ${isBannerShowed}")
                binding.frAds.visibility = View.GONE
            }
        }
    }

    private fun checkNotificationPermissionAndroid13() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireActivity(), Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_DENIED
            ) {
                permissionNotificationLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!myRatingDialog.isAdded) myRatingDialog.show(requireActivity())
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onStop() {
        super.onStop()
        AdsUtils.requestLoadBanner(requireActivity(), true)
    }
}