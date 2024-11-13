package com.example.videoart.batterychargeranimation.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.databinding.CommonErrorDialogBinding
class ErrorPopupFragment(private var callback: () -> Unit = {}) : DialogFragment() {

    private var type = ErrorType.INTERNET

    private val dialogBinding: CommonErrorDialogBinding by lazy {
        CommonErrorDialogBinding.inflate(layoutInflater)
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
        if (type == ErrorType.API) {
//            binding.imgMain.setImageResource(R.drawable.ic_server_error)
//            binding.tvTitle.text = getString(R.string.popup_error_unknown_title)
//            binding.tvContent.text = getString(R.string.popup_error_unknown_body)
        }
    }

    private fun eventSetup() {
        dialogBinding.vGoSetting.setOnClickListener {
            dismiss()
            callback()
        }
    }

    class Builder {
        private var type = ErrorType.INTERNET
        private var callback1 = {}
        fun setDialogType(type: ErrorType): Builder {
            this.type = type
            return this
        }

        fun setOnClick(callback: () -> Unit): Builder {
            this.callback1 = callback
            return  this
        }

        fun build(): ErrorPopupFragment {
            this.type
            return ErrorPopupFragment().apply {
                type = type
                this.callback = callback1
            }
        }
    }

    enum class ErrorType {
        INTERNET, API
    }
}
