package com.slideshowmaker.slideshow.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.databinding.PopupCommonBinding
import com.slideshowmaker.slideshow.utils.extentions.orFalse

class SlideshowMakerDialogFragment : DialogFragment() {
    private var onDismissHandle: (() -> Unit)? = null
    var label: String = ""
    var content: CharSequence = ""
    var primaryBtn: Builder.Button? = null
    var secondaryBtn: Builder.Button? = null
    var imgResId: Int? = null
    var gravity: Int = Gravity.CENTER

    private val dialogBinding: PopupCommonBinding by lazy {
        PopupCommonBinding.inflate(layoutInflater)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.8).toInt(), WRAP_CONTENT)
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissHandle?.invoke()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        dialog?.window?.setBackgroundDrawableResource(R.drawable.background_rounded_action_sheet)
        isCancelable = false
        return dialogBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiSetup()
        eventSetup()
    }

    private fun uiSetup() {
        dialogBinding.tvTitle.isVisible = label.isNotEmpty()
        dialogBinding.tvTitle.text = label
        dialogBinding.tvContent.text = this.content
        dialogBinding.vSecondary.isVisible =
            this.secondaryBtn != null && !this.secondaryBtn?.label?.isNullOrEmpty().orFalse()
        dialogBinding.vPrimary.isVisible = this.primaryBtn != null
        dialogBinding.vSecondary.text = this.secondaryBtn?.label
        dialogBinding.vPrimary.text = this.primaryBtn?.label
        imgResId?.let { dialogBinding.ivIcon.setImageResource(it) }
        dialogBinding.ivIcon.isVisible = imgResId != null
        dialogBinding.tvTitle.gravity = gravity
        dialogBinding.tvContent.gravity = gravity
    }

    private fun eventSetup() {
        dialogBinding.vSecondary.setOnClickListener {
            secondaryBtn?.onClickListener(this.dialog)
        }
        dialogBinding.vPrimary.setOnClickListener {
            primaryBtn?.onClickListener(this.dialog)
        }
    }

    class Builder {
        var label: String = ""
        var content: CharSequence = ""
        var primaryBtn: Button? = null
        var secondaryBtn: Button? = null
        var cancelEnable = false
        var imgResId: Int? = null
        var gravity: Int = Gravity.CENTER
        var onDismissHandle: (() -> Unit)? = null
        fun setTitle(title: String): Builder {
            this.label = title
            return this
        }

        fun setPrimaryButton(primaryButton: Button): Builder {
            this.primaryBtn = primaryButton
            return this
        }

        fun setSecondaryButton(secondaryButton: Button): Builder {
            this.secondaryBtn = secondaryButton
            return this
        }

        fun setMessage(message: CharSequence): Builder {
            this.content = message
            return this
        }

        fun setCancelable(cancelable: Boolean): Builder {
            this.cancelEnable = cancelable
            return this
        }

        fun setImageRes(imageResId: Int): Builder {
            this.imgResId = imageResId
            return this
        }

        fun setGravity(gravity: Int): Builder {
            this.gravity = gravity
            return this
        }

        fun setOnDismissListener(onDismissListener: () -> Unit): Builder {
            this.onDismissHandle = onDismissListener
            return this
        }

        fun build(): SlideshowMakerDialogFragment {
            val dialog = SlideshowMakerDialogFragment()
            dialog.label = label
            dialog.content = content
            dialog.label = label
            dialog.primaryBtn = primaryBtn
            dialog.secondaryBtn = secondaryBtn
            dialog.isCancelable = cancelEnable
            dialog.imgResId = imgResId
            dialog.gravity = gravity
            dialog.onDismissHandle = onDismissHandle
            return dialog
        }

        interface Button {
            val label: String
            fun onClickListener(dialog: Dialog?)
        }
    }
}
