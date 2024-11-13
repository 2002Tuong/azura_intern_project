package com.parallax.hdvideo.wallpapers.ui.dialog

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.PopupExplainSkipRewardedBinding
import com.parallax.hdvideo.wallpapers.extension.setOnClickListener
import com.parallax.hdvideo.wallpapers.services.log.AdEvent
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.ui.base.dialog.BaseDialogFragment
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration


class ExplainWhenSkipRewardedDialog : BaseDialogFragment() {

    var dialogCallback: ((Boolean) -> Unit)? = null
    override var canceledWhenTouchOutside = false
    override val resLayoutId: Int = R.layout.popup_explain_skip_rewarded
    private lateinit var binding: PopupExplainSkipRewardedBinding
    override fun init(view: View) {
        super.init(view)
        binding = PopupExplainSkipRewardedBinding.bind(view)
        binding.btnYes.setOnClickListener {
            dialogCallback?.invoke(true)
            TrackingSupport.recordEvent(AdEvent.RewardedClickViewOnExplain)
            dismissAllowingStateLoss()
        }
        setOnClickListener(binding.btnDisagree) {
            dialogCallback?.invoke(false)
            TrackingSupport.recordEvent(AdEvent.RewardedDeclineOnExplain)
            dismissAllowingStateLoss()
        }
    }

    override fun onResume() {
        super.onResume()
        setLayout(
            (AppConfiguration.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    companion object {
        fun show(
            fm: FragmentManager,
            callback: ((Boolean) -> Unit)? = null
        ) {
            val dialog = ExplainWhenSkipRewardedDialog()
            dialog.dialogCallback = callback
            dialog.show(fm, TAG)
        }

        const val TAG = "ExplainWhenSkipRewardedDialog"
    }

}