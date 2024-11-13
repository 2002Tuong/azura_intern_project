package com.wifi.wificharger.ui.onboarding

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wifi.wificharger.ui.onboarding.OnboardingFragment2


class OnboardingViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val context: Context
) :
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingFragment.newInstance()

            1 -> OnboardingFragment2.newInstance()

            else -> OnboardingFragment3.newInstance()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}