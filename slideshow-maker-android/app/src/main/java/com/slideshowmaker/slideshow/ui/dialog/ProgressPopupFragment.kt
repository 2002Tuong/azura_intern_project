package com.slideshowmaker.slideshow.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.databinding.LayoutProgressDialogBinding

class ProgressPopupFragment : DialogFragment() {

    private var dialogLabel = ""

    private val dialogBinding: LayoutProgressDialogBinding by lazy {
        LayoutProgressDialogBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.LoadingDialogStyle)
        dialogLabel = arguments?.getString(DIALOG_TITLE_KEY, "") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        isCancelable = false
        return dialogBinding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiSetup()
    }

    private fun uiSetup() {
        if (dialogLabel.isNotEmpty()) {
            dialogBinding.vTitle.text = dialogLabel
            dialogBinding.vTitle.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val DIALOG_TITLE_KEY = "dialog_title"

        fun newInstance(
            dialogTile: String = "",
        ): DialogFragment {
            return ProgressPopupFragment().apply {
                arguments = Bundle().apply {
                    putString(DIALOG_TITLE_KEY, dialogTile)
                }
            }
        }
    }

    fun updateProgress(progress: Int) {
        if (isAdded) {
            dialogBinding.progress.progress = progress
            dialogBinding.tvProgress.text = "$progress%"
        }
    }
}
