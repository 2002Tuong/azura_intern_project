package com.slideshowmaker.slideshow.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.data.RemoteConfigRepository
import com.slideshowmaker.slideshow.data.TrackingFactory
import com.slideshowmaker.slideshow.data.local.SessionData
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.data.models.enum.Feature
import com.slideshowmaker.slideshow.data.models.enum.MediaType
import com.slideshowmaker.slideshow.data.models.enum.VideoActionType
import com.slideshowmaker.slideshow.databinding.ActivityHomeScreenBinding
import com.slideshowmaker.slideshow.models.MyVideoModel
import com.slideshowmaker.slideshow.modules.rate.RatingManager
import com.slideshowmaker.slideshow.ui.base.BaseActivity
import com.slideshowmaker.slideshow.ui.home.models.SuggestedAppEpoxyModel_
import com.slideshowmaker.slideshow.ui.my_video.MyVideoActivity
import com.slideshowmaker.slideshow.ui.picker.ImagePickerActivity
import com.slideshowmaker.slideshow.ui.premium.PremiumActivity
import com.slideshowmaker.slideshow.ui.settings.SettingActivity
import com.slideshowmaker.slideshow.utils.AdsHelper
import com.slideshowmaker.slideshow.utils.FileHelper
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.MediaHelper
import com.slideshowmaker.slideshow.utils.Utils
import com.slideshowmaker.slideshow.utils.extentions.isFirstInstall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

open class HomeActivity : BaseActivity(), KodeinAware {

    override val kodein by lazy { (application as VideoMakerApplication).kodein }

    private val viewModel: HomeScreenViewModel by instance()
    private lateinit var layoutBinding: ActivityHomeScreenBinding

    companion object {
        const val TAKE_PICTURE_CODE = 1001
        const val RECORD_CAMERA_CODE = 1991
        const val CAMERA_PERMISSION_REQUEST_CODE = 1002
        const val STORAGE_PERMISSION_REQUEST_CODE = 1003
    }

    override fun getContentResId(): Int = R.layout.activity_home_screen

    private var isSplashFinish = false

    private var pendingFeature: Feature? = null
    private var permissionRequest = PermissionRequest.IMAGES

