package com.slideshowmaker.slideshow.ui.base

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.media.MediaRecorder
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ads.control.ads.VioAdmob
import com.airbnb.epoxy.EpoxyRecyclerView
import com.daasuu.gpuv.player.GPUPlayerView
import com.slideshowmaker.slideshow.BuildConfig
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.adapter.StickerAddedListAdapter
import com.slideshowmaker.slideshow.adapter.TextStickerAddedListAdapter
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.data.models.MusicReturnInfo
import com.slideshowmaker.slideshow.models.RecordedModel
import com.slideshowmaker.slideshow.models.StickerAddedModel
import com.slideshowmaker.slideshow.models.TextStickerAddedModel
import com.slideshowmaker.slideshow.modules.audio_manager_v3.AudioManagerV3
import com.slideshowmaker.slideshow.ui.HomeActivity
import com.slideshowmaker.slideshow.ui.custom.AddTextLayout
import com.slideshowmaker.slideshow.ui.custom.ChooseStickerLayout
import com.slideshowmaker.slideshow.ui.custom.CropVideoTimeView
import com.slideshowmaker.slideshow.ui.custom.EditTextSticker
import com.slideshowmaker.slideshow.ui.custom.StickerView
import com.slideshowmaker.slideshow.ui.select_music.SelectMusicActivity
import com.slideshowmaker.slideshow.utils.BitmapHelper
import com.slideshowmaker.slideshow.utils.DimenUtils
import com.slideshowmaker.slideshow.utils.FileHelper
import com.slideshowmaker.slideshow.utils.Logger
import kotlinx.android.synthetic.main.activity_base.baseRootView
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.frAdsEdit
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.fullScreenOtherLayoutContainer
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.icPlay
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.otherLayoutContainer
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.slideBgPreview
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.slideGlViewContainer
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.stickerContainer
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.toolsAction
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.videoControllerView
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.videoGlViewContainer
import kotlinx.android.synthetic.main.layout_view_change_music_tools.view.btnOnlineMusic
import kotlinx.android.synthetic.main.layout_view_change_music_tools.view.btnStorageMusic
import kotlinx.android.synthetic.main.layout_view_change_music_tools.view.icDelete
import kotlinx.android.synthetic.main.layout_view_change_music_tools.view.icVideoVolume
import kotlinx.android.synthetic.main.layout_view_change_music_tools.view.musicVolumeSeekBar
import kotlinx.android.synthetic.main.layout_view_change_music_tools.view.rvMusicItem
import kotlinx.android.synthetic.main.layout_view_change_music_tools.view.soundNameLabel
import kotlinx.android.synthetic.main.layout_view_change_music_tools.view.videoVolumeSeekBar
import kotlinx.android.synthetic.main.layout_view_change_sticker_tools.view.buttonAddSticker
import kotlinx.android.synthetic.main.layout_view_change_sticker_tools.view.buttonPlayAndPause
import kotlinx.android.synthetic.main.layout_view_change_sticker_tools.view.cancelAddSticker
import kotlinx.android.synthetic.main.layout_view_change_sticker_tools.view.confirmAddSticker
import kotlinx.android.synthetic.main.layout_view_change_sticker_tools.view.cropTimeView
import kotlinx.android.synthetic.main.layout_view_change_sticker_tools.view.stickerAddedListView
import kotlinx.android.synthetic.main.layout_view_change_text_tools.view.buttonAddText
import kotlinx.android.synthetic.main.layout_view_change_text_tools.view.buttonPlayAndPauseInText
import kotlinx.android.synthetic.main.layout_view_change_text_tools.view.cancelAddTextSticker
import kotlinx.android.synthetic.main.layout_view_change_text_tools.view.confirmAddText
import kotlinx.android.synthetic.main.layout_view_change_text_tools.view.cropTimeViewInText
import kotlinx.android.synthetic.main.layout_view_change_text_tools.view.textStickerAddedListView
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import timber.log.Timber
import java.io.IOException
import kotlin.math.roundToInt

