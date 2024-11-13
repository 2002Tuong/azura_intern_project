package com.slideshowmaker.slideshow.ui.my_video

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ads.control.ads.VioAdmob
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.adapter.MyVideoListAdapter
import com.slideshowmaker.slideshow.data.TrackingFactory
import com.slideshowmaker.slideshow.databinding.ActivityMyVideoScreenBinding
import com.slideshowmaker.slideshow.models.MyVideoModel
import com.slideshowmaker.slideshow.ui.base.BaseActivity
import com.slideshowmaker.slideshow.ui.premium.PremiumActivity
import com.slideshowmaker.slideshow.ui.share_video.ShareVideoActivity
import com.slideshowmaker.slideshow.ui.trim_video.TrimVideoActivity
import com.slideshowmaker.slideshow.utils.AdsHelper
import com.slideshowmaker.slideshow.utils.DimenUtils
import com.slideshowmaker.slideshow.utils.Logger
import kotlinx.android.synthetic.main.activity_base.headerView
import kotlinx.android.synthetic.main.layout_view_base_header_view.view.subRightButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import java.io.File


class MyVideoActivity : BaseActivity(), KodeinAware {
    override fun getContentResId(): Int = R.layout.activity_my_video_screen

    private val myVideoAdapter = MyVideoListAdapter()

    override val kodein by closestKodein()
    private lateinit var layoutBinding: ActivityMyVideoScreenBinding


