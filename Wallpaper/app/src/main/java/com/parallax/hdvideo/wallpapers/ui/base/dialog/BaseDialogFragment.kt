package com.parallax.hdvideo.wallpapers.ui.base.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.parallax.hdvideo.wallpapers.ui.base.activity.BaseActivity
import com.parallax.hdvideo.wallpapers.utils.other.KeyboardHelper

abstract class BaseDialogFragment : DialogFragment() {

    open var canceledWhenTouchOutside: Boolean = false
    open var dimension: Float = 0.4f
    private var mDialogView: Dialog? = null
    protected abstract val resLayoutId: Int
    var rootView: View? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val baseDialog = BaseDialog(requireContext(), theme)
        baseDialog.onBackCallback = this::onBackPressed
        baseDialog.isTouchOutside = canceledWhenTouchOutside
        baseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        try {
            val mView = baseDialog.layoutInflater.inflate(resLayoutId, null, false)
            baseDialog.setContentView(mView)
            baseDialog.setCanceledOnTouchOutside(canceledWhenTouchOutside)
            val window = baseDialog.window!!
            window.setGravity(Gravity.CENTER)
            setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setDimAmount(dimension)
            this.rootView = mView
            mDialogView = baseDialog
            init(mView)
        } catch (e: Exception) {}
        return baseDialog
    }

    override fun onDetach() {
        mDialogView = null
        super.onDetach()
        rootView = null
    }

    fun setLayout(width: Int, height: Int) {
        mDialogView?.window?.setLayout(width, height)
    }

    protected open fun init(view: View) {

    }

    open fun onBackPressed() : Boolean {
        return true
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.isDestroyed || manager.isStateSaved) return
        try {
            super.show(manager, tag)
        } catch (e: Exception) {

        }
    }

    private class BaseDialog: Dialog {

        var onBackCallback: (() -> Boolean)? = null
        var isTouchOutside: Boolean = false
        constructor(context: Context) : super(context)
        constructor(context: Context, themeResId: Int) : super(context, themeResId)
        constructor(context: Context, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener?) : super(context, cancelable, cancelListener)

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (isTouchOutside && event.action == MotionEvent.ACTION_UP) {
                (ownerActivity as? BaseActivity)?.onBackPressedLoading()
            }
            KeyboardHelper.touchEvent(event, currentFocus)
            return super.onTouchEvent(event)
        }

        override fun onBackPressed() {
            val check = onBackCallback?.invoke() ?: false
            if (check) {
                (ownerActivity as? BaseActivity)?.onBackPressedLoading()
                super.onBackPressed()
            }
        }
    }
}
