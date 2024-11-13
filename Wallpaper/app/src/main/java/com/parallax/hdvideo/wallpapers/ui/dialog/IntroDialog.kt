package com.parallax.hdvideo.wallpapers.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.LayoutIntroDialogBinding
import com.parallax.hdvideo.wallpapers.ui.base.dialog.BaseDialogFragment


class IntroDialog : BaseDialogFragment() {
    override val resLayoutId: Int
        get() = R.layout.layout_intro_dialog
    override var canceledWhenTouchOutside = true
    private val handler = Handler()
    private var dialogCallback: (() -> Unit)? = null
    private var is4dImage = false
    private lateinit var binding: LayoutIntroDialogBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        rootView?.setOnClickListener { dismissAllowingStateLoss() }
        return dialog
    }

    override fun init(view: View) {
        super.init(view)
        binding = LayoutIntroDialogBinding.bind(view)
        initIntro(view)
        handler.postDelayed({
            dismissAllowingStateLoss()
        }, 5_000)
    }

    private fun initIntro(view: View) {
        if (is4dImage) {
            binding.lavTuts.setAnimation(R.raw.rotate_tut)
            binding.headerTv.text = getString(R.string.details_intro_4D)
        } else {
            binding.lavTuts.setAnimation(R.raw.swipe_tuts)
            binding.headerTv.text = getString(R.string.msg_scroll_item_wallpaper)
        }
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun dismissAllowingStateLoss() {
        dialogCallback?.invoke()
        super.dismissAllowingStateLoss()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    companion object {
        fun show(fm: FragmentManager, callback: (() -> Unit)? = null, is4D: Boolean = false) {
            val tag = IntroDialog::class.java.name
            if (fm.findFragmentByTag(tag) != null) return
            val dialog = IntroDialog()
            dialog.dialogCallback = callback
            dialog.is4dImage = is4D
            dialog.show(fm, tag)
        }
    }

}