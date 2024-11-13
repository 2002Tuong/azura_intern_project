package com.wifi.wificharger.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.ads.control.ads.VioAdmob
import com.wifi.wificharger.databinding.ExitAppDialogLayoutBinding
import com.wifi.wificharger.utils.AdsUtils
import com.wifi.wificharger.utils.setWidthPercent

class ExitAppDialog : DialogFragment() {

    lateinit var binding: ExitAppDialogLayoutBinding

    var exitAppCallback: (() -> Unit)? = null
    var rateAppCallback: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = ExitAppDialogLayoutBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.apply {

            exitAppButton.setOnClickListener {
                exitAppCallback?.invoke()
            }

            rateAppButton.setOnClickListener {
                rateAppCallback?.invoke()
            }
        }
        isCancelable = true
        AdsUtils.nativeExit.value.let {
            if (it != null) {
                VioAdmob.getInstance().populateNativeAdView(
                    activity,
                    it,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner
                )
            }
        }


        return dialog
    }

    override fun onResume() {
        setWidthPercent(0.8f)
        super.onResume()
    }

    fun show(activity: FragmentActivity) {
        show(activity.supportFragmentManager, "ExitAppDialog")
    }

    fun onExitApp(callback: () -> Unit) {
        this.exitAppCallback = callback
    }

    fun onRateApp(callback: () -> Unit) {
        this.rateAppCallback = callback
    }
}
