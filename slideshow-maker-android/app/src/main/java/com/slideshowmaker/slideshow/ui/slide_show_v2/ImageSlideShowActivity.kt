package com.slideshowmaker.slideshow.ui.slide_show_v2

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.adapter.FrameListAdapter
import com.slideshowmaker.slideshow.adapter.ImageFilterListAdapter
import com.slideshowmaker.slideshow.adapter.ImageWithLookupListAdapter
import com.slideshowmaker.slideshow.adapter.RatioListAdapter
import com.slideshowmaker.slideshow.adapter.SlideImageListAdapter
import com.slideshowmaker.slideshow.adapter.ThemeListInHomeAdapter
import com.slideshowmaker.slideshow.adapter.TransitionListAdapter
import com.slideshowmaker.slideshow.data.RemoteConfigRepository
import com.slideshowmaker.slideshow.data.TrackingFactory
import com.slideshowmaker.slideshow.data.local.SessionData
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.data.models.AudioInfo
import com.slideshowmaker.slideshow.data.models.FilterLinkInfo
import com.slideshowmaker.slideshow.data.models.FrameLinkInfo
import com.slideshowmaker.slideshow.data.models.MusicReturnInfo
import com.slideshowmaker.slideshow.data.models.StickerForRenderInfo
import com.slideshowmaker.slideshow.data.models.ThemeLinkInfo
import com.slideshowmaker.slideshow.data.models.enum.MediaType
import com.slideshowmaker.slideshow.data.models.isNone
import com.slideshowmaker.slideshow.data.response.FilterDataSetting
import com.slideshowmaker.slideshow.data.response.FrameDataSetting
import com.slideshowmaker.slideshow.models.GSTransitionDataModel
import com.slideshowmaker.slideshow.models.RatioModel
import com.slideshowmaker.slideshow.modules.image_slide_show.ImageSlideGLView
import com.slideshowmaker.slideshow.modules.image_slide_show.ImageSlideRenderer
import com.slideshowmaker.slideshow.modules.image_slide_show.drawer.ImageSlideDataContainer
import com.slideshowmaker.slideshow.modules.music_player.MusicPlayer
import com.slideshowmaker.slideshow.modules.theme.data.ThemeData
import com.slideshowmaker.slideshow.modules.transition.GSTransitionUtils.getRandomTransitions
import com.slideshowmaker.slideshow.modules.transition.transition.GSTransition
import com.slideshowmaker.slideshow.ui.HomeActivity
import com.slideshowmaker.slideshow.ui.base.BaseSlideShow
import com.slideshowmaker.slideshow.ui.custom.BottomSheetTrimMusicView
import com.slideshowmaker.slideshow.ui.custom.EditTextSticker
import com.slideshowmaker.slideshow.ui.custom.StickerView
import com.slideshowmaker.slideshow.ui.custom.VideoControllerView
import com.slideshowmaker.slideshow.ui.dialog.ExportQualityVideoSelectionFragment
import com.slideshowmaker.slideshow.ui.dialog.SlideshowMakerDialogFragment
import com.slideshowmaker.slideshow.ui.export_video.ProcessVideoActivity
import com.slideshowmaker.slideshow.ui.picker.FromScreen
import com.slideshowmaker.slideshow.ui.picker.ImagePickerActivity
import com.slideshowmaker.slideshow.ui.premium.PremiumActivity
import com.slideshowmaker.slideshow.ui.premium.PremiumActivity.Companion.ARG_SOURCE_EXPORT_KEY
import com.slideshowmaker.slideshow.ui.premium.PremiumActivity.Companion.ARG_SOURCE_PRO_ELEMENT_KEY
import com.slideshowmaker.slideshow.ui.select_music.SelectMusicActivity.Companion.SELECT_MUSIC_REQUEST_CODE
import com.slideshowmaker.slideshow.ui.select_music.SelectMusicViewModel
import com.slideshowmaker.slideshow.utils.AdsHelper
import com.slideshowmaker.slideshow.utils.FileHelper
import com.slideshowmaker.slideshow.utils.ImageFilterHelper
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.Utils
import com.slideshowmaker.slideshow.utils.extentions.delayOnLifecycle
import com.slideshowmaker.slideshow.utils.extentions.isFirstInstall
import com.slideshowmaker.slideshow.utils.extentions.launchAndRepeatOnLifecycleStarted
import com.slideshowmaker.slideshow.utils.extentions.orFalse
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.layoutUsingProFeature
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.slideGlViewContainer
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.slideGlViewEffectContainer
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.slideNewFilterContainer
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.slideNewFrameContainer
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.stickerContainer
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.tabLayout
import kotlinx.android.synthetic.main.activity_base_tools_edit_screen.videoControllerView
import kotlinx.android.synthetic.main.layout_view_change_aspect_ratio.view.ratioListView
import kotlinx.android.synthetic.main.layout_view_change_duration_tools.view.changeDurationSeekBar
import kotlinx.android.synthetic.main.layout_view_change_duration_tools.view.totalDurationLabel
import kotlinx.android.synthetic.main.layout_view_change_duration_tools.view.tvSpeed
import kotlinx.android.synthetic.main.layout_view_change_filter_tools.view.imageListView
import kotlinx.android.synthetic.main.layout_view_change_filter_tools.view.lookupListView
import kotlinx.android.synthetic.main.layout_view_change_frame_tools.view.frameListView
import kotlinx.android.synthetic.main.layout_view_change_frame_tools.view.icAddPhotoInChangeFrame
import kotlinx.android.synthetic.main.layout_view_change_frame_tools.view.imageOfSlideShowListViewInChangeFrame
import kotlinx.android.synthetic.main.layout_view_change_theme_tools.view.icAddPhotoInChangeTheme
import kotlinx.android.synthetic.main.layout_view_change_theme_tools.view.imageOfSlideShowListViewInChangeTheme
import kotlinx.android.synthetic.main.layout_view_change_theme_tools.view.themeListView
import kotlinx.android.synthetic.main.layout_view_change_transition_tools.view.gsTransitionListView
import kotlinx.android.synthetic.main.layout_view_change_transition_tools.view.icAddPhotoInChangeTransition
import kotlinx.android.synthetic.main.layout_view_change_transition_tools.view.imageOfSlideShowListViewInChangeTransition
import kotlinx.android.synthetic.main.layout_using_pro_feature.btnUpgrade
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.kodein.di.generic.instance
import timber.log.Timber
import java.io.File


class ImageSlideShowActivity : BaseSlideShow() {

    private lateinit var imageGLView: ImageSlideGLView
    private lateinit var imageSlideRenderer: ImageSlideRenderer

    private lateinit var effectLottiePreviewView: LottieAnimationView
    private var effectUpdateJob: Job? = null
    private var selectedFrameInfo: FrameLinkInfo? = null
    private var selectedFilterInfo: FilterLinkInfo? = null
    val musicPlayer: MusicPlayer by instance()

