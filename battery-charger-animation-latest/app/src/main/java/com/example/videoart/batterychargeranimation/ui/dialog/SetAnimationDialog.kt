package com.example.videoart.batterychargeranimation.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.data.local.PreferenceUtils
import com.example.videoart.batterychargeranimation.databinding.SetAnimationDialogBinding

class SetAnimationDialog(
    private var soundCallback: (Boolean) -> Unit = {},
    private var durationCallback: () -> Unit = {},
    private var closingMethodCallback: () -> Unit = {},
    private var setAnimationCallback: () -> Unit = {}
) : DialogFragment() {
    private lateinit var binding: SetAnimationDialogBinding

    fun setSoundCallback(callback: (Boolean) -> Unit) {
        soundCallback = callback
    }

    fun setDurationCallback(callback: () -> Unit) {
        durationCallback = callback
    }

    fun setClosingMethodCallback(callback: () -> Unit) {
        closingMethodCallback = callback
    }

    fun setOnSetAnimationBtn(callback: () -> Unit) {
        setAnimationCallback = callback
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = SetAnimationDialogBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(requireActivity())
            .setView(binding.root)
        val dialog = builder.create()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_dialog)

        binding.apply {
            swithSound.isChecked = PreferenceUtils.soundActive
            swithSound.setOnCheckedChangeListener { buttonView, isChecked ->
                soundCallback.invoke(isChecked)}
            btnSelectDuration.setOnClickListener { durationCallback.invoke() }
            btnSelectCloseMethod.setOnClickListener { closingMethodCallback.invoke() }
            btnSetAnimation.setOnClickListener {
                dismiss()
                setAnimationCallback.invoke() }
        }

        PreferenceUtils.durationLiveData.observe(this) {
            binding.duration.text = getString(it.stringId)
        }

        PreferenceUtils.closingMethodLiveData.observe(this) {
            binding.closeType.text = getString(it.stringId)
        }
        return dialog

    }
}