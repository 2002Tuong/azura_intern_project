package com.parallax.hdvideo.wallpapers.ui.personalization

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.FragmentPersonalizationScreenBinding
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.margin
import com.parallax.hdvideo.wallpapers.extension.pushFragment
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseFragmentPagerAdapter
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragment
import com.parallax.hdvideo.wallpapers.ui.edit.EditUserFragment
import com.parallax.hdvideo.wallpapers.ui.list.ListWallpaperFragment
import com.parallax.hdvideo.wallpapers.ui.list.AppScreen
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.pxToDp
//import com.tp.inappbilling.billing.BillingManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt


@AndroidEntryPoint
class PersonalizationFragment : BaseFragment() {

    override val resLayoutId: Int
        get() = R.layout.fragment_personalization_screen
    private lateinit var dataBinding: FragmentPersonalizationScreenBinding
    private lateinit var pagerAdapter: BaseFragmentPagerAdapter

    @Inject
    lateinit var localStorage: LocalStorage

    override fun init(view: View) {
        dataBinding = FragmentPersonalizationScreenBinding.bind(view)
        dataBinding.backButton.margin(top = pxToDp(AppConfiguration.statusBarSize))
        dataBinding.collapsingToolbarLayout.layoutParams.height = (dataBinding.collapsingToolbarLayout.layoutParams.height + pxToDp(AppConfiguration.statusBarSize)).roundToInt()
        initData()
        initViewPager()
        setupTabLayout()
        initAction()
        // initBilling()
    }

    private fun initData() {
        val storageName = localStorage.lastName + " " + localStorage.accountName
        if(!localStorage.lastName.isNullOrEmpty() || !localStorage.accountName.isNullOrEmpty()){
            dataBinding.profileName.text = storageName
        }
    }

//    private fun initBilling() {
//        BillingManager.getInstance().googleAccount.observe(this) { googleSignInAccount ->
//            if (googleSignInAccount != null) {
//                googleSignInAccount.photoUrl?.let {
//                    Glide.with(this).load(it).circleCrop().into(dataBinding.profilePic)
//                    GlideHelper.loadBlurImage(dataBinding.ivBackground, it)
//                }
//                dataBinding.profileName.text =
//                    if (localStorage.username.isNullOrEmpty()) googleSignInAccount.displayName else localStorage.username
//
//            } else {
//                dataBinding.ivEdit.isHidden = false
//                GlideHelper.load(dataBinding.profilePic, R.drawable.ic_avatars)
//            }
//        }
//    }

    private fun initAction() {
        dataBinding.backButton.setOnClickListener { onBackPressed() }
        dataBinding.ivEdit.setOnClickListener {
            pushFragment(EditUserFragment(), singleton = true)
            refreshDataTabFavorite()
        }
    }

    private fun initViewPager() {
        val fragmentList = listOf(
            getString(R.string.download) to ListWallpaperFragment.newInstance(screenType = AppScreen.DOWNLOAD),
            getString(R.string.favorite) to ListWallpaperFragment.newInstance(screenType = AppScreen.FAVORITE),
        )
        pagerAdapter = BaseFragmentPagerAdapter(childFragmentManager).apply {
            addData(fragmentList)
        }
        dataBinding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                dataBinding.tabLayout.getTabAt(position)?.select()
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        dataBinding.viewPager.adapter = pagerAdapter
    }

    private fun getTextViewTab(resString: Int, isSelected: Boolean = false): TextView {
        val textView = TextView(context)
        textView.gravity = Gravity.CENTER
        textView.setLines(2)
        textView.textSize = 16f
        textView.typeface = ResourcesCompat.getFont(requireContext(), R.font.bevietnampro_regular)
        if (isSelected) {
            textView.setTextColor(Color.WHITE)
        } else {
            textView.setTextColor(Color.parseColor("#829AB6"))
        }
        textView.setText(resString)
        return textView
    }

    private fun setupTabLayout() {
        val tabLayout = dataBinding.tabLayout
        tabLayout.addTab(
            tabLayout.newTab().setCustomView(getTextViewTab(R.string.download, true))
        )
        tabLayout.addTab(tabLayout.newTab().setCustomView(getTextViewTab(R.string.favorite)))
        dataBinding.tabLayout.addOnTabSelectedListener(onTabSelectedListener)
    }

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            (tab.customView as TextView).setTextColor(Color.WHITE)
            dataBinding.viewPager.setCurrentItem(tab.position, true)
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            (tab?.customView as TextView).setTextColor(Color.parseColor("#829AB6"))
            val fragmentRefresh = (dataBinding.viewPager.adapter as BaseFragmentPagerAdapter).getFragment(tab.position)
            (fragmentRefresh as? ListWallpaperFragment)?.refreshData()
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {

        }
    }

    fun updateNameProfile(name: String) {
        dataBinding.profileName.text = name
    }

    fun refreshDataTabFavorite(){
        val fragment = (dataBinding.viewPager.adapter as BaseFragmentPagerAdapter).getFragment(1)
        (fragment as? ListWallpaperFragment)?.refreshData()
    }

}