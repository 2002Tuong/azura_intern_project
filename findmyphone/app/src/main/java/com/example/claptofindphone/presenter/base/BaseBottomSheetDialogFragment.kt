package com.example.claptofindphone.presenter.base

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import com.example.claptofindphone.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    protected open val maxHeightRatio: Float = 1f
    protected open val peekHeight = 0
    protected open val initState = BottomSheetBehavior.STATE_EXPANDED
    protected open val isSkipCollapsed = true
    protected open val isDraggable = false
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        bottomDialog.setOnShowListener { dialog1: DialogInterface ->
            val dialogInterface = dialog1 as BottomSheetDialog
            val bottomSheet =
                dialogInterface.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).apply {
                    maxHeight = (resources.displayMetrics.heightPixels * maxHeightRatio).toInt()
                    peekHeight = this@BaseBottomSheetDialogFragment.peekHeight
                    state = initState
                    skipCollapsed = isSkipCollapsed
                    isDraggable = isDraggable
                }
            }
        }

        return bottomDialog
    }

    override fun getTheme() = R.style.CustomBottomSheetDialogTheme
}