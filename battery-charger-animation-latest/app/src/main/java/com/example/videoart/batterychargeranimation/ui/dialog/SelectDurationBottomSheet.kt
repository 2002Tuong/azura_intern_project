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
import com.example.videoart.batterychargeranimation.databinding.SelectDurationBottomSheetBinding
import com.example.videoart.batterychargeranimation.model.Duration
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectDurationBottomSheet : BottomSheetDialogFragment() {
    private lateinit var  binding: SelectDurationBottomSheetBinding
    private var duration = Duration.DURATION_5
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SelectDurationBottomSheetBinding.inflate(layoutInflater)
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


        duration = PreferenceUtils.duration
        when(duration) {
            Duration.DURATION_5 -> radioButtonHandle(R.id.option_1)
            Duration.DURATION_10 -> radioButtonHandle(R.id.option_2)
            Duration.DURATION_30 -> radioButtonHandle(R.id.option_3)
            Duration.DURATION_ALWAYS -> radioButtonHandle(R.id.option_4)
        }

        binding.option1.setOnClickListener {
            duration = Duration.DURATION_5
            radioButtonHandle(R.id.option_1)
        }

        binding.option2.setOnClickListener {
            duration = Duration.DURATION_10
            radioButtonHandle(R.id.option_2)
        }

        binding.option3.setOnClickListener {
            duration = Duration.DURATION_30
            radioButtonHandle(R.id.option_3)
        }

        binding.option4.setOnClickListener {
            duration = Duration.DURATION_ALWAYS
            radioButtonHandle(R.id.option_4)
        }

        binding.applyBtn.setOnClickListener {
            PreferenceUtils.duration = duration
            dismiss()
        }
    }

    fun radioButtonHandle(radioBtnId: Int) {
        when(radioBtnId) {
            R.id.option_1 -> {
                binding.radiobtn1.isChecked = true
                binding.radiobtn2.isChecked = false
                binding.radiobtn3.isChecked = false
                binding.radiobtn4.isChecked = false
            }
            R.id.option_2 -> {
                binding.radiobtn1.isChecked = false
                binding.radiobtn2.isChecked = true
                binding.radiobtn3.isChecked = false
                binding.radiobtn4.isChecked = false
            }
            R.id.option_3 -> {
                binding.radiobtn1.isChecked = false
                binding.radiobtn2.isChecked = false
                binding.radiobtn3.isChecked = true
                binding.radiobtn4.isChecked = false
            }
            R.id.option_4 -> {
                binding.radiobtn1.isChecked = false
                binding.radiobtn2.isChecked = false
                binding.radiobtn3.isChecked = false
                binding.radiobtn4.isChecked = true
            }
        }
    }
}