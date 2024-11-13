package com.parallax.hdvideo.wallpapers.ui.appsuggestion

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.IntentSupporter
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.extension.pushFragment
import com.parallax.hdvideo.wallpapers.services.log.EventMoreApp
import com.parallax.hdvideo.wallpapers.services.log.EventPermission
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.ui.base.dialog.ProgressDialogFragment
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragmentBinding
import com.parallax.hdvideo.wallpapers.ui.dialog.RequestWallDialog
import com.parallax.hdvideo.wallpapers.ui.list.ListWallpaperFragment
import com.parallax.hdvideo.wallpapers.ui.list.AppScreen
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.ui.personalization.PersonalizationFragment
import com.parallax.hdvideo.wallpapers.ui.request.RequestFragment
import com.parallax.hdvideo.wallpapers.ui.settings.SettingsFragment
import com.livewall.girl.wallpapers.extension.checkPermissionStorage
import com.livewall.girl.wallpapers.extension.requestPermissionStorage
import com.parallax.hdvideo.wallpapers.databinding.FragmentSuggestedAppScreenBinding
//import com.tp.inappbilling.billing.BillingManager
//import com.tp.inappbilling.ui.IAPConfirmDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppSuggestionFragment : BaseFragmentBinding<FragmentSuggestedAppScreenBinding, AppSuggestionViewModel>() {


    override val resLayoutId: Int = R.layout.fragment_suggested_app_screen

    @Inject
    lateinit var localStorage: LocalStorage

    override fun init(view: View) {
        super.init(view)
        viewModel.requestManagerInstance = Glide.with(this)
       // setupBilling()
        setupRecyclerView()
        setupButton()
        viewModel.getMoreApp()
    }

//    private fun setupBilling() {
//        BillingManager.getInstance().googleAccount.observe(this) { googleSignInAccount ->
//            if (googleSignInAccount != null) {
//                googleSignInAccount.photoUrl?.let {
//                    Glide.with(this).load(it).circleCrop()
//                        .into(dataBinding.ivAvatar)
//                }
//                dataBinding.tvLogin.isHidden = true
//                dataBinding.tvLogOut.isHidden = false
//                dataBinding.tvNameUser.isHidden = false
//                dataBinding.tvNameUser.text =
//                    if (localStorage.username.isNullOrEmpty()) googleSignInAccount.displayName else localStorage.username
//            } else {
//                dataBinding.tvLogin.isHidden = false
//                dataBinding.tvNameUser.isHidden = true
//                dataBinding.tvLogOut.isHidden = true
//                dataBinding.ivAvatar.setImageResource(R.drawable.ic_avatar)
//            }
//        }
//    }

    override fun viewIsVisible() {
        viewModel.getMoreApp()
    }

    override fun handleLoading(config: ProgressDialogFragment.Configure): Boolean {
        dataBinding.progressBar.isHidden = !config.status
        return false
    }

    private fun setupRecyclerView() {
        if (dataBinding.appsRv.adapter != null) return
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        dataBinding.appsRv.setHasFixedSize(true)
        dataBinding.appsRv.layoutManager = layoutManager
        dataBinding.appsRv.adapter = viewModel.moreAppAdapter
        viewModel.moreAppAdapter.onClickedItemcallBack = { i, moreAppModel ->
            IntentSupporter.openById(moreAppModel.url)
            TrackingSupport.recordMoreAppEvent(EventMoreApp.DownloadApp, moreAppModel)
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (isInitialized && menuVisible) {
            setupRecyclerView()
            viewModel.getMoreApp()
        }
    }

    private fun setupButton() {
        dataBinding.LocalBlock.setOnClickListener {
            if (!requireActivity().checkPermissionStorage) {
                RequestWallDialog.newInstance(
                    true,
                    title = getString(R.string.msg_requested_permission_title),
                    textYes = getString(R.string.ok),
                    textNo = getString(R.string.cancel_keyword),
                    resourceId = R.drawable.img_permission_storage,
                    callbackYes = {
                        (activity as MainActivity).requestPermissionStorage(PERMISSION_STORAGE_CODE)
                    },
                    callbackNo = {
                        TrackingSupport.recordEvent(EventPermission.DeniedPermission)
                    },
                    isHiddenTextToSearch = true
                ).show(
                    requireActivity().supportFragmentManager,
                    RequestWallDialog.javaClass.name + "_STORAGE"
                )
            } else {
                pushFragment(ListWallpaperFragment.newInstance(AppScreen.LOCAL), singleton = true)
            }

        }

        dataBinding.MyListBlock.setOnClickListener {
            pushFragment(PersonalizationFragment(), singleton = true)
        }

        dataBinding.requestWallpaperBlock.setOnClickListener {
            pushFragment(RequestFragment(), singleton = true)
        }

        dataBinding.settingsBlock.setOnClickListener {
            pushFragment(SettingsFragment(), singleton = true)
        }

//        dataBinding.tvLogin.setOnClickListener {
//            (activity as MainActivity).googleLogin.launchLoginFlow()
//        }
//
//        dataBinding.tvLogOut.setOnClickListener {
//            IAPConfirmDialog.newInstance(object : IAPConfirmDialog.Callback {
//                override fun onClickYes(dialog: DialogFragment) {
//                    (activity as MainActivity).googleLogin.signOut()
//                    BillingManager.getInstance().signOut()
//                    dialog.dismiss()
//                    showToast(R.string.logout_success_msg)
//                }
//
//                override fun onClickNo(dialog: DialogFragment) {
//                    dialog.dismiss()
//                }
//            }).show(childFragmentManager, IAPConfirmDialog.DIALOG_CONFIRM_LOGOUT)
//        }
    }

}