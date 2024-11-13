package com.parallax.hdvideo.wallpapers.ui.dialog

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.google.android.material.checkbox.MaterialCheckBox
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.PopupHardUseFeedbackBinding
import com.parallax.hdvideo.wallpapers.ui.base.dialog.BaseDialogFragment
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration

class HardUseFeedbackDialog : BaseDialogFragment() {

    private val arrayCheckBox by lazy {
        val view = rootView ?: return@lazy arrayOf<CheckBox>()
        return@lazy arrayOf<CheckBox>(
            binding.checkBox1, binding.checkBox2,
            binding.checkBox4, binding.checkBox5
        )
    }

    private val keyContent by lazy {
        val view = rootView ?: return@lazy emptyMap<MaterialCheckBox, String>()
        return@lazy mapOf(
            binding.checkBox1 to "kobietcaidat",
            binding.checkBox2 to "kobiettai",
            binding.checkBox4 to "kobietdatnoidungmacdinh",
            binding.checkBox5 to "kohieungonngu",
        )
    }
    private lateinit var binding: PopupHardUseFeedbackBinding

    override val resLayoutId: Int
        get() = R.layout.popup_hard_use_feedback

    override fun init(view: View) {
        super.init(view)
        binding = PopupHardUseFeedbackBinding.bind(view)
        binding.btnSkip.setOnClickListener {
            dismiss()
        }

        binding.btnSubmit.setOnClickListener {
            RequestWallDialog.newInstance(false)
                .show(requireActivity().supportFragmentManager, RequestWallDialog::class.java.name)
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        setLayout(AppConfiguration.widthScreenValue * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}