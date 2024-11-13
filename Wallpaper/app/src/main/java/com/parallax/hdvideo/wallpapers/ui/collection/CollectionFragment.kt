package com.parallax.hdvideo.wallpapers.ui.collection

import android.os.Parcelable
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.parallax.hdvideo.wallpapers.databinding.FragmentCollectionScreenBinding
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.pushFragment
import com.parallax.hdvideo.wallpapers.services.log.EventOpen
import com.parallax.hdvideo.wallpapers.services.log.EventOther
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragmentBinding
import com.parallax.hdvideo.wallpapers.ui.custom.CustomGridLayoutManager
import com.parallax.hdvideo.wallpapers.ui.details.DetailFragment
import com.parallax.hdvideo.wallpapers.ui.list.ListWallpaperFragment
import com.parallax.hdvideo.wallpapers.ui.list.AppScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class CollectionFragment: BaseFragmentBinding<FragmentCollectionScreenBinding, CollectionViewModel>() {

    override val resLayoutId: Int = com.parallax.hdvideo.wallpapers.R.layout.fragment_collection_screen

    @Inject
    lateinit var localStorage: LocalStorage

    override fun init(view: View) {
        super.init(view)
        initViews()
        setupRecyclerView()
        viewModel.getData()
    }

    private fun initViews() {
        dataBinding.ivBack.setOnClickListener {
            onBackPressed()
        }
        TrackingSupport.recordEvent(EventOpen.OpenCategory)
        TrackingSupport.recordScreenView(activity, "CategoryScreen")
    }

    private fun setupRecyclerView() {
        viewModel.setup(this)
        val rvManager = CustomGridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        dataBinding.recyclerView.layoutManager = rvManager
        dataBinding.recyclerView.setHasFixedSize(true)
        viewModel.collectionAdapter.onClickedItemCallback = { _, _, data ->
            val screenType = if(data.isTopTenDevice) {
                localStorage.didNotifyTopTenDevice = true
                AppScreen.TOP_10_DEVICE
            } else AppScreen.CATEGORY
            TrackingSupport.recordEventOnlyFirebase(EventOpen.OpenCategoryList)
            pushFragment(ListWallpaperFragment.newInstance(screenType, data), tag = AppScreen.CATEGORY.name, singleton = true)
        }
        viewModel.collectionAdapter.onClickItemCallback = { position, data, itemView ->
            // last position
            if (min(data.walls.size, 5) - 1 == position) {
                TrackingSupport.recordEventOnlyFirebase(EventOpen.OpenCategoryList)
                pushFragment(ListWallpaperFragment.newInstance(AppScreen.CATEGORY, data), tag = AppScreen.CATEGORY.name, singleton = true)
            } else {
                while (data.walls.size > 5) {
                    data.walls.removeLast()
                }
                pushFragment(DetailFragment.newInstance(AppScreen.COLLECTION,
                    currentPosition = position, sharedView = itemView,
                    listData = data.walls, category = data.id),
                    animate = false, singleton = true)
            }
        }
        dataBinding.recyclerView.adapter = viewModel.collectionAdapter
        setUpHidingBottomNavigationViewOnMainActivity(dataBinding.recyclerView)
        viewModel.collectionAdapter.onDataChangedCallback = {
            rvManager.onRestoreInstanceState(recyclerViewState)
        }
    }

    fun backToTop() {
        if (!isInitialized) return
        TrackingSupport.recordEventWithScreenName(EventOther.ClickBackToTop, "screenName" to AppScreen.CATEGORY.name)
        dataBinding.recyclerView.layoutManager?.smoothScrollToPosition(dataBinding.recyclerView, null,0)
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onDestroyView() {
        if (isInitialized) recyclerViewState = dataBinding.recyclerView.layoutManager?.onSaveInstanceState()
        super.onDestroyView()
    }

    companion object {
        var recyclerViewState: Parcelable? = null
    }
}