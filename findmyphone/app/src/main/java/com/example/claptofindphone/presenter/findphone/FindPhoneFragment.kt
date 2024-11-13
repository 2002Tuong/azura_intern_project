package com.example.claptofindphone.presenter.findphone

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ads.control.ads.VioAdmob
import com.example.claptofindphone.R
import com.example.claptofindphone.data.local.PreferenceSupplier
import com.example.claptofindphone.databinding.FragmentFindPhoneBinding
import com.example.claptofindphone.models.ActivatedState
import com.example.claptofindphone.presenter.base.BaseFragment
import com.example.claptofindphone.presenter.findphone.viewmodel.FindPhoneScreenViewModel
import com.example.claptofindphone.presenter.result.ActiveManager
import com.example.claptofindphone.presenter.select.adapter.SoundListAdapter
import com.example.claptofindphone.presenter.select.model.SoundModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import javax.inject.Inject

@AndroidEntryPoint
class FindPhoneFragment : BaseFragment(R.layout.fragment_find_phone) {
    override val label: String
        get() = getString(R.string.find_phone)

    override val isBackButtonEnable: Boolean
        get() = false

    override val isBottomBarEnable: Boolean
        get() = false
    override val isFabEnable: Boolean
        get() = false

    override val isRightButtonEnable: Boolean
        get() = false
    override val isApplyButtonEnable: Boolean
        get() = false
    override val isHowToUseButtonEnable: Boolean
        get() = true

    private val findPhoneBinding by viewBinding(FragmentFindPhoneBinding::bind)
    private var soundSelectedPosition = 0

    @Inject
    lateinit var preferenceSupplier: PreferenceSupplier

    @Inject
    lateinit var activeManager: ActiveManager

