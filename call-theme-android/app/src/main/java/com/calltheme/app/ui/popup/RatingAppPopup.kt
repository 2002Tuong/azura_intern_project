package com.calltheme.app.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.calltheme.app.ui.component.CustomRatingBar
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.screentheme.app.BuildConfig
import com.screentheme.app.R
import com.screentheme.app.databinding.RatingAppDialogLayoutBinding
import com.screentheme.app.utils.extensions.composeEmail
import com.screentheme.app.utils.extensions.setWidthPercent


class RatingAppPopup : DialogFragment() {

    lateinit var binding: RatingAppDialogLayoutBinding


    private lateinit var reviewManagerInstance: ReviewManager
    private var reviewInfoInstance: ReviewInfo? = null
    var userRate: Int = 0
        set(value) {
            field = value

            binding.apply {
                rateUsButton.isEnabled = userRate > 0
                if (userRate > 0) {
                    rateUsButton.setBackgroundResource(R.drawable.set_call_theme_btn_bg)
                } else {
                    rateUsButton.setBackgroundResource(R.drawable.set_call_theme_disabled_btn_bg)
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
        setWidthPercent(0.8f)
        super.onResume()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = RatingAppDialogLayoutBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        binding.apply {

            userRate = 0
            rateUsButton.isEnabled = false

            ratingBar.setRatingChangeListener(object : CustomRatingBar.RatingChangeListener {
                override fun onRatingChanged(rating: Int) {
                    userRate = rating
                }

            })

            btnClose.setOnClickListener {
                dismiss()
            }

            rateUsButton.setOnClickListener {
                if (userRate in 1..3) {
                    requireContext().composeEmail(
                        requireContext().getString(R.string.contact_email),
                        getString(R.string.email_feedback_title, BuildConfig.VERSION_NAME)
                    )
                } else if (userRate <= 5) {
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
            val flow = reviewManagerInstance.launchReviewFlow(requireActivity(), it)
            flow.addOnCompleteListener { _ ->

            }
        }

    }
}
