package com.example.claptofindphone.presenter.dialog

import android.app.Dialog
import android.graphics.Point
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.example.claptofindphone.BuildConfig
import com.example.claptofindphone.R
import com.example.claptofindphone.databinding.RatingDialogLayoutBinding
import com.example.claptofindphone.utils.extensions.sendEmail
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory


class InappRatingDialog : DialogFragment() {

    lateinit var layoutBinding: RatingDialogLayoutBinding


    private lateinit var reviewManagerInstance: ReviewManager
    private var reviewInfoInstance: ReviewInfo? = null
    var userRating: Int = 0
        set(value) {
            field = value

            layoutBinding.apply {
                rateUsButton.isEnabled = userRating > 0
                if (userRating > 0) {
                    rateUsButton.setBackgroundResource(R.drawable.bg_button_enable)
                } else {
                    rateUsButton.setBackgroundResource(R.drawable.bg_button_disabled)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        reviewManagerInstance = ReviewManagerFactory.create(requireContext())
        reviewManagerInstance.requestReviewFlow().addOnCompleteListener {
            if (it.isSuccessful)
                reviewInfoInstance = it.result
        }
    }

    override fun onResume() {
        val window = dialog?.window
        val size = Point()
        val display = window?.windowManager?.defaultDisplay
        display?.getSize(size)
        window?.setLayout((size.x * 0.8f).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
        super.onResume()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        layoutBinding = RatingDialogLayoutBinding.inflate(layoutInflater)

        val dialogBuilder = AlertDialog.Builder(requireActivity())
        dialogBuilder.setView(layoutBinding.root)

        val dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        layoutBinding.apply {

            userRating = 0
            rateUsButton.isEnabled = false

            ratingBar.setRatingChangeListener(object : CustomRatingBar.RatingChangeListener {
                override fun onRatingChanged(rating: Int) {
                    userRating = rating
                }

            })

            btnClose.setOnClickListener {
                dismiss()
            }

            rateUsButton.setOnClickListener {
                if (userRating in 1..3) {
                    requireContext().sendEmail(
                        requireContext().getString(R.string.contact_email),
                        getString(R.string.email_feedback_label, BuildConfig.VERSION_NAME)
                    )
                } else if (userRating <= 5) {
                    requestReview()
                }

                dismiss()
            }
        }


        isCancelable = false

        return dialog
    }

    fun show(activity: FragmentActivity) {
        show(activity.supportFragmentManager, "RatingAppDialog")
    }

    private fun requestReview() {
        reviewInfoInstance?.let {
            val reviewFlow = reviewManagerInstance.launchReviewFlow(requireActivity(), it)
            reviewFlow.addOnCompleteListener { _ ->

            }
        }

    }
}