    private val permissionNotificationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        }

    override fun initViews() {
        layoutBinding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(layoutBinding.root)
        AdsHelper.isAdsSplashClosed.observe(this) {
            if (it) {
                checkNotificationPermissionAndroid13()
            }
        }
        comebackStatus = getString(R.string.do_you_want_to_leave)

        if (SharedPreferUtils.showOpenMainAdsOnFirstLaunch && isFirstInstall()) {
            SharedPreferUtils.showOpenMainAdsOnFirstLaunch = false
            TrackingFactory.Common.openMainAppFirstLaunch().track()
        }
        hideHeader()

        viewModel.addObserver(lifecycle)
        layoutBinding.apply {

        }
        AdsHelper.requestNativeHome(this,
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
            })
    }

    private fun checkNotificationPermissionAndroid13() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_DENIED
            ) {
                permissionNotificationLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    private fun onInit() {
        isSplashFinish = true
        canShowDialog = true
        when (pendingFeature) {
            Feature.SLIDE -> gotoPickMedia(MediaType.PHOTO)
            Feature.EDIT -> gotoPickMedia(VideoActionType.SLIDE)
            Feature.JOIN -> gotoPickMedia(VideoActionType.JOIN)
            Feature.TRIM -> gotoPickMedia(VideoActionType.TRIM)
            Feature.MY_VIDEOS -> {
                CoroutineScope(Dispatchers.Main).launch {
                    AdsHelper.forceShowInterMyVideo(this@HomeActivity, onNextAction = {
                        startActivity(Intent(this@HomeActivity, MyVideoActivity::class.java))
                    })
                }
            }

            null -> {}
        }
        pendingFeature = null
        if (checkStoragePermission()) {
            Thread {
                try {
                    initThemeData()
                    initDefaultAudio()
                    getAllMyStudioItem()
                    FileHelper.clearTemp()

                } catch (e: Exception) {

                }

            }.start()
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (permissionRequest) {
                PermissionRequest.IMAGES -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        STORAGE_PERMISSION_REQUEST_CODE
                    )
                }

                PermissionRequest.VIDEOS -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_MEDIA_VIDEO),
                        STORAGE_PERMISSION_REQUEST_CODE
                    )
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    //val mShare = Share()

    override fun initActions() {
        layoutBinding.btnPro.setOnClickListener {
            startActivity(Intent(this, PremiumActivity::class.java))
        }
        layoutBinding.bgButtonSlideShow.setOnClickListener {
            permissionRequest = PermissionRequest.IMAGES
            TrackingFactory.SlideshowEditor.buttonClick().track()
            if (SharedPreferUtils.clickSlideShowOnFirstLaunch && isFirstInstall()) {
                SharedPreferUtils.clickSlideShowOnFirstLaunch = false
                TrackingFactory.Common.clickSlideShowFirstLaunch().track()
            }
            if (!checkStoragePermission()) {
                pendingFeature = Feature.SLIDE
                requestStoragePermission()
                return@setOnClickListener
            }
            gotoPickMedia(MediaType.PHOTO)
        }
        layoutBinding.bgButtonMyVideo.setOnClickListener {
            permissionRequest = PermissionRequest.VIDEOS
            TrackingFactory.MyVideo.buttonClick().track()
            if (!checkStoragePermission()) {
                pendingFeature = Feature.MY_VIDEOS
                requestStoragePermission()
                return@setOnClickListener
            }
            AdsHelper.forceShowInterMyVideo(this, onNextAction = {
                startActivity(Intent(this, MyVideoActivity::class.java))
            })
        }
        layoutBinding.layoutEditVideo.setOnClickListener {
            permissionRequest = PermissionRequest.VIDEOS
            TrackingFactory.EditVideo.buttonClick().track()
            if (!checkStoragePermission()) {
                pendingFeature = Feature.EDIT
                requestStoragePermission()
                return@setOnClickListener
            }
            gotoPickMedia(MediaType.VIDEO)
        }
        layoutBinding.layoutTrim.setOnClickListener {
            permissionRequest = PermissionRequest.VIDEOS
            TrackingFactory.TrimVideo.buttonClick().track()
            if (!checkStoragePermission()) {
                pendingFeature = Feature.TRIM
                requestStoragePermission()
                return@setOnClickListener
            }
            gotoPickMedia(VideoActionType.TRIM)
        }

        layoutBinding.layoutJoinVideo.setOnClickListener {
            permissionRequest = PermissionRequest.VIDEOS
            TrackingFactory.JoinVideo.buttonClick().track()
            if (!checkStoragePermission()) {
                pendingFeature = Feature.JOIN
                requestStoragePermission()
                return@setOnClickListener
            }
            gotoPickMedia(VideoActionType.JOIN)
        }

        layoutBinding.layoutShare.setOnClickListener {
            AppOpenManager.getInstance().disableAdResumeByClickAction()
            val sendingIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(
                    Intent.EXTRA_TEXT, getString(
                        R.string.dialog_sharing_message,
                        SettingActivity.SHARE_URL
                    )
                )
                type = "text/plain"
            }

            val sharingIntent = Intent.createChooser(sendingIntent, null)
            startActivity(sharingIntent)
        }

        layoutBinding.layoutRate.setOnClickListener {
            if (isRateAvailable) {
                isRateAvailable = false
                showRatingDialog()
                object : CountDownTimer(2000, 2000) {
                    override fun onFinish() {
                        isRateAvailable = true
                    }

                    override fun onTick(millisUntilFinished: Long) {

                    }

                }.start()
            }

        }

        layoutBinding.btnPro.setOnClickListener {
            startActivity(Intent(this, PremiumActivity::class.java))
        }

        layoutBinding.btnSetting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }


        layoutBinding.groupSuggestedApp.isVisible =
            !RemoteConfigRepository.suggestedAppConfigs.isNullOrEmpty()
        layoutBinding.rvSuggestedApps.setItemSpacingRes(R.dimen.margin_1)

        layoutBinding.rvSuggestedApps.withModels {
            RemoteConfigRepository.suggestedAppConfigs?.forEach {
                SuggestedAppEpoxyModel_()
                    .id(it.packageId)
                    .app(it)
                    .addTo(this)
            }
        }

    }

    private fun countDownAvailable() {
        object : CountDownTimer(1000, 1000) {
            override fun onFinish() {
                pickMediaValid = true
            }

            override fun onTick(millisUntilFinished: Long) {

            }

        }.start()
    }

    private fun gotoPickMedia(actionKind: VideoActionType) {
        if (!checkStoragePermission()) {
            requestStoragePermission()
            return
        }

        if (Utils.getAvailableSpaceInMB() < 100) {
            showToast(getString(R.string.lack_free_space))
            return
        }

        if(actionKind == VideoActionType.JOIN || actionKind == VideoActionType.TRIM) {
            AdsHelper.forceShowInterJoinTrim(this) {
                startActivity(ImagePickerActivity.newIntent(this, actionKind, false))
            }
        }else {
            startActivity(ImagePickerActivity.newIntent(this, actionKind, false))
        }
    }

    private var pickMediaValid = true
    private fun gotoPickMedia(mediaKind: MediaType) {

        if (!checkStoragePermission()) {
            requestStoragePermission()
            return
        }
        if (Utils.getAvailableSpaceInMB() < 200) {
            showToast(getString(R.string.lack_free_space))
            return
        }
        CoroutineScope(Dispatchers.Main).launch {
            AdsHelper.forceShowInterCreate(this@HomeActivity, onNextAction = {
                if (mediaKind == MediaType.PHOTO) {
                    TrackingFactory.Common.showAdsInterSlideshowButton().track()
                    startActivity(
                        ImagePickerActivity.newIntent(
                            this@HomeActivity,
                            mediaKind,
                            false
                        )
                    )
                } else {
                    startActivity(
                        ImagePickerActivity.newIntent(
                            this@HomeActivity,
                            mediaKind,
                            false
                        )
                    )
                }
            })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            return
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkStoragePermission()) {
                    onInit()
                    TrackingFactory.Common.permissionGranted().track()
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            if (permissionRequest == PermissionRequest.VIDEOS) {
                                Manifest.permission.READ_MEDIA_VIDEO
                            } else {
                                Manifest.permission.READ_MEDIA_IMAGES
                            }
                        )
                    ) {
                        requestStoragePermission()
                    } else {
                        openActSetting()
                    }
                }
            } else {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        onInit()
                        TrackingFactory.Common.permissionGranted().track()
                    } else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            )
                        ) {
                            requestStoragePermission()
                        } else {
                            openActSetting()
                        }
                    }
                } else {
                    openActSetting()
                }
            }
            return
        }
    }

    private var shouldShowSetting = false
    private fun openActSetting() {

        val dialog = showYesNoDialogForOpenSetting(
            getString(R.string.dialog_permission_required_content) + "\n" + getString(R.string.dialog_permission_required_go_to_setting_button),
            {
                AppOpenManager.getInstance().disableAdResumeByClickAction()
                Logger.e("click Yes")
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                showToast(getString(R.string.dialog_permission_required_content))
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
                shouldShowSetting = true
            },
            { finishAfterTransition(); },
            { finishAfterTransition(); })

    }

    private fun initThemeData() {
        val themeFolder = File(FileHelper.themeFolderPath)
        if (!themeFolder.exists()) {
            themeFolder.mkdirs()
        }
        copyDefaultTheme()
    }

    private fun copyDefaultTheme() {
        val assetFile = assets.list("theme-default")
        assetFile?.let {
            for (fileName in assetFile) {
                val outputFile = File("${FileHelper.themeFolderPath}/$fileName")
                if (!outputFile.exists()) {
                    val inStream = assets.open("theme-default/$fileName")
                    val outStream = FileOutputStream(outputFile)
                    copyFile(inStream, outStream)
                }
            }
        }
    }

    fun initDefaultAudio() {
        val audioFolder = File(FileHelper.audioDefaultFolderPath)
        if (!audioFolder.exists()) {
            audioFolder.mkdirs()
        }
        copyDefaultAudio()
    }

    private fun copyDefaultAudio() {
        val assetFile = assets.list("audio")
        assetFile?.let {
            for (fileName in assetFile) {
                val outputFile = File("${FileHelper.audioDefaultFolderPath}/$fileName")
                if (!outputFile.exists()) {
                    val inStream = assets.open("audio/$fileName")
                    val outStream = FileOutputStream(outputFile)
                    copyFile(inStream, outStream)
                }
            }
        }
    }

    private fun copyFile(inputStream: InputStream, outputStream: FileOutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }
        inputStream.close()
        outputStream.close()
    }

    private fun checkStoragePermission(): Boolean { //true if GRANTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return when (permissionRequest) {
                PermissionRequest.IMAGES -> {
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED
                }

                PermissionRequest.VIDEOS -> {
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_VIDEO
                    ) == PackageManager.PERMISSION_GRANTED
                }
            }
        } else {
            return ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getAllMyStudioItem() {
        Thread {
            val myStudioFolder = File(FileHelper.myStuioFolderPath)
            val myStudioData = ArrayList<MyVideoModel>()
            if (myStudioFolder.exists() && myStudioFolder.isDirectory) {
                for (item in myStudioFolder.listFiles()) {
                    try {
                        val duration = MediaHelper.getVideoDuration(item.absolutePath)
                        myStudioData.add(
                            MyVideoModel(
                                item.absolutePath,
                                item.lastModified(),
                                duration
                            )
                        )
                    } catch (e: Exception) {
                        item.delete()
                        doSendBroadcast(item.absolutePath)
                        continue
                    }

                }
            }


        }.start()


    }

    private var isPause = false
    override fun onPause() {
        super.onPause()
        isPause = true
    }

    override fun onBackPressed() {

        if (isRateDialogShow) {
            dismissRatingDialog()
            return
        }

        if (RatingManager.getInstance().canShowRate()) {
            showRatingDialog(true)
            return
        }


        showConfirmDialog(
            content = getString(R.string.dialog_exit_label),
            okText = getString(R.string.regular_cancel),
            cancelText = getString(R.string.regular_exit),
            cancelCallBack = {
                SessionData.clear()
                finish()
            }, confirmCallback = {

            })
    }

    override fun onResume() {
        super.onResume()
        AdsHelper.loadInterCreate(this)
        AdsHelper.loadInterMyVideo(this)
        AdsHelper.loadInterJoinTrim(this)
        if (checkStoragePermission()) {
            onInit()
        }
        if (shouldShowSetting && !checkStoragePermission()) {
            shouldShowSetting = false
            openActSetting()
        }


    }

    override fun onDestroy() {
        try {
            FileHelper.clearTemp()
        } catch (e: Exception) {
        }
        super.onDestroy()
    }

}

enum class PermissionRequest {
    IMAGES, VIDEOS
}