abstract class BaseSlideShow : BaseActivity(), KodeinAware {
    override val kodein by closestKodein()
    override fun getContentResId(): Int = R.layout.activity_base_tools_edit_screen
    protected var onEditSticker = false
    protected var curMusicInfo: MusicReturnInfo? = null
    protected var toolType = ToolType.NONE
    private var curVideoVol = 1f
    val audioManagerV3: AudioManagerV3 by instance()
    private val mediaPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        }

    @Volatile
    protected var isTouchEnable = true
    private val stickerAddedAdapter = StickerAddedListAdapter(object : StickerAddedListAdapter.OnChange {
        override fun onClickSticker(stickerAddedModel: StickerAddedModel) {
            updateChangeStickerLayout(stickerAddedModel, true)
        }
    })

    private val textStickerAddedAdapter =
        TextStickerAddedListAdapter(object : TextStickerAddedListAdapter.OnChange {
            override fun onClickTextSticker(textStickerAddedModel: TextStickerAddedModel) {
                updateChangeTextStickerLayout(textStickerAddedModel, true)
            }

        })

    override fun initViews() {

        doInitViews()
        val screenWidth = DimenUtils.screenWidth(this)
        val videoPreviewScaleObj = DimenUtils.videoPreviewScale()
        Logger.e("scale = $videoPreviewScaleObj")
        slideBgPreview.layoutParams.width = (screenWidth * videoPreviewScaleObj).toInt()
        slideBgPreview.layoutParams.height = (screenWidth * videoPreviewScaleObj).toInt()
        baseRootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            baseRootView.getWindowVisibleDisplayFrame(rect)
            //  Logger.e("bottom = ${rect.bottom} -- top = ${rect.top}")
            if (baseRootView.rootView.height - (rect.bottom - rect.top) > 500) {
                //    Logger.e("key board show")
                addTextLayoutObject?.translationY = -56 * DimenUtils.density()
            } else {
                //    Logger.e("key board hide")
                addTextLayoutObject?.translationY = 0f
            }
        }

        initBanner()

    }

    private fun initBanner() {
        if (!SharedPreferUtils.proUser) {
            VioAdmob.getInstance().loadBanner(this, BuildConfig.banner)
        } else {
            frAdsEdit.visibility = View.GONE
        }
    }

    override fun reloadBanner() {
        super.reloadBanner()
        if (!SharedPreferUtils.proUser) {
            findViewById<FrameLayout>(R.id.fl_shimemr).visibility = View.INVISIBLE
            VioAdmob.getInstance().loadBanner(this, BuildConfig.banner)
        }
    }

    override fun initActions() {


        setRightTextButton(R.string.button_save_label) {
            performExportVideo()
            hideKeyboard()
        }
        doInitActions()
    }

    fun useDefaultMusic() {
        audioManagerV3.useDefault()
    }

    var isClickSelectMusicAvailable = true
    fun showLayoutChangeMusic() {
        val viewTools = View.inflate(this, R.layout.layout_view_change_music_tools, null)
        showToolsActionLayout(viewTools)

        viewTools.soundNameLabel.setClick {
            if (isClickSelectMusicAvailable) {
                isClickSelectMusicAvailable = false
                val intent = Intent(this, SelectMusicActivity::class.java)
                curMusicInfo?.let {
                    Bundle().apply {
                        putSerializable("CurrentMusic", it)
                        intent.putExtra("bundle", this)
                    }
                }

                startActivityForResult(intent, SelectMusicActivity.SELECT_MUSIC_REQUEST_CODE)

                object : CountDownTimer(1000, 1000) {
                    override fun onFinish() {
                        isClickSelectMusicAvailable = true
                    }

                    override fun onTick(millisUntilFinished: Long) {

                    }

                }.start()
            }

        }
        viewTools.icDelete.setOnClickListener {
            viewTools.icDelete.visibility = View.GONE
            audioManagerV3.returnToDefault(getCurrentVideoTimeMs())
            curMusicInfo = null
            updateChangeMusicLayout()
        }
        updateChangeMusicLayout()
        viewTools.musicVolumeSeekBar.setProgressChangeListener {
            audioManagerV3.setVolume(it / 100f)
        }
        viewTools.videoVolumeSeekBar.setProgressChangeListener {
            performChangeVideoVolume(it / 100f)
            curVideoVol = it / 100f
        }
        if (isImageSlideShow()) {
            viewTools.videoVolumeSeekBar.visibility = View.GONE
            viewTools.icVideoVolume.visibility = View.INVISIBLE
        }
        setupMusicList(viewTools.rvMusicItem)
        viewTools.btnOnlineMusic.setOnClickListener {
            val intent = Intent(this, SelectMusicActivity::class.java).apply {
                putExtra("music_source", "online")
            }

            startActivityForResult(intent, SelectMusicActivity.SELECT_MUSIC_REQUEST_CODE)
        }
        viewTools.btnStorageMusic.setOnClickListener {
            if (checkStoragePermission()) {
                val intent = Intent(this, SelectMusicActivity::class.java).apply {
                    putExtra("music_source", "local")
                }
                startActivityForResult(intent, SelectMusicActivity.SELECT_MUSIC_REQUEST_CODE)
            } else {
                requestStoragePermission()
            }
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mediaPermissionsLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                HomeActivity.STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun checkStoragePermission(): Boolean { //true if GRANTED
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }


    open fun setupMusicList(view: EpoxyRecyclerView) {

    }

    open fun reloadMusicList() {
        findViewById<EpoxyRecyclerView>(R.id.rvMusicItem)?.requestModelBuild()
    }

    fun showLayoutTrimMusic() {
        val viewTools = View.inflate(this, R.layout.layout_view_change_music_tools, null)
        showToolsActionLayout(viewTools)
    }

    private fun updateChangeMusicLayout() {
        val childView = toolsAction.getChildAt(toolsAction.childCount - 1)
        if (audioManagerV3.getAudioName() == "none") {
            childView?.icDelete?.visibility = View.GONE
            childView?.soundNameLabel?.text = getString(R.string.common_default)
            // view.icMusic.setImageResource(R.drawable.ic_music_default)
            // view.musicVolumeSeekBar.setHighlightColor(resources.getColor(R.color.grayA01))
        } else {
            childView?.icDelete?.visibility = View.GONE
            childView?.soundNameLabel?.text = audioManagerV3.getAudioName()
            // view.icMusic.setImageResource(R.drawable.ic_music_active)
            // view.musicVolumeSeekBar.setHighlightColor(resources.getColor(R.color.orangeA01))

        }
        childView?.musicVolumeSeekBar?.setProgress(audioManagerV3.getVolume() * 100)
        childView?.videoVolumeSeekBar?.setProgress(curVideoVol * 100)
    }

    protected fun getMusicData(): String = audioManagerV3.getOutMusicPath()
    protected fun getMusicVolume(): Float = audioManagerV3.getVolume()


    fun showLayoutChangeSticker() {
        val viewTools = View.inflate(this, R.layout.layout_view_change_sticker_tools, null)
        showToolsActionLayout(viewTools)

        viewTools.stickerAddedListView.apply {
            adapter = stickerAddedAdapter
            layoutManager =
                LinearLayoutManager(this@BaseSlideShow, LinearLayoutManager.HORIZONTAL, false)
        }

        viewTools.confirmAddSticker.setOnClickListener {
            setOffAllSticker()
            stickerAddedAdapter.setOffAll()
            viewTools.cropTimeView.visibility = View.INVISIBLE
            viewTools.buttonPlayAndPause.visibility = View.INVISIBLE
            showVideoController()
        }

        if (stickerAddedAdapter.itemCount < 1) {
            viewTools.cancelAddSticker.visibility = View.GONE
        }

        viewTools.cancelAddSticker.setOnClickListener {
            showYesNoDialog(getString(R.string.do_you_want_to_delete_all_sticker)) {
                /*stickerContainer.removeAllViews()
                mStickerAddedAdapter.deleteAllItem()*/
                deleteAllSticker()
                viewTools.cropTimeView.visibility = View.INVISIBLE
                viewTools.buttonPlayAndPause.visibility = View.INVISIBLE
                showVideoController()
                viewTools.cancelAddSticker.visibility = View.GONE
            }
        }

        viewTools.buttonAddSticker.setOnClickListener {
            isTouchEnable = false
            val chooseStickerLayoutInst = ChooseStickerLayout(this)
            otherLayoutContainer.removeAllViews()
            otherLayoutContainer.addView(chooseStickerLayoutInst)
            playSlideDownToUpAnimation(chooseStickerLayoutInst, otherLayoutContainer.height)
            chooseStickerLayoutInst.stickerCallback = object : ChooseStickerLayout.StickerCallback {
                override fun onSelectSticker(stickerPath: String) {
                    setOffAllSticker()
                    Thread {
                        BitmapHelper.loadBitmapFromXML(stickerPath) {
                            runOnUiThread {
                                it?.let { bitmap ->
                                    val viewId = View.generateViewId()
                                    val model = StickerAddedModel(
                                        bitmap, true, 0, getMaxDuration(), viewId
                                    )
                                    stickerAddedAdapter.setOffAll()
                                    stickerAddedAdapter.addNewSticker(model)
                                    stickerContainer.addView(StickerView(
                                        this@BaseSlideShow,
                                        null
                                    ).apply {
                                        setBitmap(
                                            bitmap,
                                            true,
                                            stickerContainer.width,
                                            stickerContainer.height
                                        )
                                        id = viewId
                                        deleteHandleCallback = {
                                            stickerContainer.removeView(this)
                                            stickerAddedAdapter.deleteItem(
                                                model
                                            )
                                            setOffAllSticker()
                                            stickerAddedAdapter.setOffAll()

                                            getTopViewInToolAction().cropTimeView.visibility =
                                                View.INVISIBLE
                                            getTopViewInToolAction().buttonPlayAndPause.visibility =
                                                View.INVISIBLE
                                            Logger.e(" --> on delete sticker")
                                            showVideoController()
                                            if (stickerAddedAdapter.itemCount < 1) {
                                                getTopViewInToolAction().cancelAddSticker.visibility =
                                                    View.GONE
                                            }
                                        }
                                    })
                                    updateChangeStickerLayout(model, false)
                                }
                            }
                        }
                    }.start()
                    onBackPressed()
                }
            }
            activeTouch()

            setRightTextButton(R.string.done, false) {
                otherLayoutContainer.removeAllViews()
                setRightTextButton(R.string.button_save_label, false) {
                    performExportVideo()
                }
            }
        }
    }

    private fun deleteAllSticker() {/*stickerContainer.removeAllViews()
                mStickerAddedAdapter.deleteAllItem()*/
        val listStickerView = ArrayList<View>()
        for (i in 0 until stickerContainer.childCount) {
            val childView = stickerContainer.getChildAt(i)
            if (childView is StickerView) {
                //stickerContainer.removeView(view)
                listStickerView.add(childView)
            }
        }
        listStickerView.forEach {
            stickerContainer.removeView(it)
        }
        stickerAddedAdapter.deleteAllItem()
    }

    protected fun getStickerAddedList(): ArrayList<StickerAddedModel> =
        stickerAddedAdapter.itemArray

    private fun updateChangeStickerLayout(
        stickerAddedModel: StickerAddedModel, autoSeek: Boolean
    ) {

        if (autoSeek) {
            performSeekTo(stickerAddedModel.startTimeInMilSec)
        }
        val childView = toolsAction.getChildAt(toolsAction.childCount - 1)
        childView.cropTimeView.visibility = View.VISIBLE
        childView.buttonPlayAndPause.apply {
            visibility = View.VISIBLE
            setOnClickListener { changeVideoStateInAddSticker(childView) }
        }
        if (stickerAddedAdapter.itemCount > 0) {
            childView.cancelAddSticker.visibility = View.VISIBLE
        }
        childView.cropTimeView.apply {
            if (!isImageSlideShow()) {
                loadVideoImagePreview(
                    getSourcePathList(),
                    DimenUtils.screenWidth(this@BaseSlideShow) - (76 * DimenUtils.density(this@BaseSlideShow)).roundToInt()
                )
            } else {
                loadImage(getSourcePathList())
            }

            setMax(getMaxDuration())
            setStartAndEnd(
                stickerAddedModel.startTimeInMilSec, stickerAddedModel.endTimeInMilSec
            )
        }

        childView.cropTimeView.onChangeListener = object : CropVideoTimeView.OnChangeListener {
            override fun onSwipeLeft(startTimeMilSec: Float) {
                changeVideoStateToPauseInAddSticker(childView)
                stickerAddedModel.startTimeInMilSec = startTimeMilSec.toInt()
            }

            override fun onUpLeft(startTimeMilSec: Float) {
                changeVideoStateToPauseInAddSticker(childView)
                stickerAddedModel.startTimeInMilSec = startTimeMilSec.toInt()
                performSeekTo(stickerAddedModel.startTimeInMilSec)
            }

            override fun onSwipeRight(endTimeMilSec: Float) {
                changeVideoStateToPauseInAddSticker(childView)
                stickerAddedModel.endTimeInMilSec = endTimeMilSec.toInt()
            }

            override fun onUpRight(endTimeMilSec: Float) {
                changeVideoStateToPauseInAddSticker(childView)
                stickerAddedModel.endTimeInMilSec = endTimeMilSec.toInt()
            }

        }
        hideVideoController()
        setOffAllSticker()
        detectInEdit(stickerAddedModel)
    }

    private fun changeVideoStateInAddSticker(view: View) {
        view.buttonPlayAndPause.apply {
            if (isPlaying()) {
                setImageResource(R.drawable.icon_play_circle)
                performPauseVideo()
            } else {
                setImageResource(R.drawable.icon_pause_circle)
                performPlayVideo()
            }
        }
    }

    private fun changeVideoStateToPauseInAddSticker(view: View) {
        view.buttonPlayAndPause.apply {
            setImageResource(R.drawable.icon_play_circle)
            performPauseVideo()
        }
    }

    private fun detectInEdit(stickerAddedModel: StickerAddedModel) {
        for (index in 0 until stickerContainer.childCount) {
            val childView = stickerContainer.getChildAt(index)
            if (childView is StickerView) {
                if (childView.getBitmap() == stickerAddedModel.bitmap) {
                    childView.setInEdit(true)
                    stickerContainer.removeView(childView)
                    stickerContainer.addView(childView)
                    return
                }
            }
        }
    }

    fun setOffAllSticker() {
        for (index in 0 until stickerContainer.childCount) {
            val childView = stickerContainer.getChildAt(index)
            if (childView is StickerView) {
                childView.setInEdit(false)
            }
        }
    }

    private var addTextLayoutObject: AddTextLayout? = null
    fun showLayoutChangeText() {
        setOffAllTextSticker()
        textStickerAddedAdapter.setOffAll()
        val viewTool = View.inflate(this, R.layout.layout_view_change_text_tools, null)
        showToolsActionLayout(viewTool)

        viewTool.buttonAddText.setOnClickListener {
            setOffAllTextSticker()
            getTopViewInToolAction().cropTimeViewInText.visibility = View.INVISIBLE
            getTopViewInToolAction().buttonPlayAndPauseInText.visibility = View.INVISIBLE
            showAddTextLayout(null, true)
        }

        viewTool.confirmAddText.setOnClickListener {
            setOffAllTextSticker()
            textStickerAddedAdapter.setOffAll()
            viewTool.cropTimeViewInText.visibility = View.INVISIBLE
            viewTool.buttonPlayAndPauseInText.visibility = View.INVISIBLE
            showVideoController()
        }
        if (textStickerAddedAdapter.itemCount < 1) {
            viewTool.cancelAddTextSticker.visibility = View.GONE
        }
        viewTool.cancelAddTextSticker.setOnClickListener {
            showYesNoDialog(getString(R.string.do_you_want_to_delete_all_text)) {
                getTopViewInToolAction().cropTimeViewInText.visibility = View.INVISIBLE
                getTopViewInToolAction().buttonPlayAndPauseInText.visibility = View.INVISIBLE/*textStickerContainer.removeAllViews()
                mTextStickerAddedAdapter.deleteAllItem()
                setOffAllTextSticker()
                mTextStickerAddedAdapter.setOffAll()*/
                deleteAllTextSticker()
                showVideoController()
                hideKeyboard()
                viewTool.cancelAddTextSticker.visibility = View.GONE
            }

        }
        viewTool.textStickerAddedListView.apply {
            adapter = textStickerAddedAdapter
            layoutManager = LinearLayoutManager(
                this@BaseSlideShow, LinearLayoutManager.HORIZONTAL, false
            )
        }
    }

    private fun deleteAllTextSticker() {
        val listTextStickerView = ArrayList<View>()
        for (i in 0 until stickerContainer.childCount) {
            val childView = stickerContainer.getChildAt(i)
            if (childView is EditTextSticker) {
                //    stickerContainer.removeView(view)
                listTextStickerView.add(childView)
            }
        }
        listTextStickerView.forEach {
            stickerContainer.removeView(it)
        }
        // stickerContainer.removeAllViews()
        textStickerAddedAdapter.deleteAllItem()
        setOffAllTextSticker()
        textStickerAddedAdapter.setOffAll()
    }

    protected fun getTextAddedList(): ArrayList<TextStickerAddedModel> =
        textStickerAddedAdapter.itemArray

    private fun showAddTextLayout(
        editTextSticker: EditTextSticker? = null, isEdit: Boolean = false
    ) {
        isTouchEnable = false

        setOffAllTextSticker()
        textStickerAddedAdapter.setOffAll()
        editTextSticker?.let {
            it.changeIsAdded(false)
            stickerContainer.removeView(it)
            it.setInEdit(true)

            //textStickerAddedDataModel = mTextStickerAddedAdapter.deleteItem(it.id)
        }

        addTextLayoutObject = AddTextLayout(this, editTextSticker)
        performPauseVideo()
        fullScreenOtherLayoutContainer.apply {
            removeAllViews()
            addView(addTextLayoutObject)
            playTranslationYAnimation(this)

        }

        setRightTextButton(R.string.done, false) {
            addTextLayoutObject?.hideKeyboard()
            if(addTextLayoutObject?.getEditTextView() == null) {
                hideAllViewInFullScreenLayout()
            }
            addTextLayoutObject?.getEditTextView()?.let {
                Log.d("AddText", "Call")
                performAddText(it)
            }

            setRightTextButton(R.string.button_save_label, false) {
                performExportVideo()
            }

        }
        setScreenTitle(getString(R.string.text_editor))
        onPauseVideo()
        if (isEdit) addTextLayoutObject?.showKeyboard()
        activeTouch()
    }

    private fun activeTouch() {
        Thread {
            Thread.sleep(500)
            isTouchEnable = true
        }.start()
    }

    private fun performAddText(editTextSticker: EditTextSticker) {

        stickerContainer.addView(editTextSticker)
        editTextSticker.changeIsAdded(true)
        getTopViewInToolAction().cancelAddTextSticker.visibility = View.VISIBLE
        val model: TextStickerAddedModel
        if (textStickerAddedAdapter.getItemBytViewId(editTextSticker.id) == null) {
            model = TextStickerAddedModel(
                editTextSticker.getMainText(), true, 0, getMaxDuration(), editTextSticker.id
            )
            textStickerAddedAdapter.addNewText(model)
        } else {
            model =
                textStickerAddedAdapter.getItemBytViewId(editTextSticker.id)!!
            model.onEdit = true
            textStickerAddedAdapter.notifyDataSetChanged()
        }

        updateChangeTextStickerLayout(model, false)
        editTextSticker.deleteListener = {
            getTopViewInToolAction().cropTimeViewInText.visibility = View.INVISIBLE
            getTopViewInToolAction().buttonPlayAndPauseInText.visibility = View.INVISIBLE



            stickerContainer.removeView(editTextSticker)
            textStickerAddedAdapter.deleteItem(model)
            setOffAllTextSticker()
            textStickerAddedAdapter.setOffAll()
            showVideoController()
            hideKeyboard()
            if (textStickerAddedAdapter.itemCount < 1) {
                getTopViewInToolAction().cancelAddTextSticker.visibility = View.GONE
            }
        }
        editTextSticker.editListener = { textSticker ->
            showAddTextLayout(textSticker, true)
            Logger.e("onEdit")
        }
        hideAllViewInFullScreenLayout()
    }

    private fun updateChangeTextStickerLayout(
        textStickerAddedModel: TextStickerAddedModel, autoSeek: Boolean
    ) {
        val childView = toolsAction.getChildAt(toolsAction.childCount - 1)
        if (autoSeek) {
            performSeekTo(textStickerAddedModel.startTimeInMilSec)
        }
        childView.cropTimeViewInText.visibility = View.VISIBLE
        childView.buttonPlayAndPauseInText.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                changeVideoStateInAddStickerInText(childView)
            }
        }

        childView.cropTimeViewInText.apply {
            if (!isImageSlideShow()) {
                loadVideoImagePreview(
                    getSourcePathList(),
                    DimenUtils.screenWidth(this@BaseSlideShow) - (76 * DimenUtils.density(this@BaseSlideShow)).roundToInt()
                )
            } else {
                loadImage(getSourcePathList())
            }
            setMax(getMaxDuration())
            setStartAndEnd(
                textStickerAddedModel.startTimeInMilSec, textStickerAddedModel.endTimeInMilSec
            )
        }
        childView.cropTimeViewInText.onChangeListener = object : CropVideoTimeView.OnChangeListener {
            override fun onSwipeLeft(startTimeMilSec: Float) {
                changeVideoStateToPauseInAddStickerInText(childView)
                textStickerAddedModel.startTimeInMilSec = startTimeMilSec.toInt()
            }

            override fun onUpLeft(startTimeMilSec: Float) {
                changeVideoStateToPauseInAddStickerInText(childView)
                textStickerAddedModel.startTimeInMilSec = startTimeMilSec.toInt()
                performSeekTo(textStickerAddedModel.startTimeInMilSec)
            }

            override fun onSwipeRight(endTimeMilSec: Float) {
                changeVideoStateToPauseInAddStickerInText(childView)
                textStickerAddedModel.endTimeInMilSec = endTimeMilSec.toInt()
            }

            override fun onUpRight(endTimeMilSec: Float) {
                changeVideoStateToPauseInAddStickerInText(childView)
                textStickerAddedModel.endTimeInMilSec = endTimeMilSec.toInt()
            }

        }
        hideVideoController()
        setOffAllTextSticker()
        detectInEdit(textStickerAddedModel)
        changeVideoStateToPauseInAddStickerInText(childView)
        onPauseVideo()
    }

    private fun changeVideoStateInAddStickerInText(view: View) {
        view.buttonPlayAndPauseInText.apply {
            if (isPlaying()) {
                setImageResource(R.drawable.icon_play_circle)
                performPauseVideo()
            } else {
                setImageResource(R.drawable.icon_pause_circle)
                performPlayVideo()
            }
        }
    }

    private fun changeVideoStateToPauseInAddStickerInText(view: View) {
        view.buttonPlayAndPauseInText.apply {
            setImageResource(R.drawable.icon_play_circle)
            performPauseVideo()
        }
    }

    private fun setOffAllTextSticker() {
        for (index in 0 until stickerContainer.childCount) {
            val childView = stickerContainer.getChildAt(index)
            if (childView is EditTextSticker) {
                childView.setInEdit(false)
            }
        }
    }

    private fun detectInEdit(textStickerAddedModel: TextStickerAddedModel) {
        for (index in 0 until stickerContainer.childCount) {
            val childView = stickerContainer.getChildAt(index)
            if (childView is EditTextSticker) {
                if (childView.id == textStickerAddedModel.viewId) {
                    childView.setInEdit(true)
                    stickerContainer.removeView(childView)
                    stickerContainer.addView(childView)
                    return
                }
            }
        }
    }

    private var mediaRecorder: MediaRecorder? = null
    private var recordFilePath = ""
    private var recordTimer: CountDownTimer? = null

    //private var mIsRecording = false
    private var curRecordModelObject: RecordedModel? = null
    override fun onResume() {
        super.onResume()
        //    hideKeyboard()
        addTextLayoutObject?.onResume()
    }

    private fun startRecordAudio() {
        performPauseVideo()
        hideVideoController()
        recordFilePath = FileHelper.getAudioRecordTempFilePath()
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(recordFilePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {

            }

            start()
            recordTimer?.start()
        }
    }

    /*    private fun stopRecordAudio() {

            showVideoController()
            try {
                mRecordingTimer?.cancel()
                mRecorder?.stop()
                mRecorder?.release()
                mRecorder = null
                mRecordingFilePath = ""
                object :CountDownTimer(2000,1000) {
                    override fun onFinish() {
                        mIsRecording = false
                    }

                    override fun onTick(millisUntilFinished: Long) {

                    }

                }.start()


            } catch (e:Exception) {
                mRecorder = null
                mIsRecording = false
                mRecordingFilePath = ""
                Logger.e("exception = ${e.printStackTrace()}")
            }


        }*/

    protected fun setGLView(glSurfaceView: GLSurfaceView) {

        slideGlViewContainer.addView(
            glSurfaceView, FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    protected fun setExoPlayerView(playerView: GPUPlayerView) {
        videoGlViewContainer.removeAllViews()
        videoGlViewContainer.addView(
            playerView, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
        )
    }

    protected fun releaseExoPlayerView() {
        slideGlViewContainer.removeAllViews()

    }

    protected fun removeGLiew() {
        slideGlViewContainer.removeAllViews()
    }

    fun updateTimeline() {
        videoControllerView.setCurrentDuration(getCurrentVideoTimeMs())
        checkInTime(getCurrentVideoTimeMs())
    }

    protected fun checkInTime(timeMs: Int) {
        checkStickerInTime(timeMs)
        checkTextInTime(timeMs)
    }

    private fun checkStickerInTime(timeMilSec: Int) {
        for (item in getStickerAddedList()) {
            if (timeMilSec >= item.startTimeInMilSec && timeMilSec <= item.endTimeInMilSec) {
                val stickerView = findViewById<View>(item.stickerViewId)
                if (stickerView.visibility != View.VISIBLE) stickerView.visibility = View.VISIBLE
            } else {
                val stickerView = findViewById<View>(item.stickerViewId)
                if (stickerView.visibility == View.VISIBLE) stickerView.visibility = View.INVISIBLE
            }
        }
    }


    private fun checkTextInTime(timeMilSec: Int) {
        for (item in getTextAddedList()) {
            if (timeMilSec >= item.startTimeInMilSec && timeMilSec <= item.endTimeInMilSec) {
                val itemView = findViewById<View>(item.viewId)
                if (itemView.visibility != View.VISIBLE) itemView.visibility = View.VISIBLE
            } else {
                val itemView = findViewById<View>(item.viewId)
                if (itemView.visibility == View.VISIBLE) itemView.visibility = View.INVISIBLE
            }
        }
    }

    fun setMaxTime(timeMs: Int) {
        videoControllerView.setMaxDuration(timeMs)
    }

    protected fun showToolsActionLayout(view: View) {
        showVideoController()
        setOffAllSticker()
        setOffAllTextSticker()
        stickerAddedAdapter.setOffAll()
        textStickerAddedAdapter.setOffAll()
        toolsAction.removeAllViews()
        toolsAction.addView(
            view, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        playTranslationYAnimation(view)
    }

    protected fun onPauseVideo() {
        if (toolType == ToolType.STICKER) {
            val view = getTopViewInToolAction()
            view.buttonPlayAndPause.setImageResource(R.drawable.icon_play_circle)
        } else if (toolType == ToolType.TEXT) {
            val view = getTopViewInToolAction()
            view.buttonPlayAndPauseInText.setImageResource(R.drawable.icon_play_circle)
        }
        audioManagerV3.pauseAudio()
        icPlay.visibility = View.VISIBLE
    }

    protected fun onPlayVideo() {
        if (toolType == ToolType.STICKER) {
            val view = getTopViewInToolAction()
            view.buttonPlayAndPause.setImageResource(R.drawable.icon_pause_circle)
        } else if (toolType == ToolType.TEXT) {
            val view = getTopViewInToolAction()
            view.buttonPlayAndPauseInText.setImageResource(R.drawable.icon_pause_circle)
        }
        audioManagerV3.playAudio()
        icPlay.visibility = View.GONE
    }

    protected fun onSeekTo(timeMs: Int) {
        Logger.e("seek to $timeMs")
        audioManagerV3.seekTo(timeMs)
        updateTimeline()
    }

    protected fun onRepeat() {
        Timber.d("onRepeat")
        audioManagerV3.repeat()
    }

    private fun hideVideoController() {
        onEditSticker = true
        performPauseVideo()
        videoControllerView.visibility = View.GONE
        icPlay.alpha = 0f
    }

    private fun showVideoController() {
        onEditSticker = false
        videoControllerView.visibility = View.VISIBLE
        icPlay.alpha = 1f
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {


        if (resultCode == Activity.RESULT_OK && requestCode == SelectMusicActivity.SELECT_MUSIC_REQUEST_CODE) {
            if (data != null) {
                val bundle = data.getBundleExtra("bundle")
                val musicReturnInfo =
                    (bundle?.getSerializable(SelectMusicActivity.MUSIC_RETURN_DATA_KEY)) as MusicReturnInfo
                changeMusicData(musicReturnInfo)
                addSelectedMusic(musicReturnInfo)
            }

        }


        super.onActivityResult(requestCode, resultCode, data)
    }

    open fun changeMusicData(musicReturnData: MusicReturnInfo) {

        if (curMusicInfo == null || curMusicInfo?.audioFilePath != musicReturnData.audioFilePath || curMusicInfo?.startOffset != musicReturnData.startOffset || curMusicInfo?.length != musicReturnData.length) {
            curMusicInfo = musicReturnData
            audioManagerV3.changeAudio(musicReturnData, getCurrentVideoTimeMs())
        }

        updateChangeMusicLayout()
        reloadMusicList()

    }

    open fun addSelectedMusic(musicReturnData: MusicReturnInfo) {

    }

    private fun getTopViewInToolAction(): View = toolsAction.getChildAt(toolsAction.childCount - 1)

    abstract fun isImageSlideShow(): Boolean

    abstract fun doInitViews()
    abstract fun doInitActions()
    abstract fun getCurrentVideoTimeMs(): Int

    abstract fun performPlayVideo()
    abstract fun performPauseVideo()
    abstract fun getMaxDuration(): Int
    abstract fun performSeekTo(timeMs: Int)
    abstract fun performSeekTo(timeMs: Int, showProgress: Boolean)
    abstract fun isPlaying(): Boolean
    abstract fun getSourcePathList(): ArrayList<String>
    abstract fun getScreenTitle(): String
    abstract fun performExportVideo()
    enum class ToolType {
        NONE, TRIM, EFFECT, THEME, TRANSITION, DURATION, MUSIC, STICKER, TEXT, FRAME, FILTER, RECORDER, RATIO
    }

    abstract fun performChangeVideoVolume(volume: Float)
    private fun hideKeyboard() {/* val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
         imm!!.hideSoftInputFromWindow(null, 1)*/
        addTextLayoutObject?.hideKeyboard()
    }

    override fun onBackPressed() {
        //  hideKeyboard()
        addTextLayoutObject?.hideKeyboard()
        when {
            otherLayoutContainer.childCount > 0 -> {
                otherLayoutContainer.removeAllViews()
                return
            }

            fullScreenOtherLayoutContainer.childCount > 0 -> {
                showYesNoDialog(getString(R.string.do_you_want_to_save), {
                    // hideAllViewInFullScreenLayout()
                    if (toolType == ToolType.TEXT) {
                        addTextLayoutObject?.getEditTextView()?.let {
                            performAddText(it)
                        }
                    }
                }, {
                    hideAllViewInFullScreenLayout()
                    if (toolType == ToolType.TEXT) {
                        addTextLayoutObject?.onCancelEdit()?.let {
                            performAddText(it)
                            Logger.e("on cancel edit text")/* val dataModel = mTextStickerAddedAdapter.getItemBytViewId(it.id)
                             if(dataModel!=null) {
                                 mTextStickerAddedAdapter.deleteItem(dataModel)
                             }
                             it.deleteCallback?.invoke()*/
                        }

                        addTextLayoutObject = null
                    }
                })

                return
            }

            else -> {
                super.onBackPressed()
            }
        }
    }

    private fun hideAllViewInFullScreenLayout() {

        fullScreenOtherLayoutContainer.removeAllViews()
        setScreenTitle(screenTitle())
        setRightTextButton(R.string.done) {
            performExportVideo()
            hideKeyboard()
        }
        setScreenTitle(getScreenTitle())

    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard()
    }

    protected fun setOffAllStickerAndText() {
        setOffAllSticker()
        setOffAllTextSticker()
        stickerAddedAdapter.setOffAll()
        textStickerAddedAdapter.setOffAll()

    }


}