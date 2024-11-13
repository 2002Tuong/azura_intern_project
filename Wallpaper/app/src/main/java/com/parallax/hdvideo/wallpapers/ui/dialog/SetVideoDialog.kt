package com.parallax.hdvideo.wallpapers.ui.dialog

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.PopupSetVideoBinding
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.ui.base.dialog.BaseDialogFragment
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration

class SetVideoDialog: BaseDialogFragment() {

    private var dialogCallback: ((Boolean) -> Unit)? = null
    override var canceledWhenTouchOutside: Boolean = false

    override val resLayoutId: Int = R.layout.popup_set_video
    private lateinit var binding: PopupSetVideoBinding
    override fun init(view: View) {
        super.init(view)
        binding = PopupSetVideoBinding.bind(view)
        val subTitle =
            getString(R.string.wallpaper_might_not_be_compatible) + "\n" + getString(R.string.sorry_for_inconvenience)
        binding.subtitle.text = subTitle
        binding.btnOk.setOnClickListener {
            dialogCallback?.invoke(true)
            dismissAllowingStateLoss()
        }
        binding.btnSkip.setOnClickListener {
            dialogCallback?.invoke(false)
            dismissAllowingStateLoss()
        }
        if (!RemoteConfig.commonData.isSupportedVideo) {
            binding.tvSetVideo.isHidden = true
            binding.btnSkip.isHidden = true
        }
    }

    override fun onResume() {
        super.onResume()
        val aspectRatio = 0.9
        setLayout(
            (AppConfiguration.displayMetrics.widthPixels * aspectRatio).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        fun show(fm: FragmentManager?, cb: ((Boolean) -> Unit)) {
            val fragmentManager = fm ?: return
            val tag = "SetVideoDialog"
            val setVideoDialog = SetVideoDialog()
            setVideoDialog.dialogCallback = cb
            setVideoDialog.show(fragmentManager, tag)
        }
    }
}