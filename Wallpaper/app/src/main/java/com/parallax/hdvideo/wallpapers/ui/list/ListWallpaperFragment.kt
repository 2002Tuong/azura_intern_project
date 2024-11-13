package com.parallax.hdvideo.wallpapers.ui.list

import android.os.Parcelable
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.data.model.Category
import com.parallax.hdvideo.wallpapers.data.model.HashTag
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.databinding.FragmentListWallpaperScreenBinding
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.*
import com.parallax.hdvideo.wallpapers.remote.model.WallpaperURLBuilder
import com.parallax.hdvideo.wallpapers.services.log.EventSetting
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.ui.base.dialog.ProgressDialogFragment
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragmentBinding
import com.parallax.hdvideo.wallpapers.ui.custom.CustomGridLayoutManager
import com.parallax.hdvideo.wallpapers.ui.custom.ItemDecorationRecyclerView
import com.parallax.hdvideo.wallpapers.ui.details.DetailFragment
import com.parallax.hdvideo.wallpapers.ui.details.DetailViewModel
import com.parallax.hdvideo.wallpapers.ui.dialog.ConfirmDialogFragment
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.ui.main.MainViewModel
import com.parallax.hdvideo.wallpapers.ui.main.fragment.MainFragmentViewModel
import com.parallax.hdvideo.wallpapers.ui.personalization.PersonalizationFragment
import com.parallax.hdvideo.wallpapers.ui.search.SearchViewModel
import com.parallax.hdvideo.wallpapers.ui.search.SearchWallpaperAdapter
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.dpToPx
import com.parallax.hdvideo.wallpapers.utils.other.GlideSupport
import com.parallax.hdvideo.wallpapers.utils.pxToDp
import com.parallax.hdvideo.wallpapers.utils.rx.RxBus
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ListWallpaperFragment: BaseFragmentBinding<FragmentListWallpaperScreenBinding, ListWallpaperViewModel>() {

    override val resLayoutId: Int
        get() = R.layout.fragment_list_wallpaper_screen

    private var cate = Category()
    private var hashTag: HashTag? = null
    private var typeOfScreen: AppScreen = AppScreen.CATEGORY
    private val searchViewModel: SearchViewModel by viewModels()
    private val detailViewModel: DetailViewModel by viewModels()
    private val TAG: String get() = typeOfScreen.name
    private lateinit var gridLayoutManager: GridLayoutManager
    var isLivePhoto = false

    private val mainActivityViewModel by activityViewModels<MainViewModel>()

    @Inject
    lateinit var localStorage: LocalStorage

    companion object {
        private const val TYPE_SCREEN = "1"
        private const val DATA = "2"
        private const val HASHTAG = "3"
        private var recyclerViewState: Parcelable? = null
        fun newInstance(screenType: AppScreen, category : Category? = null, hashTag: HashTag? = null) : ListWallpaperFragment {
            return ListWallpaperFragment().apply {
                arguments = bundleOf(TYPE_SCREEN to screenType, DATA to category, HASHTAG to hashTag)
            }
        }
    }

    override fun init(view: View) {
        super.init(view)
        arguments?.let {
            typeOfScreen = (it.getSerializable(TYPE_SCREEN) as? AppScreen) ?: AppScreen.CATEGORY
            cate = (it.getSerializable(DATA) as? Category) ?: Category()
            hashTag = (it.getSerializable(HASHTAG) as HashTag?)
        }
        viewModel.searchAdapter.requestManagerInstance = Glide.with(this)
        initObservers()
        setupTabLayout()
        setupViews()
        setupRecyclerView()
        if (typeOfScreen == AppScreen.CATEGORY || typeOfScreen == AppScreen.LOCAL ||
            typeOfScreen == AppScreen.FAVORITE || typeOfScreen == AppScreen.DOWNLOAD || typeOfScreen == AppScreen.TRENDING ||
            typeOfScreen == AppScreen.TOP_10_DEVICE || typeOfScreen == AppScreen.BEST_HASH_TAG || typeOfScreen == AppScreen.TOP_DOWNLOAD || typeOfScreen == AppScreen.PARALLAX
        ) {
            observeLiveData(searchViewModel)
            getData(true)
        }
        addTrackingOpenScreen()
    }

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            (tab.customView as TextView).setGradientTextView(true)
            dataBinding.scrollTopButton.hide()
            when (tab.position) {
                1 -> {
                    viewModel.searchAdapter.setFilter(SearchWallpaperAdapter.FiltersType.VIDEO)
                }
                2 -> {
                    viewModel.searchAdapter.setFilter(SearchWallpaperAdapter.FiltersType.PHOTO)
                }
                else -> {
                    viewModel.searchAdapter.setFilter(SearchWallpaperAdapter.FiltersType.MIX)
                }
            }
            val manager = dataBinding.recyclerView.layoutManager
            (tab.tag as? Int)?.let {
                manager?.scrollToPosition(it)
            } ?: kotlin.run {
                manager?.scrollToPosition(0)
            }
            dataBinding.recyclerView.stopScroll()
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
            (tab?.customView as TextView).setGradientTextView(false)
            val manager = (dataBinding.recyclerView.layoutManager as GridLayoutManager)
            val scrollPos = manager.findFirstCompletelyVisibleItemPosition()
            tab.tag = if (scrollPos == -1) {
                manager.findFirstVisibleItemPosition()
            } else {
                scrollPos
            }
        }

        override fun onTabReselected(tab: TabLayout.Tab?) {

        }
    }

    private fun setupTabLayout() {
        if (!WallpaperHelper.isSupportLiveWallpaper) {
            dataBinding.filterTabs.isHidden = true
        }else{
            val tabLayout = dataBinding.filterTabs
            tabLayout.addTab(tabLayout.newTab().setCustomView(getTextViewTab(R.string.all_keyword, true)))
            tabLayout.addTab(tabLayout.newTab().setCustomView(getTextViewTab(R.string.live_wallpapers)))
            tabLayout.addTab(tabLayout.newTab().setCustomView(getTextViewTab(R.string.wallpapers)))
            dataBinding.filterTabs.addOnTabSelectedListener(onTabSelectedListener)
        }

        if ((typeOfScreen != AppScreen.CATEGORY && typeOfScreen != AppScreen.HOME_HASH_TAG) || cate.id == Category.VIDEO_CATEGORY_ID) {
            dataBinding.filterTabs.isHidden = true
            dataBinding.recyclerView.margin(top = 0f)
            dataBinding.ivHeader.isHidden = true
            dataBinding.viewGradient.isHidden = true
            dataBinding.backButtonToolbar.isHidden = true
        } else {
            dataBinding.coordinatorLayout.setBackgroundResource(R.drawable.bg_main_re)
        }
        if (typeOfScreen == AppScreen.LOCAL) {
            dataBinding.coordinatorLayout.setBackgroundResource(R.drawable.bg_main_re)
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

    private fun addTrackingOpenScreen() {
//        when (screenType) {
//            //TODO
//        }
    }

    private fun setUpHeader() {
        dataBinding.collapsingToolbarLayout.apply {
            layoutParams.height += AppConfiguration.statusBarSize
        }
        dataBinding.toolbar.apply {
            layoutParams.height += dpToPx(16f)
        }
        dataBinding.backButtonToolbar.setOnClickListener { onBackPressed() }
        when {
            hashTag != null -> {
                val headerUrl = if (hashTag!!.url.isNullOrEmpty()) hashTag!!.toUrl(0) else hashTag!!.toUrl()
                viewModel.searchAdapter.requestManagerInstance.load(headerUrl).into(dataBinding.ivHeader)
            }
            cate.isVideo -> {
                dataBinding.ivHeader.setImageResource(R.drawable.bg_cate_live_wallpaper_re)
            }
            typeOfScreen == AppScreen.BEST_HASH_TAG -> {
                dataBinding.ivHeader.setImageResource(R.drawable.best_hash_tag_re)
            }
            else -> {
                viewModel.searchAdapter.requestManagerInstance.load(WallpaperURLBuilder.shared.getFullStorage().plus(cate.url)).into(dataBinding.ivHeader)
            }
        }
    }

    override fun handleLoading(config: ProgressDialogFragment.Configure) : Boolean {
        if (!config.status) {
//            dataBinding.refreshLayout.setRefreshing(false)
        }
        return super.handleLoading(config)
    }

    private fun setupRecyclerView() {
        if (typeOfScreen == AppScreen.TOP_10_DEVICE) {
            viewModel.searchAdapter = searchViewModel.wallpaperAdapter
        }
        gridLayoutManager = CustomGridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        dataBinding.recyclerView.layoutManager = gridLayoutManager
        dataBinding.recyclerView.setHasFixedSize(true)
        val itemDecoration = ItemDecorationRecyclerView(marginItem = dpToPx(8f))
        viewModel.searchAdapter.marginOfItem = itemDecoration.marginItem
        dataBinding.recyclerView.addItemDecoration(itemDecoration)
         dataBinding.recyclerView.apply {
             (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
         }
        viewModel.searchAdapter.onClickedItemCallback = { view, position, _ ->
            val posInList = viewModel.searchAdapter.getPositionInListData(position)
            if (posInList >= 0) {
                val detailFragment = DetailFragment.newInstance(
                    typeOfScreen,
                    viewModel.searchAdapter.listData,
                    posInList,
                    view,
                    category = cate.id,
                    hashTag = hashTag
                )
                val tag = javaClass.name.plus("_").plus(detailFragment.javaClass.name)
                pushFragment(detailFragment, tag = tag, animate = false, singleton = true)
                refreshDataTabFavorite()
            }
        }

        viewModel.searchAdapter.onClickTopButtonCallback = { wall ->
            detailViewModel.favoriteWallpaper(wall) { it ->
                it?.let {
                    RxBus.push(it,0)
                    if(typeOfScreen == AppScreen.DOWNLOAD){
                        refreshDataTabFavorite()
                    }
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
                    MainFragmentViewModel.upDateCacheList(it)
                }
            }
        }
        viewModel.searchAdapter.onClickDeleteButtonCallback = { wallpaper ->
            ConfirmDialogFragment.show(
                parentFragmentManager,
                getString(R.string.ask_confirm_delete_wall),
                textNo = getString(R.string.cancel_keyword),
                isReverseColorButton = true,
                textYes = getString(R.string.ok),
                isShowHeaderImage = true
            ) {
                detailViewModel.deleteWallpaperFromDatabase(wallpaper) {
                }
            }
        }

        viewModel.searchAdapter.setGoToTopButton(dataBinding.scrollTopButton)
        viewModel.searchAdapter.screenType = typeOfScreen
        dataBinding.recyclerView.adapter = viewModel.searchAdapter
    }

    fun getData(isRefresh: Boolean) {
        when(typeOfScreen) {
            AppScreen.CATEGORY -> {
                viewModel.idCate = cate.id ?: ""
                viewModel.category = cate
                viewModel.getData(isRefresh)
            }
            AppScreen.FAVORITE -> {
                viewModel.getFavoriteData(isRefresh)
            }
            AppScreen.DOWNLOAD -> {
                viewModel.getHistoryDownload(isRefresh)
            }
            AppScreen.LOCAL -> {
                TrackingSupport.recordEvent(EventSetting.OpenLocalStorageList)
                viewModel.loadLocalStorage()
            }
            AppScreen.COUPLE -> {
                viewModel.searchCoupleWallpaper(isRefresh)
            }
            AppScreen.TRENDING -> {
                viewModel.getTrendingWallpaper(isLivePhoto)
            }
            AppScreen.TOP_DOWNLOAD -> {
                viewModel.getTopDownLoadData()
            }
            AppScreen.BEST_HASH_TAG -> {
                viewModel.getBestHashTagData()
            }
            AppScreen.PARALLAX -> {
                viewModel.loadImage4D(isRefresh)
            }
            else -> {}
        }
    }

    fun refreshData() {
        if (isInitialized) {
            viewModel.couldRefreshData = true
            getData(true)
        }
    }

    private fun refreshDataTabFavorite(){
        val parentFragment = parentFragment
        (parentFragment as? PersonalizationFragment)?.refreshDataTabFavorite()
    }

    private fun setupViews() {
        if (typeOfScreen == AppScreen.LOCAL) {
            dataBinding.headerLayout.isHidden = false
            dataBinding.appBarLayout.isHidden = true
            dataBinding.recyclerView.margin(
                top = pxToDp(
                    dataBinding.headerLayout.layoutParams.height + dpToPx(
                        12f
                    )
                )
            )
            dataBinding.headerLayout.apply {
                layoutParams.height += AppConfiguration.statusBarSize
            }
            dataBinding.backButton.setOnClickListener {
                popFragment()
            }

        } else if (typeOfScreen == AppScreen.DOWNLOAD || typeOfScreen == AppScreen.FAVORITE) {
            dataBinding.headerLayout.isHidden = true
            dataBinding.appBarLayout.isHidden = true
            dataBinding.recyclerView.margin(top = 0f)
            dataBinding.headerLayout.apply {
                layoutParams.height += AppConfiguration.statusBarSize
            }
            dataBinding.backButton.setOnClickListener {
                popFragment()
            }
        } else {
            setUpHeader()
        }
        when(typeOfScreen) {
            AppScreen.CATEGORY -> {
                if (cate.isVideo) {
                    dataBinding.scrollTopButton.margin(bottom = 138f)
                    dataBinding.appBarLayout.isHidden = true
                    setUpHidingBottomNavigationViewOnMainActivity(dataBinding.recyclerView)
                } else {
                    dataBinding.collapsingToolbarLayout.title = cate.name
                    dataBinding.tvTitle.text = cate.name
                }
            }
            AppScreen.FAVORITE -> {
                dataBinding.titleTv.text = getString(R.string.favorite)
                dataBinding.noResultTV.text = getString(R.string.empty_image_favorite)
            }
            AppScreen.DOWNLOAD -> {
                viewModel.searchAdapter.onClickTopButtonCallback = {
                    ConfirmDialogFragment.show(
                        parentFragmentManager,
                        title = getString(R.string.ask_confirm_delete_wall)
                    ) {
                        viewModel.updateWallpaper(it)
                    }
                }
                dataBinding.titleTv.text = getString(R.string.download)
                dataBinding.noResultTV.text = getString(R.string.empty_image_download)
            }
            AppScreen.LOCAL -> {
                dataBinding.titleTv.text = getString(R.string.locally_storage_title)
            }
            AppScreen.COUPLE -> {
                dataBinding.collapsingToolbarLayout.title = getString(R.string.couple_wallpapers)
                dataBinding.tvTitle.text = getString(R.string.couple_wallpapers)
            }
            AppScreen.TRENDING -> {
                dataBinding.scrollTopButton.margin(bottom = 138f)
                dataBinding.appBarLayout.isHidden = true
                setUpHidingBottomNavigationViewOnMainActivity(dataBinding.recyclerView)
            }
            AppScreen.HOME_HASH_TAG -> {
                if (hashTag == null) return
                dataBinding.collapsingToolbarLayout.title = hashTag?.name
                dataBinding.tvTitle.text = hashTag?.name
                viewModel.getListWallpaperByHashTag(hashTag!!, true)
            }
            AppScreen.TOP_DOWNLOAD -> {
                dataBinding.scrollTopButton.margin(bottom = 138f)
                dataBinding.appBarLayout.isHidden = true
                setUpHidingBottomNavigationViewOnMainActivity(dataBinding.recyclerView)
            }
            AppScreen.BEST_HASH_TAG -> {
                dataBinding.collapsingToolbarLayout.title = getString(R.string.for_you)
                dataBinding.tvTitle.text = getString(R.string.for_you)
            }
            AppScreen.PARALLAX -> {
                dataBinding.scrollTopButton.margin(bottom = 138f)
                dataBinding.appBarLayout.isHidden = true
                setUpHidingBottomNavigationViewOnMainActivity(dataBinding.recyclerView)
            }
            else -> {
            }
        }
    }

    private fun initObservers() {
        if (typeOfScreen == AppScreen.DOWNLOAD || typeOfScreen == AppScreen.FAVORITE) {
            viewModel.isEmptyListLiveData.observe(viewLifecycleOwner) { isEmpty ->
                showOrHideEmptyViews(!isEmpty)
            }
            viewModel.searchAdapter.onDataChangedCallback = {
                showOrHideEmptyViews(!viewModel.searchAdapter.emptyData)
            }
        }
        RxBus.subscribe(ListWallpaperFragment.javaClass.name + "_"+ typeOfScreen.name,WallpaperModel::class){
            viewModel.searchAdapter.updateFavoriteWallpaper(it)
        }
    }

    private fun showOrHideEmptyViews(isHidden: Boolean) {
        dataBinding.emptyView.isHidden = isHidden
        dataBinding.ivNoResult.isHidden = isHidden
        dataBinding.noResultTV.isHidden = isHidden
        dataBinding.btnDiscover.setOnClickListener {
            (activity as MainActivity).navigationController.popToRoot()
            (activity as MainActivity).navigateToTab(2)
        }
    }

    override fun scrollToItemPosition(position: Int, item: WallpaperModel) {
        viewModel.searchAdapter.scrollToItemPosition(getItemPosition(item))
    }

    private fun getItemPosition(item: WallpaperModel) = viewModel.searchAdapter.findItemPosition(item)

    override fun refreshDataIfNeeded() {
        super.refreshDataIfNeeded()
        if (isInitialized) viewModel.searchAdapter.refreshAdapterIfNeed()
    }

    override fun onDestroy() {
        GlideSupport.clearMemory()
        RxBus.unregister(ListWallpaperFragment.javaClass.name + "_"+ typeOfScreen.name)
        super.onDestroy()
    }

    override fun onDestroyView() {
        if (typeOfScreen == AppScreen.TRENDING) {
            recyclerViewState = gridLayoutManager.onSaveInstanceState()
        }
        val check = typeOfScreen == AppScreen.CATEGORY || typeOfScreen == AppScreen.HOME_HASH_TAG || typeOfScreen == AppScreen.TRENDING
        val isSearchHashtag = typeOfScreen == AppScreen.HOME_HASH_TAG
        if (isInitialized && check) viewModel.searchAdapter.logTrackingTotalWall(typeOfScreen, isSearchHashtag)
        super.onDestroyView()
    }

}