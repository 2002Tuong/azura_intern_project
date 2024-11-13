package com.slideshowmaker.slideshow.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.databinding.PopupPermissionBinding

class PermissionPopupFragment : DialogFragment() {

    var goSettingHandle: (() -> Unit)? = null

    var onDismissedHandle: (() -> Unit)? = null

    private val dialogBinding: PopupPermissionBinding by lazy {
        PopupPermissionBinding.inflate(layoutInflater)
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
        eventSetup()
    }

    private fun eventSetup() {
        dialogBinding.vCancel.setOnClickListener {
            dismiss()
        }
        dialogBinding.vGoSetting.setOnClickListener {
            dismiss()
            goSettingHandle?.invoke()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissedHandle?.invoke()
    }
}
