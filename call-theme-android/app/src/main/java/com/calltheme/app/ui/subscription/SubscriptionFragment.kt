package com.calltheme.app.ui.subscription

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.ui.dialog.ThreeDaysFreeTrialPopup
import com.screentheme.app.databinding.FragmentSubscriptionBinding
import com.screentheme.app.models.PremiumPlan
import com.screentheme.app.utils.extensions.openPrivacy
import com.screentheme.app.utils.extensions.openTermOfService
import com.screentheme.app.utils.helpers.BillingClientProvider

class SubscriptionFragment : BaseFragment() {

    private lateinit var binding: FragmentSubscriptionBinding
    private val threeDaysFreeTrialDialog = ThreeDaysFreeTrialPopup()

    lateinit var adapter: SubscriptionAdapter

    var premiumPlan: PremiumPlan? = null

    override fun getViewBinding(): ViewBinding {
        binding = FragmentSubscriptionBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {

        adapter = SubscriptionAdapter(requireContext())
        adapter.setOnItemClick { premiumPlan ->
            this.premiumPlan = premiumPlan
        }

        BillingClientProvider.getInstance(requireActivity()).onPremiumPlansLoaded { premiumPlans ->

            requireActivity().runOnUiThread {
                adapter.updateItems(premiumPlans)
            }
        }


        threeDaysFreeTrialDialog.setOnDismissCallback {
            findNavController().popBackStack()
        }

        threeDaysFreeTrialDialog.setOnContinue {
            if (premiumPlan != null) {
                BillingClientProvider.getInstance(requireActivity()).purchasePremiumPlan(premiumPlan!!)
            }
        }

        binding.apply {
            closeButton.setOnClickListener {

                if (premiumPlan != null && premiumPlan!!.sku != null) {
                    threeDaysFreeTrialDialog.showWithPrice(requireActivity(), premiumPlan!!)
                }
            }

            unlockButton.setOnClickListener {
                if (premiumPlan != null) {
                    BillingClientProvider.getInstance(requireActivity()).purchasePremiumPlan(premiumPlan!!)
                }
            }

            termOfServiceButton.setOnClickListener {
                requireContext().openTermOfService()
            }

            privacyPolicyButton.setOnClickListener {
                requireContext().openPrivacy()
            }

            subscriptionRecyclerView.adapter = adapter
            subscriptionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun registerObservers() {
    }
}