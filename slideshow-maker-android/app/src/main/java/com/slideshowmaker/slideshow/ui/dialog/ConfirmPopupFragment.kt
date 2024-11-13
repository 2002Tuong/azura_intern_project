package com.slideshowmaker.slideshow.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.databinding.PopupConfirmBinding

class ConfirmPopupFragment : DialogFragment() {

    private var label: String = ""
    private var message: String = ""
    private var noText: String = ""
    private var yesText: String = ""
    private var onYesClickCallBack: (() -> Unit)? = null
    private var onNoClickCallBack: (() -> Unit)? = null

    private val dialogBinding: PopupConfirmBinding by lazy {
        PopupConfirmBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CenterDialog)
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
        if (label.isNotEmpty()) {
            dialogBinding.tvTitle.text = label
        } else {
            dialogBinding.tvTitle.visibility = View.GONE
        }

        dialogBinding.tvContent.text = this.message
        dialogBinding.tvCancel.text = this.noText.ifEmpty { getString(R.string.regular_cancel) }
        dialogBinding.tvConfirm.text = this.yesText.ifEmpty { getString(R.string.regular_ok) }
    }

    private fun eventSetup() {
        dialogBinding.vCancel.setOnClickListener {
            onNoClickCallBack?.invoke()
            dismiss()
        }
        dialogBinding.vConfirm.setOnClickListener {
            dismiss()
            onYesClickCallBack?.invoke()
        }
        dialogBinding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    class Builder {
        var label = ""
        var message = ""
        var noText = ""
        var yesText = ""
        var onYesClickCallBack: (() -> Unit)? = null
        var onNoClickCallBack: (() -> Unit)? = null

        fun setTitle(title: String): Builder {
            this.label = title
            return this
        }

        fun setContent(content: String): Builder {
            this.message = content
            return this
        }

        fun setCancelText(cancel: String): Builder {
            this.noText = cancel
            return this
        }

        fun setOkText(ok: String): Builder {
            this.yesText = ok
            return this
        }

        fun setOkCallBack(callback: () -> Unit): Builder {
            this.onYesClickCallBack = callback
            return this
        }

        fun setCancelCallBack(callback: (() -> Unit)? = null): Builder {
            this.onNoClickCallBack = callback
            return this
        }

        fun build(): ConfirmPopupFragment {
            val dialog = ConfirmPopupFragment()
            dialog.label = this.label
            dialog.message = this.message
            dialog.noText = this.noText
            dialog.yesText = this.yesText
            dialog.onYesClickCallBack = this.onYesClickCallBack
            dialog.onNoClickCallBack = this.onNoClickCallBack

            return dialog
        }
    }
}
