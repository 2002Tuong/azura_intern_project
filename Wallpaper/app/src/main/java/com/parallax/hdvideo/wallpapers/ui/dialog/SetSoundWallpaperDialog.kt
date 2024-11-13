package com.parallax.hdvideo.wallpapers.ui.dialog

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.PopupSetSoundWallpaperBinding
import com.parallax.hdvideo.wallpapers.ui.base.dialog.BaseDialogFragment
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration

class SetSoundWallpaperDialog: BaseDialogFragment() {

    override val resLayoutId: Int = R.layout.popup_set_sound_wallpaper

    var dialogCallback: ((Boolean) -> Unit)? = null
    private lateinit var binding: PopupSetSoundWallpaperBinding
    override fun init(view: View) {
        super.init(view)
        binding = PopupSetSoundWallpaperBinding.bind(view)
        binding.btnNo.setOnClickListener {
            dismissAllowingStateLoss()
        }
        binding.btnNext.setOnClickListener {
            dialogCallback?.invoke(isSoundVideo)
            dismissAllowingStateLoss()
        }
        if (isSoundVideo) {
            binding.chkBtnSound.isChecked = true
        } else {
            binding.chkBtnNoSound.isChecked = true
        }
        setSoundView(isSoundVideo)
        binding.tvTitle.text = getString(R.string.title_popup_set_sound, getString(R.string.wallpaper))
        binding.chkBtnSound.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                isSoundVideo = true
                setSoundView(true)
            }
        }
        binding.chkBtnNoSound.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                isSoundVideo = false
                setSoundView(false)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        setLayout(
                (AppConfiguration.displayMetrics.widthPixels * 0.866).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setSoundView(isSound: Boolean) {
        val view = rootView ?: return
        val alphaElementSound = if (isSound) 1f else 0.6f
        val alphaElementNoSound = if (isSound) 0.6f else 1f
        binding.ivNoSound.alpha = alphaElementNoSound
        binding.chkBtnNoSound.alpha = alphaElementNoSound
        binding.chkBtnSound.alpha = alphaElementSound
        binding.ivSound.alpha = alphaElementSound
    }

    companion object {
        var isSoundVideo = false
        fun show(
                fm: FragmentManager,
                soundOn: Boolean = false,
                callback: ((Boolean) -> Unit)? = null
        ) {
            val dialog = SetSoundWallpaperDialog()
            dialog.dialogCallback = callback
            isSoundVideo = soundOn
            dialog.show(fm, TAG)
        }

        const val TAG = "SetSoundWallpaperDialog"
    }

}