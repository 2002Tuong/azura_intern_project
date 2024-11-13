package com.parallax.hdvideo.wallpapers.ui.dialog

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.View
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.PopupRequestWallBinding
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.extension.pushFragment
import com.parallax.hdvideo.wallpapers.ui.base.dialog.BaseDialogFragment
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.ui.request.RequestFragment
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration


class RequestWallDialog : BaseDialogFragment() {

    private var callbackOnClickYes: (() -> Unit)? = null
    private var callbackOnClickNo: (() -> Unit)? = null
    private var title: String? = null
    private var content: String? = null
    private var yesText: String? = null
    private var noText: String? = null
    private var resId: Int? = null
    override var canceledWhenTouchOutside = false
    private var hideTextToSearch: Boolean = false

    override val resLayoutId: Int
        get() = R.layout.popup_request_wall
    private lateinit var binding: PopupRequestWallBinding
    override fun init(view: View) {
        super.init(view)
        binding = PopupRequestWallBinding.bind(view)
        view.layoutParams.apply {
            width = (AppConfiguration.displayMetrics.widthPixels * 0.8).toInt()
        }

        val dialogType = arguments?.getString(TYPE_DIALOG_KEY) ?: REQUEST_WALLPAPER_TYPE
        if (dialogType == THANK_FEEDBACK_TYPE) {
            binding.btnSkip.isHidden = true
            binding.btnRequestWall.text = getString(R.string.ok)
            binding.txtDirectSearchMsg.isHidden = true
            binding.txtInviteMsg.text = getString(R.string.thanks_feedback_message)
            binding.ivHeader.setImageResource(R.drawable.img_header_thanks)
            binding.btnRequestWall.setOnClickListener {
                callbackOnClickYes?.invoke()
                dismiss()
            }
        } else {
            binding.btnSkip.isHidden = false
            binding.btnRequestWall.text = getString(R.string.requested_wall_title)
            binding.txtInviteMsg.typeface = Typeface.DEFAULT_BOLD
            binding.txtInviteMsg.text = getString(R.string.popup_request_wall_msg)
            if ((activity as MainActivity).viewModel.useSearchFeature) {
                binding.txtDirectSearchMsg.isHidden = true
                binding.ivHeader.setImageResource(R.drawable.img_requestwall)
            } else {
                binding.ivHeader.setImageResource(R.drawable.img_requestwall_search)
                binding.txtDirectSearchMsg.isHidden = false
                binding.txtDirectSearchMsg.text = buildSpannableStringToSearch()
                binding.txtDirectSearchMsg.movementMethod = LinkMovementMethod.getInstance()
            }
            binding.btnRequestWall.setOnClickListener {
                pushFragment(
                    RequestFragment(),
                    animate = true,
                    singleton = true,
                    tag = RequestFragment.TAGS + "_DIALOG_REQUEST_WALL"
                )
                callbackOnClickYes?.invoke()
                dismiss()
            }
        }

        yesText?.also { binding.btnRequestWall.text = it }
        noText?.also { binding.btnSkip.text = it }
        resId?.also { binding.ivHeader.setImageResource(it) }
        title?.also { binding.txtInviteMsg.text = it }
        if (hideTextToSearch) {
            binding.txtDirectSearchMsg.isHidden = true
            binding.btnRequestWall.setOnClickListener {
                callbackOnClickYes?.invoke()
                dismiss()
            }
        }
        binding.btnSkip.setOnClickListener {
            callbackOnClickNo?.invoke()
            dismiss()
        }


    }

    private fun buildSpannableStringToSearch(): SpannableStringBuilder {

        val spannableStringBuilder = SpannableStringBuilder()
        spannableStringBuilder.append(getString(R.string.popup_request_wall_msg_direct_search))
        spannableStringBuilder.append(" ")
        val index = spannableStringBuilder.length
        spannableStringBuilder.append(getString(R.string.here_upper))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // navigate to search screen
                (activity as MainActivity).navigationController.popToRoot()
                (activity as MainActivity).navigateToTab(2)
                dismiss()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.parseColor("#FF9541E6")
                ds.isUnderlineText = true
            }
        }
        spannableStringBuilder.setSpan(
            clickableSpan,
            index,
            spannableStringBuilder.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableStringBuilder.setSpan(
            ForegroundColorSpan(Color.parseColor("#FF9541E6")),
            index,
            spannableStringBuilder.length,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        spannableStringBuilder.setSpan(
            UnderlineSpan(), index,
            spannableStringBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableStringBuilder
    }

    companion object {
        const val TYPE_DIALOG_KEY = "TYPE_DIALOG_KEY"
        const val THANK_FEEDBACK_TYPE = "THANK_FEEDBACK_TYPE"
        const val REQUEST_WALLPAPER_TYPE = "REQUEST_WALLPAPER_TYPE"

        fun newInstance(
            isTypeRequest: Boolean,
            title: String? = null,
            textNo: String? = null,
            textYes: String? = null,
            resourceId: Int? = null,
            callbackNo: (() -> Unit)? = {},
            callbackYes: (() -> Unit)? = null,
            isHiddenTextToSearch: Boolean = false,
        ): RequestWallDialog {
            val bundle = Bundle()
            val typeDialog = if (isTypeRequest) REQUEST_WALLPAPER_TYPE else THANK_FEEDBACK_TYPE
            bundle.putString(TYPE_DIALOG_KEY, typeDialog)
            val dialog = RequestWallDialog()
            dialog.yesText = textYes
            dialog.noText = textNo
            dialog.callbackOnClickYes = callbackYes
            dialog.callbackOnClickNo = callbackNo
            dialog.title = title
            dialog.resId = resourceId
            dialog.arguments = bundle
            dialog.hideTextToSearch = isHiddenTextToSearch
            return dialog
        }
    }
}