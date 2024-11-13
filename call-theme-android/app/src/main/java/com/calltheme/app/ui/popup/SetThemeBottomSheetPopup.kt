package com.calltheme.app.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.screentheme.app.databinding.SetCallThemeModalBottomSheetContentBinding
import com.screentheme.app.utils.extensions.setWidthPercent

class SetThemeBottomSheetPopup(
    private val onWatchAds: () -> Unit,
    private val onBuyPro: () -> Unit
) : DialogFragment() {

    private var binding: SetCallThemeModalBottomSheetContentBinding? = null
//    private val remoteConfig: RemoteConfig by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SetCallThemeModalBottomSheetContentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireNotNull(binding) {
            "viewBinding has not been initialized"
        }
        binding!!.btnWatchAds.setOnClickListener {
            dismiss()
            onWatchAds.invoke()
        }
//        viewBinding!!.btnPremium.isVisible = remoteConfig.enablePremium
//        viewBinding!!.btnPremium.setOnClickListener {
//            dismiss()
//            onBuyPro.invoke()
//        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setWidthPercent(0.9f)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = SetCallThemeModalBottomSheetContentBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding!!.root)

        val dialog = builder.create()
        binding!!.btnWatchAds.setOnClickListener {
            dismiss()
            onWatchAds.invoke()
        }
//        dialog.setOnShowListener {
//            val bottomSheetDialog = dialog as BottomSheetDialog
//            val bottomSheet =
//                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
//            bottomSheet?.setBackgroundResource(R.color.black_background)
//        }
        return dialog
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}