    val viewModel: MyVideoViewModel by instance()
    override fun initViews() {
        layoutBinding = ActivityMyVideoScreenBinding.inflate(layoutInflater)
        setContentView(layoutBinding.root)
        setRightButton(R.drawable.icon_delete_24dp) {
            if (myVideoAdapter.getNumberItemSelected() < 1) {
                showToast(getString(R.string.toast_nothing_item_selected))
                return@setRightButton
            }
            showYesNoDialog(getString(R.string.delete_popup_do_you_want_delete_items)) {
                deleteItemSelected()
            }
        }

        setSubRightButton(R.drawable.icon_check_all_none) {
            Logger.e("check all")
            var checkedAllItem = true
            for (item in myVideoAdapter.itemArray) {
                if (!item.checked && item.filePath.length > 5) {
                    checkedAllItem = false
                    break
                }
            }
            Logger.e("allItemChecked = $checkedAllItem")
            if (checkedAllItem) {
                myVideoAdapter.setOffAll()
                headerView.subRightButton.setImageResource(R.drawable.icon_check_all_none)
            } else {
                selectAll()
                headerView.subRightButton.setImageResource(R.drawable.icon_check_all)
            }

        }

        hideButton()
        setScreenTitle(getString(R.string.my_video_button))
        val columnSize = 110 * DimenUtils.density(this)
        val totalColumns = DimenUtils.screenWidth(this) / columnSize
        layoutBinding.allMyStudioListView.apply {
            adapter = myVideoAdapter
            layoutManager = LinearLayoutManager(context)
        }
        //getAllMyStudioItem()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.videosState.collectLatest {
                    myVideoAdapter.setItemList(ArrayList(it))
                    myVideoAdapter.notifyDataSetChanged()
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadingState.collectLatest {
                    if (it) {
                        showLoadingDialog()
                    } else {
                        dismissLoadingDialog()
                        val totalItem = myVideoAdapter.getTotalItem()
                        if (totalItem >= 1) {
                            requestNativeView()
                        } else {
                            layoutBinding.frAds.visibility = View.GONE
                        }
                    }
                }
            }
        }
        viewModel.getMyVideos()
        layoutBinding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun requestNativeView() {
        AdsHelper.requestNativeMyVideo(
            this,
            onLoaded = {
                VioAdmob.getInstance().populateNativeAdView(
                    this,
                    it,
                    layoutBinding.frAds,
                    layoutBinding.includeNative.shimmerContainerBanner
                )
            },
            onLoadFail = {
                layoutBinding.frAds.visibility = View.GONE
            }
        )
    }

    private var selectedMode = false
    override fun initActions() {
        myVideoAdapter.onLongPressCallback = {
        }
        myVideoAdapter.onSelectChangeCallback = {
            Thread {

                val selectedItemNumber = myVideoAdapter.getNumberItemSelected()
                val total = myVideoAdapter.getTotalItem()

                if (selectedItemNumber == total) {
                    runOnUiThread {
                        headerView.subRightButton.setImageResource(R.drawable.icon_check_all)
                    }

                } else {
                    runOnUiThread {
                        headerView.subRightButton.setImageResource(R.drawable.icon_check_all_none)
                    }
                }
            }.start()
        }
        myVideoAdapter.onClickItemCallback = {
            if (!selectedMode) {
                ShareVideoActivity.gotoActivity(this, it.filePath)
                TrackingFactory.MyVideo.playVideo().track()
            }
        }

        myVideoAdapter.onClickOpenMenuCallback = { view, myStudioDataModel ->
            val themeWrapper: Context = ContextThemeWrapper(this, R.style.VideoMaker_PopupMenu)

            val popupMenu = PopupMenu(themeWrapper, view)
            popupMenu.menuInflater.inflate(R.menu.my_video_item_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item?.itemId) {

                    R.id.delete -> {
                        onDeleteItem(myStudioDataModel.filePath)
                    }

                    R.id.trim -> {

                        TrimVideoActivity.gotoActivity(
                            this@MyVideoActivity, myStudioDataModel.filePath
                        )


                    }

                    R.id.share -> {
                        shareVideoFile(myStudioDataModel.filePath)

                    }
                }
                popupMenu.dismiss()
                true
            }
            popupMenu.show()
        }

        layoutBinding.btnPro.setOnClickListener {
            startActivity(Intent(this, PremiumActivity::class.java))
        }
    }

    private fun onDeleteItem(path: String) {
        showYesNoDialog(getString(R.string.delete_popup_do_you_want_delete_item)) {
            val file = File(path)
            if (file.exists()) {

                try {
                    file.delete()
                    myVideoAdapter.onDeleteItem(path)
                    updateEmptyIcon()
                    doSendBroadcast(path)
                } catch (e: Exception) {
                    e.printStackTrace()

                }


            }
        }

    }

    private fun openSelectMode() {
        selectedMode = true
        myVideoAdapter.selectedMode = true
        myVideoAdapter.notifyDataSetChanged()
        showButton()
    }

    private fun closeSelectMode() {
        selectedMode = false
        myVideoAdapter.selectedMode = false
        myVideoAdapter.notifyDataSetChanged()
        hideButton()
        myVideoAdapter.setOffAll()

    }

    private fun showButton() {
        showRightButton()
        showSubRightButton()
    }

    private fun hideButton() {
        hideRightButton()
        hideSubRightButton()
    }

    private fun selectAll() {
        myVideoAdapter.selectAll()
    }

    private fun deleteItemSelected() {
        showLoadingDialog()
        Thread {
            val selectedVideos = ArrayList<MyVideoModel>()
            for (item in myVideoAdapter.itemArray) {
                if (item.checked && item.filePath.isNotEmpty()) {
                    selectedVideos.add(item)
                }
            }
            for (item in selectedVideos) {
                val videoFile = File(item.filePath)
                videoFile.delete()
                doSendBroadcast(item.filePath)
                //mAllMyStudioAdapter.itemList.remove(item)
                runOnUiThread {
                    myVideoAdapter.onDeleteItem(item.filePath)
                }

            }

            runOnUiThread {
                updateEmptyIcon()
                closeSelectMode()
                // getAllMyStudioItem()
                dismissLoadingDialog()
            }
        }.start()

    }

    private fun getAllMyStudioItem() {

//        Thread {
//            if (mMyVideoAdapter.itemCount > 0) {
//                val deletePathList = ArrayList<String>()
//                mMyVideoAdapter.itemList.forEachIndexed { index, myStudioDataModel ->
//
//                    if (myStudioDataModel.filePath.length > 5 && !File(myStudioDataModel.filePath).exists()) {
//                        deletePathList.add(myStudioDataModel.filePath)
//                    }
//
//                }
//
//                runOnUiThread {
//                    deletePathList.forEach {
//                        mMyVideoAdapter.onDeleteItem(it)
//                    }
//                }
//
//            } else {
//                runOnUiThread {
//                    showProgressDialog()
//                }
//                val folder = File("/storage/emulated/0/Movies/${getString(R.string.app_name)}")
//                val myStudioDataList = ArrayList<MyVideoDataModel>()
//                if (folder.exists() && folder.isDirectory) {
//                    for (item in folder.listFiles().orEmpty()) {
//                        try {
//                            //MediaUtils.getVideoSize(item.absolutePath)
//                            val duration = MediaUtils.getVideoDuration(item.absolutePath)
//                            if (item.exists()) myStudioDataList.add(
//                                MyVideoDataModel(
//                                    item.absolutePath, item.lastModified(), duration
//                                )
//                            )
//
//                        } catch (e: java.lang.Exception) {
//                            item.delete()
//                            doSendBroadcast(item.absolutePath)
//                            continue
//                        }
//
//                    }
//                }
//                myStudioDataList.sort()
//
//                runOnUiThread {
//                    mMyVideoAdapter.setItemList(myStudioDataList)
//                    if (myStudioDataList.size > 0) {
//                        mMyVideoAdapter.notifyDataSetChanged()
//                        iconNoItem.visibility = View.GONE
//                    } else {
//                        iconNoItem.visibility = View.VISIBLE
//                    }
//                    dismissProgressDialog()
//                }
//            }
//
//
//        }.start()


    }

    private fun updateEmptyIcon() {
        Thread {
            val total = myVideoAdapter.getTotalItem()
            if (total <= 0) {
                runOnUiThread {
                    layoutBinding.iconNoItem.visibility = View.VISIBLE
                    layoutBinding.allMyStudioListView.visibility = View.GONE
                }

            } else {
                runOnUiThread {
                    layoutBinding.iconNoItem.visibility = View.GONE
                    layoutBinding.allMyStudioListView.visibility = View.VISIBLE
                }

            }
        }.start()
    }

    override fun onBackPressed() {
        if (isYesNoDialogShow) {
            dismissYesNoDialog()
            return
        }
        if (selectedMode) {
            closeSelectMode()
        } else {
            super.onBackPressed()
        }
    }


    override fun onResume() {
        super.onResume()
        getAllMyStudioItem()
    }

}