    companion object {
        val imagePickedListKey = "Image picked list"
    }

    @Volatile
    private var imageSlideDataContainer: ImageSlideDataContainer = ImageSlideDataContainer(
        ArrayList()
    )

    private val images = ArrayList<String>()
    private var countDownTimer: CountDownTimer? = null
    private var currentTimeInMillis = 0
    private val isPlayingStateFlow = MutableStateFlow(false)
    private var isPlay: Boolean
        get() = isPlayingStateFlow.value
        set(value) {
            isPlayingStateFlow.value = value
        }

    private var needReload = false
    private val slideImageListAdapter = SlideImageListAdapter()
    private var randomGsTransions1: List<GSTransition> = emptyList()
    private var randomGsTransions2: List<GSTransition> = emptyList()

    private val musicViewModel: SelectMusicViewModel by instance()
    private var themeData1 = ThemeData()
        set(value) {
            field = value
            checkProSubscriptionRequired()
        }
    private var frameData = FrameLinkInfo("none", "none", false, "none", "none", "none")
        set(value) {
            field = value
            checkProSubscriptionRequired()
        }
    private var filterData = FilterLinkInfo("none", "none", false, "none", "none", "none")
        set(value) {
            field = value
            checkProSubscriptionRequired()
        }

    private var gsTransitions = emptyList<GSTransition>()

    private var gsTransition = GSTransition()
        set(value) {
            field = value
            checkProSubscriptionRequired()
        }

