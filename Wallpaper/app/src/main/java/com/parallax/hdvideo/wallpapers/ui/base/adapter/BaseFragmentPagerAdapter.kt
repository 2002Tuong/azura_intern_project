package com.parallax.hdvideo.wallpapers.ui.base.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragment

class BaseFragmentPagerAdapter(fm : FragmentManager) : FragmentStatePagerAdapter(fm,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val listFragment = mutableListOf<Pair<String?, BaseFragment>>()

    override fun getCount(): Int = listFragment.size

    fun addData(title: String? , fragment: BaseFragment) {
        listFragment.add(title to fragment)
    }

    fun addData(vararg item: Pair<String, BaseFragment>) {
        listFragment.addAll(item)
    }

    fun addData(items : List<Pair<String, BaseFragment>>) {
        listFragment.addAll(items)
    }

    fun addData(fragment: BaseFragment) {
        listFragment.add(null to fragment)
    }

    fun addData(vararg fragments : BaseFragment) {
        fragments.forEach {
            listFragment.add(null to it)
        }
    }

    fun addData (items : ArrayList<BaseFragment>) {
        items.forEach {
            listFragment.add(null to it)
        }
    }

    fun getFragment(position: Int) = listFragment[position].second

    fun <T: Fragment>findFragment(position: Int): T? {
        @Suppress("UNCHECKED_CAST")
        return getFragment(position) as? T
    }

    override fun getItem(position: Int): Fragment {
        return getFragment(position)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return listFragment[position].first
    }

    val allFragments get() = listFragment.map { it.second }
}