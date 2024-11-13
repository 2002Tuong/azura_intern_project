package com.calltheme.app.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.screentheme.app.databinding.ExitAppDialogLayoutBinding
import com.screentheme.app.utils.extensions.setWidthPercent

class ExitAppPopup : DialogFragment() {

    lateinit var binding: ExitAppDialogLayoutBinding

    var exitAppHandle: (() -> Unit)? = null
    var rateAppHandle: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = ExitAppDialogLayoutBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.apply {

            exitAppButton.setOnClickListener {
                exitAppHandle?.invoke()
            }

            rateAppButton.setOnClickListener {
                rateAppHandle?.invoke()
            }
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
        this.exitAppHandle = callback
    }

    fun onRateApp(callback: () -> Unit) {
        this.rateAppHandle = callback
    }
}
