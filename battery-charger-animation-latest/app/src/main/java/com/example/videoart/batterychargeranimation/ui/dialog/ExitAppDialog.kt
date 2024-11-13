package com.example.videoart.batterychargeranimation.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.ads.control.ads.VioAdmob
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.databinding.ExitAppDialogLayoutBinding
import com.example.videoart.batterychargeranimation.extension.setWidthPercent
import com.example.videoart.batterychargeranimation.utils.AdsUtils

class ExitAppDialog : DialogFragment() {

    lateinit var binding: ExitAppDialogLayoutBinding

    var exitAppCallback: (() -> Unit)? = null
    var rateAppCallback: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = ExitAppDialogLayoutBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)

        val dialog = builder.create()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog)

        binding.apply {

            exitAppButton.setOnClickListener {
                exitAppCallback?.invoke()
            }

            rateAppButton.setOnClickListener {
                rateAppCallback?.invoke()
            }
        }
        isCancelable = true

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        AdsUtils.nativeExit.observe(viewLifecycleOwner) {
            it?.let { apNativeAd ->
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    apNativeAd,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner
                )
            }
        }

        AdsUtils.nativeExitLoadFail.observe(viewLifecycleOwner) {
            if(it) {
                binding.frAds.isVisible = false
            }
        }
        return binding.root
    }

    override fun onResume() {
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
