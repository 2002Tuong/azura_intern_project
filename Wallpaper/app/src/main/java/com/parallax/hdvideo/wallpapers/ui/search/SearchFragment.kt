package com.parallax.hdvideo.wallpapers.ui.search

import android.os.Parcelable
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.data.model.HashTag
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.databinding.FragmentSearchScreenBinding
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.extension.isInvisible
import com.parallax.hdvideo.wallpapers.extension.popFragment2
import com.parallax.hdvideo.wallpapers.extension.pushFragment
import com.parallax.hdvideo.wallpapers.extension.setGradientTextView
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.services.log.EventOther
import com.parallax.hdvideo.wallpapers.services.log.EventSearch
import com.parallax.hdvideo.wallpapers.services.log.TrackingScreen
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragmentBinding
import com.parallax.hdvideo.wallpapers.ui.custom.CustomGridLayoutManager
import com.parallax.hdvideo.wallpapers.ui.custom.ItemDecorationRecyclerView
import com.parallax.hdvideo.wallpapers.ui.details.DetailFragment
import com.parallax.hdvideo.wallpapers.ui.details.DetailViewModel
import com.parallax.hdvideo.wallpapers.ui.list.AppScreen
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.ui.request.RequestFragment
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import com.parallax.hdvideo.wallpapers.utils.dpToPx
import com.parallax.hdvideo.wallpapers.utils.rx.RxBus
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : BaseFragmentBinding<FragmentSearchScreenBinding, SearchViewModel>() {

    override val resLayoutId: Int = R.layout.fragment_search_screen
    var curTextSearch: String? = null
    var hashTag: HashTag? = null
    private var searchTitle: String? = null
    private var hadSearched = false
    private var trackingScreen = TrackingScreen.HASH_TAG_SEARCH_TREND
    var isSearchedFromHashTag = false
    private val detailViewModel: DetailViewModel by viewModels()

    override fun init(view: View) {
        super.init(view)
        dataBinding.viewModel = viewModel
        setUpView()
        setupRecyclerView()
        setupActions()
        setupTabLayout()
        postDelayed({
            if (hashTag != null) {
                isSearchedFromHashTag = true
                dataBinding.searchView.text = hashTag?.name
                val textSearch = hashTag?.hashtag ?: hashTag?.name
                dataBinding.searchView.currentHashTag = textSearch
                searchWallpaper(textSearch, hashTag?.topDown == true, hashTag)
                dataLoaded = true
            }
        }, 0)
        viewModel.getHashTags()
        RxBus.subscribe(SearchFragment.javaClass.name,WallpaperModel::class){
            viewModel.wallpaperAdapter.updateFavoriteWallpaper(it)
        }
        TrackingSupport.recordScreenView(activity, TAGS)
    }

    private fun setUpView() {
        dataBinding.appBarLayout.layoutParams.height =
            AppConfiguration.statusBarSize + resources.getDimensionPixelOffset(R.dimen.height_header)
    }

    override fun onBackPressed(): Boolean {
        if (!isInitialized) return super.onBackPressed()
        if (isSearchedFromHashTag) popFragment2(this,animateRightOrLeft = true)
        showOrHiddenNoResultView(true)
        (activity as MainActivity).showBottomTabs()
        dataBinding.filterTabs.getTabAt(0)?.select()
        if (hiddenSearchingView()) return true
        if (hadSearched) {
            hadSearched = false
            dataBinding.wallpapersRv.isInvisible = true
            dataBinding.scrollTopButton.hide()
            dataBinding.filterTabs.isHidden = true
            dataBinding.recyclerView.isInvisible = false
            viewModel.wallpaperAdapter.removeAll()
            dataBinding.cancelButton.isHidden = true
            dataBinding.searchView.text = null
            viewModel.reset()
            dataBinding.searchView.setShowKeyboard(false)
            return true
        } else {
            dataBinding.searchView.text = ""
        }
        return false
    }

    private fun showOrHiddenNoResultView(isShow: Boolean) {
        dataBinding.noResultTV.isHidden = isShow
        dataBinding.ivNoResult.isHidden = isShow
        dataBinding.btnSendRequest.isHidden = isShow
    }

    override fun viewIsVisible() {
        TrackingSupport.recordScreenView(activity, TAGS)
    }

    override fun refreshDataIfNeeded() {
        if (isInitialized) {
            viewModel.wallpaperAdapter.refreshAdapterIfNeed()
            viewModel.trendHashTagAdapter.refreshAdapterIfNeed()
        }
    }

    private fun setupActions() {
        dataBinding.searchView.onSearchCallback = {
            TrackingSupport.recordSearchKeywords(EventSearch.SearchWithKeyWord, it)
            viewModel.addToSearchHistory(it, dataBinding.searchView.text)
            searchWallpaper(it, false)
            trackingScreen = TrackingScreen.SEARCH_RESULT
            if (it == dataBinding.searchView.currentHashTag) {
                trackingScreen = TrackingScreen.HASH_TAG_SEARCH_TREND
            }
        }
        dataBinding.cancelButton.setOnClickListener {
           onBackPressed()
        }
        dataBinding.searchView.onClickEvent = {
            dataBinding.cancelButton.isHidden = false
            dataBinding.suggestRv.isInvisible = false
            showOrHiddenNoResultView(true)
            dataBinding.recyclerView.isHidden = true
            dataBinding.filterTabs.isHidden = true
            dataBinding.wallpapersRv.isInvisible = true
            dataBinding.scrollTopButton.hide()
        }

        viewModel.searchSuggestionAdapter.onScrolledCallback = { _, _, _ ->
            dataBinding.searchView.setShowKeyboard(false)
        }
        viewModel.searchSuggestionAdapter.onClickNewTextCallback = { item, _ ->
            dataBinding.searchView.text = item.name
            dataBinding.searchView.currentHashTag = item.hashTag
            TrackingSupport.recordEventOnlyFirebase(EventSearch.SearchWithHashTag)
        }

        dataBinding.btnSendRequest.setOnClickListener {
            pushFragment(
                RequestFragment(),
                animate = true,
                singleton = true,
                tag = RequestFragment.TAGS + "_SEARCH"
            )
        }
    }

    override fun onPause() {
        dataBinding.searchView.setShowKeyboard(false)
        dataBinding.searchView.alterSearchViewForSearch(true)
        super.onPause()
    }

    override fun onDestroyView() {
        recyclerViewState = dataBinding.recyclerView.layoutManager?.onSaveInstanceState()
        val check = trackingScreen != TrackingScreen.SEARCH_RESULT
        if (isInitialized && check) viewModel.wallpaperAdapter.logTrackingTotalWall(AppScreen.SEARCH, true)
        super.onDestroyView()
    }

    override fun onDestroy() {
        RxBus.unregister(SearchFragment.javaClass.name)
        super.onDestroy()
    }

    private fun setupRecyclerView() {
        val decoration = ItemDecorationRecyclerView(marginItem = dpToPx(8f))
        val requestManager = Glide.with(this)
        // top trends
        val mainRvLayoutManager = CustomGridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
        dataBinding.recyclerView.layoutManager = mainRvLayoutManager
        dataBinding.recyclerView.addItemDecoration(decoration)

        viewModel.trendHashTagAdapter.onClickColor = { _, item ->
            val hashTag = item.hashtag
            val colorName = item.name
            searchWallpaper(hashTag, item.topDown, item)
            dataBinding.searchView.text = colorName
            dataBinding.searchView.currentHashTag = hashTag
            viewModel.addToSearchHistory(hashTag, colorName, item.id)
            TrackingSupport.recordEvent(EventSearch.SearchByColor)
            trackingScreen = TrackingScreen.HASH_TAG_SEARCH_TREND
        }
        viewModel.trendHashTagAdapter.onDataChangedCallback = {
            mainRvLayoutManager.onRestoreInstanceState(recyclerViewState)
            dataBinding.progressBar.isHidden = true
        }
        viewModel.trendHashTagAdapter.marginOfItem = decoration.marginItem
        viewModel.trendHashTagAdapter.requestManagerInstance = requestManager
        dataBinding.recyclerView.adapter = viewModel.trendHashTagAdapter
        dataBinding.recyclerView.setHasFixedSize(true)

        //on item top trend click
        viewModel.trendHashTagAdapter.onClickedItemCallback = { _, _, data ->
            dataBinding.searchView.text = data.name
            val textSearch = data.hashtag ?: data.name
            dataBinding.searchView.currentHashTag = textSearch
            searchWallpaper(textSearch, data.topDown, data)
            viewModel.addToSearchHistory(textSearch, data.name, data.id)
            when {
                data.topDown -> {
                    trackingScreen = TrackingScreen.TOP_DOWNLOAD
                    TrackingSupport.recordEvent(EventSearch.SearchByTopDownload)
                    TrackingSupport.recordScreenView(activity, TrackingScreen.TOP_DOWNLOAD.name)
                }
                data.topTrending -> {
                    trackingScreen = TrackingScreen.TRENDING
                    TrackingSupport.recordEventOnlyFirebase(EventSearch.OpenTrendingFromSearch)
                    TrackingSupport.recordScreenView(activity, trackingScreen.name)
                }
                else -> {
                    trackingScreen = TrackingScreen.HASH_TAG_SEARCH_TREND
                    TrackingSupport.recordEventOnlyFirebase(EventSearch.OpenHashTagsSearchTrend)
                    TrackingSupport.recordScreenView(activity, TrackingScreen.HASH_TAG_SEARCH_TREND.name)
                }
            }
        }

        // list wallpapers
        dataBinding.wallpapersRv.layoutManager =
            CustomGridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        dataBinding.wallpapersRv.setHasFixedSize(true)
        viewModel.wallpaperAdapter.marginOfItem = decoration.marginItem
        dataBinding.wallpapersRv.addItemDecoration(decoration)
        viewModel.wallpaperAdapter.setGoToTopButton(dataBinding.scrollTopButton)
        viewModel.wallpaperAdapter.onClickedItemCallback = { itemView, position, _ ->
            val pos = viewModel.wallpaperAdapter.getPositionInListData(position)
            if (pos >= 0) {
                setShowKeyboard(false)
                dataBinding.searchView.alterSearchViewForSearch(true)
                val fragment = DetailFragment.newInstance(
                    AppScreen.SEARCH,
                    viewModel.wallpaperAdapter.listData,
                    pos,
                    itemView,
                    trackingScreen = trackingScreen
                )
                pushFragment(
                    fragment,
                    animate = false,
                    singleton = true,
                    tag = DetailFragment.TAGS + "_SEARCH"
                )
            }
        }

        viewModel.wallpaperAdapter.onClickTopButtonCallback = { wall ->
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

        viewModel.wallpaperAdapter.requestManagerInstance = requestManager
        dataBinding.wallpapersRv.adapter = viewModel.wallpaperAdapter
        viewModel.wallpaperAdapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                if (!dataBinding.wallpapersRv.isInvisible)
                    showOrHiddenNoResultView(viewModel.wallpaperAdapter.amount != 0)
            }
        })
        // suggest hashTags
        viewModel.searchSuggestionAdapter.onClickedItemcallBack = { _, item ->
            dataBinding.searchView.text = item.name
            dataBinding.searchView.currentHashTag = item.hashTag
            val textSearch = item.hashTag ?: item.name
            searchWallpaper(textSearch, item.topDown, item)
            viewModel.addToSearchHistory(textSearch, item.name)
            prepareSearchLayout()
            dataBinding.searchView.setShowKeyboard(false)
            dataBinding.searchView.alterSearchViewForSearch(true)
            TrackingSupport.recordEventOnlyFirebase(EventSearch.SearchWithHashTag)
        }
        dataBinding.suggestRv.setHasFixedSize(true)
        dataBinding.suggestRv.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        dataBinding.suggestRv.adapter = viewModel.searchSuggestionAdapter
        setUpHidingBottomNavigationViewOnMainActivity(dataBinding.recyclerView)
    }

    private fun setupTabLayout() {
        if (!WallpaperHelper.isSupportLiveWallpaper) {
            dataBinding.filterTabs.isHidden = true
            return
        }
        val tabLayout = dataBinding.filterTabs
        tabLayout.addTab(tabLayout.newTab().setCustomView(getTextViewTab(R.string.all_keyword, true)))
        tabLayout.addTab(tabLayout.newTab().setCustomView(getTextViewTab(R.string.live_wallpapers)))
        tabLayout.addTab(tabLayout.newTab().setCustomView(getTextViewTab(R.string.wallpapers)))
        dataBinding.filterTabs.addOnTabSelectedListener(onTabSelectedListener)
    }

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        var canUnselect = true
        override fun onTabSelected(tab: TabLayout.Tab) {
            (tab.customView as TextView).setGradientTextView(true)
            when (tab.position) {
                1 -> {
                    viewModel.wallpaperAdapter.setFilter(SearchWallpaperAdapter.FiltersType.VIDEO)
                    TrackingSupport.recordEventOnlyFirebase(EventSearch.FilterVideo)
                }
                2 -> {
                    viewModel.wallpaperAdapter.setFilter(SearchWallpaperAdapter.FiltersType.PHOTO)
                    TrackingSupport.recordEventOnlyFirebase(EventSearch.FilterWallpaper)
                }
                else -> viewModel.wallpaperAdapter.setFilter(SearchWallpaperAdapter.FiltersType.MIX)
            }
            (tab.tag as? Int)?.let {
                dataBinding.wallpapersRv.layoutManager?.scrollToPosition(it)
            } ?: kotlin.run {
                dataBinding.wallpapersRv.layoutManager?.scrollToPosition(0)
            }
            dataBinding.wallpapersRv.stopScroll()
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            (tab.customView as TextView).setGradientTextView(false)
            if (!canUnselect) {
                canUnselect = true
                return
            }
            val scrollPosition =
                (dataBinding.wallpapersRv.layoutManager as GridLayoutManager).findFirstCompletelyVisibleItemPosition()
            tab.tag = if (scrollPosition == -1) {
                (dataBinding.wallpapersRv.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
            } else {
                scrollPosition
            }
        }

        override fun onTabReselected(tab: TabLayout.Tab) {

        }
    }

    private fun getTextViewTab(resString: Int, isSelected: Boolean = false): TextView {
        val textView = TextView(context)
        textView.apply {
            gravity = Gravity.CENTER
            setLines(2)
            textSize = 16f
            typeface = ResourcesCompat.getFont(requireContext(), R.font.bevietnampro_regular)
            setGradientTextView(false)
        }

        textView.setGradientTextView(isSelected)
        textView.setText(resString)
        return textView
    }

    fun searchWallpaper(text: String?, title: String?) {
        val name = title?.replace(AppConstants.EMOJIS_REGEX.toRegex(), "")
        if (!text.isNullOrEmpty()) {
            if (isInitialized) {
                dataBinding.searchView.text = name
                dataBinding.searchView.currentHashTag = text
                setShowKeyboard(false)
                searchWallpaper(text, text == HashTag.TAG_TOP_DOWN)
                viewModel.addToSearchHistory(text, name)
            } else {
                curTextSearch = text
                curTextSearch = name
            }
        }
    }

    fun searchWallpaperWithHashTag(hashtagSearch: HashTag?) {
        if (hashtagSearch != null) {
            isSearchedFromHashTag = true
            dataBinding.searchView.text = hashtagSearch.name
            val textSearch = hashtagSearch.hashtag ?: hashtagSearch.name
            dataBinding.searchView.currentHashTag = textSearch
            searchWallpaper(textSearch, hashtagSearch.topDown, hashtagSearch)
            dataLoaded = true
        }
    }

    fun setShowKeyboard(isShow: Boolean) {
        if (isInitialized)
            dataBinding.searchView.setShowKeyboard(isShow)
    }

    private fun searchWallpaper(text: String?, isTopDown: Boolean, hashTag: HashTag? = null) {
        (activity as MainActivity).viewModel.useSearchFeature = true
        (activity as MainActivity).showOrHideBottomTabs(true)
        viewModel.currentHashTag = hashTag
        val isCouple = text == RemoteConfig.commonData.hashTagCouple
        if (isTopDown) dataBinding.searchView.text = getString(R.string.top_download)
        val check = viewModel.search(text, isRefresh = true, isTopDown = isTopDown, isCouple = isCouple)
        hadSearched = true
        if (check) {
            (0 until dataBinding.filterTabs.tabCount).forEach {
                dataBinding.filterTabs.getTabAt(it)?.tag = null
            }
            onTabSelectedListener.canUnselect = false
            dataBinding.filterTabs.getTabAt(0)?.select()
            dataBinding.wallpapersRv.smoothScrollToPosition(0)
        }
        hadSearched = true
        prepareSearchLayout()
        dataBinding.searchView.alterSearchViewForSearch(true)
    }

    private fun prepareSearchLayout() {
        dataBinding.progressBar.isHidden = true
        if (WallpaperHelper.isSupportLiveWallpaper) dataBinding.filterTabs.isHidden = false
        dataBinding.wallpapersRv.isInvisible = false
        showOrHiddenNoResultView(true)
        dataBinding.recyclerView.isInvisible = true
        dataBinding.suggestRv.isInvisible = true
        dataBinding.cancelButton.isHidden = true
        dataBinding.cancelButton.isHidden = false
        dataBinding.searchView.setShowKeyboard(false)
    }

    private fun hiddenSearchingView(): Boolean {
        if (!hadSearched) {
            dataBinding.searchView.text = ""
            dataBinding.recyclerView.isHidden = false
        }
        if (!dataBinding.suggestRv.isInvisible) {
            dataBinding.suggestRv.isInvisible = true
            dataBinding.searchView.text = ""
            if (hadSearched) {
                dataBinding.wallpapersRv.isInvisible = true
                dataBinding.recyclerView.isHidden = false
            } else {
                dataBinding.wallpapersRv.isInvisible = true
                dataBinding.scrollTopButton.hide()
            }
            if (dataBinding.recyclerView.isInvisible) {
                if (WallpaperHelper.isSupportLiveWallpaper) dataBinding.filterTabs.isInvisible = false
            }
            dataBinding.cancelButton.isHidden = true
            dataBinding.searchView.setShowKeyboard(false)
            return true
        }
        return false
    }

    fun backMainSearch() {
        if (!isInitialized) return
       // showOrHiddenNoResultView(true)
        if (hiddenSearchingView()) return
        if (viewModel.wallpaperAdapter.emptyData) {
            val manager = dataBinding.recyclerView.layoutManager as GridLayoutManager
            if (manager.findFirstVisibleItemPosition() <= 0) onBackPressed()
            else {
                TrackingSupport.recordEventWithScreenName(EventOther.ClickBackToTop, "screenName" to AppScreen.SEARCH.name)
                dataBinding.recyclerView.smoothScrollToPosition(0)
            }
        } else {
            val manager = dataBinding.wallpapersRv.layoutManager as GridLayoutManager
            val pos = manager.findFirstVisibleItemPosition()
            if (pos <= 0) onBackPressed()
            else {
                TrackingSupport.recordEventWithScreenName(EventOther.ClickBackToTop, "screenName" to AppScreen.HOME_HASH_TAG.name)
                dataBinding.wallpapersRv.smoothScrollToPosition(0)
            }
        }
    }

    override fun scrollToItemPosition(position: Int, item: WallpaperModel) {
        viewModel.wallpaperAdapter.scrollToItemPosition(position)
    }

    fun getViewModel() = if (isInitialized) viewModel else null

    companion object {
        var recyclerViewState: Parcelable? = null
        const val TAGS = "SearchFragment"
    }
}
