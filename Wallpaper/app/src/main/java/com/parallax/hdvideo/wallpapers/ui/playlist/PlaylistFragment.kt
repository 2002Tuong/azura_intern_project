package com.parallax.hdvideo.wallpapers.ui.playlist

import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.FragmentPlaylistScreenBinding
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.extension.pushFragment
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragmentBinding
import com.parallax.hdvideo.wallpapers.ui.custom.ItemDecorationRecyclerView
import com.parallax.hdvideo.wallpapers.ui.details.DetailFragment
import com.parallax.hdvideo.wallpapers.ui.dialog.ConfirmDialogFragment
import com.parallax.hdvideo.wallpapers.ui.dialog.SelectTimePeriodDialog
import com.parallax.hdvideo.wallpapers.ui.list.AppScreen
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.dpToPx
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistFragment : BaseFragmentBinding<FragmentPlaylistScreenBinding, PlaylistViewModel>() {

    override val resLayoutId: Int = R.layout.fragment_playlist_screen
    private val spanCount = 2
    private var isServiceTurnedOn : Boolean = false
    private var durationDelay: Int = WallpaperApp.instance.localStorage.wallpaperChangeDelayTimeInMin
    private val mainControlsList by lazy {
        arrayListOf(dataBinding.titleSetWallpaper,dataBinding.delayTimeTV
            ,dataBinding.recyclerView,dataBinding.setWallpaperButton)
    }

    override fun init(view: View) {
        super.init(view)
        setupView(view)
        setupRecyclerView()
        setupObserver()
    }

    private fun setupRecyclerView() {
        val layoutManager = GridLayoutManager(context, spanCount, GridLayoutManager.VERTICAL, false)
        dataBinding.recyclerView.layoutManager = layoutManager
        dataBinding.recyclerView.setHasFixedSize(true)
        val itemDecoration = ItemDecorationRecyclerView(marginItem = dpToPx(8f))
        viewModel.mainAdapter.marginOfItem = itemDecoration.marginItem
        viewModel.setup(this)
        dataBinding.recyclerView.addItemDecoration(itemDecoration)
        dataBinding.recyclerView.adapter = viewModel.mainAdapter
    }


    private fun setupView(view: View) {
        viewModel.mainAdapter.onClickTopButtonCallback = {
            ConfirmDialogFragment.show(parentFragmentManager,
                getString(R.string.ask_msg_confirm_remove_item_playlist)) {
                    viewModel.deleteWallpaperFromPlaylist(it)
            }
        }
        dataBinding.delayTimeTV.setOnClickListener {
            SelectTimePeriodDialog.show(parentFragmentManager, durationDelay) { text, duration ->
                this.durationDelay = duration
                viewModel.updateCurrentSettings(
                    dataBinding.delayTimeTV,
                    duration
                )
            }
        }

        dataBinding.setWallpaperButton.setOnClickListener {
            setWallpaper()
        }
        durationDelay = viewModel.updateCurrentSettings(
            dataBinding.delayTimeTV,
            durationDelay
        )
        viewModel.mainAdapter.onClickedItemCallback = { itemView, position, _ ->
            val posInList = viewModel.mainAdapter.getPositionInListData(position)
            if (posInList >= 0) {
                val fragment = DetailFragment.newInstance(AppScreen.PLAYLIST,  viewModel.mainAdapter.listData, posInList, itemView)
                val tag = javaClass.name.plus("_").plus(fragment.javaClass.name)
                pushFragment(fragment, tag = tag, animate = false, singleton = true)
            }
        }
        dataBinding.noImagesButton.setOnClickListener {
            (requireActivity() as MainActivity).navigateToTab(0)
        }
    }

    private fun setupObserver() {
        viewModel.serviceStateLiveData.observe(viewLifecycleOwner, Observer { info ->
            if (info == null || info.isEmpty()) {
                Logger.d("PlaylistFragment observe empty")
                return@Observer
            }
            isServiceTurnedOn = info.firstOrNull { !it.state.isFinished } != null
            dataBinding.setWallpaperButton.setSwitched(isServiceTurnedOn)
        })
        viewModel.queueStatus.observe(viewLifecycleOwner, Observer {
            switchLayout(it)
        })
        refreshData()
    }

    fun refreshData() {
        if (isInitialized) {
            viewModel.retrieveWallpaperQueue()
        }
    }

    private fun setWallpaper() {
        if (!viewModel.mainAdapter.emptyData) {
            val checkTurnOn = isServiceTurnedOn
            if (checkTurnOn) {
                ConfirmDialogFragment.show(parentFragmentManager, getString(R.string.ask_msg_confirm_turn_off_playlist)) {
                    viewModel.cancelServicesPlayList()
                    dataBinding.setWallpaperButton.onOff()
                }
            } else {
            }
        } else {
            ConfirmDialogFragment.show(parentFragmentManager,
                title = getString(R.string.ask_msg_warning_empty_playlist),
                textYes = getString(R.string.ok),
                callbackNo = null) {
                viewModel.cancelServicesPlayList()
            }
        }
    }

    private fun switchLayout(showMainLayout: Boolean) {
        mainControlsList.forEach {
            it.isHidden = !showMainLayout
        }
        dataBinding.noImagesTV.isHidden = showMainLayout
        dataBinding.noImagesButton.isHidden = showMainLayout
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun newInstance(): PlaylistFragment {
            return PlaylistFragment()
        }
    }
}