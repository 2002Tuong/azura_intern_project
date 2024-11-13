package com.example.claptofindphone.presenter.permission

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ads.control.ads.VioAdmob
import com.example.claptofindphone.R
import com.example.claptofindphone.data.local.PreferenceSupplier
import com.example.claptofindphone.data.remote.RemoteConfigProvider
import com.example.claptofindphone.databinding.FragmentPermissionBinding
import com.example.claptofindphone.presenter.base.BaseFragment
import com.example.claptofindphone.utils.extensions.openAppSettings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PermissionFragment : BaseFragment(R.layout.fragment_permission) {
    override val label: String
        get() = getString(R.string.grant_permission_title)

    override val isBackButtonEnable: Boolean
        get() = false

    override val isBottomBarEnable: Boolean
        get() = false
    override val isFabEnable: Boolean
        get() = false

    private val mPermissionBinding by viewBinding(FragmentPermissionBinding::bind)

    private var rejectCount = 0

    override val isRightButtonEnable: Boolean
        get() = false

    override val bannerVisibility: Int
        get() = View.GONE

    @Inject
    lateinit var preferenceSupplier: PreferenceSupplier
    @Inject
    lateinit var remoteConfigProvider: RemoteConfigProvider

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            preferenceSupplier.setIsRecordPermissionGranted(isGranted)
            if (!isGranted) {
                rejectCount++
            }
            if(remoteConfigProvider.permissionBtnEnabledState) {
                mPermissionBinding.swRecordPermission.setImageResource(if (isGranted) R.drawable.icon_switch_on else R.drawable.icon_switch_off)
                mPermissionBinding.btnContinue.setBackgroundResource(if(isGranted) R.drawable.button_continue_enable else R.drawable.button_continue_disable)
            }
            //mPermissionBinding.swRecordPermission.setImageResource(if (isGranted) R.drawable.ic_switch_on else R.drawable.ic_switch_off)
            //mPermissionBinding.btnContinue.isEnabled = isGranted
        }

    private fun navigateToFindPhoneScreen() {
        preferenceSupplier.firstOpenComplete = true
        findNavController().navigate(PermissionFragmentDirections.navigateToFindPhoneFragment())
    }

    private fun showAlertDialogNoticePermission(title: String, okBtnString: String, onOkClick:() -> Unit) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage(title)

        dialogBuilder.setPositiveButton(okBtnString) { dialog, _ ->
            dialog.dismiss()
            onOkClick()
        }

        dialogBuilder.setNegativeButton(requireContext().getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }

        dialogBuilder.show()
    }

    override fun initView() {
        super.initView()
        if (preferenceSupplier.isRecordPermissionGranted()) {
                navigateToFindPhoneScreen()
        }
        mPermissionBinding.swRecordPermission.setOnClickListener {
            onButtonClick()
        }
        mPermissionBinding.btnContinue.setOnClickListener {
            onButtonClick()
        }

        initAds()
        initBanner()
        adsHelper.requestNativeHome(requireActivity())
        adsHelper.loadBannerFindPhone()
    }

    private fun triggerRecordPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) -> {
                preferenceSupplier.setIsRecordPermissionGranted(true)
            }

            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.RECORD_AUDIO
                )
            }
        }
    }

    private fun initBanner() {
        adsHelper.bannerAdViewPermission.distinctUntilChanged().observe(this) {
            it?.let {
                (it.parent as? ViewGroup)?.removeView(it)
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                mPermissionBinding.bottomLayout.addView(it)
            }
        }
    }

    private fun initAds() {
        return
        adsHelper.permissionApNativeAdLoadFail.observe(viewLifecycleOwner) {
            if (it) {
                mPermissionBinding.frAds.visibility = View.GONE
                adsHelper.languageApNativeAdLoadFail.value = false
            }
        }
        adsHelper.permissionApNativeAd.observe(viewLifecycleOwner) {
            if (it != null) {
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    it,
                    mPermissionBinding.frAds,
                    mPermissionBinding.includeNative.shimmerContainerBanner
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(isRecordAudioPermissionGranted(Manifest.permission.RECORD_AUDIO)) {
            preferenceSupplier.setIsRecordPermissionGranted(true)
        }
    }

    private fun isRecordAudioPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun onButtonClick() {
        if (preferenceSupplier.isRecordPermissionGranted()) {
            navigateToFindPhoneScreen()
        } else {
            if(rejectCount > 0 ) {
                if(this.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    showAlertDialogNoticePermission(
                        title = requireContext().getString(R.string.notice_grant_permission_dialog),
                        okBtnString = requireContext().getString(R.string.yes)
                    ) {
                        triggerRecordPermission()
                    }
                } else {
                    showAlertDialogNoticePermission(
                        title = requireContext().getString(R.string.notice_grant_permission_dialog_2),
                        okBtnString =  requireContext().getString(R.string.setting)
                    ) {
                        requireActivity().openAppSettings()
                    }
                }
            } else {
                triggerRecordPermission()
            }
        }
    }
}