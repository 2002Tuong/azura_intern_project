package com.example.claptofindphone.presenter.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.example.claptofindphone.databinding.ExitDialogLayoutBinding
import com.example.claptofindphone.utils.extensions.setWidthPercent

class ExitDialog : DialogFragment() {
    private lateinit var layoutBinding: ExitDialogLayoutBinding

    private var onClickExitApp: (() -> Unit)? = null
    private var onClickRateApp: (() -> Unit)? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        layoutBinding = ExitDialogLayoutBinding.inflate(layoutInflater)
        val Dialogbuilder = AlertDialog.Builder(requireActivity())
        Dialogbuilder.setView(layoutBinding.root)

        val dialog = Dialogbuilder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        layoutBinding.exitAppButton.setOnClickListener {
            onClickExitApp?.invoke()
        }

        layoutBinding.rateAppButton.setOnClickListener {
            onClickRateApp?.invoke()
        }
        isCancelable = true

        return dialog
    }

    override fun onResume() {
        setWidthPercent(0.8f)
        super.onResume()
    }

    fun show(activity: FragmentActivity) {
        show(activity.supportFragmentManager, "ExitAppDialog")
    }

    fun onExitApp(callback: () -> Unit) {
        this.onClickExitApp = callback
    }

    fun onRateApp(callback: () -> Unit) {
        this.onClickRateApp = callback
    }
}