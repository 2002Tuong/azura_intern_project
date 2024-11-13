package com.example.videoart.batterychargeranimation.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.videoart.batterychargeranimation.R

class LoadingDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val progressDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading) // Replace with your custom layout or progress indicator
            .setCancelable(false)
            .create()

        // Set dialog background to transparent
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        return progressDialog
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
}
