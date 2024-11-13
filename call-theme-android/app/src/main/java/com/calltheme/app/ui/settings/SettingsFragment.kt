package com.calltheme.app.ui.settings

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.calltheme.app.ui.activity.MainActivity
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.ui.picklanguage.PickLanguageFragment
import com.screentheme.app.BuildConfig
import com.screentheme.app.R
import com.screentheme.app.databinding.FragmentSettingsBinding
import com.screentheme.app.models.LanguageModel
import com.screentheme.app.utils.ConsentHelper
import com.screentheme.app.utils.extensions.composeEmail
import com.screentheme.app.utils.extensions.openPrivacy
import com.screentheme.app.utils.extensions.shareApp
import com.screentheme.app.utils.helpers.LanguageSupporter

class SettingsFragment : BaseFragment() {

    private var rootFragment: Fragment? = null

    companion object {
        fun newInstance(rootFragment: Fragment): SettingsFragment {
            val fragment = SettingsFragment()
            fragment.rootFragment = rootFragment
            return fragment
        }
    }

    private lateinit var binding: FragmentSettingsBinding

    private val setupSettingsFragment = SettingPreferencesFragment()

    override fun getViewBinding(): ViewBinding {
        binding = FragmentSettingsBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {

        rootFragment?.setFragmentResultListener(PickLanguageFragment.REQUEST_PICKUP_LANGUAGE_KEY) { key, bundle ->

            val selectedLanguage = bundle.getParcelable("language") as? LanguageModel

            if (selectedLanguage != null) {
                val mainActivity = activity as MainActivity

                LanguageSupporter.setAppLanguage(mainActivity, selectedLanguage)
                mainActivity.recreate()
            }

        }

        childFragmentManager.beginTransaction().apply {
            replace(R.id.settings, setupSettingsFragment)
            commit()
        }

        setupSettingsFragment.setOnPreferenceClickListener { preference ->

            when (preference.key) {
                "language_preference" -> {
                    try {
                        getNavController().navigate(R.id.action_navigation_home_to_navigation_update_language)
                    } catch (exception: IllegalArgumentException) {

                    }

                }

                "share_preference" -> {
                    requireContext().shareApp()
                }

                "feedback_preference" -> {
                    requireContext().composeEmail(
                        requireContext().getString(R.string.contact_email),
                        getString(R.string.email_feedback_title, BuildConfig.VERSION_NAME)
                    )
                }

                "rate_us_preference" -> {
                    ratingAppPopup.show(requireActivity())
                }

                "policy_preference" -> {
                    requireContext().openPrivacy()
                }
                "cmp_policy" -> {
                    myActivity?.let {
                        ConsentHelper.updateConsent(it, loadAds = {}, onDone = { it.finish() })
                    }
                }
            }
        }

        binding.apply {

            upgradePremiumButton.setOnClickListener {
                try {
                    getNavController().navigate(R.id.action_navigation_home_to_navigation_subscription)
                } catch (exception: IllegalArgumentException) {

                }
            }
            goProButton.visibility = View.GONE

//            goProButton.visibility = if (BillingClientHelper.getInstance(requireActivity()).isPurchased) View.GONE else View.VISIBLE
            goProButton.setOnClickListener {
                try {
                    getNavController().navigate(R.id.action_navigation_home_to_navigation_subscription)
                } catch (exception: IllegalArgumentException) {

                }
            }
            premiumLayout.visibility = View.GONE
//            premiumLayout.visibility = if (BillingClientHelper.getInstance(requireActivity()).isPurchased) View.GONE else View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun registerObservers() {

    }

}