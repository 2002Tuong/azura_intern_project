package com.example.videoart.batterychargeranimation.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.data.local.PreferenceUtils
import com.example.videoart.batterychargeranimation.databinding.SelectClosingMethodBottomSheetBinding
import com.example.videoart.batterychargeranimation.model.ClosingMethod
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectClosingMethodBottomSheet: BottomSheetDialogFragment() {
    private lateinit var binding: SelectClosingMethodBottomSheetBinding
    private var closingMethod = ClosingMethod.SINGLE_TAP

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SelectClosingMethodBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setUpViews()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheetDialog = dialog as BottomSheetDialog
            val bottomSheet =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
        }
        return  dialog
    }

    private fun setUpViews() {
        closingMethod = PreferenceUtils.closingMethod
        when(closingMethod) {
            ClosingMethod.SINGLE_TAP -> radioButtonHandle(R.id.option_1)
            ClosingMethod.DOUBLE_TAP -> radioButtonHandle(R.id.option_2)
        }

        binding.option1.setOnClickListener {
            closingMethod = ClosingMethod.SINGLE_TAP
            radioButtonHandle(R.id.option_1)
        }

        binding.option2.setOnClickListener {
            closingMethod = ClosingMethod.DOUBLE_TAP
            radioButtonHandle(R.id.option_2)
        }

        binding.applyBtn.setOnClickListener {
            PreferenceUtils.closingMethod = closingMethod
            dismiss()
        }
    }

    fun radioButtonHandle(radioBtnId: Int) {
        when(radioBtnId) {
            R.id.option_1 -> {
                binding.radiobtn1.isChecked = true
                binding.radiobtn2.isChecked = false
            }
            R.id.option_2 -> {
                binding.radiobtn1.isChecked = false
                binding.radiobtn2.isChecked = true
            }
        }
    }
}