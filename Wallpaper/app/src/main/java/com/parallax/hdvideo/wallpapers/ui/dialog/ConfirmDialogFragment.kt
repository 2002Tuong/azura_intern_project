package com.parallax.hdvideo.wallpapers.ui.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.ui.base.dialog.BaseDialogFragment
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.dpToPx
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConfirmDialogFragment: BaseDialogFragment() {

    private var callbackYesCallback: (() -> Unit)? = null
    private var callbackNoCallbakc: (() -> Unit)? = null
    private var title: String? = null
    private var content: String? = null
    private var showCheckBox: Boolean = false
    private var yesText: String? = null
    private var noText: String? = null
    override var resLayoutId: Int = R.layout.popup_confirm
    private var canBackPress = true
    private var shouldReverseColorButton = false
    private var showHeaderImage = false

    @Inject
    lateinit var storage: LocalStorage

    override fun init(view: View) {
        super.init(view)
        val dialogConfirmBtnYes = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.dialogConfirmBtnYes)
        val dialogConfirmBtnNo = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.dialogConfirmBtnNo)
        val dialogConfirmTitle = view.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.dialogConfirmTitle)
        val dialogConfirmContent = view.findViewById<TextView>(R.id.dialogConfirmContent)
        dialogConfirmBtnYes.setOnClickListener {
            callbackYesCallback?.invoke()
            if (canBackPress) {
                dismissAllowingStateLoss()
            }
        }
        dialogConfirmBtnNo.setOnClickListener {
            callbackNoCallbakc?.invoke()
            if (canBackPress) {
                dismissAllowingStateLoss()
            }
        }

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.closeBtn).setOnClickListener { dismissAllowingStateLoss() }

        yesText?.also {dialogConfirmBtnYes.text = it }
        noText?.also { dialogConfirmBtnNo.text = it }
        dialogConfirmBtnNo.isHidden = callbackNoCallbakc == null
        dialogConfirmTitle.text = title
        dialogConfirmTitle.isHidden = title == null
        dialogConfirmContent.text = content
        dialogConfirmContent.isHidden = content == null
        view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkBox).isHidden = !showCheckBox
        if (showCheckBox) {
            view.findViewById<com.google.android.material.checkbox.MaterialCheckBox>(R.id.checkBox).setOnCheckedChangeListener { _, isChecked ->
                storage.askedViewDownloaded = isChecked
            }
        }

        if(shouldReverseColorButton){
            dialogConfirmBtnYes.background = ContextCompat.getDrawable(requireContext(),R.drawable.bg_btn_send_request_cancel_re)
            dialogConfirmBtnYes.setTextColor(Color.parseColor("#536295"))
            dialogConfirmBtnNo.background = ContextCompat.getDrawable(requireContext(),R.drawable.bg_btn_send_request_re)
            dialogConfirmBtnNo.setTextColor(Color.WHITE)
        }
        view.findViewById<androidx.appcompat.widget.AppCompatImageView>(R.id.ivHeader).isHidden = !showHeaderImage
        if (!showHeaderImage) {
            val clContainer = view.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.clContainer)
            clContainer.setPadding(
                clContainer.paddingLeft,
                dpToPx(20f),
                clContainer.paddingRight,
                clContainer.paddingBottom
            )
        }

    }

    fun setData(
        title: String? = null,
        message: String? = null,
        textNo: String? = null,
        textYes: String? = null,
    ) {
        val dialogConfirmBtnYes = view?.findViewById<com.google.android.material.button.MaterialButton>(R.id.dialogConfirmBtnYes)
        val dialogConfirmBtnNo = view?.findViewById<com.google.android.material.button.MaterialButton>(R.id.dialogConfirmBtnNo)
        val dialogConfirmTitle = view?.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.dialogConfirmTitle)
        val dialogConfirmContent = view?.findViewById<TextView>(R.id.dialogConfirmContent)
        title?.let { dialogConfirmTitle?.text = it }
        message?.let { dialogConfirmContent?.text = it }
        textNo?.let { dialogConfirmBtnNo?.text = it }
        textYes?.let { dialogConfirmBtnYes?.text = it }
    }

    override fun onStart() {
        super.onStart()
        setLayout((AppConfiguration.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onBackPressed(): Boolean {
        return canBackPress
    }

    override fun onDetach() {
        callbackYesCallback = null
        callbackNoCallbakc = null
        super.onDetach()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    companion object {
        const val TAG = "ConfirmDialogFragment"
        fun show(fm: FragmentManager?,
                 title: String? = null,
                 message: String? = null,
                 isShowCheckBox: Boolean = false,
                 textNo: String? = null,
                 textYes: String? = null,
                 canBack: Boolean = true,
                 tag: String? = null,
                 isReverseColorButton : Boolean = false,
                 isShowHeaderImage : Boolean = false,
                 callbackNo: (() -> Unit)? = {},
                 callbackYes: (() -> Unit)? = null) {

            val tags = tag ?: TAG
            val manager = fm ?: return
            synchronized(tags) {
                if (manager.findFragmentByTag(tags) != null) return
                try {
                    val confirmDialog = ConfirmDialogFragment()
                    confirmDialog.showCheckBox = isShowCheckBox
                    confirmDialog.yesText = textYes
                    confirmDialog.noText = textNo
                    confirmDialog.callbackYesCallback = callbackYes
                    confirmDialog.callbackNoCallbakc = callbackNo
                    confirmDialog.title = title
                    confirmDialog.content = message
                    confirmDialog.canBackPress = canBack
                    confirmDialog.shouldReverseColorButton  = isReverseColorButton
                    confirmDialog.showHeaderImage = isShowHeaderImage
                    confirmDialog.setData(title, message, textNo, textYes)
                    confirmDialog.show(manager, tags)
                } catch (e: IllegalStateException) {

                }
            }
        }
    }
}