    private val proPlanLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                checkProSubscriptionRequired()
            }
        }

    private val proSubscriptionRequired: Boolean
        get() = !SharedPreferUtils.proUser && (gsTransition.isPro || themeData1.isPro)

    private val newThemeListAdapter = ThemeListInHomeAdapter(lifecycleScope, this)
    private val newFrameListAdapter = FrameListAdapter()

    private val transitionListAdapter = TransitionListAdapter {
        /*if (it.gsTransition.isWatchAds &&
            !SessionInfo.isTransitionUsed(it.gsTransition.id)
            && RemoteConfigRepository.isVersionAdsEnable
        ) {
            performPauseVideo()
            watchAdsForTransition(it)
        } else {
            useTransition(it)
        }*/
        useTransition(it)
    }

    private fun watchAdsForTransition(transitionModel: GSTransitionDataModel) {
        SlideshowMakerDialogFragment.Builder()
            .setMessage(getString(R.string.watch_video_to_use_transition))
            .setPrimaryButton(object : SlideshowMakerDialogFragment.Builder.Button {
                override val label: String
                    get() = getString(R.string.regular_ok)

                override fun onClickListener(dialog: Dialog?) {
                    dialog?.dismiss()
                    useTransition(transitionModel)
                    performPlayVideo()
                }

            })
            .setSecondaryButton(object : SlideshowMakerDialogFragment.Builder.Button {
                override val label: String
                    get() = getString(R.string.regular_cancel)

                override fun onClickListener(dialog: Dialog?) {
                    dialog?.dismiss()
                    performPlayVideo()
                }

            }).build()
            .show(supportFragmentManager, null)

    }

    private fun useTransition(transitionModel: GSTransitionDataModel) {
        gsTransitions = when (transitionModel.gsTransition.id) {
            "random_1" -> randomGsTransions1
            "random_2" -> {
                randomGsTransions2
            }

            else -> listOf(transitionModel.gsTransition)
        }
        gsTransition = transitionModel.gsTransition
        TrackingFactory.SlideshowEditor.useTransition(transitionModel.gsTransition.id).track()
        layoutUsingProFeature.isVisible =
            !SharedPreferUtils.proUser && transitionModel.gsTransition.isPro
        performChangeTransition(gsTransition)
        transitionListAdapter.highlightItem(gsTransition)
        SessionData.markTransitionUsed(transitionModel.gsTransition.id)
    }

    private val imageWithLookupListAdapter = ImageWithLookupListAdapter {
        doSeekById(it)
    }


    private val imageFilterListAdapter: ImageFilterListAdapter =
        ImageFilterListAdapter(onSelectLookup = ::onSelectFilter)

    private val ratioListAdapter = RatioListAdapter {
        onRatioChanged(it)
    }


    override fun isImageSlideShow(): Boolean = true

    override fun doInitViews() {
        AdsHelper.loadInterExport(this)
        if (SharedPreferUtils.openSlideshowEditorOnFirstLaunch && isFirstInstall()) {
            SharedPreferUtils.openSlideshowEditorOnFirstLaunch = false
            TrackingFactory.Common.openSlideshowEditorFirstLaunch().track()
        }
        TrackingFactory.SlideshowEditor.viewEditor().track()
        useDefaultMusic()
        setScreenTitle(getString(R.string.slide_show_editor_label))

        tabLayout.addTab(
            tabLayout.newTab().setCustomView(R.layout.view_custom_tab)
                .setText(getString(R.string.transition_button))
                .setIcon(R.drawable.ic_transition)
        )

        tabLayout.addTab(
            tabLayout.newTab().setCustomView(R.layout.view_custom_tab)
                .setText(getString(R.string.effect_button))
                .setIcon(R.drawable.icon_effect)
        )
        tabLayout.addTab(
            tabLayout.newTab().setCustomView(R.layout.view_custom_tab)
                .setText(getString(R.string.music_button))
                .setIcon(R.drawable.icon_music)
        )
        tabLayout.addTab(
            tabLayout.newTab().setCustomView(R.layout.view_custom_tab)
                .setText(getString(R.string.editor_frame))
                .setIcon(R.drawable.icon_frame)
        )
        tabLayout.addTab(
            tabLayout.newTab().setCustomView(R.layout.view_custom_tab)
                .setText(getString(R.string.editor_filter))
                .setIcon(R.drawable.icon_filter)
        )

        tabLayout.addTab(
            tabLayout.newTab().setCustomView(R.layout.view_custom_tab)
                .setText(getString(R.string.ratio))
                .setIcon(R.drawable.icon_ratio)
        )

        tabLayout.addTab(
            tabLayout.newTab().setCustomView(R.layout.view_custom_tab)
                .setText(getString(R.string.duration_button))
                .setIcon(R.drawable.icon_clock)
        )
        tabLayout.addTab(
            tabLayout.newTab().setCustomView(R.layout.view_custom_tab)
                .setText(getString(R.string.sticker_button))
                .setIcon(R.drawable.icon_sticker)
        )
        tabLayout.addTab(
            tabLayout.newTab().setCustomView(R.layout.view_custom_tab)
                .setText(getString(R.string.text_button))
                .setIcon(R.drawable.ic_text)
        )


        canShowDialog = true
        val themeFileName = intent.getStringExtra("themeFileName") ?: ""
        if (themeFileName.isNotEmpty()) {
            themeData1 = ThemeData(
                FileHelper.themeFolderUnzip + "/${themeFileName}",
                ThemeData.ThemType.NOT_REPEAT,
                themeFileName
            )
        }

        imageGLView = ImageSlideGLView(this, null)
        imageSlideRenderer = ImageSlideRenderer(gsTransition)
        imageGLView.doSetRenderer(imageSlideRenderer)
        setGLView(imageGLView)

        effectLottiePreviewView = LottieAnimationView(this).apply {
            repeatCount = LottieDrawable.INFINITE
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        slideGlViewEffectContainer.addView(
            effectLottiePreviewView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        showLoadingDialog()
        val imgList = intent.getStringArrayListExtra(imagePickedListKey)

        /*  imageList?.let {
              onInitSlide(it)
          }*/

        toolType = ToolType.TRANSITION
        showLayoutChangeTransition()

        btnUpgrade.setOnClickListener {
            proPlanLauncher.launch(PremiumActivity.newIntent(this, ARG_SOURCE_PRO_ELEMENT_KEY))
        }
        TrackingFactory.Common.showAdsInterstitial("slideshow").track()
        if (imgList == null || imgList.size < 1) {
            finishAfterTransition()
        } else {
            onInitSlide(imgList)
        }
    }

    private fun showEffectSelection(themeData: ThemeData) {
        if (themeData1.themeVideoFilePath != themeData.themeVideoFilePath) {
            themeData1 = themeData

            LottieCompositionFactory.clearCache(this)

            if (themeData.themeVideoFilePath == "none") {
                effectLottiePreviewView.pauseAnimation()
                effectLottiePreviewView.cancelAnimation()
                effectLottiePreviewView.isInvisible = true
            } else {
                effectLottiePreviewView.isInvisible = false
                effectLottiePreviewView.cancelAnimation()

                effectLottiePreviewView.setAnimation(
                    File(FileHelper.themeFolderPath)
                        .resolve(themeData.lottieFileName)
                        .inputStream(),
                    themeData.id,
                )
                effectLottiePreviewView.playAnimation()

                hideLoadingDialog()
            }
        }
    }

    private fun getBitmapFromUnzipFolder(idTheme: String) {

    }

    private fun checkProSubscriptionRequired() {
        layoutUsingProFeature.isVisible = proSubscriptionRequired
        if (proSubscriptionRequired)
            setRightTextIcon(R.drawable.icon_crown_badge)
        else
            setRightTextIcon(null)
    }

    private fun onInitSlide(pathList: ArrayList<String>) {
        images.clear()
        currentTimeInMillis = 0
        images.addAll(pathList)
        randomGsTransions1 = getRandomTransitions(images.size)
        randomGsTransions2 = getRandomTransitions(
            RemoteConfigRepository.randomTransitionConfig.random2?.ids.orEmpty(),
            images.size
        )
        gsTransitions = randomGsTransions1
        slideImageListAdapter.addImagePathList(images)
        Thread {
            imageSlideDataContainer = ImageSlideDataContainer(images)
            runOnUiThread {
                setMaxTime(imageSlideDataContainer.getMaxDurationMs())
                dismissLoadingDialog()
                doPlayVideo()
                playVideo()
                setRightTextEnabled(true)
                onRatioChanged(ratioListAdapter.getItem(0))
                if (themeData1.themeVideoFilePath != "none")
                    performChangeTheme(themeData1)
            }
        }.start()
    }

    private var currentFrameId = 0L
    private fun playVideo() {
        countDownTimer = object : CountDownTimer(4000000, 40) {
            override fun onFinish() {
                start()
            }

            override fun onTick(millisUntilFinished: Long) {
                if (isPlay) {
                    currentTimeInMillis += 40
                    if (currentTimeInMillis >= imageSlideDataContainer.getMaxDurationMs()) {
                        /*doPauseVideo()
                        doRepeat()
                        doPlayVideo()*/
                        //doSeekTo(1)
                        doRepeat()
                    } else {
                        updateTimeline()
                        val slideFrameData = imageSlideDataContainer.getFrameDataByTime(currentTimeInMillis)
                        if (slideFrameData.slideId != currentFrameId) {
                            imageSlideRenderer.resetData()
                            currentFrameId = slideFrameData.slideId
                            if (gsTransitions.size > 1) {
                                performChangeTransition(
                                    gsTransitions.getOrNull(
                                        imageSlideDataContainer.currentSlideIndex
                                    ) ?: gsTransitions.firstOrNull() ?: GSTransition()
                                )
                            }
                        }
                        imageSlideRenderer.changeFrameData(slideFrameData)
                        onStick()

                    }

                }
            }
        }.start()
    }

    private var mCurrentLookupType = ImageFilterHelper.LookupType.DEFAULT
    private fun onStick() {
        val pos =
            currentTimeInMillis / (imageSlideDataContainer.transitionTimeInMillis + imageSlideDataContainer.delayTimeInMillis)
        slideImageListAdapter.changeHighlightItem(pos)

    }

    override fun doInitActions() {


        setRightTextButton(R.string.button_save_label) {
            if (SharedPreferUtils.doesSaveSlideShowFirstTime && isFirstInstall()) {
                TrackingFactory.Common.clickSaveSlideShowFirstTime().track()
                SharedPreferUtils.doesSaveSlideShowFirstTime = false
            }
            if (proSubscriptionRequired) {
                proPlanLauncher.launch(PremiumActivity.newIntent(this, ARG_SOURCE_EXPORT_KEY))
            } else {
                doExportVideo()
            }
        }
        setRightTextEnabled(false)

        videoControllerView.onChangeListenerCallback = object : VideoControllerView.OnChangeListener {
            override fun onUp(timeMilSec: Int) {
                doSeekTo(timeMilSec)
            }

            override fun onMove(progress: Float) {

            }

        }

        imageGLView.setOnClickListener {
            if (onEditSticker) return@setOnClickListener
            if (needReload) {
                // mImageSlideDataContainer.onRepeat()
                currentTimeInMillis = 0
                needReload = false
            }
            if (isPlay) {
                doPauseVideo()
            } else {
                doPlayVideo()
            }
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                //TODO("Not yet implemented")
                when (tab!!.position) {
                    0 -> {
                        toolType = ToolType.TRANSITION
                        showLayoutChangeTransition()
                    }

                    1 -> {
                        toolType = ToolType.THEME
                        showLayoutChangeTheme()
                    }

                    2 -> {
                        toolType = ToolType.MUSIC
                        showLayoutChangeMusic()
                    }

                    3 -> {
                        toolType = ToolType.FRAME
                        showLayoutChangeFrame()
                    }

                    4 -> {
                        toolType = ToolType.FILTER
                        showLayoutChangeFilter()
                    }

                    5 -> {
                        toolType = ToolType.RATIO
                        showLayoutChangeRatio()
                    }

                    6 -> {
                        toolType = ToolType.DURATION
                        showLayoutChangeDuration()
                    }

                    7 -> {
                        toolType = ToolType.STICKER
                        showLayoutChangeSticker()
                    }

                    8 -> {
                        toolType = ToolType.TEXT
                        showLayoutChangeText()
                    }

                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //TODO("Not yet implemented")
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                //TODO("Not yet implemented")
            }
        })


        // TODO fix crash
        slideImageListAdapter.onClickItemCallback = {
            doSeekTo(it * (imageSlideDataContainer.delayTimeInMillis + imageSlideDataContainer.transitionTimeInMillis))
        }
    }

    private var selectedMusicId: String = "defaultAudio"

    override fun onBackPressed() {
        if (canShowDialog) {
            showYesNoDialog(comebackStatus) {
                val intent = Intent(this, HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("play-splash", false)
                }
                startActivity(intent)
            }
        } else {
            super.onBackPressed()
        }

    }
    override fun setupMusicList(view: EpoxyRecyclerView) {
        launchAndRepeatOnLifecycleStarted {
            musicViewModel.downloadedMusic.collect {

                val remoteAudioConfig = RemoteConfigRepository.audioConfigs
                val remoteAudioInfos = it.map { downloaded ->
                    val remoteAudio = remoteAudioConfig.orEmpty().firstOrNull { downloaded.id == it.id }
                    if (remoteAudio != null) {
                        AudioInfo(
                            downloaded.filePath,
                            remoteAudio.name.orEmpty(),
                            "audio/mp3",
                            downloaded.duration.toLong(),
                            downloaded.id,
                            remoteAudio.thumbnailUrl.orEmpty()
                        )
                    } else
                        null
                }.filterNotNull()
                    .take(5)
                    .toMutableList()

                remoteAudioInfos.addAll(
                    0,
                    listOf(
                        AudioInfo(
                            FileHelper.defaultAudio,
                            getString(R.string.inapp_music_1),
                            "audio/mp3",
                            0L,
                            "defaultAudio",
                            ""
                        ),
                        AudioInfo(
                            FileHelper.airboundAudio,
                            getString(R.string.inapp_music_2),
                            "audio/mp3",
                            0L,
                            "airboundAudio",
                            ""
                        ),
                        AudioInfo(
                            FileHelper.allNightDanceAudio,
                            getString(R.string.inapp_music_3),
                            "audio/mp3",
                            0L,
                            "allNightDanceAudio",
                            ""
                        ),
                        AudioInfo(
                            FileHelper.topOfTheMorning,
                            getString(R.string.inapp_music_4),
                            "audio/mp3",
                            0L,
                            "topOfTheMorning",
                            ""
                        ),
                        AudioInfo(
                            FileHelper.waitForUsAudio,
                            getString(R.string.inapp_music_5),
                            "audio/mp3",
                            0L,
                            "waitForUsAudio",
                            ""
                        ),
                    )
                )

                view.withModels {
                    remoteAudioInfos.forEach { it ->
                        SlideShowMusicItemEpoxyView_()
                            .id(it.fileId)
                            .item(it)
                            .playing(isPlay)
                            .selected(selectedMusicId == it.fileId)
                            .toggleClickListener { model, parentView, checkedView, isChecked, position ->
                                if (isChecked) {
                                    val music = model.item()
                                    if (curMusicInfo?.audioFilePath != music.filePath) {
                                        changeMusicData(
                                            MusicReturnInfo(
                                                music.filePath,
                                                music.filePath,
                                                0,
                                                music.duration.toInt()
                                            )
                                        )
                                        doPlayVideo()
                                    }
                                    selectedMusicId = music.fileId
                                }
                                requestModelBuild()
                            }
                            .playListener { model, parentView, clickedView, position ->
                                val music = model.item()
                                if (curMusicInfo?.audioFilePath != music.filePath) {
                                    changeMusicData(
                                        MusicReturnInfo(
                                            music.filePath,
                                            music.filePath,
                                            0,
                                            music.duration.toInt()
                                        )
                                    )
                                    doPlayVideo()
                                } else if (isPlay) {
                                    doPauseVideo()
                                } else {
                                    doPlayVideo()
                                }
                                selectedMusicId = music.fileId
                                requestModelBuild()
                            }
                            .trimClickListener { model, parentView, clickedView, position ->
                                showTrimMusic(model.item())
                            }
                            .addTo(this)
                    }
                }
                view.delayOnLifecycle(200L) {
                    val index = remoteAudioInfos.indexOfLast { it.fileId == selectedMusicId }
                    view.scrollToPosition(index)
                }
            }
        }
    }

    private fun showTrimMusic(audioInfo: AudioInfo) {
        val toolView = findViewById<BottomSheetTrimMusicView>(R.id.trimMusicView)
        performPauseVideo()
        if (toolView.isVisible.orFalse()) {
            toolView.dismiss()
        } else {
            toolView.showBottomSheet()
        }
        toolView.setCallback(object : BottomSheetTrimMusicView.Callback {
            override fun onDismissed() {
                runOnUiThread {
                    performPlayVideo()
                }
            }

        })
        toolView.setAudioData(audioInfo)
    }

    override fun changeMusicData(musicReturnData: MusicReturnInfo) {
        super.changeMusicData(musicReturnData)
        selectedMusicId = musicReturnData.fileId
    }

    override fun addSelectedMusic(musicReturnData: MusicReturnInfo) {
        super.addSelectedMusic(musicReturnData)
        musicViewModel.addSelectedMusic(musicReturnData)
    }

    override fun getCurrentVideoTimeMs(): Int = currentTimeInMillis
    override fun performPlayVideo() {
        doPlayVideo()
    }

    override fun performPauseVideo() {
        doPauseVideo()
    }

    override fun getMaxDuration(): Int = imageSlideDataContainer.getMaxDurationMs()

    override fun performSeekTo(timeMs: Int, showProgress: Boolean) {
        Logger.e("timeMs = $timeMs")
        if (timeMs >= imageSlideDataContainer.getMaxDurationMs()) {
            doRepeat()
        } else {
            doSeekTo(timeMs)
        }
    }

    override fun performSeekTo(timeMs: Int) {
        if (timeMs >= imageSlideDataContainer.getMaxDurationMs()) {
            doRepeat()
            Logger.e("performSeekTo -> doRepeat()")
            return
        }
        Logger.e("performSeekTo -> doSeekTo(timeMs)")
        doSeekTo(timeMs)
    }

    override fun isPlaying(): Boolean = isPlay
    override fun getSourcePathList(): ArrayList<String> = images
    override fun getScreenTitle(): String = getString(R.string.slide_show_editor_label)

    override fun performExportVideo() {
        doExportVideo()
    }

    override fun performChangeVideoVolume(volume: Float) {

    }

    private fun showLayoutChangeTheme() {
        val viewTool = View.inflate(this, R.layout.layout_view_change_theme_tools, null)
        showToolsActionLayout(viewTool)

        viewTool.imageOfSlideShowListViewInChangeTheme.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        viewTool.imageOfSlideShowListViewInChangeTheme.adapter = slideImageListAdapter

        viewTool.themeListView.adapter = newThemeListAdapter
        viewTool.themeListView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        newThemeListAdapter.itemArray.clear()
        newThemeListAdapter.notifyDataSetChanged()
        newThemeListAdapter.addItem(
            ThemeLinkInfo(
                link = "none",
                fileName = "None",
                thumb = "None",
                name = "None",
                isPro = false,
                id = "",
                lottieFileName = "none",
                lottieLink = "none"
            )
        )
        val themeV2 = RemoteConfigRepository.themeConfigV2
            .map {
                ThemeLinkInfo(
                    link = it.videoUrl,
                    fileName = it.fileName,
                    thumb = it.thumbnailUrl,
                    name = it.name.orEmpty(),
                    isPro = it.isPro.orFalse(),
                    id = it.id.orEmpty(),
                    lottieFileName = it.lottieFileName,
                    lottieLink = it.lottieUrl,
                )
            }
        newThemeListAdapter.addItems(themeV2)
        Timber.tag("ImageSlideShowActivity").d("ThemeV2 ==> $themeV2")
        newThemeListAdapter.isRewardLoaded = true
        newThemeListAdapter.notifyDataSetChanged()
        newThemeListAdapter.onItemClickCallback = { linkData ->
            checkProSubscriptionRequired()
            val themFilePath = FileHelper.themeFolderUnzip + "/${linkData.id}"
            TrackingFactory.SlideshowEditor.useTheme(linkData.id).track()
            if (linkData.link == "none") {
                val themeData = ThemeData()
                performChangeTheme(themeData)
            } else {
                if (File(themFilePath).exists()) {
                    val themeData = ThemeData(
                        themeVideoFilePath = themFilePath,
                        themeType = ThemeData.ThemType.NOT_REPEAT,
                        themeName = linkData.fileName,
                        isPro = linkData.isPro,
                        id = linkData.id,
                        lottieFileName = linkData.lottieFileName,
                        lottieLink = linkData.lottieLink,
                    )
                    performChangeTheme(themeData)
                } else {
                    // TODO Download theme
                    if (Utils.isInternetAvailable()) {
                        doPauseVideo()
                        /*if (RemoteConfigRepository.isVersionAdsEnable)
                            showWatchAdsDialog(R.string.watch_ads_to_download_effect) {
                                showProgressDialog(getString(R.string.dialog_download_effect))
                                onDownloadTheme(
                                    link = linkData.link,
                                    fileName = linkData.fileName,
                                    idTheme = linkData.id,
                                    lottieFileLink = linkData.lottieLink,
                                    lottieFileName = linkData.lottieFileName,
                                    {
                                        hideProgressDialog()
                                        mNewThemeListAdapter.notifyDataSetChanged()
                                        val themeData = ThemeData(
                                            themeVideoFilePath = themFilePath,
                                            themeType = ThemeData.ThemType.NOT_REPEAT,
                                            themeName = linkData.fileName,
                                            isPro = linkData.isPro,
                                            id = linkData.id,
                                            lottieFileName = linkData.lottieFileName,
                                            lottieLink = linkData.lottieLink,
                                        )
                                        performChangeTheme(themeData)
                                    }
                                )
                            }
                        else
                            onDownloadTheme(
                                link = linkData.link,
                                fileName = linkData.fileName,
                                idTheme = linkData.id,
                                lottieFileLink = linkData.lottieLink,
                                lottieFileName = linkData.lottieFileName,
                                {
                                    hideProgressDialog()
                                    mNewThemeListAdapter.notifyDataSetChanged()
                                    val themeData = ThemeData(
                                        themeVideoFilePath = themFilePath,
                                        themeType = ThemeData.ThemType.NOT_REPEAT,
                                        themeName = linkData.fileName,
                                        isPro = linkData.isPro,
                                        id = linkData.id,
                                        lottieFileName = linkData.lottieFileName,
                                        lottieLink = linkData.lottieLink,
                                    )
                                    performChangeTheme(themeData)
                                }
                            )*/
                        showProgressDialog(getString(R.string.popup_download_effect))
                        onDownloadTheme(
                            link = linkData.link,
                            fileName = linkData.fileName,
                            idTheme = linkData.id,
                            lottieFileLink = linkData.lottieLink,
                            lottieFileName = linkData.lottieFileName,
                            {
                                hideProgressDialog()
                                newThemeListAdapter.notifyDataSetChanged()
                                val themeData = ThemeData(
                                    themeVideoFilePath = themFilePath,
                                    themeType = ThemeData.ThemType.NOT_REPEAT,
                                    themeName = linkData.fileName,
                                    isPro = linkData.isPro,
                                    id = linkData.id,
                                    lottieFileName = linkData.lottieFileName,
                                    lottieLink = linkData.lottieLink,
                                )
                                performChangeTheme(themeData)
                            }
                        )

                    } else {
                        showToast(getString(R.string.can_not_connect_to_internet))
                    }
                }
            }

        }
        // mThemeListAdapter.highlightItem(mThemeData.themeVideoFilePath)

        viewTool.icAddPhotoInChangeTheme.setOnClickListener {
            doAddMoreImage()
        }

    }


    private fun showLayoutChangeTransition() {
        val viewTool = View.inflate(this, R.layout.layout_view_change_transition_tools, null)
        showToolsActionLayout(viewTool)

        viewTool.imageOfSlideShowListViewInChangeTransition.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        viewTool.imageOfSlideShowListViewInChangeTransition.adapter = slideImageListAdapter
        viewTool.gsTransitionListView.adapter = transitionListAdapter
        viewTool.gsTransitionListView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        transitionListAdapter.highlightItem(gsTransition)

        viewTool.icAddPhotoInChangeTransition.setOnClickListener {
            doAddMoreImage()
        }
    }

    private var addMoreAvailable = true
    private fun doAddMoreImage() {
        if (addMoreAvailable) {
            addMoreAvailable = false
            val intent = ImagePickerActivity.newIntent(this, MediaType.PHOTO, true, FromScreen.SLIDESHOW)
            startActivityForResult(intent, ImagePickerActivity.ADD_MORE_PHOTO_REQUEST_CODE)
            object : CountDownTimer(1000, 1000) {
                override fun onFinish() {
                    addMoreAvailable = true
                }

                override fun onTick(millisUntilFinished: Long) {

                }

            }.start()
        }

    }

    private fun showLayoutChangeDuration() {
        val viewTool = View.inflate(this, R.layout.layout_view_change_duration_tools, null)
        showToolsActionLayout(viewTool)
        val totalTimeInMillis =
            (imageSlideDataContainer.getCurrentDelayTimeMs() + imageSlideDataContainer.transitionTimeInMillis)
        viewTool.changeDurationSeekBar.setCurrentDuration(totalTimeInMillis / 1000)
        viewTool.totalDurationLabel.text = getString(
            R.string.editor_video_duration_total,
            Utils.convertSecToTimeString(imageSlideDataContainer.getMaxDurationMs() / 1000)
        )
        viewTool.tvSpeed.text = getString(R.string.editor_video_duration_speed, totalTimeInMillis / 1000)

        viewTool.changeDurationSeekBar.setDurationChangeListener({
            doPauseVideo()
            doChangeDelayTime(it)
            needReload = true
            videoControllerView.setCurrentDuration(0)
            viewTool.totalDurationLabel.text =
                getString(
                    R.string.editor_video_duration_total,
                    Utils.convertSecToTimeString(imageSlideDataContainer.getMaxDurationMs() / 1000)
                )
            viewTool.tvSpeed.text = getString(R.string.editor_video_duration_speed, it)

            videoControllerView.setMaxDuration(imageSlideDataContainer.getMaxDurationMs())
        }, {
            doRepeat()


        })
    }

    private fun showLayoutChangeRatio() {
        val viewTool = View.inflate(this, R.layout.layout_view_change_aspect_ratio, null)
        showToolsActionLayout(viewTool)
        viewTool.ratioListView.adapter = ratioListAdapter
        viewTool.ratioListView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun onRatioChanged(item: RatioModel) {
        onPauseVideo()
        Timber.d("onRatioChanged ${item.h} ${item.v}")
        slideGlViewContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
            dimensionRatio = "${item.h}:${item.v}"
        }
        imageSlideDataContainer.ratio = item.h.toFloat() / item.v
        imageSlideDataContainer.initData()
        doRepeat()

        selectedFrameInfo?.let {
            it.ratio = imageSlideDataContainer.ratio
            onFrameSelected(it)
        }
        selectedFilterInfo?.let {
            it.ratio = imageSlideDataContainer.ratio
            onSelectFilter(it)
        }
    }

    /**
     * Change Frame Layout
     */
    private fun showLayoutChangeFrame() {
        val viewTool = View.inflate(this, R.layout.layout_view_change_frame_tools, null)
        showToolsActionLayout(viewTool)

        viewTool.imageOfSlideShowListViewInChangeFrame.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        viewTool.imageOfSlideShowListViewInChangeFrame.adapter = slideImageListAdapter

        viewTool.frameListView.adapter = newFrameListAdapter
        viewTool.frameListView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        newFrameListAdapter.itemArray.clear()
        newFrameListAdapter.notifyDataSetChanged()
        newFrameListAdapter.addItem(
            FrameLinkInfo(
                link = "none",
                fileName = "None",
                thumb = "None",
                isPro = false,
                id = "none",
                frameFileName = "",
            )
        )
        val frameList = RemoteConfigRepository
            .frameConfig
            .map {
                it.toFrameLinkData().apply {
                    ratio = imageSlideDataContainer.ratio
                }
            }
        newFrameListAdapter.addItems(frameList)
        newFrameListAdapter.isRewardLoaded = true
        newFrameListAdapter.notifyDataSetChanged()
        newFrameListAdapter.onItemClickCallback = { frameData ->
            selectedFrameInfo = frameData
            checkProSubscriptionRequired()
            //            doPauseVideo()

            //            TrackingFactory.SlideshowEditor.useTheme(frameData.fileName).track()
            onFrameSelected(frameData)
        }

        viewTool.icAddPhotoInChangeFrame.setOnClickListener {
            doAddMoreImage()
        }

    }

    private fun onFrameSelected(frameData: FrameLinkInfo) {
        when {
            frameData.isNone() -> {
                performChangeFrame(frameData)
            }

            File(FileHelper.frameFolderPath).resolve(frameData.getRatioFileName()).exists() -> {
                performChangeFrame(frameData)
            }

            else -> {
                if (Utils.isInternetAvailable()) {
                    doPauseVideo()
                    /*if (RemoteConfigRepository.isVersionAdsEnable) {
                        showWatchAdsDialog(R.string.watch_ads_to_download_frame) {
                            showProgressDialog(getString(R.string.download_frame))
                            onDownloadFrame(
                                frameLinkData = frameData,
                                onComplete = {
                                    hideProgressDialog()
                                    performChangeFrame(frameData)
                                }
                            )
                        }
                    } else {
                        showProgressDialog(getString(R.string.download_frame))
                        onDownloadFrame(
                            frameLinkData = frameData,
                            onComplete = {
                                hideProgressDialog()
                                performChangeFrame(frameData)
                            }
                        )
                    }*/
                    showProgressDialog(getString(R.string.download_frame))
                    onDownloadFrame(
                        frameLinkData = frameData,
                        onComplete = {
                            hideProgressDialog()
                            performChangeFrame(frameData)
                        }
                    )

                } else {
                    showToast(getString(R.string.can_not_connect_to_internet))
                }
            }
        }
    }


    private fun showLayoutChangeFilter() {
        val viewTool = View.inflate(this, R.layout.layout_view_change_filter_tools, null)
        showToolsActionLayout(viewTool)
        imageFilterListAdapter.itemArray.clear()
        imageFilterListAdapter.addItem(
            FilterLinkInfo(
                name = "none",
                link = "none",
                isPro = false,
                id = "none",
                thumb = "none",
                filterFileName = "none"
            )
        )
        val filterArray = RemoteConfigRepository
            .filterConfig
            .map {
                it.toFilterLinkData().apply {
                    ratio = imageSlideDataContainer.ratio
                }
            }

        viewTool.lookupListView.adapter = imageFilterListAdapter
        viewTool.lookupListView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        imageFilterListAdapter.addItems(filterArray)

        viewTool.imageListView.adapter = imageWithLookupListAdapter.apply {
            setItemList(imageSlideDataContainer.getSlideList())
        }
        viewTool.imageListView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

    }

    private fun doChangeDelayTime(time: Int) {
        imageSlideDataContainer.delayTimeInMillis =
            time * 1000 - imageSlideDataContainer.transitionTimeInMillis
    }

    private fun performChangeTheme(themeData: ThemeData) {
        doPauseVideo()
        newThemeListAdapter.changeCurrentThemeName(themeData.themeName)
        showEffectSelection(themeData)
        object : CountDownTimer(100, 100) {
            override fun onFinish() {
                doPlayVideo()
            }

            override fun onTick(millisUntilFinished: Long) {

            }

        }.start()

    }

    private fun performChangeFrame(frameLinkData: FrameLinkInfo) {
        TrackingFactory.SlideshowEditor.useFrame(frameLinkData.id).track()
        frameData = frameLinkData
        newFrameListAdapter.changeCurrentFrameName(frameLinkData.fileName)
        Glide.with(this)
            .load(File(FileHelper.frameFolderPath).resolve(frameLinkData.getRatioFileName()))
            .into(slideNewFrameContainer)
        doPlayVideo()
    }

    private fun onSelectFilter(filter: FilterLinkInfo) {
        TrackingFactory.SlideshowEditor.useFilter(filter.id).track()

        selectedFilterInfo = filter
        checkProSubscriptionRequired()

        when {
            filter.isNone() -> {
                performChangeFilter(filter)
            }

            filter.localFile().exists() -> {
                performChangeFilter(filter)
            }

            else -> {
                if (Utils.isInternetAvailable()) {
                    doPauseVideo()
                    /*if (RemoteConfigRepository.isVersionAdsEnable) {
                        showWatchAdsDialog(R.string.watch_ads_to_download_filter) {
                            showProgressDialog(getString(R.string.download_filter))
                            onDownloadFilter(
                                filterLinkData = filter,
                                onComplete = {
                                    hideProgressDialog()
                                    mImageFilterListAdapter.notifyDataSetChanged()
                                    performChangeFilter(filter)
                                }
                            )
                        }
                    } else {
                        showProgressDialog(getString(R.string.download_filter))
                        onDownloadFilter(
                            filterLinkData = filter,
                            onComplete = {
                                hideProgressDialog()
                                mImageFilterListAdapter.notifyDataSetChanged()
                                performChangeFilter(filter)
                            }
                        )
                    }*/

                    showProgressDialog(getString(R.string.download_filter))
                    onDownloadFilter(
                        filterLinkInfo = filter,
                        onComplete = {
                            hideProgressDialog()
                            imageFilterListAdapter.notifyDataSetChanged()
                            performChangeFilter(filter)
                        }
                    )

                } else {
                    showToast(getString(R.string.can_not_connect_to_internet))
                }
            }
        }
    }

    private fun performChangeFilter(filterLinkInfo: FilterLinkInfo) {
        filterData = filterLinkInfo
        imageFilterListAdapter.changeCurrentFilter(filterLinkInfo.id)
        Glide.with(this)
            .load(filterLinkInfo.localFile())
            .into(slideNewFilterContainer)
        doPlayVideo()
    }

    // Slide image n -> image n + 1 with gsTransition
    private fun performChangeTransition(gsTransition: GSTransition) {
        imageGLView.changeTransition(gsTransition)
    }

    private fun doPauseVideo() {
        if (!isPlay) return
        isPlay = false
        imageSlideRenderer.onPause()
        onPauseVideo()
    }

    private fun doPlayVideo() {
        isPlay = true
        onPlayVideo()
    }

    private fun doSeekTo(
        timeMs: Int,
        showProgress: Boolean = true,
        forceAutoPlay: Boolean? = null
    ) {
        val canAutoPlay = isPlay || forceAutoPlay.orFalse()
        doPauseVideo()

        imageSlideRenderer.setUpdateTexture(true)
        currentTimeInMillis = timeMs
        imageSlideRenderer.seekTheme(currentTimeInMillis)
        if (showProgress)
            showLoadingDialog()
        Thread {
            val frameData = imageSlideDataContainer.seekTo(timeMs)
            currentFrameId = 1L
            imageSlideRenderer.resetData()
            runOnUiThread {
                dismissLoadingDialog()

                imageSlideRenderer.changeFrameData(frameData)

                onSeekTo(timeMs)
                if (canAutoPlay) doPlayVideo()
                else doPauseVideo()


            }
        }.start()

    }

    private fun reloadInTime(timeMs: Int) {
        val canAutoPlay = true
        doPauseVideo()
        Thread {
            val frameData = imageSlideDataContainer.seekTo(timeMs, true)
            currentTimeInMillis = timeMs
            runOnUiThread {
                imageSlideRenderer.changeFrameData(frameData)

                if (canAutoPlay) doPlayVideo()
                else doPauseVideo()
            }
        }.start()
    }

    private fun doSeekById(id: Long) {
        doPauseVideo()
        val timeInMillis = imageSlideDataContainer.getStartTimeById(id)
        doSeekTo(timeInMillis)
        onStick()
    }

    private fun doRepeat() {
        doPauseVideo()
        imageSlideDataContainer.onRepeat()
        imageSlideRenderer.resetData()
        doSeekTo(0, forceAutoPlay = true)
        currentTimeInMillis = 0
        Logger.e("doRepeat")
        onRepeat()
        doPlayVideo()
    }

    override fun onPause() {
        super.onPause()
        effectUpdateJob?.cancel()
        imageGLView.onPause()
        //        effectPreviewView.onPause()
        doPauseVideo()
    }

    override fun onResume() {
        super.onResume()
        imageGLView.onResume()

        Thread {
            images.forEach {
                if (!File(it).exists()) {
                    runOnUiThread {
                        finish()
                    }

                }
            }
        }.start()

        effectUpdateJob = isPlayingStateFlow
            .onEach {
                if (it) {
                    effectLottiePreviewView.playAnimation()
                } else {
                    effectLottiePreviewView.pauseAnimation()
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun onDestroy() {
        super.onDestroy()
        imageSlideRenderer.onDestroy()
        musicPlayer.release()
    }

    private fun doExportVideo() {
        doPauseVideo()
        if (SharedPreferUtils.proUser) {
            performExport(ExportQualityVideoSelectionFragment.QUALITY_1080P, 0)
            return
        }
        showExportDialog(false) { quality, ratio ->
            if (quality < 1) {
                showToast(getString(R.string.please_choose_video_quality_label))
            } else {
                if (quality == ExportQualityVideoSelectionFragment.QUALITY_1080P) {
                    if (!SharedPreferUtils.proUser)
                        proPlanLauncher.launch(
                            PremiumActivity.newIntent(
                                this,
                                ARG_SOURCE_EXPORT_KEY
                            )
                        )
                    else performExport(quality, ratio)
                } else if (quality == ExportQualityVideoSelectionFragment.QUALITY_720P) {
                    val highQuality =
                        RemoteConfigRepository.videoQualityConfigs?.firstOrNull { it.quality == "720" }
                    if (highQuality == null || highQuality.typeAds == "inter") {
                        performExport(quality, ratio)
                    } else {
                        performExport(quality, ratio)
                    }
                } else {
                    val normalQuality =
                        RemoteConfigRepository.videoQualityConfigs?.firstOrNull { it.quality == "480" }
                    if (normalQuality?.typeAds == "video") {
                        performExport(quality, ratio)
                    } else {
                        performExport(quality, ratio)
                    }
                }
            }
        }
    }

    private fun performExport(quality: Int, ratio: Int) {
        TrackingFactory.SlideshowEditor.clickSave(
            quality,
            gsTransition.id,
            themeData1.themeName,
            filterData.id,
            frameData.id
        ).track()
        dismissExportDialog()
        doPauseVideo()
        AdsHelper.forceShowInterExport(this) {
            prepareForExport(quality)
        }
    }

    private fun prepareForExport(quality: Int) {
//        showLoadingDialog()
        Thread {
            val stickerAddedForRenderInfo = ArrayList<StickerForRenderInfo>()
            for (item in getStickerAddedList()) {
                val bitmap = Bitmap.createBitmap(
                    stickerContainer.width,
                    stickerContainer.height,
                    Bitmap.Config.ARGB_8888
                )
                val view = findViewById<View>(item.stickerViewId)

                /*    val inEdit = if (view is StickerView) {
                        view.inEdit
                    } else false
                    if (view is StickerView) runOnUiThread {
                        view.setInEdit(false)
                    }
                    view.draw(Canvas(bitmap))
                    if (view is StickerView) runOnUiThread {
                        view.setInEdit(inEdit)
                    }*/
                if (view is StickerView) view.getOutBitmap(Canvas(bitmap))


                val outputPath = FileHelper.saveStickerToTemp(bitmap)
                stickerAddedForRenderInfo.add(
                    StickerForRenderInfo(
                        outputPath,
                        item.startTimeInMilSec,
                        item.endTimeInMilSec
                    )
                )
            }

            for (item in getTextAddedList()) {
                val bitmap = Bitmap.createBitmap(
                    stickerContainer.width,
                    stickerContainer.height,
                    Bitmap.Config.ARGB_8888
                )
                val view = findViewById<View>(item.viewId)
                /*          val inEdit = if (view is EditTextSticker) {
                              view.inEdit
                          } else false
                          if (view is EditTextSticker)
                              runOnUiThread {
                                  view.setInEdit(false)
                              }



                          view.draw(Canvas(bitmap))
                          if (view is EditTextSticker)
                              runOnUiThread {
                                  view.setInEdit(inEdit)
                              }*/
                if (view is EditTextSticker) view.getOutBitmap(Canvas(bitmap))
                val outputPath = FileHelper.saveStickerToTemp(bitmap)
                stickerAddedForRenderInfo.add(
                    StickerForRenderInfo(
                        outputPath,
                        item.startTimeInMilSec,
                        item.endTimeInMilSec
                    )
                )
            }

            val imageSlideDataArr = imageSlideDataContainer.getSlideList()
            val delayTime = imageSlideDataContainer.delayTimeInMillis
            val musicDataPath = getMusicData()
            val musicDataVolume = getMusicVolume()
            val themeData = themeData1
            val frameData = frameData
            val filter = filterData
            val intent = Intent(this, ProcessVideoActivity::class.java)
            Bundle().apply {
                putSerializable("stickerDataList", stickerAddedForRenderInfo)
                putSerializable("imageDataList", imageSlideDataArr)
                putInt("delayTime", delayTime)
                putString("musicPath", musicDataPath)
                putFloat("musicVolume", musicDataVolume)
                putFloat("ratio", imageSlideDataContainer.ratio)
                putString("themeData", themeData.id)
                putInt("themeScaleType", effectLottiePreviewView.scaleType.ordinal)
                putString("frameData", frameData.getRatioFileName())
                putString("filterData", filter.getRatioFileName())
                putInt("videoQuality", quality)
                putSerializable("gsTransition", ArrayList(gsTransitions.map { it.id }))
                intent.putExtra("bundle", this)
                intent.putExtra(ProcessVideoActivity.action, ProcessVideoActivity.renderSlideActionCode)
            }
            AdsHelper.requestNativeSaving(this)
            runOnUiThread {
                dismissLoadingDialog()
                startActivity(intent)
            }
        }.start()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Logger.e("request code = $requestCode")
        if (requestCode == ImagePickerActivity.ADD_MORE_PHOTO_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.let {
                    val pathArray = it.getStringArrayListExtra(imagePickedListKey)
                    val preImageList = imageSlideDataContainer.imageList
                    val newImageList = ArrayList(preImageList.plus(pathArray.orEmpty()))
                    Timber.d("Image list updated, before ${preImageList.size}, after :${newImageList.size}")
                    randomGsTransions1 =
                        randomGsTransions1.plus(getRandomTransitions(newImageList.size - preImageList.size))
                    randomGsTransions2 = randomGsTransions2.plus(
                        getRandomTransitions(
                            RemoteConfigRepository.randomTransitionConfig.random2?.ids.orEmpty(),
                            newImageList.size - preImageList.size
                        )
                    )
                    if (gsTransition.id == "random_1") {
                        gsTransitions = randomGsTransions1
                    } else if (gsTransition.id == "random_2") {
                        gsTransitions = randomGsTransions2
                    }

                    pathArray?.let { paths ->
                        Logger.e("size result= ${paths.size}")
                        showLoadingDialog()
                        Thread {
                            imageSlideDataContainer.setNewImageList(newImageList)
                            runOnUiThread {
                                imageSlideRenderer.setUpdateTexture(true)
                                setMaxTime(imageSlideDataContainer.getMaxDurationMs())
                                doRepeat()
                                // doPlayVideo()
                                slideImageListAdapter.addImagePathList(newImageList)
                                dismissLoadingDialog()
                            }

                        }.start()


                    }
                }
            }
        }
        if (requestCode == SELECT_MUSIC_REQUEST_CODE) {
            doPlayVideo()
        }
    }

    private fun showWatchAdsDialog(messageResId: Int, onComplete: () -> Unit) {
        SlideshowMakerDialogFragment.Builder()
            .setMessage(getString(messageResId))
            .setPrimaryButton(object : SlideshowMakerDialogFragment.Builder.Button {
                override val label: String
                    get() = getString(R.string.regular_ok)

                override fun onClickListener(dialog: Dialog?) {
                    dialog?.dismiss()
                    onComplete()
                }

            })
            .setSecondaryButton(object : SlideshowMakerDialogFragment.Builder.Button {
                override val label: String
                    get() = getString(R.string.regular_cancel)

                override fun onClickListener(dialog: Dialog?) {
                    doPlayVideo()
                    dialog?.dismiss()
                }

            }).build()
            .show(supportFragmentManager, null)
    }

}


fun FrameDataSetting.toFrameLinkData(): FrameLinkInfo = FrameLinkInfo(
    link = frameLink,
    packLink = framePackLink,
    ratioExtension = ratioExtension,
    fileName = name.orEmpty(),
    thumb = thumbnailUrl,
    isPro = isPro.orFalse(),
    id = id.orEmpty(),
    frameFileName = frameFileName
)

fun FilterDataSetting.toFilterLinkData(): FilterLinkInfo = FilterLinkInfo(
    link = filterLink,
    name = name.orEmpty(),
    thumb = thumbnailFilterUrl,
    isPro = isPro.orFalse(),
    id = id.orEmpty(),
    filterFileName = filterFileName,
    packLink = framePackLink,
    ratioExtension = ratioExtension
)

fun FilterLinkInfo.localFile(): File = File(FileHelper.filterFolderPath).resolve(getRatioFileName())