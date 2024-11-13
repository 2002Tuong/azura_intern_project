package com.parallax.hdvideo.wallpapers.ui.main

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.viewpager.widget.ViewPager
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.gson.Gson
import com.livewall.girl.wallpapers.extension.checkPermissionStorage
import com.livewall.girl.wallpapers.extension.requestPermissionStorage
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.BuildConfig
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.ads.AdsManager
import com.parallax.hdvideo.wallpapers.data.model.Category
import com.parallax.hdvideo.wallpapers.data.model.HashTag
import com.parallax.hdvideo.wallpapers.data.model.WallpaperModel
import com.parallax.hdvideo.wallpapers.databinding.ActivityMainBinding
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.IntentSupporter
import com.parallax.hdvideo.wallpapers.extension.isHiddenAnimate
import com.parallax.hdvideo.wallpapers.extension.isInvisible
import com.parallax.hdvideo.wallpapers.extension.isNullOrEmptyOrBlank
import com.parallax.hdvideo.wallpapers.extension.permissionGranted
import com.parallax.hdvideo.wallpapers.extension.popFragment
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.services.log.EventNotification
import com.parallax.hdvideo.wallpapers.services.log.EventOther
import com.parallax.hdvideo.wallpapers.services.log.EventPermission
import com.parallax.hdvideo.wallpapers.services.log.EventRateApp
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.services.wallpaper.LiveWallpaper
import com.parallax.hdvideo.wallpapers.services.worker.ClearCacheWorker
import com.parallax.hdvideo.wallpapers.services.worker.FCMWorker
import com.parallax.hdvideo.wallpapers.services.worker.LastTimeModifyWallpaperWorker
import com.parallax.hdvideo.wallpapers.services.worker.HashTagsNotificationWorker
import com.parallax.hdvideo.wallpapers.services.worker.OnlineNotificationWorker
import com.parallax.hdvideo.wallpapers.services.worker.TopTenDeviceNotificationWorker
import com.parallax.hdvideo.wallpapers.services.worker.NotificationWorker
import com.parallax.hdvideo.wallpapers.ui.appsuggestion.AppSuggestionFragment
import com.parallax.hdvideo.wallpapers.ui.base.activity.BaseActivityBinding
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseFragmentPagerAdapter
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragment
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragment.Companion.PERMISSION_STORAGE_CODE
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragment.Companion.PREVIEW_CODE
import com.parallax.hdvideo.wallpapers.ui.collection.CollectionFragment
import com.parallax.hdvideo.wallpapers.ui.custom.bottommenu.BubbleNavigationChangeListener
import com.parallax.hdvideo.wallpapers.ui.details.DetailFragment
import com.parallax.hdvideo.wallpapers.ui.details.DetailFragment.Companion.CODE_PREVIEW_VIDEO
import com.parallax.hdvideo.wallpapers.ui.dialog.ConfirmDialogFragment
import com.parallax.hdvideo.wallpapers.ui.dialog.InviteRatingDialog
import com.parallax.hdvideo.wallpapers.ui.dialog.SetSoundWallpaperDialog
import com.parallax.hdvideo.wallpapers.ui.editor.EditorFragment
import com.parallax.hdvideo.wallpapers.ui.home.HomeFragment
import com.parallax.hdvideo.wallpapers.ui.language.LanguageFragment
import com.parallax.hdvideo.wallpapers.ui.list.ListWallpaperFragment
import com.parallax.hdvideo.wallpapers.ui.list.AppScreen
import com.parallax.hdvideo.wallpapers.ui.onboarding.OnboardingFragment
import com.parallax.hdvideo.wallpapers.ui.search.SearchFragment
import com.parallax.hdvideo.wallpapers.ui.splash.welcom.WelcomeFragment
import com.parallax.hdvideo.wallpapers.utils.AppConstants
import com.parallax.hdvideo.wallpapers.utils.LanguageUtils
import com.parallax.hdvideo.wallpapers.utils.Logger
import com.parallax.hdvideo.wallpapers.utils.network.NetworkUtils
import com.parallax.hdvideo.wallpapers.utils.notification.NotificationUtils
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivityBinding<ActivityMainBinding, MainViewModel>() {

    override val resLayoutId: Int = R.layout.activity_main
    private var _hasShownRating = false
    private var tempImgUri: Uri? = null
    private var waitingForRatingDialog = false
    private var hasPermissionStorage = false
    private var isLoadData = false

    @Inject
    lateinit var localStorage: LocalStorage
    @Inject
    lateinit var languageUtils: LanguageUtils
    private var canShowUsingCollection = false
    private var hideBottomBar = true
    private var bundleNotification: Bundle? = null
    private var lastShowInviteFromDetail = 0L
    private var timeBeginMainActivity = System.currentTimeMillis()
    private lateinit var mainAdapter: BaseFragmentPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        languageUtils.loadLocale(this)
        dataBinding.root.isInvisible = true
        AdsManager.isVipUser = localStorage.isVipUser
        AdsManager.setActivity(this)
        if(!localStorage.firstOpenComplete) {
            navigationController.pushFragment(OnboardingFragment::class, animate = false, singleton = true)
            navigationController.pushFragment(LanguageFragment::class, animate = false, singleton = true)
        }
        navigationController.pushFragment(WelcomeFragment::class, animate = false, singleton = true)
        Log.d("ViewModel", "callSecond")
        setUpObservers()
        setFirebaseConfig()

        if (localStorage.openAppCount == 0) {
            localStorage.openedLastTime =
                LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } else {
            viewModel.resetPageId()
        }

        if (viewModel.isHasConfig) {
            loadAllData()
        }
        postDelayed({
            dataBinding.root.isInvisible = false
        }, 800)
        addNeededTracking()

        AdsManager.loadBanner(this ) {
            showBanner(false)
        }
    }

    fun showBanner(isVisible: Boolean) {
        dataBinding.frAds.isVisible = isVisible
    }

    private fun setUpViewPager() {
        val listFragment = ArrayList<Pair<String,BaseFragment>>()
        listFragment.add(getString(R.string.home) to HomeFragment())
        listFragment.add(getString(R.string.collection_keyword) to CollectionFragment())
        listFragment.add(getString(R.string.search) to SearchFragment())
        listFragment.add(getString(R.string.user) to AppSuggestionFragment())
        mainAdapter = BaseFragmentPagerAdapter(supportFragmentManager).apply {
            addData(listFragment)
        }
        dataBinding.mainViewPager.setPagingEnabled(false)
        dataBinding.mainViewPager.adapter = mainAdapter
        dataBinding.mainViewPager.offscreenPageLimit = 3
        dataBinding.mainViewPager.addOnPageChangeListener(object :
            ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                dataBinding.bottomNavigationViewLinear.setCurrentActiveItem(position)
            }
        })
    }

    private fun setUpObservers() {
        viewModel.listener.observe(this) {
            if (it) {
                downloadConfig()
            }
            Logger.d("network", it)
        }
        viewModel.configState.observe(this) {
            refreshDataIfNeeded()
            loadAllData()
            val connected = NetworkUtils.isNetworkConnected()
            if (connected) {
//                viewModel.getOriginStorage()
//                viewModel.getBestStorage()
//                viewModel.getAllHashtag()
            }
        }
    }

    private fun addNeededTracking() {
        val currentTime =
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Calendar.getInstance().time)
        if (currentTime.equals(localStorage.lastDayOpenApp)) TrackingSupport.recordEvent(EventOther.ReOpenAppInADay)
        localStorage.lastDayOpenApp = currentTime

        //tracking open app count
        var countOpen = localStorage.openAppCount
        if (countOpen == 0)
            AppEventsLogger.newLogger(WallpaperApp.instance).logEvent("first_open")
        countOpen += 1
        localStorage.openAppCount = countOpen
    }

    private fun loadAllData() {
        setUpViewPager()
        if (!isLoadData && NetworkUtils.isNetworkConnected()) {
            isLoadData = true
            navigateToTab(0)
            setupBottomNavigation()
            postDelayed({
                //checkVersion()
                _hasShownRating =
                    if (localStorage.countRating == 4) false else localStorage.ratingApp
                viewModel.scheduleNotification()
//                viewModel.getMoreApp()
            }, 2_000)
        }
    }

    private fun setupBottomNavigation() {
        dataBinding.bottomNavigationViewLinear.setNavigationChangeListener(object :
            BubbleNavigationChangeListener {
            override fun onNavigationChanged(view: View?, position: Int) {
                if (position < 0 || position >= 4) return@onNavigationChanged
                when (position) {
                    0 -> {
                        navigateToTab(0)
                    }
                    1 -> {
                        navigateToTab(1)
                    }
                    2 -> {
                        navigateToTab(2)
                    }
                    3 -> {
                        navigateToTab(3)
                    }
                }
            }

            override fun onChooseAgainTab(position: Int) {
                when (position) {
                    0 -> {
                        val homeFragment = currentFragment as HomeFragment
                        if (!homeFragment.popChildFragment()) {
                            homeFragment.refreshMainFragment()
                        }
                    }
                    1 -> {
                        (currentFragment as CollectionFragment).backToTop()
                    }
                    2 -> {
                        (currentFragment as SearchFragment).backMainSearch()
                    }
                    3 -> {
                    }
                }
            }
        })
    }

    fun checkIntent(intent: Intent?) {
        intent ?: return
        getUriFromIntent(intent)?.also { handleMediaFileLocal(intent, it) }
        handleNotification(intent)
        intent.apply {
            replaceExtras(null)
            action = null
            data = null
            flags = 0
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkIntent(intent)
    }

    private val downloadConfigRunnable = Runnable {
        viewModel.downloadAppConfig(true)
    }

    private fun downloadConfig(delay: Long = 0) {
        removeCallbacks(downloadConfigRunnable)
        postDelayed(downloadConfigRunnable, delay)
    }

    override fun didClickConfirmNetwork() {
        downloadConfig()
    }

    private fun refreshDataIfNeeded() {
        val frg = currentFragment ?: return
        if (viewModel.configState.value == true) {
            frg.refreshDataIfNeeded()
            frg.navigationController?.listFragment?.forEach { fr ->
                val fg = fr as? BaseFragment
                if (fg != null) {
                    if (fg.dataLoaded)
                        fg.refreshDataIfNeeded()
                    if (fg.isAdded) {
                        fg.childFragmentManager.fragments.forEach {
                            (it as? BaseFragment)?.refreshDataIfNeeded()
                        }
                    }
                }
            }
            frg.childFragmentManager.fragments.forEach {
                (it as? BaseFragment)?.also { fg ->
                    if (fg.isAdded) fg.refreshDataIfNeeded()
                }
            }
            navigationController.listFragment.forEach {
                (it as? BaseFragment)?.also { fg ->
                    if (fg.isAdded) fg.refreshDataIfNeeded()
                }
            }
        }
    }

    override fun shouldOverrideBackPressed(): Boolean {
        return if (currentFragment !is HomeFragment) {
            navigateToTab(0)
            false
        } else true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PREVIEW_CODE) {
            if (grantResults.permissionGranted) {
                tempImgUri?.also { startEditorFragment(it) }
            }
        }
        if (requestCode == PERMISSION_STORAGE_CODE) {
            if (grantResults.permissionGranted) {
                navigationController.pushFragment(
                    ListWallpaperFragment.newInstance(AppScreen.LOCAL),
                    singleton = true
                )
                TrackingSupport.recordEvent(EventPermission.AllowPermission)
            } else {
                TrackingSupport.recordEvent(EventPermission.DeniedPermission)
            }
        }
        tempImgUri = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODE_PREVIEW_VIDEO) {
            if (resultCode == Activity.RESULT_OK) {
                val uri = DetailFragment.previewWallpaperModel?.run {
                    second.pathCacheFullVideo ?: second.toUrl()
                }
                if (uri == null) {
                    showToast(R.string.setting_wallpaper_error)
                } else {
                    setupObserverSaveVideo()
                    viewModel.saveWallpaperVideo(uri)
                }
            } else {
                showToast(R.string.setting_wallpaper_error)
            }
        }
        DetailFragment.previewWallpaperModel = null
    }

    private fun setFirebaseConfig() {
        FirebaseRemoteConfig.getInstance().setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 60L else 3600L)
                .build()
        )
    }

    private fun checkVersion() {
        FirebaseRemoteConfig.getInstance().fetchAndActivate()
            .addOnCompleteListener(this) { config ->
                if (config.isSuccessful) {
                    val deviceVersion =
                        FirebaseRemoteConfig.getInstance().getString(AppConstants.LATEST_VERSION_APP)
                            .trim()

                    if (deviceVersion.isNotEmpty()) {
                        val listInfo = deviceVersion.split("_")
                        var required = false
                        var latestAppVersion = 0
                        if (listInfo.isNotEmpty()) {
                            latestAppVersion = listInfo[0].toIntOrNull() ?: 0
                            required = listInfo.getOrNull(1)?.contains("required") ?: false
                        }
                        if (latestAppVersion > BuildConfig.VERSION_CODE) {
                            showDialogVersion(required)
                        }
                    }
                }
            }
    }

    private fun showDialogVersion(isRequired: Boolean) {
        return
        //if (isFinishing) return
        val newPackageName =
            if (isRequired) FirebaseRemoteConfig.getInstance().getString(AppConstants.NEW_PACKAGE)
                .trim() else packageName
        val title = if (isRequired) getString(R.string.title_msg_dialog_app_die) else getString(
            R.string.title_msg_dialog_update_app,
            getString(R.string.app_name)
        )
        val callbackNo = {}
        ConfirmDialogFragment.show(
            supportFragmentManager,
            title = title,
            textYes = getString(R.string.update),
            textNo = getString(R.string.skip_action),
            canBack = !isRequired,
            callbackNo = if (isRequired) null else callbackNo,
            callbackYes = {
                IntentSupporter.openById(newPackageName)
            }
        )
    }

    private fun handleNotification(intent: Intent?) {
        val it = intent ?: return
        val bundle = it.extras ?: return
        val objectId = bundle.getString(NotificationUtils.OBJECT_ID)
        val wallId = bundle.getString(NotificationUtils.Wall_ID)
        val cateId = bundle.getString(NotificationUtils.CATE_ID)
        val action = it.action
        when {
            action == NotificationUtils.ACTION_NOTIFICATION -> {
                handleNotification(intent.getBundleExtra(NotificationUtils.EXTRA_TAG))
            }
            handleObjectFCM(objectId, bundle.getString(NotificationUtils.TITLE)) -> {

            }
            wallId != null -> {
                navigationController.pushFragment(
                    DetailFragment.newInstance(
                        AppScreen.NOTIFY,
                        listOf(WallpaperModel().apply {
                            id = wallId
                            name = bundle.getString(NotificationUtils.NAME)
                            url = bundle.getString(NotificationUtils.URL)
                            hashTag = bundle.getString(NotificationUtils.HASHTAG)
                        }),
                        0
                    )
                )
            }
            cateId != null -> {

                navigationController.pushFragment(
                    ListWallpaperFragment.newInstance(
                        AppScreen.CATEGORY,
                        Category(
                            cateId,
                            name = bundle.getString(NotificationUtils.NAME),
                            url = bundle.getString(NotificationUtils.URL)
                        )
                    ),
                    tag = AppScreen.CATEGORY.name
                )
            }
            else -> {
                handleNotification(bundle)
            }
        }
    }

    private fun handleNotification(bundle: Bundle?) {
        if (bundle == null) return
        val dataNotify = bundle.getString(NotificationUtils.DATA) ?: return
        when (bundle.getString(NotificationUtils.ACTION)) {
            FCMWorker.TAGS -> {
                val mapData = Gson().fromJson(dataNotify, Map::class.java) ?: return
                val titleNotify = mapData[NotificationUtils.TITLE] as? String
                val objectId = mapData[NotificationUtils.OBJECT_ID] as? String

                val wallpaperId = mapData[NotificationUtils.Wall_ID] as? String
                val categoryId = mapData[NotificationUtils.CATE_ID] as? String
                when {
                    handleObjectFCM(objectId, titleNotify) -> {

                    }
                    wallpaperId != null -> {
                        navigationController.pushFragment(
                            DetailFragment.newInstance(
                                AppScreen.NOTIFY,
                                listOf(WallpaperModel().apply {
                                    id = wallpaperId
                                    name = mapData[NotificationUtils.NAME] as? String
                                    hashTag = mapData[NotificationUtils.HASHTAG] as? String
                                    url = mapData[NotificationUtils.URL] as? String
                                }),
                                0
                            )
                        )
                    }
                    categoryId != null -> {
                        navigationController.pushFragment(
                            ListWallpaperFragment.newInstance(
                                AppScreen.CATEGORY,
                                Category(
                                    categoryId,
                                    name = mapData[NotificationUtils.NAME] as? String,
                                    url = mapData[NotificationUtils.URL] as? String
                                )
                            ),
                            tag = AppScreen.CATEGORY.name
                        )
                    }
                }
            }
            HashTagsNotificationWorker.TAGS,
            OnlineNotificationWorker.TAGS,
            LastTimeModifyWallpaperWorker.TAGS -> {
                try {
                    handleLastTimeChangedWallpaperNotification(dataNotify.toInt())
                } catch (e: Exception) {
                }
            }
            NotificationWorker.TAGS -> {
                val title = bundle.getString(NotificationUtils.TITLE)
                val textSearch = dataNotify.replace("keysearch:", "")
                goToSearchScreen(textSearch, title)
            }
            TopTenDeviceNotificationWorker.ACTION -> {
                val fragmentInstance = ListWallpaperFragment.newInstance(
                    AppScreen.TOP_10_DEVICE,
                    Category("CategoryModel.TOP_TEN_DEVICE", null)
                )
                navigationController.pushFragment(
                    fragmentInstance,
                    animate = false,
                    singleton = true,
                    tag = AppScreen.TOP_10_DEVICE.name
                )
                localStorage.didNotifyTopTenDevice = true
            }
        }
        bundle.clear()
    }

    /**
     * @param objectId format is keysearch:key_work_abc or hashtag:tag_abc or moreapp:packageName
     */
    private fun handleObjectFCM(objectId: String?, title: String?): Boolean {
        if (objectId.isNullOrEmptyOrBlank()) return false
        val idData = objectId!!.split(":")
        return if (idData.size > 1) {
            TrackingSupport.recordEventOnlyFirebase(EventNotification.NotificationOpenAll)
            when {
                idData[0].startsWith(NotificationUtils.FCM_KEY_SEARCH) -> {
                    goToSearchScreen(idData[1], idData[1])
                    true
                }
                idData[0].startsWith(NotificationUtils.FCM_HASHTAG) -> {
                    goToSearchScreen(idData[1], title ?: idData[1])
                    true
                }
                idData[0].startsWith(NotificationUtils.FCM_MORE_APP) -> {
                    IntentSupporter.openById(idData[1])
                    true
                }
                else -> false
            }
        } else false
    }

    private fun goToSearchScreen(text: String? = null, title: String? = null) {
        if (currentFragment !is SearchFragment) navigateToTab(2)
        val fragment = currentFragment as? SearchFragment
        fragment?.searchWallpaper(text, title)
    }

    private fun goToSearchScreen(hashtag: HashTag?) {
        if (currentFragment !is SearchFragment) navigateToTab(2)
        val fragment = currentFragment as? SearchFragment
        fragment?.searchWallpaperWithHashTag(hashtag)
    }

    //check DWT-537 for more details
    private fun handleLastTimeChangedWallpaperNotification(type: Int) {
        TrackingSupport.recordEventOnlyFirebase(EventNotification.NotificationOpenAll)
        when (type) {
            0 -> {
                TrackingSupport.recordEventOnlyFirebase(EventNotification.DayOneNotificationOpen)
                if (currentFragment !is HomeFragment) navigateToTab(0)
            }
            1 -> {
                TrackingSupport.recordEventOnlyFirebase(EventNotification.DayTwoNotificationOpen)
                toBestHashTagScreen()
            }
            2 -> {
                TrackingSupport.recordEventOnlyFirebase(EventNotification.DayFourNotificationOpen)
                if (currentFragment !is SearchFragment) navigateToTab(2)
            }
            3 -> {
                TrackingSupport.recordEventOnlyFirebase(EventNotification.DaySevenNotificationOpen)
                toTopDownloadScreen()
            }
            4 -> {
                TrackingSupport.recordEventOnlyFirebase(EventNotification.WeeklyNotificationOpen)
                when (localStorage.indexContentForNotificationType[4]) {
                    0 -> if (currentFragment !is HomeFragment) navigateToTab(0)
                    1 -> toBestHashTagScreen()
                    2 -> toTopDownloadScreen()
                    else -> {
                        if (currentFragment !is SearchFragment) navigateToTab(2)
                    }
                }
            }
        }
    }

    private fun toBestHashTagScreen() {
        val fragmentInstance = ListWallpaperFragment.newInstance(screenType = AppScreen.BEST_HASH_TAG)
        navigationController.pushFragment(
            fragmentInstance,
            animate = false,
            singleton = true,
            tag = AppScreen.BEST_HASH_TAG.name
        )
    }

    private fun toTopDownloadScreen() {
        if (currentFragment !is SearchFragment) navigateToTab(2)
        (currentFragment as SearchFragment?)?.searchWallpaper(
            getString(R.string.top_download),
            getString(R.string.top_download)
        )
    }

    fun navigateToTab(tab: Int, screenType: AppScreen? = null) {
        dataBinding.mainViewPager.currentItem = tab
        currentFragment = (dataBinding.mainViewPager.adapter as BaseFragmentPagerAdapter).getFragment(tab)
        dataBinding.bottomNavigationViewLinear.setCurrentActiveItem(tab)
    }

    fun showOrHideBottomTabs(isHidden: Boolean) {
        if (isInitialized) {
            removeCallbacks(showBottomTabsRunnable)
            if (hideBottomBar != isHidden) {
                hideBottomBar = isHidden
                dataBinding.bottomNavigationViewLinear.isHiddenAnimate = isHidden
                dataBinding.dividerLine.isHiddenAnimate = isHidden
            }
        }
    }

    fun showBottomTabs(delay: Long = 0) {
        removeCallbacks(showBottomTabsRunnable)
        postDelayed(showBottomTabsRunnable, delay)
    }

    private val showBottomTabsRunnable = Runnable {
        hideBottomBar = false
        dataBinding.bottomNavigationViewLinear.animate().cancel()
        dataBinding.bottomNavigationViewLinear.isHiddenAnimate = false
    }

    private fun bundleNotification(intent: Intent?): Bundle? {
        val notifyIntent = intent ?: return null
        return if (notifyIntent.action == NotificationUtils.ACTION_NOTIFICATION) {
            bundleNotification = intent.getBundleExtra(NotificationUtils.EXTRA_TAG)
            bundleNotification
        } else {
            notifyIntent.extras
        }
    }

    val hasShownDialogRating get() = _hasShownRating

    fun showDialogRating(): Boolean {
        if (isFinishing || isDestroyed || !isAppInFore) {
            waitingForRatingDialog = true
            return false
        }
        if (!_hasShownRating) {
            _hasShownRating = true
            postDelayed({
                InviteRatingDialog().show(
                    supportFragmentManager,
                    InviteRatingDialog::class.java.name
                )
                TrackingSupport.recordEvent(EventRateApp.ShowInviteRate)
            }, 1_000)
            return true
        }
        return false
    }

    private fun handleMediaFileLocal(intent: Intent?, uri: Uri?): Boolean {
        val typeIntent = intent?.type
        if (typeIntent == null || uri == null) return false
        val audioInfo = intent.getStringExtra(AppConstants.KEY_INTENT_SHARE)
        return when {
            typeIntent.startsWith("image/") -> {
                if (intent.action == Intent.ACTION_VIEW) {
                    if (requestPermissionStorage(PREVIEW_CODE)) {
                        startEditorFragment(uri)
                    } else {
                        tempImgUri = uri
                    }
                } else {
                    startEditorFragment(uri)
                }
                true
            }
            typeIntent.startsWith("video/") -> {
                when (audioInfo) {
                    AppConstants.EMPTY_STRING -> {
                        startWallpaperServices(uri.toString(), false)
                    }
                    else -> {
                        startWallpaperServices(uri.toString(), true)
                    }
                }
            }
            else -> false
        }
    }

    private fun getUriFromIntent(intent: Intent?): Uri? {
        val intent1 = intent ?: return null
        return when (intent1.action) {
            Intent.ACTION_VIEW -> intent1.data
            Intent.ACTION_SEND -> {
                intent1.clipData?.run {
                    if (itemCount > 0) getItemAt(0).uri
                    else null
                }
            }
            else -> null
        }
    }

    private fun setupObserverSaveVideo() {
        if (!viewModel.hasSavedWallpaperLiveData.hasObservers()) {
            viewModel.hasSavedWallpaperLiveData.observe(this) { check ->
                showToast(if (check) R.string.setting_wallpaper_successfully else R.string.setting_wallpaper_error)
            }
        }
    }

    private fun startWallpaperServices(uri: String, isSoundVideo: Boolean): Boolean {
        if (!RemoteConfig.commonData.isSupportedVideo) {
            showToast(R.string.warning_android_12_message_first)
            return false
        }
        return if (WallpaperHelper.isSupportLiveWallpaper) {
            val wallpaperModel = WallpaperModel()
            wallpaperModel.pathCacheFullVideo = uri
            DetailFragment.previewWallpaperModel = CODE_PREVIEW_VIDEO to wallpaperModel
            if (isSoundVideo) {
                // SetSoundWallpaperDialog.show(supportFragmentManager) {
                try {
                    SetSoundWallpaperDialog.isSoundVideo = false
                    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                    intent.putExtra(
                        WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        ComponentName(WallpaperApp.instance, LiveWallpaper::class.java)
                    )
                    startActivityForResult(intent, CODE_PREVIEW_VIDEO)
                } catch (e: Exception) {
                    showToast(R.string.activity_not_support)
                }
                // }
            } else {
                try {
                    SetSoundWallpaperDialog.isSoundVideo = false
                    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                    intent.putExtra(
                        WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        ComponentName(WallpaperApp.instance, LiveWallpaper::class.java)
                    )
                    startActivityForResult(intent, CODE_PREVIEW_VIDEO)
                } catch (e: Exception) {
                    showToast(R.string.activity_not_support)
                }
            }
            true
        } else {
            showToast(R.string.activity_not_support)
            false
        }
    }

    private fun startEditorFragment(uri: Uri) {
        navigationController.findFragment(EditorFragment::class)?.popFragment(animate = false)
        if (currentFragment is SearchFragment) {
            (currentFragment as SearchFragment).setShowKeyboard(false)
        }
        navigationController.pushFragment(
            EditorFragment::class,
            bundle = bundleOf(EditorFragment.TAGS to uri),
            animate = false
        )
    }

    fun pauseOrPlayVideo(play: Boolean) {
//        if (isFinishing || !this::pagerAdapter.isInitialized) return
//            (pagerAdapter.getFragment(0) as HomeFragment).pauseOrPlayVideo(play)
        if (currentFragment !is HomeFragment) return
        (currentFragment as HomeFragment?)?.pauseOrPlayVideo(play)
    }

    override fun onResume() {
        super.onResume()
        if (isInitialized) {
            viewModel.cancelLastTimeChangeNotification()
        }
        if (waitingForRatingDialog) {
            showDialogRating()
            waitingForRatingDialog = false
        }
        //refreshViewIfLanguageChanged()
    }

    private fun refreshViewIfLanguageChanged() {
        val language = Locale.getDefault().language.toLowerCase(Locale.ENGLISH)
        if (language != RemoteConfig.languageCode) {
            post {
                RemoteConfig.languageCode = language
                if (topFragment !is WelcomeFragment) {
                    navigationController.popToRoot()
                }
                navigateToTab(0)
            }
        }
    }

    override fun onPause() {
        if (isInitialized)
            viewModel.scheduleNotificationHashTags()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        hasPermissionStorage = checkPermissionStorage
    }

    private fun addTrackingTimeUserOnScreen() {
        val timeOnScreenInMinute =
            ((System.currentTimeMillis() - timeBeginMainActivity) / 60_000).toInt()
        Logger.d("timeon = $timeOnScreenInMinute")
        if (timeOnScreenInMinute in 1..10) {
            val eventName =
                String.format(EventOther.E2UseAppXMinute.nameEvent, timeOnScreenInMinute)
            TrackingSupport.recordEventOnlyFirebase(eventName)
        }
    }

    fun searchWallpaper(hashtag: HashTag?) {
        val fragment = navigationController.topFragment as? SearchFragment
        fragment?.searchWallpaperWithHashTag(hashtag)
        if (fragment == null) {
            goToSearchScreen(hashtag)
        }
    }

    override fun onDestroy() {
        addTrackingTimeUserOnScreen()
        AdsManager.destroyAll()
        Logger.d("MainActivity onDestroy")
        ClearCacheWorker.start()
        super.onDestroy()
        RemoteConfig.clear()
        DetailFragment.previewWallpaperModel = null
        CollectionFragment.recyclerViewState = null
        SearchFragment.recyclerViewState = null
    }
}

