package com.slideshowmaker.slideshow.ui.permission

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.coroutineScope
import com.ads.control.admob.AppOpenManager
import com.ads.control.ads.VioAdmob
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.TrackingFactory
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.databinding.ActivityPermissionBinding
import com.slideshowmaker.slideshow.ui.HomeActivity
import com.slideshowmaker.slideshow.ui.PermissionRequest
import com.slideshowmaker.slideshow.ui.base.BaseActivity
import com.slideshowmaker.slideshow.utils.AdsHelper
import com.slideshowmaker.slideshow.utils.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PermissionActivity : BaseActivity() {
    private lateinit var binding: ActivityPermissionBinding
    private var permissionRequest = PermissionRequest.IMAGES
    private var shouldShowSetting = false

    override fun getContentResId(): Int = R.layout.activity_permission

    override fun initViews() {
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AdsHelper.requestNativePermission(this)
        initAds()
    }

    private fun initAds() {
        AdsHelper.apNativePermission.observe(this) {
            Log.d("AdsPermission", "${it}")
            if (it != null) {
                VioAdmob.getInstance().populateNativeAdView(
                    this,
                    it,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner
                )
                AdsHelper.apNativeLanguage.value = null

            }
        }

        AdsHelper.apNativeLanguageLoadFail.observe(this) {
            if (it) {
                binding.frAds.visibility = View.GONE
                AdsHelper.apNativeLanguageLoadFail.value = false
            }
        }
    }

    override fun initActions() {
        binding.callingScreenSwitch.setOnClickListener {
            if(!checkStoragePermission()) {
                requestStoragePermission()
            }
        }

        binding.btnContinue.setOnClickListener {
            launchHome()
        }
    }

    override fun onResume() {
        super.onResume()
        if(checkStoragePermission()) {
            onInit()
        }

        if (shouldShowSetting && !checkStoragePermission()) {
            shouldShowSetting = false
            openActSetting()
        }
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

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (permissionRequest) {
                PermissionRequest.IMAGES -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        HomeActivity.STORAGE_PERMISSION_REQUEST_CODE
                    )
                }

                PermissionRequest.VIDEOS -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_MEDIA_VIDEO),
                        HomeActivity.STORAGE_PERMISSION_REQUEST_CODE
                    )
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                HomeActivity.STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == HomeActivity.CAMERA_PERMISSION_REQUEST_CODE) {
            return
        }
        if(requestCode == HomeActivity.STORAGE_PERMISSION_REQUEST_CODE) {
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
        }
    }

    private fun onInit() {
        binding.callingScreenSwitch.setBackgroundResource(R.drawable.icon_switch_on)
        binding.btnContinue.setBackgroundResource(R.drawable.button_continue_enable)
        binding.btnContinue.isEnabled = true
    }

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

    private fun launchHome() {
        lifecycle.coroutineScope.launch {
            SharedPreferUtils.setFirstOpenComplete(true)
            delay(100)
            Intent(this@PermissionActivity, HomeActivity::class.java).apply {
                startActivity(this)
                finish()
            }
        }
    }
}