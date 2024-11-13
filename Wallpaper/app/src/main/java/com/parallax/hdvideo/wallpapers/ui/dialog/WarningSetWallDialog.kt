package com.parallax.hdvideo.wallpapers.ui.dialog

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.PopupWarningSetWallBinding
import com.parallax.hdvideo.wallpapers.extension.setOnClickListener
import com.parallax.hdvideo.wallpapers.services.log.EventOther
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.ui.base.dialog.BaseDialogFragment
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration

class WarningSetWallDialog : BaseDialogFragment() {

    var callback: (() -> Unit)? = null
    override var canceledWhenTouchOutside = false
    override val resLayoutId: Int = R.layout.popup_warning_set_wall
    private lateinit var binding: PopupWarningSetWallBinding
    override fun init(view: View) {
        super.init(view)
        binding = PopupWarningSetWallBinding.bind(view)
        binding.btnYes.setOnClickListener {
            callback?.invoke()
            TrackingSupport.recordEvent(EventOther.ErrorAndroid12Yes)
            dismissAllowingStateLoss()
        }
        setOnClickListener(binding.btnDisagree) {
            TrackingSupport.recordEvent(EventOther.ErrorAndroid12No)
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
            callback: (() -> Unit)? = null
        ) {
            val dialog = WarningSetWallDialog()
            dialog.callback = callback
            dialog.show(fm, TAG)
        }

        const val TAG = "WarningSetWallDialog"
    }

}