    private lateinit var iconListAdapter: SoundListAdapter
    private lateinit var soundTypeList: ArrayList<SoundModel>
    private val mFindPhoneScreenViewModel: FindPhoneScreenViewModel by viewModels({ requireActivity() })
    private val permissionRequestedLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->

        }

    override fun initView() {
        super.initView()
        setUpSoundList()
        //setUpTapToActiveButton()
        //setUpActiveButton()
        startServiceIfAllow()

        findPhoneBinding.imgTapToActive.setImageResource(soundTypeList[preferenceSupplier.getSoundType()].iconRes)
        activeManager.isActiveState.observe(viewLifecycleOwner) { active ->
            findPhoneBinding.txtTapToFindPhone.isSelected = active
            findPhoneBinding.imgTapToActive.isSelected = active
            if (active) {
                findPhoneBinding.txtTapToFindPhone.text = getString(R.string.tap_to_stop)
                findPhoneBinding.txtTapToFindPhone.setOnClickListener {
                    navigateToResult(ActivatedState.Deactivate)
                }
                findPhoneBinding.imgTapToActive.setOnClickListener {
                    navigateToResult(ActivatedState.Deactivate)
                }
            } else {
                findPhoneBinding.txtTapToFindPhone.text = getString(R.string.tap_to_active)
                findPhoneBinding.txtTapToFindPhone.setOnClickListener {
                    navigateToResult(ActivatedState.Active)
                }
                findPhoneBinding.imgTapToActive.setOnClickListener {
                    navigateToResult(ActivatedState.Active)
                }
            }
        }

        loadAds()
        //initAds()
        initBanner()
        adsHelper.isAdsSplashClosed.observe(viewLifecycleOwner) {
            requestNotificationPermission()
        }

        findPhoneBinding.txtFindPhone.isSelected = true
        findPhoneBinding.txtSetting.isSelected = false
        findPhoneBinding.txtSetting.clicksWithLifecycleScope {
            it.findNavController().apply {
                if (currentDestination?.id != R.id.findPhoneFragment) navigateUp()
                navigate(FindPhoneFragmentDirections.globalToSettingFragment())
            }
        }
    }

    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            permissionRequestedLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    private fun navigateToResult(activatedState: ActivatedState) {
        try {
            findNavController().navigate(
                FindPhoneFragmentDirections.navigateToResultFragment(activatedState)
            )
        } catch (exception: Exception) {
            FirebaseCrashlytics.getInstance().recordException(exception)
        }
    }

    private fun startServiceIfAllow() {
        if (preferenceSupplier.isFindPhoneActivated()) {
            startService()
        } else {
            stopService()
        }
    }

    private fun setUpTapToActiveButton() {
        findPhoneBinding.txtTapToFindPhone.text = if (preferenceSupplier.isFindPhoneActivated()) {
            context?.getString(R.string.tap_to_stop)
        } else {
            context?.getString(R.string.tap_to_active)
        }
        findPhoneBinding.txtTapToFindPhone.isSelected =
            preferenceSupplier.isFindPhoneActivated()
        findPhoneBinding.txtTapToFindPhone.setOnClickListener {
            if (it.isSelected) {
                stopService()
            } else {
                startService()
            }

            val isActivated = preferenceSupplier.isFindPhoneActivated()
            preferenceSupplier.setFindPhoneActivated(!isActivated)
            findPhoneBinding.txtTapToFindPhone.isSelected =
                preferenceSupplier.isFindPhoneActivated()
            findPhoneBinding.txtTapToFindPhone.text = if (it.isSelected) {
                context?.getString(R.string.tap_to_stop)
            } else {
                context?.getString(R.string.tap_to_active)
            }
            findPhoneBinding.imgTapToActive.isSelected =
                preferenceSupplier.isFindPhoneActivated()
        }
    }

    private fun setUpActiveButton() {
        findPhoneBinding.imgTapToActive.isSelected =
            preferenceSupplier.isFindPhoneActivated()
        findPhoneBinding.imgTapToActive.setImageResource(soundTypeList[preferenceSupplier.getSoundType()].iconRes)
        findPhoneBinding.imgTapToActive.setOnClickListener {
            if (it.isSelected) {
                stopService()
            } else {
                startService()
            }
            val isActivated = preferenceSupplier.isFindPhoneActivated()
            preferenceSupplier.setFindPhoneActivated(!isActivated)
            findPhoneBinding.txtTapToFindPhone.isSelected =
                preferenceSupplier.isFindPhoneActivated()
            findPhoneBinding.imgTapToActive.isSelected =
                preferenceSupplier.isFindPhoneActivated()
            findPhoneBinding.txtTapToFindPhone.text = if (it.isSelected) {
                context?.getString(R.string.tap_to_stop)
            } else {
                context?.getString(R.string.tap_to_active)
            }
        }
    }

    private fun setUpSoundList() {
        soundTypeList = mFindPhoneScreenViewModel.setUpSoundTypeList()
        findPhoneBinding.rclSounds.layoutManager =
            GridLayoutManager(context, 3, RecyclerView.VERTICAL, false)
        soundSelectedPosition = preferenceSupplier.getSoundType()
        val soundListAdapter1 = SoundListAdapter(
            requireContext(),
            soundTypeList.filterIndexed { index, _ ->
                index < 3
            },
            ::onItemClick,
            SoundListAdapter.ScreenType.FindPhone,
            soundSelectedPosition
        )
        iconListAdapter = soundListAdapter1
        findPhoneBinding.rclSounds.adapter = iconListAdapter

        findPhoneBinding.rclSounds2.layoutManager =
            GridLayoutManager(context, 3, RecyclerView.VERTICAL, false)
        val soundListAdapter2 = SoundListAdapter(
            requireContext(),
            soundTypeList.filterIndexed { index, _ ->
                index > 3
            },
            ::onItemClick,
            SoundListAdapter.ScreenType.FindPhone,
            soundSelectedPosition - 4
        )
        findPhoneBinding.rclSounds2.adapter = soundListAdapter2
    }

    private fun onItemClick(soundType: SoundModel, position: Int) {
        findPhoneBinding.imgTapToActive.setImageResource(soundType.iconRes)
        adsHelper.forceShowInterSelectSound(requireActivity()) {
            findNavController().navigate(
                FindPhoneFragmentDirections.navigateToSelectFragment(
                    soundType
                )
            )
            adsHelper.loadInterSelectSound(requireActivity(), true)
        }
        soundSelectedPosition = position
        Log.d("FindPhone", "$soundSelectedPosition")
    }

    override fun onResume() {
        super.onResume()
        findPhoneBinding.rclSounds.scrollToPosition(preferenceSupplier.getSoundType())
        adsHelper.loadInterSelectSound(requireActivity(), false)
    }

    private fun initAds() {
        adsHelper.homeApNativeAdLoadFail.observe(viewLifecycleOwner) {
            if (it) {
                findPhoneBinding.frAds.visibility = View.GONE
                adsHelper.homeApNativeAdLoadFail.value = false
            }
        }

        adsHelper.homeApNativeAd.observe(viewLifecycleOwner) {
            it?.let { nativeAd ->
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    nativeAd,
                    findPhoneBinding.frAds,
                    findPhoneBinding.includeNative.shimmerContainerBanner
                )
            }
        }
    }

    private fun initBanner() {
        adsHelper.bannerAdViewFindPhone.distinctUntilChanged().observe(viewLifecycleOwner) {
            it?.let {
                (it.parent as? ViewGroup)?.removeView(it)
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                findPhoneBinding.bottomLayout.addView(it)
            }
        }
    }

    private fun loadAds() {
        adsHelper.requestNativeHome(requireActivity())
        adsHelper.requestNativeResult(requireActivity())
        adsHelper.requestNativeSelect(requireActivity())
        adsHelper.loadBannerSelectSound()
        adsHelper.loadBannerHowToUse()
        adsHelper.loadBannerSetting()
        adsHelper.loadBannerSoundActive()
    }
}