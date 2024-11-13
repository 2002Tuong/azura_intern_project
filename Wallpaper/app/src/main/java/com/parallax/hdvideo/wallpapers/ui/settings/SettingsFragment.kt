package com.parallax.hdvideo.wallpapers.ui.settings

import android.content.Intent
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import com.parallax.hdvideo.wallpapers.WallpaperApp
import com.parallax.hdvideo.wallpapers.BuildConfig
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.FragmentSettingsScreenBinding
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.*
import com.parallax.hdvideo.wallpapers.remote.RemoteConfig
import com.parallax.hdvideo.wallpapers.services.log.EventOther
import com.parallax.hdvideo.wallpapers.services.log.EventPermission
import com.parallax.hdvideo.wallpapers.services.log.EventSetting
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.services.wallpaper.LiveWallpaper
import com.parallax.hdvideo.wallpapers.services.worker.PeriodicWallpaperChangeWorker
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragmentBinding
import com.parallax.hdvideo.wallpapers.ui.base.viewmodel.BaseViewModel
import com.parallax.hdvideo.wallpapers.ui.dialog.ConfirmDialogFragment
import com.parallax.hdvideo.wallpapers.ui.home.HomeFragment
import com.parallax.hdvideo.wallpapers.ui.list.ListWallpaperFragment
import com.parallax.hdvideo.wallpapers.ui.list.AppScreen
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.ui.policy.PolicyFragment
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.dpToPx
import com.parallax.hdvideo.wallpapers.utils.notification.NotificationUtils
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseFragmentBinding<FragmentSettingsScreenBinding, BaseViewModel>() {

    override val resLayoutId: Int = R.layout.fragment_settings_screen

    @Inject
    lateinit var localStorage : LocalStorage

    override fun init(view: View) {
        super.init(view)
        delayedAmount = 300
        initDelayed()
        dataBinding.privacyPolicyButton.setOnClickListener {
            val policyFragment = PolicyFragment()
            pushFragment(policyFragment,tag = tag)
            TrackingSupport.recordEvent(EventSetting.OpenPolicy)
        }
        dataBinding.resetButton.setOnClickListener {
            ConfirmDialogFragment.show(parentFragmentManager, title = getString(R.string.ask_confirm_reset_wallpaper),isReverseColorButton = true,textYes = getString(R.string.accept_action), textNo = getString(R.string.skip_action)) {
                context?.stopService(Intent(WallpaperApp.instance, LiveWallpaper::class.java))
                TrackingSupport.recordEvent(EventSetting.ResetDefaultSetting)
                if (WallpaperHelper.reset()) {
                    cancelServicesPlayList()
                    showToast(R.string.reset_all_wallpaper_success)
                }
            }
        }
        if (!RemoteConfig.commonData.isSupportedDevice) {
            dataBinding.notificationSwitchBlock.isHidden = true
        }
        dataBinding.autoplaySwitch.setOnCheckedChangeListener { _, isChecked ->
            val fragment = (activity as MainActivity).currentFragment as? HomeFragment
            fragment?.setAutoPlayInMainFragment(isChecked)
            localStorage.isAutoPlayVideo = isChecked
            if (isChecked) TrackingSupport.recordEvent(EventOther.AutoPlayVideoOn)
            else TrackingSupport.recordEvent(EventOther.AutoPlayVideoOff)
        }
        dataBinding.contactButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.contact_email)))
                putExtra(Intent.EXTRA_SUBJECT, "Send request email")
                putExtra(Intent.EXTRA_TEXT, "Hello, ")
            }
            try {
                startActivity(Intent.createChooser(intent, "Send mail..."))
            } catch (ex: Exception) {
                showToast("There are no email clients installed.")
            }
            TrackingSupport.recordEvent(EventSetting.SendMailSupport)
        }
        dataBinding.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            localStorage.isOnNotification = isChecked
            (activity as? MainActivity)?.apply { viewModel.scheduleNotification() }
            if (isChecked) TrackingSupport.recordEvent(EventOther.NotificationOn)
            else TrackingSupport.recordEvent(EventOther.NotificationOff)
            removeCallbacks(subscribeTopicsFCMRunnable)
            postDelayed(subscribeTopicsFCMRunnable, 500)
        }
        dataBinding.backButton.setOnClickListener {
            popFragment()
        }
        initTest7AspectRatio()
    }

    private fun initTest7AspectRatio() {
        if (BuildConfig.DEBUG) {
            dataBinding.aspectRatioView.isHidden = true
            dataBinding.restartBtn.setOnClickListener {
                val value = dataBinding.editText.text.toString().toFloatOrNull()
                if (value != null) {
                    localStorage.aspectRatio = value
                    AppConfiguration.forTesting()
                    val ac = requireActivity()
                    ac.finish()
                    startActivity(Intent(ac, MainActivity::class.java))
                }
            }
            dataBinding.chooseTv.setOnClickListener {
                showPopup(it)
            }
            dataBinding.editText.setText(localStorage.aspectRatio?.toString())
        }
    }

    private fun showPopup(view: View) {
        val arrayAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, AppConfiguration.ARRAY_ASPECT_RATIO
            .mapIndexed {index, ratio ->
                "aspect " + ratio + " : " + AppConfiguration.RATIO_SCREEN_DATA[index] })
        val listPopupWindow = ListPopupWindow(requireContext())
        listPopupWindow.anchorView =  view
        listPopupWindow.width = AppConfiguration.widthScreenValue * 80 / 100
        listPopupWindow.verticalOffset = dpToPx(5f)
        listPopupWindow.setOnItemClickListener { _, _, position, _ ->
            val aspect = AppConfiguration.ARRAY_ASPECT_RATIO[position]
            localStorage.aspectRatio = aspect
            dataBinding.editText.setText(aspect.toString())
            listPopupWindow.dismiss()
        }
        listPopupWindow.setAdapter(arrayAdapter)
        listPopupWindow.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_STORAGE_CODE && grantResults.permissionGranted) {
            pushFragment(ListWallpaperFragment.newInstance(AppScreen.LOCAL), singleton = true)
            TrackingSupport.recordEvent(EventPermission.AllowPermission)
        } else if (requestCode == PERMISSION_STORAGE_CODE && grantResults.permissionDenied) {
            TrackingSupport.recordEvent(EventPermission.DeniedPermission)
        }
    }

    private fun initDelayed() {
        dataBinding.autoplaySwitch.isChecked = localStorage.isAutoPlayVideo
        dataBinding.notificationSwitch.isChecked = localStorage.isOnNotification
        TrackingSupport.recordEvent(EventSetting.OpenSetting)
    }

    private fun cancelServicesPlayList() {
        localStorage.playlistCurIndex = 0
        localStorage.shouldAutoChangeWallpaper = false
        PeriodicWallpaperChangeWorker.cancel()
    }

    private val subscribeTopicsFCMRunnable = Runnable {
        if (dataBinding.notificationSwitch.isChecked) NotificationUtils.subscribeTopics()
        else NotificationUtils.unsubscribeTopics()
    }

}