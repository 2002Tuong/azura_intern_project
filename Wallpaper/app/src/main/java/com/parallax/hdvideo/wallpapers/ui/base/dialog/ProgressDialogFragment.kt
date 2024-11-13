package com.parallax.hdvideo.wallpapers.ui.base.dialog

import android.content.DialogInterface
import android.view.View
import com.google.gson.Gson
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.utils.dpToPx

class ProgressDialogFragment : BaseDialogFragment() {

    var isGoBack = false

    override val resLayoutId: Int = R.layout.progress_bar
    override var dimension: Float = 0.2f
    override fun init(view: View) {
        super.init(view)
    }

    override fun onStart() {
        super.onStart()
        rootView?.layoutParams?.apply {
            width = dpToPx(60f)
            height = dpToPx(60f)
        }
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    override fun onBackPressed(): Boolean {
        return isGoBack
    }

    data class Configure(val status: Boolean, val touchOutside: Boolean, val canGoBack: Boolean) {
        override fun toString(): String {
            return "Configure = " + Gson().toJson(this)
        }
    }

}
