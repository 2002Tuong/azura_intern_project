package com.slideshowmaker.slideshow.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.fragment.app.DialogFragment
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.databinding.LoadDialogBinding

class LoadingPopupFragment : DialogFragment() {

    private var dialogLabel = ""

    private val dialogBinding: LoadDialogBinding by lazy {
        LoadDialogBinding.inflate(layoutInflater)
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
        eventSetup()
    }

    private fun uiSetup() {
        if (dialogLabel.isNotEmpty()) {
            dialogBinding.vContent.text = dialogLabel
            dialogBinding.vContent.visibility = View.VISIBLE
        }
    }

    private fun eventSetup() {
        dialogBinding.vSpinner.startAnimation(setAnimationLoading())
    }

    companion object {
        const val TAG = "LoadingDialogFragment"
        private const val DIALOG_TITLE_KEY = "dialog_title"

        fun newInstance(
            dialogTile: String = "",
        ): DialogFragment {
            return LoadingPopupFragment().apply {
                arguments = Bundle().apply {
                    putString(DIALOG_TITLE_KEY, dialogTile)
                }
            }
        }
    }

    private fun setAnimationLoading(): RotateAnimation {
        val rotateAnimation = RotateAnimation(
            0.0f,
            360.0f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
        )
        rotateAnimation.interpolator = LinearInterpolator()
        rotateAnimation.repeatCount = Animation.INFINITE
        rotateAnimation.duration = 700

        return rotateAnimation
    }
}
