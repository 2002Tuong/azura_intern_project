package com.parallax.hdvideo.wallpapers.ui.details

//import com.tp.inappbilling.ui.PurchaseVipDialog
//import com.tp.inappbilling.utils.UserType
import android.animation.*
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.size
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdView
import com.livewall.girl.wallpapers.extension.checkPermissionStorage
import com.livewall.girl.wallpapers.extension.requestPermissionStorage
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.ads.AdsManager
import com.parallax.hdvideo.wallpapers.ads.AdWrapper
import com.parallax.hdvideo.wallpapers.data.model.HashTag
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.databinding.FragmentDetailScreenBinding
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.*
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.services.log.*
import com.parallax.hdvideo.wallpapers.services.wallpaper.LiveWallpaper
import com.parallax.hdvideo.wallpapers.services.wallpaper.ParallaxService
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragmentBinding
import com.parallax.hdvideo.wallpapers.ui.base.viewmodel.BaseViewModel
import com.parallax.hdvideo.wallpapers.ui.dialog.*
import com.parallax.hdvideo.wallpapers.ui.home.HomeFragment
import com.parallax.hdvideo.wallpapers.ui.list.ListWallpaperFragment
import com.parallax.hdvideo.wallpapers.ui.list.AppScreen
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.ui.main.fragment.MainFragment
import com.parallax.hdvideo.wallpapers.ui.search.SearchFragment
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.dpToPx
import com.parallax.hdvideo.wallpapers.utils.file.FileUtils
import com.parallax.hdvideo.wallpapers.utils.image.CropBitmapTransformation
import com.parallax.hdvideo.wallpapers.utils.other.GlideSupport
import com.parallax.hdvideo.wallpapers.utils.rx.RxBus
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class DetailFragment : BaseFragmentBinding<FragmentDetailScreenBinding, DetailViewModel>() {

    override val resLayoutId: Int = R.layout.fragment_detail_screen

    @Inject
    lateinit var localStorage: LocalStorage
    private var screenType: AppScreen = AppScreen.HOME
    private var listWallpaper = listOf<WallpaperModel>()
    private var currentPos: Int = 0
    private var curTrackingScreen: TrackingScreen? = null
    private var oldPos: Int = 0
    private var coordinateSharedView: Rect? = null
    private var cateId: String? = null
    private var hashTag: HashTag? = null
    private var statusViewJob: Job? = null
    private var adView: AdView? = null
    private var isAnimation = false
    private val offsetscreenPageLimit = 2
    private lateinit var requestManager: RequestManager
    private var isFromHistoryScreen: Boolean = false
    private var onShowingGroupViews = false
    private var hasLoadMoreDataFirst = false
    private var screenName = 0L
    private var disposableLoadMoreData: Disposable? = null
    private val mainActivity get() = activity as? MainActivity
    private var isShowingFullHashTag = false
    private var curTimeOnPageSelected: Long = System.currentTimeMillis()
    var wallpaperModel: WallpaperModel? = null
    var arrayHashTagWallpaper = ArrayList<HashTag>()
    private var scrolled = false

    private val searchMoreViewModel: BaseViewModel? by lazy {
        when (screenType) {
            AppScreen.CATEGORY -> {
                findFragment(
                    ListWallpaperFragment::class,
                    tag = AppScreen.CATEGORY.name
                )?.viewModel
            }

            AppScreen.SEARCH -> {
                (mainActivity?.currentFragment as? SearchFragment)?.getViewModel()
            }

            else -> null
        }
    }
    private var shareOrDownloadCode = 0
    private var showAdWhenSwipingTag = "swipe"
    private var nextAdPos = 0
    private var previousAdPos = 0
    private var downButtonAnimator: Animator? = null
    /*private var bannerAdView: AdView? by Delegates.observable(null) { _, _, newValue ->
        if (newValue != null) dataBinding.downloadButton.margin(bottom = 16f)
        else dataBinding.downloadButton.margin(bottom = 16f)
    }*/
    //region lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            screenType = (it.getSerializable(SCREEN_TAG) as? AppScreen) ?: AppScreen.HOME
            screenName = it.getLong(SCREEN_NAME, 0)
            showAdWhenSwipingTag = "swipe$screenName"
            currentPos = it.getInt(POSITION_TAG)
            curTrackingScreen = it.getSerializable(TRACK_SCREEN_TAG) as? TrackingScreen
            coordinateSharedView = it.getParcelable(RECT_TAG)
            cateId = it.getString(CATEGORY)
            hashTag = (it.getSerializable(HASHTAG) as HashTag?)
        }

        RxBus.subscribe(RxBus.DISMISS_FRAGMENT_DETAIL + screenName, AdWrapper::class) {
            val tag = it.tag.toLongOrNull() ?: return@subscribe
            //Type = 1: on ad closed
            if (it.type == 1) {
                if (tag == screenName) {
                    downloadAfterRewardedAd(it.state)
                }
            } else {
                popFragmentAfterAd(tag)
            }
        }

        AdsManager.showInterstitialAd(tag = screenName.toString())
    }

    override fun init(view: View) {
        super.init(view)
        isFromHistoryScreen = screenType == AppScreen.DOWNLOAD || screenType == AppScreen.FAVORITE
                || screenType == AppScreen.LOCAL
        dataBinding.containerTop.layoutParams.height = dpToPx(60f) + AppConfiguration.statusBarSize
        setupGlide()
        mListData[screenName]?.let {
            listWallpaper = it
            if (screenType == AppScreen.COLLECTION) {
                viewModel.listWallFromCategories.clear()
                viewModel.listWallFromCategories.addAll(it.toMutableList())
            }
        }
        if (listWallpaper.isEmpty()) {
            TrackingSupport.recordEvent(
                EventData.EmptyDataEventInDetail,
                "screenType" to screenType.name
            )
        } else {
            setupAnimationStartup()
            setupViewPager()
        }
        setVisibilityGroupViews(false)
        viewModel.screenName = screenName.toString()
        viewModel.screenType = screenType
        setupObserver()
        setupButton()
        view.doOnPreDraw {
            viewDidLoad()
        }
        buildSettingButtonAnimation()
    }

    private fun viewDidLoad() {
        if (dataBinding.viewPager.height > 0) {
            viewModel.detailAdapter.itemWidthValue = dataBinding.viewPager.width
            viewModel.detailAdapter.itemHeightValue = dataBinding.viewPager.height
        }
        coordinateSharedView?.let {
            viewModel.detailAdapter.setThumbnailSize(it.width(), it.height())
        }
        viewModel.detailAdapter.keepPos = -1
        if (listWallpaper.isNotEmpty()) {
            dataBinding.viewPager.adapter = viewModel.detailAdapter
            animationStartup()
            dataBinding.viewPager.setCurrentItem(currentPos, false)
            showLessHashTag(viewModel.detailAdapter.getDataOrNull(currentPos))

            dataBinding.viewPager.runWhenReady {
                val model = viewModel.detailAdapter.getData(currentPos)
                handleShowIntro(model)
            }
        }
        dataBinding.flexboxHashTag.layoutParams.width =
            AppConfiguration.widthScreenValue - dataBinding.actionsLayout.width
        TrackingSupport.recordScreenView(activity, "DetailFragment")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == DOWNLOAD_CODE) {
            if (grantResults.permissionGranted) {
                showRewardedAd(DOWNLOAD_CODE)
            } else {
                ConfirmDialogFragment.show(
                    childFragmentManager,
                    callbackYes = {
                        requestPermissionStorage(requestCode)
                    },
                    message = getString(R.string.msg_requested_permission_denied_title) + "\n\n" + getString(
                        R.string.msg_requested_permission_denied_content
                    )
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            CongratulationDialog.show(childFragmentManager)
            previewWallpaperModel?.second?.apply {

            }
            previewWallpaperModel?.also { model ->
                viewModel.saveWallpaperVideo(model.second)
            }
            previewWallpaperModel = null
        }
    }

    private fun setupGlide() {
        requestManager = Glide.with(this)
    }

    private fun popFragmentAfterAd(tag: Long) {
        if (tag != screenName) return
        super.onBackPressed()
    }

    //endregion

    //region Action

    private fun setupObserver() {
        viewModel.getPhotoLiveData().observe(viewLifecycleOwner) {
            val model = it.second
            previewWallpaperModel = null
            if (model != null) {
                updateStatusView()
                if (abs(it.first) == SHARE_CODE) {
                    if (!IntentSupporter.shareWallpaper(activity, model)) {
                        showToast("Error setting sharing service")
                    }
                } else {
                    notificationHasDownloadedPhoto(it.first, model)
                }
                if (it.first > 0) {
                    downloadWallCount += 1
                    if (downloadWallCount > RemoteConfig.commonData.rewardedAdCount) downloadWallCount =
                        0
                }
            } else {
                showToast(R.string.download_wallpaper_fail)
                openNetworkSettings()
            }
        }

        viewModel.getVideoLiveData().observe(viewLifecycleOwner) {
            val model = it.second
            previewWallpaperModel = null
            val code = it.first
            if (model == null) {
                showToast(R.string.download_wallpaper_fail)
                openNetworkSettings()
            } else {
                updateStatusView()
                if (abs(it.first) == SHARE_CODE) {
                    if (!IntentSupporter.shareWallpaper(activity, model)) {
                        showToast("Error setting sharing service")
                    }
                } else {
                    previewWallpaperModel = code to model
                    if (abs(code) == DOWNLOAD_CODE) {
                        CongratulationDialog.show(childFragmentManager, isForDownload = true) {
                            startPreviewVideo(code)
                        }
                    } else {
                        startPreviewVideo(code)
                    }
                }
            }
        }

        viewModel._loadMoreDataState.observe(viewLifecycleOwner) {
            if (!it && !hasLoadMoreDataFirst) {
                hasLoadMoreDataFirst = true
                isFromHistoryScreen = true
                refreshIfDataEmpty()
            }
            AdsManager.reloadAd(500, isForced = true)
        }
        searchMoreViewModel?.also { vm ->
            viewModel.compositeDisposable.add(vm.loadMoreLiveData.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    viewModel.detailAdapter.addData(it)
                })
        }
        viewModel.setWallLiveEvent.observe(viewLifecycleOwner) {
            playOrPause(false)
            if (it) CongratulationDialog.show(childFragmentManager)
            else showToast(R.string.setting_wallpaper_error)
        }
        viewModel.getParallaxLiveData().observe(viewLifecycleOwner) {
            if (it == null) {
                showToast(R.string.download_wallpaper_fail)
                openNetworkSettings()
            } else {
                updateStatusView()
            }

            showPreviewParallax(it)
        }
    }

    override fun refreshDataIfNeeded() {
        if (isInitialized) {
            val pos = viewModel.detailAdapter.getDataOrNull(currentPos)
            viewModel.detailAdapter.notifyDataSetChanged()
            dataBinding.viewPager.setCurrentItem(currentPos, false)
        }
    }

    private fun setupButton() {
        dataBinding.backButton.setOnClickListener { onBackPressed() }
        dataBinding.favoriteButton.setOnClickListener {
            viewModel.favoriteWallpaper(dataBinding.viewPager.currentItem) {
                if (it != null) {
                    RxBus.push(it, 0)
                    updateFavoriteView(it, true)
                } else {
                    showToast("That bai")
                }
            }
        }
        setOnClickListener(dataBinding.downloadButton) {
            if (viewModel.detailAdapter.doingLoad4DImage) return@setOnClickListener
            val model =
                viewModel.detailAdapter.getDataOrNull(currentPos) ?: return@setOnClickListener
            addTrackingClickDownload(model)
            if (model.isFromLocalStorage || model.hasDownloaded) {
                when (model.getWallpaperModelType()) {
                    WallpaperModel.WallpaperModelType.VIDEO -> {
                        if (!RemoteConfig.commonData.isSupportedVideo) {
                            showToast(R.string.warning_android_12_message_first)
                        } else {
                            if (model.isFromLocalStorage) {
                                previewWallpaperModel = CODE_PREVIEW_VIDEO to model
                                startPreviewVideo(CODE_PREVIEW_VIDEO)
                            } else viewModel.downloadData(SET_WALL_CODE, currentPos)
                        }
                    }

                    WallpaperModel.WallpaperModelType.IMAGE -> {
                        SetWallpaperDialog.show(
                            requireActivity().supportFragmentManager,
                            callback = {
                                viewModel.setWallpaper(
                                    model,
                                    WallpaperHelper.WallpaperType.init(it)
                                )
                            })
                    }

                    WallpaperModel.WallpaperModelType.PARALLAX -> showPreviewParallax(model)
                }
            } else {
                if (model.isVideo && model.hasVideoFile && !RemoteConfig.commonData.isSupportedVideo) {
                    WarningSetWallDialog.show(childFragmentManager) {
                        viewModel.detailAdapter.getDataOrNull(currentPos)?.let { downloadData(it) }
                    }
                } else if (model.isVideo && !model.hasVideoFile) {
                    showToast(R.string.msg_loading_video)
                } else {
                    when (model.getWallpaperModelType()) {
                        WallpaperModel.WallpaperModelType.PARALLAX -> {
                            if (File(
                                    FileUtils.folderParallax.path + "/" + viewModel.getNameFolder(
                                        model
                                    )
                                ).exists()
                            )
                                showPreviewParallax(model)
                            else
                                downloadData(model)
                        }

                        else -> downloadData(model)
                    }
                }
            }
        }
        dataBinding.shareButton.setOnClickListenerDebounce(1000L) {
            TrackingSupport.recordEvent(EventOther.ShareWallpaper)
            val model = viewModel.detailAdapter.getDataOrNull(currentPos)
                ?: return@setOnClickListenerDebounce
            if (model.isFromLocalStorage) {
                if (!IntentSupporter.shareWallpaper(activity, model)) {
                    showToast("Error setting sharing service")
                }
            } else {
                showRewardedAd(SHARE_CODE)
            }
        }

        //preview options

        dataBinding.ivOptionPreview.setOnClickListener {
            changePreview()
        }
    }

    private fun changePreview() {
        when (viewModel.detailAdapter.currentScreenState) {
            PopupWindowAdapter.ScreenState.PREVIEW_LOCK -> {
                viewModel.detailAdapter.setPreview(PopupWindowAdapter.ScreenState.PREVIEW_HOME)
                dataBinding.ivOptionPreview.setImageResource(R.drawable.ic_lock_screen_white_re)
            }

            PopupWindowAdapter.ScreenState.PREVIEW_HOME -> {
                viewModel.detailAdapter.setPreview(PopupWindowAdapter.ScreenState.PREVIEW_LOCK)
                dataBinding.ivOptionPreview.setImageResource(R.drawable.ic_home_screen_white_re)
            }

            PopupWindowAdapter.ScreenState.PREVIEW_IMAGE -> {
                viewModel.detailAdapter.setPreview(PopupWindowAdapter.ScreenState.PREVIEW_LOCK)
                dataBinding.ivOptionPreview.setImageResource(R.drawable.ic_home_screen_white_re)
            }
        }
        //  isShowingGroupViews = false
        setStatusBar(false)
        // setVisibilityGroupViews(isShowingGroupViews)
    }

    private fun addTrackingClickDownload(model: WallpaperModel) {
        TrackingSupport.recordEventOnlyFirebase(EventDownload.ClickedDownloadAll)
        if (model.isVideo) {
            TrackingSupport.recordEventOnlyFirebase(EventDownload.ClickedDownloadVideo)
        } else {
            TrackingSupport.recordEventOnlyFirebase(EventDownload.ClickedDownloadImage)
        }
        when (screenType) {
            AppScreen.TRENDING -> {
                TrackingSupport.recordEventOnlyFirebase(EventDownload.DownLoadFromTrending)

            }

            AppScreen.CATEGORY -> {
                TrackingSupport.recordEventOnlyFirebase(EventDownload.DownLoadFromCategory)
            }

            AppScreen.SEARCH -> {
                when (curTrackingScreen) {
                    TrackingScreen.SEARCH_RESULT -> {
                        TrackingSupport.recordEventOnlyFirebase(EventDownload.DownloadFromSearch)
                    }

                    TrackingScreen.HASH_TAG_SEARCH_TREND -> {
                        TrackingSupport.recordEventOnlyFirebase(EventDownload.ClickedDownloadFromHashTagList)
                        TrackingSupport.recordEventOnlyFirebase(EventDownload.DownLoadFromHashTagListFromST)
                    }

                    TrackingScreen.TOP_DOWNLOAD -> {
                        TrackingSupport.recordEventOnlyFirebase(EventDownload.DownLoadFromTopDown)
                    }

                    TrackingScreen.TRENDING -> {
                        TrackingSupport.recordEventOnlyFirebase(EventDownload.DownLoadFromTrending)
                    }

                    else -> {}
                }
            }

            AppScreen.HOME_HASH_TAG -> {
                TrackingSupport.recordEventOnlyFirebase(EventDownload.ClickedDownloadHashTag.nameEvent + "list_from_home")
                TrackingSupport.recordEventOnlyFirebase(EventDownload.ClickedDownloadFromHashTagList)
            }

            AppScreen.HOME -> {
                if (currentPos > 0 && !isFromHistoryScreen) {
                    TrackingSupport.recordEventOnlyFirebase(EventDownload.ClickedDownloadHashTagSimilar)
                } else if (currentPos == 0) {
                    //download from home
                    TrackingSupport.recordEventOnlyFirebase(EventDownload.ClickedDownloadFromHome)
                    TrackingSupport.recordEventOnlyFirebase(EventDownload.ClickedDownloadFromHomeContentType.nameEvent + model.contentType)
                }
            }

            else -> {}
        }
    }

    private fun downloadData(item: WallpaperModel) {
        val currentTime = System.currentTimeMillis()
        val lastTime = dataBinding.downloadButton.tag as? Long ?: 0
        if (currentTime - lastTime < 1000) {
            return
        }
        dataBinding.downloadButton.tag = currentTime

        if (item.is4DImage) {
            showRewardedAd(DOWNLOAD_CODE)
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (requireContext().checkPermissionStorage) {
                showRewardedAd(DOWNLOAD_CODE)
            } else {
                ConfirmDialogFragment.show(childFragmentManager,
                    message = getString(R.string.msg_requested_permission_title) + "\n\n" + getString(
                        R.string.msg_requested_permission_content
                    ),
                    callbackNo = {
                        showToast(R.string.msg_requested_permission_title)
                        TrackingSupport.recordEvent(EventPermission.DeniedPermission)
                    }, callbackYes = {
                        requestPermissionStorage(DOWNLOAD_CODE)
                    })
            }
        } else {
            showRewardedAd(DOWNLOAD_CODE)
        }
    }

    private fun updateDownloadButton(isEnabled: Boolean) {
        if (isEnabled) {
            // dataBinding.iconRewardedAd.setImageResource(R.drawable.ic_rewarded_ad)
            dataBinding.downloadButton.setImageResource(R.drawable.ic_download_re)
            startAnimationDownloadButton()
        } else {
            cancelAnimationDownloadButton()
        }
    }

    private fun buildSettingButtonAnimation() {
        val downAnimator = ObjectAnimator.ofPropertyValuesHolder(
            dataBinding.downloadButton,
            PropertyValuesHolder.ofFloat("scaleX", 1f, 1.1f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f, 1.1f, 1f)
        ).setDuration(300)
        val rippleAnimator = ObjectAnimator.ofPropertyValuesHolder(
            dataBinding.downloadButtonAnimationHelper,
            PropertyValuesHolder.ofFloat("scaleX", 0.9f, 2.3f),
            PropertyValuesHolder.ofFloat("scaleY", 0.9f, 2.3f),
            PropertyValuesHolder.ofFloat("alpha", 0.7f, 0f)
        ).setDuration(600)

        val animatorSet = AnimatorSet()
        animatorSet.startDelay = 1500
        downButtonAnimator = animatorSet
        animatorSet.playTogether(downAnimator, rippleAnimator)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            private var mCanceled = false
            override fun onAnimationStart(animation: Animator) {
                mCanceled = false
            }

            override fun onAnimationEnd(animation: Animator) {
                if (!mCanceled) startAnimationDownloadButton()
            }

            override fun onAnimationCancel(animation: Animator) {
                dataBinding.downloadButtonAnimationHelper.alpha = 0f
                dataBinding.downloadButton.scaleX = 1f
                dataBinding.downloadButton.scaleY = 1f
                mCanceled = true
            }
        })
    }

    private fun startAnimationDownloadButton() {
        downButtonAnimator?.apply {
            if (!isRunning) {
                cancel()
                start()
            }
        }
    }

    private fun cancelAnimationDownloadButton() {
        downButtonAnimator?.cancel()
    }

    private fun updateFavoriteView(model: WallpaperModel?, isShowToast: Boolean) {
        context ?: return
        val item = model ?: WallpaperModel()
        if (item.isFavorite) {
            if (isShowToast) {
                showToast(R.string.add_wallpaper_favorite_successfully)
                val scaleInX =
                    ObjectAnimator.ofFloat(dataBinding.heartImageView, "scaleX", 1f, 1.6f, 1f)
                val scaleInY =
                    ObjectAnimator.ofFloat(dataBinding.heartImageView, "scaleY", 1f, 1.6f, 1f)
                val animatorSet = AnimatorSet()
                animatorSet.interpolator = DecelerateInterpolator()
                animatorSet.duration = 300
                animatorSet.playTogether(scaleInX, scaleInY)
                animatorSet.start()
                dataBinding.lavFavorite.isHidden = false
                dataBinding.lavFavorite.cancelAnimation()
                dataBinding.lavFavorite.playAnimation()
                dataBinding.lavFavorite.addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        dataBinding.lavFavorite.isHidden = true
                    }

                    override fun onAnimationCancel(animation: Animator) {
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                    }

                })

            }
            dataBinding.heartImageView.setImageResource(R.drawable.ic_favorite_re)
        } else {
            if (isShowToast) showToast(R.string.unfavorite_wallpaper_successfully)
            dataBinding.heartImageView.setImageResource(R.drawable.ic_heart_detail_re)
        }
    }

    private fun setupViewPager() {
        val currentFragment = (activity as MainActivity).currentFragment
        if (currentFragment is HomeFragment) {
            val fromFragment = currentFragment.getFragmentTab()
            if (fromFragment is MainFragment) {
                val loadMorePS = fromFragment.viewModel.loadMorePublishSubject
                loadMorePS.also {
                    viewModel.add(it.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { res ->
                            val filteredList =
                                res.filterNot { item -> item.url.isNullOrEmptyOrBlank() }
                            viewModel.detailAdapter.addData(filteredList)
                        })
                }
            }
            if (fromFragment is ListWallpaperFragment) {
                val loadMorePS = fromFragment.viewModel.loadMoreLiveData
                loadMorePS.also {
                    viewModel.add(it.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { res ->
                            val filteredList =
                                res.filterNot { item -> item.url.isNullOrEmptyOrBlank() }
                            viewModel.detailAdapter.addData(filteredList)
                        })
                }
            }
        }

        viewModel.detailAdapter.requestManager = requestManager
        oldPos = currentPos
        val item = listWallpaper.getOrNull(currentPos)

        if (screenType == AppScreen.HOME) {
            val filteredList = mutableListOf<WallpaperModel>()
            listWallpaper.withIndex().forEach { (index, value) ->
                if (value.url != null) {
                    filteredList.add(value)
                    if (index == currentPos) {
                        currentPos = filteredList.size - 1
                    }
                }
            }
            viewModel.detailAdapter.setData(filteredList)
        } else {
            viewModel.detailAdapter.addData(listWallpaper)
        }
        val viewPager = dataBinding.viewPager
        viewPager.offscreenPageLimit = offsetscreenPageLimit
        // viewPager.pageMargin = dpToPx(3f)
        viewModel.detailAdapter.startPos = currentPos
        viewModel.detailAdapter.currentPos = currentPos
        nextAdPos = currentPos + RemoteConfig.commonData.showAdWhenSwiping
        previousAdPos = currentPos - RemoteConfig.commonData.showAdWhenSwiping
        viewModel.detailAdapter.onClickedItemCallback = { _, _ ->
            if (isShowingFullHashTag) {
                showLessHashTag(wallpaperModel)
            } else {
                onShowingGroupViews = !onShowingGroupViews
                setVisibilityGroupViews(onShowingGroupViews)
                setStatusBar(onShowingGroupViews)
                if (onShowingGroupViews) addTrackingShowBar2Seconds()
            }
        }
        viewModel.detailAdapter.onLoad4DImageSuccessListener {
            dataBinding.downloadButton.apply {
                isEnabled = it
                alpha = if (it) 1f else 0.5f
                if (it) {
                    setImageResource(R.drawable.ic_set_wallpaper_re)
                    startAnimationDownloadButton()
                    dataBinding.txtErrorMessage.isHidden = true
                    countDownToHideErrorMessage.cancel()
                } else {
                    cancelAnimationDownloadButton()
                    setImageResource(R.drawable.ic_disable_set_wallpaper_re)

                    dataBinding.txtErrorMessage.isHidden = false
                    countDownToHideErrorMessage.start()
                }
            }
        }
        dataBinding.titleTv.text =
            viewModel.detailAdapter.getDataOrNull(currentPos)?.name ?: getString(
                R.string.name_native_ads
            )
    }

    private val onPageChangeListenerObject = object : ViewPager2.OnPageChangeCallback() {
        var canRefreshStatusAd = false
        private var oldPosition = -1
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            wallpaperModel = viewModel.detailAdapter.getDataOrNull(position)
            if (wallpaperModel != null) {
                dataBinding.titleTv.text = wallpaperModel?.name
                if (position != oldPosition && oldPosition != -1)
                    TrackingSupport.recordSlideWallInDetail(
                        EventDetail.DetailSlideWallpaper,
                        wallpaperModel!!
                    )
                val currentFrag = (activity as MainActivity).currentFragment
                currentFrag?.scrollToItemPosition(position, wallpaperModel!!)
                fromFragment?.scrollToItemPosition(
                    position,
                    wallpaperModel!!
                )
            } else {
                dataBinding.titleTv.text = getString(R.string.name_native_ads)
            }
            showLessHashTag(wallpaperModel)
            viewModel.detailAdapter.currentPos = position
            currentPos = position
            playVideo()
            updateStatusView()
            if (position > oldPosition) {
                // handleLoadMore(position)
            }
            oldPosition = position
            setStatusBar(onShowingGroupViews)
            canRefreshStatusAd = true
            //showInter(position)
            curTimeOnPageSelected = System.currentTimeMillis()

            scrolled = true
            localStorage.isFirstTimeIntroImageDisplayed = true
            countDownTimer.cancel()

            dataBinding.txtErrorMessage.isHidden = true
            countDownToHideErrorMessage.cancel()
            handleShowIntro(wallpaperModel)

            if (wallpaperModel?.is4DImage == true) {
                viewModel.detailAdapter.visibleParallaxView()
            } else {
                viewModel.detailAdapter.remove4DBehindAndBeforeCurrentItem()
            }
            Log.d("ViewPager2", "bindView parallax")
        }

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            if (state == ViewPager2.SCROLL_STATE_DRAGGING || state == ViewPager2.SCROLL_STATE_SETTLING) {
                Log.d("ViewPager2", state.toString())
                //          viewModel.adapter.cancelLoad4D()
            }
            if (wallpaperModel?.is4DImage == true && state == ViewPager2.SCROLL_STATE_IDLE) {
                Log.d("ViewPager2", state.toString())
                //        viewModel.adapter.cancelLoad4D()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showLessHashTag(model: WallpaperModel?) {
        isShowingFullHashTag = false
        model?.hashTag?.let {
            dataBinding.flexboxHashTag.removeAllViews()
            var arrayHashTag = ArrayList<String>()
            arrayHashTag.addAll(refactorHashTag(it))
            arrayHashTag = filterHashTag(arrayHashTag)
            val widthFlexbox = AppConfiguration.widthScreenValue - dataBinding.actionsLayout.width
            var numberHashTag = 0
            var lastXPosHashTag = 0
            var numLine = 1
            val maxLineHashTag = 2
            val marginHashTag = 14
            arrayHashTag.let {
                for (i in 0 until arrayHashTag.size) {
                    if (arrayHashTag[i].isNotEmpty()) {
                        val textView = buildTextViewHashTag(arrayHashTag[i])
                        textView.setOnClickListener {
                            gotoSearchScreen(getHashTagFromName(textView.text.toString()))
                        }
                        textView.measure(0, 0)
                        val textViewMore =
                            buildTextViewHashTag("+" + (arrayHashTag.size - numberHashTag))
                        textViewMore.measure(0, 0)
                        if (lastXPosHashTag + textView.measuredWidth + marginHashTag < widthFlexbox && numLine <= maxLineHashTag) {
                            lastXPosHashTag += textView.measuredWidth + marginHashTag
                            dataBinding.flexboxHashTag.addView(textView)
                        } else if (lastXPosHashTag + textView.measuredWidth + marginHashTag + (textViewMore.measuredWidth) > widthFlexbox && numLine < maxLineHashTag) {
                            lastXPosHashTag = textView.measuredWidth + marginHashTag
                            dataBinding.flexboxHashTag.addView(textView)
                            numLine++
                        } else {
                            // case number line > 2 show text view more
                            val tvMore =
                                buildTextViewHashTag("+" + (arrayHashTag.size - numberHashTag))
                            tvMore.measure(0, 0)
                            tvMore.setOnClickListener {
                                showAllHashTag(model)
                            }
                            if (lastXPosHashTag + tvMore.measuredWidth + marginHashTag > widthFlexbox) {
                                dataBinding.flexboxHashTag.removeViewAt(dataBinding.flexboxHashTag.size - 1)
                                tvMore.text = "+" + (arrayHashTag.size - numberHashTag + 1)
                            }
                            dataBinding.flexboxHashTag.addView(tvMore)
                            break
                        }
                        numberHashTag++
                    }
                }
            }
            val params = dataBinding.scrollViewHashTag.layoutParams
            params.height = dataBinding.flexboxHashTag.height
            dataBinding.scrollViewHashTag.layoutParams = params
        }
    }

    private fun showAllHashTag(model: WallpaperModel) {
        isShowingFullHashTag = true
        dataBinding.flexboxHashTag.removeAllViews()
        model.hashTag?.let {
            var arrayHashTag = ArrayList<String>()
            arrayHashTag.addAll(refactorHashTag(it))
            arrayHashTag = filterHashTag(arrayHashTag)
            for (i in 0 until arrayHashTag.size) {
                if (arrayHashTag[i].isNotEmpty()) {
                    val textView = buildTextViewHashTag(arrayHashTag[i])
                    textView.setOnClickListener {
                        gotoSearchScreen(getHashTagFromName(textView.text.toString()))
                    }
                    dataBinding.flexboxHashTag.addView(textView)
                }
            }
            val imageView = ImageView(context)
            imageView.setImageResource(R.drawable.ic_caretcircledoubleup_re)
            val params = ViewGroup.MarginLayoutParams(
                ViewGroup.MarginLayoutParams.WRAP_CONTENT,
                ViewGroup.MarginLayoutParams.WRAP_CONTENT
            )
            params.setMargins(14, 14, 0, 14)
            imageView.layoutParams = params
            imageView.setOnClickListener {
                showLessHashTag(model)
            }
            dataBinding.flexboxHashTag.addView(imageView)

            val param = dataBinding.scrollViewHashTag.layoutParams
            param.height = dataBinding.flexboxHashTag.height
            dataBinding.scrollViewHashTag.layoutParams = param
        }
    }

    private fun gotoSearchScreen(hashTag: HashTag?) {
        if (screenType == AppScreen.SEARCH) {
            popFragment2(this, animateRightOrLeft = false)
            mainActivity?.searchWallpaper(hashTag)
        } else {
            val searchFragment = SearchFragment()
            searchFragment.hashTag = hashTag
            pushFragment(searchFragment, animate = true, singleton = false)
        }
    }

    private fun filterHashTag(listHashTag: ArrayList<String>): ArrayList<String> {
        val result = ArrayList<String>()
        val allHashTag = mainActivity?.viewModel?.arrayHashTag
        if (allHashTag.isNullOrEmpty()) return result
        // filter hash tag if not exist in list all hashtag
        listHashTag.forEach {
            allHashTag.forEach { hashtag ->
                if (hashtag.hashtag.equals(it)) {
                    hashtag.name?.let { it1 -> result.add(it1) }
                    arrayHashTagWallpaper.add(hashtag)
                    return@forEach
                }
            }
        }
        return result
    }

    private fun refactorHashTag(hashTagKey: String): ArrayList<String> {
        val arrayHashTag = ArrayList<String>()
        try {
            arrayHashTag.addAll(hashTagKey.split(","))
        } catch (e: Exception) {
            arrayHashTag.add(hashTagKey)
        }
        arrayHashTag.removeAll(listOf("", null))
        return arrayHashTag
    }

    private fun getHashTagFromName(name: String): HashTag? {
        arrayHashTagWallpaper.forEach {
            if (it.name.equals(name)) {
                return it
            }
        }
        return null
    }

    private fun buildTextViewHashTag(text: String): TextView {
        val textView = TextView(activity)
        textView.text = text
        textView.textSize = 14f
        textView.typeface =
            context?.let { ResourcesCompat.getFont(it, R.font.bevietnampro_regular) }
        textView.setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
        val params = ViewGroup.MarginLayoutParams(
            ViewGroup.MarginLayoutParams.WRAP_CONTENT,
            ViewGroup.MarginLayoutParams.WRAP_CONTENT
        )
        params.setMargins(14, 14, 0, 14)
        textView.layoutParams = params
        textView.setPadding(dpToPx(8f), dpToPx(8f), dpToPx(8f), dpToPx(8f))
        textView.background =
            ResourcesCompat.getDrawable(resources, R.drawable.bg_hashtag_detail_re, null)
        return textView
    }

    private fun addTrackingShowBar2Seconds() {
        val timeDiff = System.currentTimeMillis() - curTimeOnPageSelected
        if (timeDiff <= 2000) {
            TrackingSupport.recordEvent(EventDetail.DetailShowBarIn2Second)
        }
    }

    private fun showInter(position: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(100)
            if (nextAdPos == position) {
                nextAdPos += RemoteConfig.commonData.showAdWhenSwiping
                AdsManager.showInterstitialAd(isForced = true, showAdWhenSwipingTag)
            } else if (previousAdPos == position) {
                previousAdPos -= RemoteConfig.commonData.showAdWhenSwiping
                AdsManager.showInterstitialAd(isForced = true, showAdWhenSwipingTag)
            }
        }
    }

    private fun handleLoadMore(position: Int) {
        if (RemoteConfig.commonData.isActiveServer
            && !isFromHistoryScreen
            && position + offsetscreenPageLimit * 2 > viewModel.detailAdapter.getCountSize()
        ) {
            when (screenType) {
                AppScreen.COLLECTION -> viewModel.loadMoreCollectionData(cateId)
                AppScreen.HOME_HASH_TAG -> viewModel.loadMoreCollectionOnFeaturedFragment(hashTag)
                AppScreen.HOME -> viewModel.loadMoreMainFragment()
                else -> searchMoreViewModel?.loadMoreData() ?: viewModel.getListWallpaper()
            }
        }
    }

    private fun playVideo() {
        removeCallbacks(runnablePlayVideo)
        postDelayed(runnablePlayVideo, 300)
    }

    private val runnablePlayVideo = Runnable {
        viewModel.detailAdapter.playVideoCurrent(isForced = true)
    }

    private fun updateStatusView(completed: (() -> Unit)? = null) {
        if (!isInitialized) return
        statusViewJob?.cancel()
        statusViewJob = CoroutineScope(Dispatchers.IO).launch {
            delay(200)
            val wallModel = viewModel.detailAdapter.getDataOrNull(currentPos)?.also {
                val dbModel = viewModel.getWallpaperFromDatabase(it.id)
                if (dbModel != null) {
                    it.isFavorite = dbModel.isFavorite
                    it.hasDownloaded = dbModel.hasDownloaded
                    it.isPlaylist = dbModel.isPlaylist
                    it.hasShownRewardAd = dbModel.hasShownRewardAd
                } else {
                    it.isFavorite = false
                    it.isPlaylist = false
                    it.hasDownloaded = false
                    it.hasShownRewardAd = false
                }
            } ?: return@launch
            withContext(Dispatchers.Main) {
                // dataBinding.iconRewardedAd.isHidden = !canShowRewardedAd(model)
                updateShareButton(wallModel)
                dataBinding.downloadButton.alpha = 1f
                if (wallModel.hasDownloaded || wallModel.isFromLocalStorage || wallModel.is4DImage) {
                    dataBinding.downloadButton.setImageResource(R.drawable.ic_set_wallpaper_re)
                    if (wallModel.isVideo && !RemoteConfig.commonData.isSupportedVideo) {
                        dataBinding.downloadButton.alpha = 0.5f
                    }
                } else {
                    dataBinding.downloadButton.setImageResource(R.drawable.ic_download_re)
                    val check = when {
                        wallModel.isFromLocalStorage -> false
                        wallModel.hasVideoFile -> true
                        else -> !wallModel.isVideo
                    }
                    updateDownloadButton(check)
                }
                updateFavoriteView(wallModel, false)

                when (wallModel.getWallpaperModelType()) {
                    WallpaperModel.WallpaperModelType.PARALLAX -> dataBinding.ivType.setImageResource(
                        R.drawable.ic_4d_re
                    )

                    WallpaperModel.WallpaperModelType.VIDEO -> dataBinding.ivType.setImageResource(R.drawable.ic_live_re)
                    WallpaperModel.WallpaperModelType.IMAGE -> dataBinding.ivType.setImageResource(
                        android.R.color.transparent
                    )
                }
                completed?.invoke()
            }
        }
    }

    private fun updateShareButton(model: WallpaperModel) {
        dataBinding.shareButton.isHidden = model.is4DImage
    }
    //endregion

    //region animation startup

    private fun setupAnimationStartup() {
        val locationView = coordinateSharedView
        if (locationView != null) {
            isAnimation = true
            dataBinding.backgroundView.alpha = 0f
            dataBinding.viewPager.isInvisible = true
            dataBinding.containerTop.isInvisible = true
            val layoutParams = dataBinding.imageViewAnimation.layoutParams as FrameLayout.LayoutParams
            val viewWidth = locationView.width()
            val viewHeight = locationView.height()
            layoutParams.width = viewWidth
            layoutParams.height = viewHeight
            layoutParams.topMargin = locationView.top
            layoutParams.leftMargin = locationView.left
            dataBinding.imageViewAnimation.layoutParams = layoutParams

            val wallModel = listWallpaper.getOrNull(currentPos) ?: return
            val requestOptions = if (wallModel.canCropImage)
                RequestOptions.bitmapTransform(
                    CropBitmapTransformation(
                        viewWidth,
                        viewHeight,
                        CropBitmapTransformation.CropType.CENTER
                    )
                )
            else
                RequestOptions.overrideOf(viewWidth, viewHeight).dontTransform()
            val data: Any =
                if (wallModel.isFromLocalStorage) wallModel.toUrl() else wallModel.toUrl().toGlideUrl
            requestManager.load(data)
                .apply(requestOptions.diskCacheStrategy(DiskCacheStrategy.DATA))
                .into(dataBinding.imageViewAnimation)
        } else {
            dataBinding.imageViewAnimation.isHidden = true
        }
    }

    private fun animationStartup() {
        val fromRect = coordinateSharedView
        if (!isAnimation || fromRect == null) {
            endAnimationStartup()
            return
        }
        viewModel.detailAdapter.showOrHiddenPreview(show = false, animate = false)

        var itemWidth = viewModel.detailAdapter.itemWidthValue
        var itemHeight = viewModel.detailAdapter.itemHeightValue
        val r1 = itemHeight.toFloat() / itemWidth
        val r2 = fromRect.height().toFloat() / fromRect.width()
        val model = viewModel.detailAdapter.getDataOrNull(currentPos) ?: return
        val toRect = when {
            !model.canCropImage -> {
                Rect(0, 0, itemWidth, itemHeight)
            }

            r1 > r2 -> {
                itemWidth = (itemHeight / r2).toInt()
                val x = (viewModel.detailAdapter.itemWidthValue - itemWidth) / 2
                Rect(x, 0, viewModel.detailAdapter.itemWidthValue - x, viewModel.detailAdapter.itemHeightValue)
            }

            else -> {
                itemHeight = (itemWidth * r2).toInt()
                val y = (viewModel.detailAdapter.itemHeightValue - itemHeight) / 2
                Rect(0, y, viewModel.detailAdapter.itemWidthValue, viewModel.detailAdapter.itemHeightValue - y)
            }
        }

        dataBinding.imageViewAnimation.pivotX = fromRect.width() / 2f
        dataBinding.imageViewAnimation.pivotY = fromRect.height() / 2f
        val scaleInX = toRect.width().toFloat() / fromRect.width()
        val scaleInY = toRect.height().toFloat() / fromRect.height()
        val transitionInX = toRect.exactCenterX() - fromRect.exactCenterX()
        val transitionInY = toRect.exactCenterY() - fromRect.exactCenterY()
        val duration = 300L
        val animator = dataBinding.imageViewAnimation.animate()
        animator.scaleX(scaleInX).scaleY(scaleInY)
            .translationX(transitionInX)
            .translationY(transitionInY)
        animator.interpolator = DecelerateInterpolator()
        animator.duration = duration
        animator.withEndAction { endAnimationStartup() }
        animator.setUpdateListener {
            val value = it.animatedValue as Float
            dataBinding.backgroundView.alpha = value
        }
        animator.start()
    }

    private fun endAnimationStartup() {
        dataBinding.viewPager.isInvisible = false
        if (isAnimation) {
            val animator =
                ObjectAnimator.ofFloat(dataBinding.imageViewAnimation, "alpha", 1f, 0f)
            animator.duration = 500L
            animator.interpolator = DecelerateInterpolator()
            animator.startDelay = 100
            animator.doOnEnd {
                dataBinding.imageViewAnimation.setImageDrawable(null)
            }
            animator.start()
        }
        isAnimation = false
        handlePrepareData()
    }

    private fun handlePrepareData() {
        dataBinding.viewPager.registerOnPageChangeCallback(onPageChangeListenerObject)
        val model = viewModel.detailAdapter.getDataOrNull(currentPos) ?: return

        setStatusBar(false)
        saveGender()
        viewModel.statisticHashTags(model)
        AdsManager.reloadAd(200)
        viewModel.detailAdapter.onRenderFirstFrameCallback = {
            val wallModel = viewModel.detailAdapter.getDataOrNull(currentPos)
            if (wallModel?.hasVideoFile == true
                && !wallModel.hasDownloaded
                && !wallModel.isFromLocalStorage
            ) {
                updateDownloadButton(true)
            }
        }
    }

    private fun handleShowIntro(model: WallpaperModel?) {
        if (model == null) return
        if (model.is4DImage) {
            showIntro4DImage()
        } else {
            showIntroImage()
        }
    }

    private fun showIntro4DImage() {
        if (!localStorage.isFirstTimeIntro4DDisplayed) {
            IntroDialog.show(childFragmentManager, {
                countDownTimer.start()
                localStorage.isFirstTimeIntro4DDisplayed = true
                viewModel.detailAdapter.firstTimeIntroShowed = true
                viewModel.detailAdapter.showOrHiddenPreview(true)
            }, true)
        } else {
            viewModel.detailAdapter.firstTimeIntroShowed = true
            viewModel.detailAdapter.showOrHiddenPreview(true)
        }
    }

    private fun showIntroImage() {
        if (!localStorage.isFirstTimeIntroImageDisplayed) {
            localStorage.isFirstTimeIntroImageDisplayed = true
            showIntroImageCore()
        } else {
            viewModel.detailAdapter.firstTimeIntroShowed = true
            viewModel.detailAdapter.showOrHiddenPreview(true)
        }
    }

    private val countDownTimer = object : CountDownTimer(6000, 1000) {
        override fun onTick(p0: Long) {
        }

        override fun onFinish() {
            if (!scrolled && !localStorage.isFirstTimeIntroImageDisplayed) {
                showIntroImageCore()
                localStorage.isFirstTimeIntroImageDisplayed = true
            }
        }
    }

    private val countDownToHideErrorMessage = object : CountDownTimer(2000, 1000) {
        override fun onTick(p0: Long) {
        }

        override fun onFinish() {
            handleFadeOutImage()
        }
    }

    private fun handleFadeOutImage() {
        val anim = AlphaAnimation(1.0f, 0.0f)
        anim.duration = 500
        anim.repeatMode = Animation.REVERSE
        dataBinding.txtErrorMessage.startAnimation(anim)
        dataBinding.txtErrorMessage.isHidden = true
    }

    private fun showIntroImageCore() {
        postDelayed({
            IntroDialog.show(childFragmentManager, {
                viewModel.detailAdapter.firstTimeIntroShowed = true
                viewModel.detailAdapter.showOrHiddenPreview(true)
            }, false)
        }, 1_500)
    }

    private fun refreshIfDataEmpty() {
        if (viewModel.detailAdapter.getCountSize() > 1) return
        isFromHistoryScreen = true
        currentPos = oldPos
        viewModel.detailAdapter.setData(listWallpaper)
        dataBinding.viewPager.setCurrentItem(currentPos, false)
        nextAdPos = currentPos + RemoteConfig.commonData.showAdWhenSwiping
        previousAdPos = currentPos - RemoteConfig.commonData.showAdWhenSwiping
    }

    //endregion

    private val setupWallLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == AppCompatActivity.RESULT_OK) {
            localStorage.parallaxInfoChange = localStorage.parallaxInfo
            CongratulationDialog.show(childFragmentManager, is4DImage = true)
        }
    }

    private fun showPreviewParallax(model: WallpaperModel) {
        try {
            if (!model.count.isNullOrEmptyOrBlank() && model.count!!.toInt() > 0) {
                localStorage.parallaxInfo = model
                val service = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                service.putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(WallpaperApp.instance, ParallaxService::class.java)
                )

                setupWallLauncher.launch(service)
            } else showToast(R.string.activity_not_support)
        } catch (e: Exception) {
            Logger.d("show preview detail fragment exception = ${e.message}")
            TrackingSupport.recordEvent(EventSetWall.SetWallpaperVideoFail)
            showToast(R.string.activity_not_support)
        }
    }

    private fun startPreviewVideo(code: Int) {
        if (!RemoteConfig.commonData.isSupportedVideo) return
        SetSoundWallpaperDialog.isSoundVideo = false
        showPreview(code)
    }

    private fun showPreview(code: Int) {
        try {
            if (WallpaperHelper.isSupportLiveWallpaper) {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                intent.putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(WallpaperApp.instance, LiveWallpaper::class.java)
                )
                startActivityForResult(intent, abs(code))
            } else showToast(R.string.activity_not_support)
        } catch (e: Exception) {
            Logger.d("show preview detail fragment exception = ${e.message}")
            TrackingSupport.recordEvent(EventSetWall.SetWallpaperVideoFail)
            showToast(R.string.activity_not_support)
        }
    }

    override fun onBackPressed(): Boolean {
        viewModel.detailAdapter.unRegisterSensor()

        if (isAnimation) return false
        val ac = mainActivity
//        val canShowVip = ac?.canInviteVip == true
        val canShowRatingDialog =
            isInitialized && viewModel.hasDownloadedWallpaper && ac?.hasShownDialogRating != true
//        if (canShowVip || canShowRatingDialog) {
//            return super.onBackPressed()
//        }
//        if (!AdsManager.showInterstitialAd(tag = screenName.toString())) {
//            return super.onBackPressed()
//        }
        return super.onBackPressed()
//        return false
    }

    private fun notificationHasDownloadedPhoto(code: Int, model: WallpaperModel) {
        CongratulationDialog.show(childFragmentManager, isForDownload = true) {
            SetWallpaperDialog.show(requireActivity().supportFragmentManager, callback = {
                viewModel.setWallpaper(
                    model,
                    WallpaperHelper.WallpaperType.init(it)
                )
            })
        }
    }

    private fun setVisibilityGroupViews(isShow: Boolean) {
        dataBinding.containerTop.isHidden = !isShow
        dataBinding.actionsLayout.isInvisible = !isShow
        dataBinding.ivType.isHidden = !isShow
        dataBinding.flexboxHashTag.isHidden = !isShow
    }

    override fun onBackPressedLoading() {
        viewModel.cancelDownloadVideo()
    }

    private fun saveGender() {
        val model = viewModel.detailAdapter.getDataOrNull(currentPos) ?: return
        if (localStorage.sexOrNull != null) return
        val hashTag = model.hashTag
        val hashTagMale = "gioitinhnam"
        val hashTagFemale = "gioitinhnu"
        val male = localStorage.getData(AppConstants.SEX_MALE, Int::class) ?: 0
        val female = localStorage.getData(AppConstants.SEX_FEMALE, Int::class) ?: 0
        if (hashTag != null) {
            if (hashTag.contains(hashTagMale)) {
                if (!hashTag.contains(hashTagFemale)) {
                    localStorage.putData(AppConstants.SEX_MALE, male + 1)
                    if (male > 0) {
                        localStorage.sex = AppConstants.SEX_MALE
                    }
                }
            } else if (hashTag.contains(hashTagFemale)) {
                localStorage.putData(AppConstants.SEX_FEMALE, female + 1)
                if (female > 0) {
                    localStorage.sex = AppConstants.SEX_FEMALE
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isInitialized) {
            viewModel.detailAdapter.unregisterTimer(requireContext())
        }
        playOrPause(false)

        if (viewModel.detailAdapter.getData(currentPos).is4DImage) {
            viewModel.detailAdapter.unRegisterSensor()
            viewModel.detailAdapter.clearViewParallax4D()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isInitialized) {
            viewModel.detailAdapter.registerTimer(requireContext())
            updateStatusView()
        }
        playOrPause(true)
        if (viewModel.detailAdapter.getCountSize() > 0
            && viewModel.detailAdapter.getData(currentPos).is4DImage
        ) {
            viewModel.detailAdapter.visibleParallaxView()
        }
    }

    private fun playOrPause(isPlay: Boolean) {
        if (isInitialized) {
            if (isPlay) viewModel.detailAdapter.playVideoCurrent(isPlay)
            else viewModel.detailAdapter.pauseVideo()
        }
    }

    private fun canShowRewardedAd(item: WallpaperModel): Boolean {
        return when {
            AdsManager.isVipUser || item.isFromLocalStorage -> false
            item.isVideo -> !item.hasShownRewardAd && !item.hasDownloaded
            else -> {
                val count = RemoteConfig.commonData.rewardedAdCount
                if (count < 0) false
                else {
                    if (item.hasShownRewardAd || item.hasDownloaded) false
                    else downloadWallCount < 0 || downloadWallCount >= count
                }
            }
        }
    }

    private fun showRewardedAd(code: Int) {
        val item = viewModel.detailAdapter.getDataOrNull(dataBinding.viewPager.currentItem) ?: return
        shareOrDownloadCode = code
        var scheduleChangeDialogUI = false
        if (canShowRewardedAd(item) && AdsManager.canShowRewardAd && !item.isFromLocalStorage) {

        } else {
            viewModel.downloadData(
                if (item.hasDownloaded || item.hasShownRewardAd) -code else code,
                dataBinding.viewPager.currentItem
            )
            AdsManager.reloadAd(5_000)
        }
    }

    private fun downloadAfterRewardedAd(status: Int) {
        if (!isInitialized) return
        if (status == 0) {
            val userEarnedReward =
                viewModel.detailAdapter.getDataOrNull(dataBinding.viewPager.currentItem)?.hasShownRewardAd
            if (userEarnedReward == true) {
                updateStatusView {
                    viewModel.downloadData(shareOrDownloadCode, dataBinding.viewPager.currentItem)
                }
            }
        } else if (status == 1) {
            viewModel.updateRewardAd(dataBinding.viewPager.currentItem)
        }
    }

    override fun onDestroyView() {
        statusViewJob?.cancel()
        previewWallpaperModel = null
        adView?.destroy()
        adView = null
        disposableLoadMoreData?.dispose()
        cancelAnimationDownloadButton()
        super.onDestroyView()
        RxBus.unregister(RxBus.DISMISS_FRAGMENT_DETAIL + screenName)
    }

    override fun onDestroy() {
        super.onDestroy()
        setStatusBar(true, isLight = false)
        GlideSupport.clearMemory()
        mainActivity?.let { ac ->
            if (isInitialized && viewModel.hasDownloadedWallpaper) {
                ac.showDialogRating()
            } else {
                TrackingSupport.recordEventOnlyFirebase(EventIap.E0IapViewVipBackDetail)
            }
        }
    }

    companion object {
        // = 1 ConfirmDialogFragmentSetWallpaper, = 2 preview video
        const val CODE_PREVIEW_VIDEO = 3
        const val TAGS = "DetailFragment"
        var previewWallpaperModel: Pair<Int, WallpaperModel>? = null
        private const val SCREEN_TAG = "SCREEN_TAG"
        private const val SCREEN_NAME = "SCREEN_NAME"
        private const val POSITION_TAG = "POSITION_TAG"
        private const val TRACK_SCREEN_TAG = "TRACK_SCREEN_TAG"
        private const val RECT_TAG = "RECT_TAG"
        private const val CATEGORY = "CATEGORY"
        private const val HASHTAG = "HASHTAG"
        var downloadWallCount = -1
        private val mListData = mutableMapOf<Long, List<WallpaperModel>>()

        @Synchronized
        fun newInstance(
            screenType: AppScreen,
            listData: List<WallpaperModel>,
            currentPosition: Int,
            sharedView: View? = null,
            category: String? = null,
            hashTag: HashTag? = null,
            trackingScreen: TrackingScreen? = null
        ): DetailFragment {
            synchronized(TAGS) {
                val rectView = calculateLocation(sharedView)
                val curFragment = DetailFragment()
                val bundle = Bundle()
                if (mListData.size > 1) mListData.clear()
                val screenName = getKey(listData) ?: System.currentTimeMillis()
                mListData[screenName] = listData
                bundle.putParcelable(RECT_TAG, rectView)
                bundle.putSerializable(SCREEN_TAG, screenType)
                bundle.putInt(POSITION_TAG, currentPosition)
                bundle.putLong(SCREEN_NAME, screenName)
                bundle.putString(CATEGORY, category)
                bundle.putSerializable(HASHTAG, hashTag)
                bundle.putSerializable(TRACK_SCREEN_TAG, trackingScreen)
                curFragment.arguments = bundle
                return curFragment
            }
        }

        private fun getKey(listData: List<WallpaperModel>): Long? {
            for ((key, value) in mListData) {
                if (value == listData) return key
            }
            return null
        }

        private fun calculateLocation(view: View?): Rect? {
            if (view == null) return null
            val rect = Rect()
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            rect.set(location[0], location[1], location[0] + view.width, location[1] + view.height)
            return rect
        }
    }
}