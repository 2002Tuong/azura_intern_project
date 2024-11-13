package com.parallax.hdvideo.wallpapers.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.RewardAdBottomDialogBinding
import com.parallax.hdvideo.wallpapers.extension.setOnClickListener
import com.parallax.hdvideo.wallpapers.services.log.AdEvent
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport

class RewardAdBottomSheetFragment: BottomSheetDialogFragment() {

    private var bottomSheetCallback: ((Boolean) -> Unit)? = null
    private lateinit var binding: RewardAdBottomDialogBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =  super.onCreateDialog(savedInstanceState)
        dialog.setCanceledOnTouchOutside(false)
        isCancelable
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = RewardAdBottomDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setOnClickListener(binding.btnWatchRewardAd) {
            bottomSheetCallback?.invoke(true)
            TrackingSupport.recordEvent(AdEvent.RewardedClickView)
            dismissAllowingStateLoss()
        }
        setOnClickListener(binding.btnSkipWatchReward) {
            bottomSheetCallback?.invoke(false)
            TrackingSupport.recordEvent(AdEvent.RewardedClickCancel)
            dismissAllowingStateLoss()
        }
        binding.tvGuide.text = String.format(getString(R.string.watch_reward_ads_guideline), getString(R.string.wallpaper))
    }

    companion object {
        const val TAG = "RewardAdBottomSheetFragment"

        fun show(fm: FragmentManager, callback: ((Boolean) -> Unit)? = null) {
            if (fm.isStateSaved || fm.isDestroyed) return
            synchronized(TAG) {
                if (fm.findFragmentByTag(TAG) != null) return
                val fg = RewardAdBottomSheetFragment()
                fg.bottomSheetCallback = callback
                fg.show(fm, TAG)
            }
        }
    }
}