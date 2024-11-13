package com.wifi.wificharger.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.wifi.wificharger.ui.components.CustomRatingBar
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.wifi.wificharger.BuildConfig
import com.wifi.wificharger.R
import com.wifi.wificharger.databinding.RatingAppDialogLayoutBinding
import com.wifi.wificharger.utils.composeEmail
import com.wifi.wificharger.utils.setWidthPercent

class RatingAppDialog : DialogFragment() {

    lateinit var binding: RatingAppDialogLayoutBinding
    var onDismiss: () -> Unit = {}
    var onFinishRate: () -> Unit = {}

    private lateinit var reviewManager: ReviewManager
    private var reviewInfo: ReviewInfo? = null
    var userRating: Int = 0
        set(value) {
            field = value

            binding.apply {
                rateUsButton.isEnabled = userRating > 0
                if (userRating > 0) {
                    rateUsButton.setBackgroundResource(R.drawable.rounded_blue_background)
                } else {
                    rateUsButton.setBackgroundResource(R.drawable.disable_button_bg)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        reviewManager = ReviewManagerFactory.create(requireContext())
        reviewManager.requestReviewFlow().addOnCompleteListener {
            if (it.isSuccessful)
                reviewInfo = it.result
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

            userRating = 0
            rateUsButton.isEnabled = false

            ratingBar.setRatingChangeListener(object : CustomRatingBar.RatingChangeListener {
                override fun onRatingChanged(rating: Int) {
                    userRating = rating
                }

            })

            btnClose.setOnClickListener {
                dismiss()
                onDismiss.invoke()
            }

            rateUsButton.setOnClickListener {
                if (userRating in 1..3) {
                    requireContext().composeEmail(
                        requireContext().getString(R.string.contact_email),
                        getString(R.string.email_feedback_title, BuildConfig.VERSION_NAME)
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
        reviewInfo?.let {
            val flow = reviewManager.launchReviewFlow(requireActivity(), it)
            flow.addOnCompleteListener { _ ->
                onFinishRate.invoke()
            }
        } ?: onFinishRate.invoke()

    }
}
