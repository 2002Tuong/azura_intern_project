package com.parallax.hdvideo.wallpapers.ui.base.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class BaseFragmentPagerV2Adapter(tabLayout: TabLayout, viewPager2: ViewPager2, fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = listData.size
    private var listData = mutableListOf<Pair<String, Fragment>>()
    private val tabLayoutMediator: TabLayoutMediator = TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
        tab.text = listData[position].first
    }

    override fun createFragment(position: Int): Fragment {
        return listData[position].second
    }

    fun addData(title: String, fragment: Fragment) {
        listData.add(title to fragment)
    }

    fun addData(vararg item: Pair<String, Fragment>) {
        listData.addAll(item)
    }

    fun attach()  {
        tabLayoutMediator.attach()
    }
}