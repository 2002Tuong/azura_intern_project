package com.calltheme.app.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.screentheme.app.R

class DialogLoadingFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val progressDialog = AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_loading) // Replace with your custom layout or progress indicator
            .setCancelable(false)
            .create()

        // Set dialog background to transparent
        progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        return progressDialog
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (tag == "loading_dialog" && isAdded) return
        super.show(manager, tag)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }
}
