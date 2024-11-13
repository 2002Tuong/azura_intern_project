package com.artgen.app.ui.screen.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.CompositionLocalProvider
import androidx.fragment.app.Fragment
import com.artgen.app.R
import com.artgen.app.data.remote.RemoteConfig
import com.artgen.app.databinding.FragmentOnboardingBinding
import com.artgen.app.utils.LocalAdsManager
import com.artgen.app.utils.LocalRemoteConfig
import org.koin.android.ext.android.inject

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private val remoteConfig: RemoteConfig by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.composeView.setContent {
            CompositionLocalProvider(
                LocalRemoteConfig provides remoteConfig
            ) {
                when (arguments?.getInt(ARG_PAGE, 0) ?: 0) {
                    0 -> OnboardingPage(
                        titleRes = R.string.explore_various_styles,
                        drawable = R.drawable.onboarding_1
                    )
                    1 -> OnboardingPage(
                        titleRes = R.string.advanced_ai_technology,
                        drawable = R.drawable.onboarding_2
                    )
                    else -> OnboardingPage(
                        titleRes = R.string.modern_trends,
                        drawable = R.drawable.onboarding_3
                    )
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_PAGE = "PAGE"
        fun newInstance(page: Int): OnboardingFragment {
            val args = Bundle()
            args.putInt(ARG_PAGE, page)

            val fragment = OnboardingFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
