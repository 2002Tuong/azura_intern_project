package com.example.videoart.batterychargeranimation.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.databinding.CompleteDialogBinding
import com.example.videoart.batterychargeranimation.databinding.SetAnimationDialogBinding

class CompleteDialog(
    private val doneCallback: () -> Unit = {}
) : DialogFragment(){
    private lateinit var binding: CompleteDialogBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = CompleteDialogBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireActivity())
            .setView(binding.root)
        val dialog = builder.create()

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog)

        binding.completeBtn.setOnClickListener {
            dismiss()
            //doneCallback.invoke()
        }

        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        doneCallback.invoke()
    }
}
