package com.parallax.hdvideo.wallpapers.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.PopupConfirmSetWallpaperBinding
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.extension.setOnClickListener
import com.parallax.hdvideo.wallpapers.ui.base.activity.BaseActivity
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.other.KeyboardHelper

class SetWallpaperDialog : BottomSheetDialogFragment() {

    private var canceledOnTouchOutside = true
    private lateinit var mDialog : Dialog

    var dialogCallback: ((index: Int) -> Unit)? = null
    var dialogOnDismiss : (() -> Unit)? = null
    val layoutId: Int = R.layout.popup_confirm_set_wallpaper
    private lateinit var binding: PopupConfirmSetWallpaperBinding
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        setStyle(STYLE_NORMAL, R.style.bottomSheetSetWallpaper)
        val dialog = SetWallpaperBottomSheetDialog(requireContext(), theme)
        dialog.isCancelOnTouchOutside = canceledOnTouchOutside
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (AppConfiguration.heightScreenValue * 0.4171875).toInt())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val mView = dialog.layoutInflater.inflate(layoutId, null, false)
        dialog.setContentView(mView)
        val window = dialog.window!!
        window.setGravity(Gravity.CENTER)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        mDialog = dialog
        init(mView)
        return dialog
    }

    fun init(view: View) {
        binding = PopupConfirmSetWallpaperBinding.bind(view)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            binding.dialogConfirmBoth.isHidden = true
            binding.dialogConfirmLock.isHidden = true
        }
        setOnClickListener(binding.dialogConfirmHome, binding.dialogConfirmLock, binding.dialogConfirmBoth) { v ->
            val tag = v.tag!!.toString().toInt() //  1, 2, 3
            dialogCallback?.invoke(tag)
            dismiss()
        }
        setOnClickListener(binding.btnSkip) {
            dismiss()
        }
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,(AppConfiguration.heightScreenValue * 0.4171875f).toInt())
    }

    override fun onCancel(dialog: DialogInterface) {
        dialogOnDismiss?.invoke()
        super.onCancel(dialog)
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.isDestroyed || manager.isStateSaved) return
        try {
            super.show(manager, tag)
        } catch (e: Exception) {

        }
    }

    companion object {
        private var setWallpaperDialog: SetWallpaperDialog? = null
        fun show(
            fm: FragmentManager?,
            callback: ((index: Int) -> Unit)? = null,
            onDismiss: (() -> Unit)? = null
        ) {
            val fragmentManager = fm ?: return
            val tag = "SetWallpaperDialog"
            synchronized(tag) {
                if (setWallpaperDialog != null) return
                val confirmDialog = SetWallpaperDialog()
                setWallpaperDialog = confirmDialog
                confirmDialog.dialogCallback = callback
                confirmDialog.dialogOnDismiss = onDismiss
                confirmDialog.show(fragmentManager, tag)
            }
        }

        fun dismiss() {
            setWallpaperDialog?.dismiss()
        }
    }


    override fun onDetach() {
        dialogCallback = null
        setWallpaperDialog = null
        super.onDetach()
    }

    private class SetWallpaperBottomSheetDialog : BottomSheetDialog {
        var isCancelOnTouchOutside : Boolean = false
        var onBackPressListener: (() -> Boolean)? = null
        constructor(context: Context) : super(context)
        constructor(context: Context, themeResId : Int) : super(context, themeResId)
        constructor(context: Context, cancelable: Boolean, cancelListener: DialogInterface.OnCancelListener?) : super(context, cancelable, cancelListener)

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (isCancelOnTouchOutside && event.action == MotionEvent.ACTION_UP) {
                (ownerActivity as? BaseActivity)?.onBackPressedLoading()
            }
            KeyboardHelper.touchEvent(event, currentFocus)
            return super.onTouchEvent(event)
        }

        override fun onBackPressed() {
            val check = onBackPressListener?.invoke() ?: false
            if(check) {
                (ownerActivity as? BaseActivity)?.onBackPressedLoading()
                super.onBackPressed()
            }
        }
    }
}