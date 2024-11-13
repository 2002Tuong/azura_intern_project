package com.slideshowmaker.slideshow.ui.base

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import com.ironsource.mediationsdk.IronSource
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.ui.dialog.ConfirmPopupFragment
import com.slideshowmaker.slideshow.ui.dialog.ErrorPopupFragment
import com.slideshowmaker.slideshow.ui.dialog.LoadingPopupFragment

abstract class SnapSlideActivity : AppCompatActivity() {


    private var loadingPopup: DialogFragment? = null
    private var confirmPopup: DialogFragment? = null
    private var errorPopup: DialogFragment? = null

    private val dialogs = mutableListOf<DialogFragment?>()

    override fun onResume() {
        super.onResume()
        IronSource.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }

    fun showConfirmDialog(
        title: String = "",
        content: String,
        cancelText: String = getString(R.string.regular_cancel),
        okText: String = "OK",
        confirmCallback: () -> Unit,
        cancelCallBack: (() -> Unit)? = null,
    ) {
        confirmPopup = ConfirmPopupFragment.Builder()
            .setTitle(title)
            .setContent(content)
            .setCancelText(cancelText)
            .setOkText(okText)
            .setOkCallBack(confirmCallback)
            .setCancelCallBack(cancelCallBack)
            .build()

        confirmPopup?.show(supportFragmentManager, "")
        dialogs.add(confirmPopup)
    }

    fun showInternetErrorPopup() {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return
        hideAllDialog()
        errorPopup = ErrorPopupFragment.Builder()
            .setDialogType(ErrorPopupFragment.ErrorType.INTERNET)
            .build()
        errorPopup?.show(supportFragmentManager, "")
        dialogs.add(errorPopup)
    }

    fun showAPIErrorPopup() {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            return
        }
        hideAllDialog()
        errorPopup = ErrorPopupFragment.Builder()
            .setDialogType(ErrorPopupFragment.ErrorType.API)
            .build()
        errorPopup?.show(supportFragmentManager, "")
        dialogs.add(errorPopup)
    }

    fun showLoadingDialog(withTitle: String = "") {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            return
        }
        showLoadingDialogUnsafe(withTitle)
    }

    fun showLoadingDialogUnsafe(withTitle: String = "") {
        hideAllDialog()
        loadingPopup = LoadingPopupFragment.newInstance(withTitle)
        loadingPopup?.show(supportFragmentManager, LoadingPopupFragment.TAG)
        dialogs.add(loadingPopup)
    }

    fun hideLoadingDialog() {
        loadingPopup?.dismissAllowingStateLoss()
    }

    fun hideAllDialog() {
        dialogs.forEach {
            it?.dismissAllowingStateLoss()
        }
        dialogs.clear()
    }
}