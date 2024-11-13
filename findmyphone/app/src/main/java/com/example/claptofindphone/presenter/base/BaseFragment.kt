package com.example.claptofindphone.presenter.base

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.claptofindphone.R
import com.example.claptofindphone.ads.BannerAdsController
import com.example.claptofindphone.presenter.MainActivity
import com.example.claptofindphone.presenter.common.throttleFirst
import com.example.claptofindphone.utils.AdsHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks
import javax.inject.Inject

@AndroidEntryPoint
abstract class BaseFragment : Fragment {
    constructor(@LayoutRes layoutId: Int) : super(layoutId)

    constructor() : super()

    @JvmField
    protected var TAG = this.javaClass.simpleName

    private lateinit var loadingDialog: AlertDialog

    protected open val viewModel: BaseViewModel by viewModels()

    protected open val label = ""

    protected open val isTopBarEnable = true

    protected open val isBackButtonEnable = true

    protected open val isRightButtonEnable = false

    protected open val isApplyButtonEnable = false
    protected open val isHowToUseButtonEnable = false

    protected open val isProfileMenuEnable = false

    protected open val isFabEnable = true

    protected open val isBottomBarEnable = true

    protected open val bannerVisibility = View.VISIBLE

    protected open val bannerPlacement: BannerAdsController.BannerPlacement = BannerAdsController.BannerPlacement.FIND_PHONE

    @Inject
    lateinit var adsHelper: AdsHelper

    protected open fun initView() {
        loadingDialog =
            AlertDialog.Builder(context, R.style.DialogLoading).setView(initLoadingDialogView())
                .setCancelable(false).create()
        setTitle(label)
        showTopAppBar(isTopBarEnable)
        showBackButton(isBackButtonEnable)
        showProfileMenu(isProfileMenuEnable)
        showFab(isFabEnable)
        showBottomBar(isBottomBarEnable)
        showRightButton(isRightButtonEnable)
        showApplyButton(isApplyButtonEnable)
        showHowToUse(isHowToUseButtonEnable)
        showBannerAds(bannerVisibility)
    }

    protected open fun bindViewModel() {
        with(viewModel) {
            onLoadingStateFlow.onEachWithLifecycleScope {
                showLoading(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        bindViewModel()
    }

    private fun initLoadingDialogView(): View {
        return View.inflate(context, R.layout.loading_dialog, null)
    }

    fun showTopAppBar(isShow: Boolean) {
        (requireActivity() as? MainActivity)?.showTopBar(isShow)
    }

    open fun setTitle(title: String) {
        (requireActivity() as? MainActivity)?.setTitle(title)
    }

    open fun startService() {
        (requireActivity() as? MainActivity)?.startService()
    }

    open fun stopService() {
        (requireActivity() as? MainActivity)?.stopService()
    }

    fun showBackButton(isShow: Boolean) {
        (requireActivity() as? MainActivity)?.showBackButton(isShow)
    }

    fun showRightButton(isShow: Boolean) {
        (requireActivity() as? MainActivity)?.showRightButton(isShow)
    }

    fun showApplyButton(isShow: Boolean) {
        (requireActivity() as? MainActivity)?.showApplyButton(isShow)
    }

    fun showHowToUse(isShow: Boolean) {
        (requireActivity() as? MainActivity)?.showHowToUseButton(isShow)
    }

    fun showProfileMenu(isShow: Boolean) {
        (requireActivity() as? MainActivity)?.showProfileMenu(isShow)
    }

    fun showBottomBar(isShow: Boolean) {
        (requireActivity() as? MainActivity)?.showBottomBar(isShow)
    }

    fun showBannerAds(isBannerVisible: Int) {
        (requireActivity() as? MainActivity)?.showBannerAds(isBannerVisible)
    }

    fun showFab(isShow: Boolean) {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if(bannerVisibility == View.VISIBLE) {
            Log.d("banner", "loadBanner")
            reloadBannerAds(bannerPlacement)
        }
    }

    fun reloadBannerAds(bannerAdPlacement: BannerAdsController.BannerPlacement) {
        when(bannerAdPlacement) {
            BannerAdsController.BannerPlacement.HOW_TO_USE -> adsHelper.loadBannerHowToUse(true)
            BannerAdsController.BannerPlacement.SELECT_SOUND -> adsHelper.loadBannerSelectSound(true)
            BannerAdsController.BannerPlacement.FIND_PHONE -> adsHelper.loadBannerFindPhone(true)
            BannerAdsController.BannerPlacement.SETTING -> adsHelper.loadBannerSetting(true)
            BannerAdsController.BannerPlacement.LANGUAGE -> adsHelper.loadBannerLanguage(true)
            BannerAdsController.BannerPlacement.ONBOARDING -> adsHelper.loadBannerOnboard(true)
            BannerAdsController.BannerPlacement.PERMISSION -> adsHelper.loadBannerPermission(true)
            BannerAdsController.BannerPlacement.ACTIVE_SOUND -> adsHelper.loadBannerSoundActive(true)
        }

    }

    protected fun showLoading(isShow: Boolean) {
        if (isShow) loadingDialog.show() else loadingDialog.hide()

    }

    open fun applySelect() {

    }

    open fun stopSound() {

    }

    protected fun hideKeyBoard(view: View) {
        (requireActivity() as? MainActivity)?.hideKeyBoard(view)
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    protected fun View.clicksWithLifecycleScope(action: (View) -> Unit) {
        this.clicks()
            .throttleFirst(500)
            .catch {
                Log.d(TAG, "clicks Exception: $it")
            }
            .onEach {
                action(this)
            }.launchIn(lifecycleScope)
    }

    protected fun <T> Flow<T>.onEachWithLifecycleScope(action: suspend (T) -> Unit) {
        this.flowWithLifecycle(lifecycle)
            .onEach { action(it) }
            .launchIn(lifecycleScope)
    }
}