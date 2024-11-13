package com.parallax.hdvideo.wallpapers.ui.main.fragment

import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.baoyz.widget.PullRefreshLayout
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.ads.AdsManager
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.databinding.FragmentMainScreenBinding
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.isNullOrEmptyOrBlank
import com.parallax.hdvideo.wallpapers.extension.pushFragment
import com.parallax.hdvideo.wallpapers.extension.showLoading
import com.parallax.hdvideo.wallpapers.services.log.*
import com.parallax.hdvideo.wallpapers.ui.base.activity.BaseActivity
import com.parallax.hdvideo.wallpapers.ui.base.dialog.ProgressDialogFragment
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragmentBinding
import com.parallax.hdvideo.wallpapers.ui.custom.CustomGridLayoutManager
import com.parallax.hdvideo.wallpapers.ui.custom.ItemDecorationRecyclerView
import com.parallax.hdvideo.wallpapers.ui.details.DetailFragment
import com.parallax.hdvideo.wallpapers.ui.details.DetailViewModel
import com.parallax.hdvideo.wallpapers.ui.home.HomeFragment
import com.parallax.hdvideo.wallpapers.ui.list.ListWallpaperFragment
import com.parallax.hdvideo.wallpapers.ui.list.AppScreen
import com.parallax.hdvideo.wallpapers.ui.splash.welcom.WelcomeFragment
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.dpToPx
import com.parallax.hdvideo.wallpapers.utils.rx.RxBus
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class MainFragment : BaseFragmentBinding<FragmentMainScreenBinding, MainFragmentViewModel>() {

    override val resLayoutId: Int = R.layout.fragment_main_screen
    private val spanCount = 2
    @Inject
    lateinit var localStorage: LocalStorage
    private lateinit var layoutManager: CustomGridLayoutManager
    private val detailViewModel: DetailViewModel by viewModels()

    override fun init(view: View) {
        super.init(view)
        delayedAmount = 2000L
        dataBinding.viewModel = viewModel
        setupRecyclerView()
        for(item in MainFragmentViewModel.listCache) {
        }
        if (MainFragmentViewModel.listCache.isEmpty()) {
            viewModel.getData(isRefresh = true)
        } else {
            view.doOnPreDraw {
                viewModel.mainAdapter.setData(MainFragmentViewModel.listCache)
                layoutManager.onRestoreInstanceState(recyclerViewState)
            }
        }
        postDelayed(this::initDelayed, delayedAmount)

        RxBus.subscribe(MainFragment.javaClass.name,WallpaperModel::class){
            viewModel.mainAdapter.updateFavoriteWallpaper(it)
        }
    }

    override fun didRemoveFragment(fragment: Fragment) {
        super.didRemoveFragment(fragment)
        if (isInitialized) {
            if (fragment is WelcomeFragment && viewModel.mainAdapter.emptyData) {
                showLoading()
            }
            pauseOrPlayVideo(true)
        }
    }

    override fun prepareToPushFragment(fragment: Fragment) {
        pauseOrPlayVideo(false)
    }

    override fun refreshDataIfNeeded() {
        super.refreshDataIfNeeded()
        if (isInitialized) viewModel.mainAdapter.refreshAdapterIfNeed()
    }

    private fun initDelayed() {
        setupRefreshView()
        val isSupportedVideoWallpaper = WallpaperHelper.isSupportLiveWallpaper
        if(!isSupportedVideoWallpaper
            && !localStorage.videoWallpaperUnsupportedDialogDisplayed) {
//            ConfirmDialogFragment.show(
//                parentFragmentManager,
//                title = getString(R.string.video_not_supporting_prompt),
//                textYes = getString(R.string.next_keyword),
//                callbackNo = null
//            )
            localStorage.videoWallpaperUnsupportedDialogDisplayed = true
        }
        if (!localStorage.didCheckIsSupportVideoWall) {
            if (isSupportedVideoWallpaper) TrackingSupport.recordEvent(EventData.IsSupportLiveWallDevice)
            else TrackingSupport.recordEvent(EventData.IsNotSupportLiveWallDevice)
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        pauseOrPlayVideo(menuVisible)
        Logger.d("setMenuVisibility", menuVisible)
    }

    override fun viewIsVisible() {
        super.viewIsVisible()
        FirebaseAnalyticSupport.recordScreen(activity, TAGS)
    }

    override fun handleLoading(config: ProgressDialogFragment.Configure) : Boolean {
        if (!config.status) {
            dataBinding.refreshLayout.setRefreshing(false)
        }
        return super.handleLoading(config)
    }

    private fun setupRefreshView() {
        dataBinding.refreshLayout.setColor(ContextCompat.getColor(requireContext(), R.color.colorSecondary))
        dataBinding.refreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_SMARTISAN)
        dataBinding.refreshLayout.setOnRefreshListener {
            viewModel.cancelAll()
            viewModel.getData(isRefresh = true)
            TrackingSupport.recordEventWithScreenName(EventOther.Reload, "screenName" to AppScreen.HOME.name)
        }
    }

    //region RecyclerView

    private fun setupRecyclerView() {
        layoutManager = CustomGridLayoutManager(context, spanCount, GridLayoutManager.VERTICAL, false)
        dataBinding.recyclerView.layoutManager = layoutManager
        dataBinding.recyclerView.setHasFixedSize(true)
        val decoration = ItemDecorationRecyclerView(marginItem = dpToPx(8f))
        viewModel.mainAdapter.marginOfItem = decoration.marginItem
        viewModel.setup(this)
        dataBinding.recyclerView.addItemDecoration(decoration)
        viewModel.mainAdapter.setGoToTopButton(dataBinding.scrollTopButtonMainFragment)
        onClickRecyclerView()
        setUpHidingBottomNavigationViewOnMainActivity(dataBinding.recyclerView)
    }

    private fun onClickRecyclerView() {
        viewModel.mainAdapter.onClickedItemCallback = { view, position, data ->
            var pos = viewModel.mainAdapter.getPositionInListData(position)
            val list = viewModel.mainAdapter.listData
            if (pos >= 0 && list.isNotEmpty()) {
                val filteredList = list.filterNot { it.url.isNullOrEmptyOrBlank() }
                if (list.size != filteredList.size) {
                    pos = filteredList.indexOf(data)
                }
                val fragment = DetailFragment.newInstance(AppScreen.HOME, filteredList, pos, view)
                pushFragment(
                    fragment,
                    animate = false,
                    tag = DetailFragment.TAGS + "_" + TAGS,
                    singleton = true
                )
            }
        }
        viewModel.mainAdapter.onClickTopButtonCallback = { wall ->
            detailViewModel.favoriteWallpaper(wall) { it ->
                it?.let {
                    RxBus.push(it,0)
                    if (it.isFavorite) {
                        Toast.makeText(
                            context,
                            getString(R.string.add_wallpaper_favorite_successfully),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            getString(R.string.unfavorite_wallpaper_successfully),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        viewModel.mainAdapter.onClickItemHashTagCallback = { _, _, data, _ ->
            TrackingSupport.recordEventOnlyFirebase(EventSearch.OpenHashTagsFromHome)
            val fragment =
                ListWallpaperFragment.newInstance(AppScreen.HOME_HASH_TAG, hashTag = data)
            pushFragment(fragment, animate = true, singleton = true)
        }
    }
    //endregion

    fun pauseOrPlayVideo(play: Boolean) {
        if (!isInitialized || AppConfiguration.lowMemory) return
        if (isMenuVisible && play && (activity as? BaseActivity)?.navigationController?.isEmptyFragment == true) {
            viewModel.mainAdapter.downloadVideo()
        } else {
            viewModel.mainAdapter.stopVideo()
        }
    }

    fun back() {
        if (!scrollToTop()) {
            dataBinding.refreshLayout.setRefreshing(true)
            viewModel.cancelAll()
            viewModel.getData(isRefresh = true)
        }
    }

    fun scrollToTop() : Boolean {
        if (!isInitialized) return true
        val currentPos = (dataBinding.recyclerView.layoutManager as GridLayoutManager)
            .findFirstVisibleItemPosition()
        return if(currentPos > 0) {
            dataBinding.recyclerView.layoutManager?.smoothScrollToPosition(dataBinding.recyclerView, null, 0)
            (parentFragment as HomeFragment?)?.expandCollapsingToolbar()
            TrackingSupport.recordEventWithScreenName(EventOther.ClickBackToTop, "screenName" to AppScreen.HOME.name)
            true
        } else false
    }

    override fun scrollToItemPosition(position: Int, item: WallpaperModel) {
        viewModel.mainAdapter.scrollToItemPosition(getItemPosition(item))
    }

    private fun getItemPosition(item: WallpaperModel) = viewModel.mainAdapter.findItemPosition(item)

    override fun onStart() {
        pauseOrPlayVideo(true)
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainAdsInter", "call onResume")
        AdsManager.loadInterstitialAd(reload = true)
    }

    override fun onPause() {
        pauseOrPlayVideo(false)
        super.onPause()
    }

    override fun onDestroyView() {
        if (isInitialized) {
            recyclerViewState = dataBinding.recyclerView.layoutManager?.onSaveInstanceState()
            viewModel.mainAdapter.logTrackingTotalWall(AppScreen.HOME)
        }
        super.onDestroyView()
    }

    override fun onDestroy() {
        RxBus.unregister(MainFragment.javaClass.name)
        super.onDestroy()
    }

    companion object {
        const val TAGS = "MainFragment"
        private var recyclerViewState: Parcelable? = null
    }
}