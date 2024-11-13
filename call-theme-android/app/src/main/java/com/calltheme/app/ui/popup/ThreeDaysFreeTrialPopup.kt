package com.calltheme.app.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.screentheme.app.R
import com.screentheme.app.databinding.ThreeDaysFreeTrialLayoutBinding
import com.screentheme.app.models.PremiumPlan
import com.screentheme.app.utils.extensions.setWidthPercent
import org.joda.time.Period

class ThreeDaysFreeTrialPopup : DialogFragment() {

    lateinit var binding: ThreeDaysFreeTrialLayoutBinding

    private var dismissCallback: (() -> Unit)? = null
    private var continueCallback: (() -> Unit)? = null

    private var premiumPlan: PremiumPlan? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        setWidthPercent(0.8f)
        super.onResume()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        binding = ThreeDaysFreeTrialLayoutBinding.inflate(layoutInflater)

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)

        isCancelable = false

        val dialog = builder.create()

        // Calculate the desired height as 50% of the device's height
        val inflateHeight = (resources.displayMetrics.heightPixels * 0.5).toInt()

        // Set the dialog's layout parameters
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, inflateHeight)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.apply {
            val price = if(premiumPlan?.isProduct == true) {
                premiumPlan?.formattedBasePlanPrice
            } else {
                val period = convertToPeriodText(
                    premiumPlan!!.basePlanPeriod
                )
                "${premiumPlan?.formattedBasePlanPrice}/$period"
            }
            if (premiumPlan?.hasFreeTrial() == true) {
                threeDaysFreeTrialTextView.text = getString(R.string.popup_premium_free_trial_des, premiumPlan?.freeTrialPeriod, price)
                startYearly.text = getString(R.string.popup_premium_title, premiumPlan?.name, premiumPlan?.freeTrialPeriod)
            } else {
                threeDaysFreeTrialTextView.text = getString(R.string.then_pay, price)
                startYearly.text = getString(R.string.start_with, premiumPlan?.name)
            }

            closeButton.setOnClickListener {
                dismissCallback?.invoke()
                dismiss()
            }

            continueButton.setOnClickListener {
                continueCallback?.invoke()
            }
        }

        isCancelable = false

        return dialog
    }

    fun showWithPrice(activity: FragmentActivity, premiumPlan: PremiumPlan) {
        this.premiumPlan = premiumPlan
        if (!this.isAdded) show(activity.supportFragmentManager, "ThreeDaysFreeTrialDialog")
    }

    fun setOnDismissCallback(callback: () -> Unit) {
        this.dismissCallback = callback
    }

    fun setOnContinue(callback: () -> Unit) {
        this.continueCallback = callback
    }
    fun convertToPeriodText(isoPeriod: String): String {
        return try {
            val period = Period.parse(isoPeriod)
            when {
                period.years > 0 -> "year"
                period.months > 0 -> "month"
                period.weeks > 0 -> "week"
                else -> ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}
