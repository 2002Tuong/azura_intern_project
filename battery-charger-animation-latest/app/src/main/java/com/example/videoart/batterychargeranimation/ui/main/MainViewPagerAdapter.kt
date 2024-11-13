package com.example.videoart.batterychargeranimation.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MainViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val fragmentList: List<Fragment> = listOf()) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return  fragmentList.count()
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}