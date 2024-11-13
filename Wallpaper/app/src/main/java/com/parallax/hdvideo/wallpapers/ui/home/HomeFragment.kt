package com.parallax.hdvideo.wallpapers.ui.home

import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.activityViewModels
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.data.model.Category
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.databinding.FragmentHomeScreenBinding
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseFragmentPagerAdapter
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragment
import com.parallax.hdvideo.wallpapers.ui.custom.HomeMenuItem
import com.parallax.hdvideo.wallpapers.ui.dialog.MoreAppDialog
import com.parallax.hdvideo.wallpapers.ui.list.ListWallpaperFragment
import com.parallax.hdvideo.wallpapers.ui.list.AppScreen
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.ui.main.MainViewModel
import com.parallax.hdvideo.wallpapers.ui.main.fragment.MainFragment
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    override val resLayoutId: Int = R.layout.fragment_home_screen

    private lateinit var homeAdapter: BaseFragmentPagerAdapter
    private lateinit var dataBinding: FragmentHomeScreenBinding
    private val mainActivityViewModel by activityViewModels<MainViewModel>()

    override fun init(view: View) {
        super.init(view)
        dataBinding = FragmentHomeScreenBinding.bind(view)
        setupView()
        initTabLayout()
        scrollToTabAfterLayout()
    }

    private fun initTabLayout() {
        buildTabItem(R.string.featured,R.drawable.ic_stars_re,R.drawable.ic_star_disable_re,true)
        if(WallpaperHelper.isSupport4DImage(requireContext())){
            buildTabItem(R.string.image_4d,R.drawable.ic_4d_enable_re,R.drawable.ic_4d_re)
        }

        if (WallpaperHelper.isSupportLiveWallpaper){
            buildTabItem(R.string.live_keyword,R.drawable.ic_live_enable_re,R.drawable.ic_live_re)
        }
//        buildTabItem(R.string.trending,R.drawable.ic_flash_enable_re,R.drawable.ic_flash_re)
//        buildTabItem(R.string.top_download,R.drawable.ic_cup_enable_re,R.drawable.ic_cup_re)
        dataBinding.tabLayoutMenu.addOnTabSelectedListener(object  : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    dataBinding.homeViewPager.setCurrentItem(tab.position,true)
                    (tab.customView as HomeMenuItem).selected()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {
                    if(tab.customView != null){
                        (tab.customView as HomeMenuItem).unSelected()
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })
    }

    private fun buildTabItem(@StringRes tittle : Int, @DrawableRes resIconSelect : Int, @DrawableRes resIconUnSelect : Int,isSelected : Boolean = false) {
        val layout = dataBinding.tabLayoutMenu
        if (isSelected) layout.addTab(
            layout.newTab().setCustomView(
                HomeMenuItem(requireContext()).setData(
                    tittle,
                    resIconSelect,
                    resIconUnSelect
                ).selected()
            )
        )
        else layout.addTab(
            layout.newTab().setCustomView(
                HomeMenuItem(requireContext()).setData(
                    tittle,
                    resIconSelect,
                    resIconUnSelect
                )
            )
        )
    }

    private fun setupView() {
        val fragment = ListWallpaperFragment.newInstance(screenType = AppScreen.TRENDING)
        fragment.isLivePhoto = true
        val fragmentList = ArrayList<Pair<String,BaseFragment>>()
        fragmentList.add(getString(R.string.featured) to MainFragment())

        if (WallpaperHelper.isSupport4DImage(requireContext())) {
            fragmentList.add(
                getString(R.string.image_4d) to ListWallpaperFragment.newInstance(
                    screenType = AppScreen.PARALLAX
                )
            )
        }
        if (WallpaperHelper.isSupportLiveWallpaper) {
            fragmentList.add(
                getString(R.string.live_keyword) to ListWallpaperFragment.newInstance(
                    screenType = AppScreen.CATEGORY, category = Category(
                        id = Category.VIDEO_CATEGORY_ID,
                        name = WallpaperApp.instance.getString(R.string.live_wallpapers)
                    )
                )
            )
        }
//        fragmentList.add(getString(R.string.trending) to ListWallpaperFragment.newInstance(screenType = AppScreen.TRENDING))
//        fragmentList.add(getString(R.string.top_download) to ListWallpaperFragment.newInstance(screenType = AppScreen.TOP_DOWNLOAD))

        homeAdapter = BaseFragmentPagerAdapter(childFragmentManager).apply {
            addData(fragmentList)
        }
        dataBinding.homeViewPager.adapter = homeAdapter
        dataBinding.homeViewPager.offscreenPageLimit = 4
        dataBinding.homeViewPager.addOnPageChangeListener(object :
            ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                dataBinding.tabLayoutMenu.getTabAt(position)?.select()
            }
        })
        dataBinding.homeTabLayout.setupWithViewPager(dataBinding.homeViewPager)
        dataBinding.homeTabLayout.updateFromUIWhenSelectedTab { pos ->
            if (pos == 0) {
                mainActivityViewModel.openSearchFromUI = "home"
            } else {
                mainActivityViewModel.openSearchFromUI = "trending"
            }
        }
    }

    private fun scrollToTabAfterLayout() {
        postDelayed({dataBinding.homeViewPager.setCurrentItem(currentTab, true)},500)
    }

    private fun navigateToTab(tab: Int): Boolean =
        if (this::dataBinding.isInitialized && tab != dataBinding.homeViewPager.currentItem) {
            dataBinding.homeViewPager.setCurrentItem(tab, true)
            true
        } else false

    fun refreshMainFragment() {
        if (!navigateToTab(0)) {
            findFragment<MainFragment>(0)?.back()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseFragment> findFragment(position: Int): T? {
        if (this::homeAdapter.isInitialized) {
            return homeAdapter.getFragment(position) as? T
        }
        return null
    }

    fun pauseOrPlayVideo(play: Boolean) {
        findFragment<MainFragment>(0)?.pauseOrPlayVideo(play)
    }

    fun setAutoPlayInMainFragment(play: Boolean) {
        findFragment<MainFragment>(0)?.viewModel?.mainAdapter?.isAutoPlayVideo = play
    }

    fun expandCollapsingToolbar() {
        dataBinding.appBarLayout.setExpanded(true)
    }

    override fun onBackPressed(): Boolean {
        if (!super.onBackPressed()) {
            if (!navigateToTab(0)) {
                if (findFragment<MainFragment>(0)?.scrollToTop() == false) {
                    val listMoreApp = (activity as? MainActivity)?.viewModel?.listMoreAppModel ?: emptyList()
                    MoreAppDialog.show(childFragmentManager, listMoreApp) {
                        Toast.makeText(
                            WallpaperApp.instance,
                            getString(R.string.thanks_for_using),
                            Toast.LENGTH_SHORT
                        ).show()
                        activity?.finish()
                    }
                }
            }
        }
        return true
    }

    override fun scrollToItemPosition(position: Int, item: WallpaperModel) {
        val currentFragmentTab = homeAdapter.getFragment(dataBinding.homeViewPager.currentItem)
        currentFragmentTab.scrollToItemPosition(position,item)
    }

    fun getFragmentTab(): BaseFragment {
        return homeAdapter.getFragment(dataBinding.homeViewPager.currentItem)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (this::dataBinding.isInitialized) {
            currentTab = dataBinding.homeViewPager.currentItem
            currentPositionMenu = dataBinding.homeViewPager.currentItem
        }

    }

    companion object {
        var currentTab = 0
        var currentPositionMenu = 0
    }

}