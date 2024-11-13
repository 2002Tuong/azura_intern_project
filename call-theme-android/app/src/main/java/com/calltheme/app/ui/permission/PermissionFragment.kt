package com.calltheme.app.ui.permission

import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.utils.AdsUtils
import com.screentheme.app.R
import com.screentheme.app.databinding.FragmentPermissionBinding
import com.screentheme.app.utils.extensions.isAlreadyDefaultDialer

class PermissionFragment : BaseFragment() {

    lateinit var mPermissionBinding: FragmentPermissionBinding

    private var isNavigatingOut = false

    override fun getViewBinding(): ViewBinding {
        mPermissionBinding = FragmentPermissionBinding.inflate(layoutInflater)
        return mPermissionBinding
    }

    override fun onStop() {
        if (!isNavigatingOut) {
            myActivity?.let {
                AdsUtils.requestNativePermission(it, reload = true)
            }
        }
        super.onStop()
    }

    override fun onViewCreated() {
        AppOpenManager.getInstance().enableAppResume()
        myActivity?.let { AdsUtils.requestNativeHome(it) }
        if (requireContext().isAlreadyDefaultDialer()) {
            navigateToHomeScreen()
        }
        mPermissionBinding.callingScreenSwitch.setOnClickListener {
            triggerCallScreenPermission()
        }
        mPermissionBinding.btnContinue.setOnClickListener {
            navigateToHomeScreen()
        }

        initAds()
    }

    override fun registerObservers() = Unit

    private fun navigateToHomeScreen() {
        findNavController().navigate(R.id.action_permissionFragment_to_navigation_home)
    }

    private fun triggerCallScreenPermission() {
        val mainActivity = myActivity as? com.calltheme.app.ui.activity.MainActivity ?: return
        if (!mainActivity.isAlreadyDefaultDialer()) {
            AdsUtils.requestNativePermission(mainActivity, reload = true)
            mainActivity.launchSetDefaultDialerIntent { _ ->
                val isGranted = mainActivity.isAlreadyDefaultDialer()
                mPermissionBinding.callingScreenSwitch.isChecked = isGranted
                mPermissionBinding.btnContinue.setBackgroundResource(if (isGranted) R.drawable.btn_continue_enable else R.drawable.btn_continue_disable)
                mPermissionBinding.btnContinue.isEnabled = isGranted
            }
        } else {
            mPermissionBinding.callingScreenSwitch.isChecked = true
        }
    }

    private fun initAds() {
        AdsUtils.permissionApNativeAdLoadFail.observe(viewLifecycleOwner) {
            if (it) {
                mPermissionBinding.frAds.visibility = View.GONE
            }
        }
        AdsUtils.permissionApNativeAd.observe(viewLifecycleOwner) {
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
}