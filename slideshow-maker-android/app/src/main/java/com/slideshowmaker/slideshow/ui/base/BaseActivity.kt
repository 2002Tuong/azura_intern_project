package com.slideshowmaker.slideshow.ui.base

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.ads.control.ads.VioAdmobCallback
import com.airbnb.lottie.LottieAnimationView
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.ironsource.mediationsdk.IronSource
import com.slideshowmaker.slideshow.BuildConfig
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.RemoteConfigRepository
import com.slideshowmaker.slideshow.data.models.FilterLinkInfo
import com.slideshowmaker.slideshow.data.models.FrameLinkInfo
import com.slideshowmaker.slideshow.data.models.zipfolder.ZipPageLoader
import com.slideshowmaker.slideshow.databinding.ActivityBaseBinding
import com.slideshowmaker.slideshow.databinding.LayoutRateDialogBinding
import com.slideshowmaker.slideshow.databinding.LayoutYesNoDialogBinding
import com.slideshowmaker.slideshow.modules.rate.RatingManager
import com.slideshowmaker.slideshow.ui.HomeActivity
import com.slideshowmaker.slideshow.ui.dialog.ConfirmPopupFragment
import com.slideshowmaker.slideshow.ui.dialog.ErrorPopupFragment
import com.slideshowmaker.slideshow.ui.dialog.ExportQualityVideoSelectionFragment
import com.slideshowmaker.slideshow.ui.dialog.LoadingPopupFragment
import com.slideshowmaker.slideshow.ui.dialog.ProgressPopupFragment
import com.slideshowmaker.slideshow.ui.dialog.SlideshowMakerDialogFragment
import com.slideshowmaker.slideshow.utils.FileHelper
import com.slideshowmaker.slideshow.utils.LanguageHelper
import com.slideshowmaker.slideshow.utils.Logger
import com.slideshowmaker.slideshow.utils.Utils
import com.slideshowmaker.slideshow.utils.extentions.fadeInAnimation
import com.slideshowmaker.slideshow.utils.extentions.openAppInStore
import com.slideshowmaker.slideshow.utils.extentions.orFalse
import com.slideshowmaker.slideshow.utils.extentions.scaleAnimation
import com.slideshowmaker.slideshow.utils.extentions.sendEmail
import kotlinx.android.synthetic.main.layout_rate_dialog.view.btnStar1
import kotlinx.android.synthetic.main.layout_rate_dialog.view.btnStar2
import kotlinx.android.synthetic.main.layout_rate_dialog.view.btnStar3
import kotlinx.android.synthetic.main.layout_rate_dialog.view.btnStar4
import kotlinx.android.synthetic.main.layout_rate_dialog.view.btnStar5
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import kotlin.math.roundToInt

abstract class BaseActivity : AppCompatActivity() {

    private var initLayoutComplete = false
    var canShowDialog = false
    var comebackStatus = ""
    var isRateAvailable = true

    private var isLoadingShow = false
    var isProgressShow = false
    protected var isExportDialogShow = false
    protected var isYesNoDialogShow = false
    protected var isRateDialogShow = false

    private val dialogs = mutableListOf<DialogFragment?>()
    protected lateinit var baseLayoutBinding: ActivityBaseBinding


    override fun onResume() {
        super.onResume()
        IronSource.onResume(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        baseLayoutBinding = ActivityBaseBinding.inflate(layoutInflater)
        LanguageHelper.loadLocale(this)
        super.onCreate(savedInstanceState)
        setContentView(baseLayoutBinding.root)

        comebackStatus = getString(R.string.do_you_want_to_come_back_label)
        baseLayoutBinding.mainContentLayout.apply {
            removeAllViews()
            addView(View.inflate(context, getContentResId(), null))
        }
        showHeader()

        baseLayoutBinding.headerView.screenTitle.text = screenTitle()

        baseLayoutBinding.headerView.icBack.setOnClickListener {
            hideKeyboard()
            onBackPressed()
        }
        initViews()
        initActions()

        val config = RemoteConfigRepository.versionConfig
        if (config != null) {
            if (RemoteConfigRepository.versionConfig?.isForceUpdate.orFalse()) {
                showForceUpdateDialog(config.title, config.message, config.url)
            } else if (RemoteConfigRepository.versionConfig?.isNewUpdate.orFalse() && this is HomeActivity) {
                showUpdateDialog(config.title, config.message, config.url)
            }
        }

        hideNavigationBar()
    }

    private fun showForceUpdateDialog(title: String?, message: String?, url: String?) {
        LanguageHelper.loadLocale(this)
        val slideDialog = SlideshowMakerDialogFragment.Builder()
            .setTitle(title ?: getString(R.string.dialog_force_update_title))
            .setMessage(message ?: getString(R.string.dialog_force_update_body))
            .setPrimaryButton(
                object : SlideshowMakerDialogFragment.Builder.Button {
                    override val label: String
                        get() = getString(R.string.dialog_force_update_btn_update)

                    override fun onClickListener(dialog: Dialog?) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(url),
                            ),
                        )
                    }
                },
            )
            .setGravity(Gravity.START)
            .setCancelable(false)
            .build()
        slideDialog.show(supportFragmentManager, null)
    }

    private fun showUpdateDialog(title: String?, message: String?, url: String?) {
        LanguageHelper.loadLocale(this)
        val slideDialog = SlideshowMakerDialogFragment.Builder()
            .setTitle(title ?: getString(R.string.dialog_update_available_title))
            .setMessage(
                message ?: HtmlCompat.fromHtml(
                    getString(R.string.dialog_update_available_body),
                    HtmlCompat.FROM_HTML_MODE_COMPACT,
                ),
            )
            .setPrimaryButton(
                object : SlideshowMakerDialogFragment.Builder.Button {
                    override val label: String
                        get() = getString(R.string.dialog_update_available_btn_update)

                    override fun onClickListener(dialog: Dialog?) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(url),
                            ),
                        )
                    }
                },
            )
            .setSecondaryButton(
                object : SlideshowMakerDialogFragment.Builder.Button {
                    override val label: String
                        get() = getString(R.string.regular_cancel)

                    override fun onClickListener(dialog: Dialog?) {
                        dialog?.dismiss()
                    }
                },
            )
            .setGravity(Gravity.START)
            .setCancelable(true)
            .setOnDismissListener {
            }
            .build()
        slideDialog.show(supportFragmentManager, null)
    }

    protected fun shareVideoFile(filePath: String) {
        if (!File(filePath).exists()) return
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "video/*"
        if (Build.VERSION.SDK_INT < 24) {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(File(filePath)))
        } else {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.putExtra(
                Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    File(filePath)
                )
            )
        }

        startActivity(intent)
    }

    protected fun checkSettingAutoUpdateTime(): Boolean {
        val int1 = Settings.Global.getInt(contentResolver, Settings.Global.AUTO_TIME)
        val int2 = Settings.Global.getInt(contentResolver, Settings.Global.AUTO_TIME_ZONE)
        Logger.e("i1 = $int1 --- i2 = $int2")
        if (int1 == 1 && int2 == 1) return true
        return false
    }


    fun setSearchInputListener(onSearchQuery: (String) -> Unit) {
        baseLayoutBinding.headerView.inputSearchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                onSearchQuery.invoke(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }

    var searchingMode = false
    fun showSearchInput() {
        if (searchingMode) return
        searchingMode = true
        openKeyboard()
        baseLayoutBinding.headerView.screenTitle.visibility = View.GONE
        baseLayoutBinding.headerView.inputSearchEditText.visibility = View.VISIBLE
        baseLayoutBinding.headerView.inputSearchEditText.requestFocus()
        baseLayoutBinding.headerView.icClearSearch.visibility = View.VISIBLE
        baseLayoutBinding.headerView.icClearSearch.setOnClickListener {
            baseLayoutBinding.headerView.inputSearchEditText.setText("")
        }

    }

    fun hideSearchInput() {
        searchingMode = false
        baseLayoutBinding.headerView.screenTitle.visibility = View.VISIBLE
        baseLayoutBinding.headerView.inputSearchEditText.visibility = View.GONE
        baseLayoutBinding.headerView.icClearSearch.visibility = View.GONE
        baseLayoutBinding.headerView.inputSearchEditText.setText("")

    }


    protected fun setScreenTitle(title: String) {
        baseLayoutBinding.headerView.screenTitle.text = title
    }


    protected fun showHeader() {
        baseLayoutBinding.headerView.root.visibility = View.VISIBLE
    }

    protected fun hideHeader() {
        baseLayoutBinding.headerView.root.visibility = View.GONE
    }

    open fun screenTitle(): String = ""

    fun setRightButton(drawableId: Int? = null, onClick: () -> Unit) {
        drawableId?.let {
            baseLayoutBinding.headerView.rightButton.setImageResource(it)
            baseLayoutBinding.headerView.rightButton.visibility = View.VISIBLE
            baseLayoutBinding.headerView.rightButton.setOnClickListener {
                onClick.invoke()
            }
        }
    }

    fun setRightTextButton(
        textResId: Int? = null,
        iconVisible: Boolean = false,
        onClick: () -> Unit
    ) {
        textResId?.let {
            baseLayoutBinding.headerView.rightTextButton.setText(textResId)
            if (!iconVisible)
                baseLayoutBinding.headerView.rightTextButton.setCompoundDrawables(null, null, null, null)
            baseLayoutBinding.headerView.rightTextButton.setOnClickListener { onClick.invoke() }
            baseLayoutBinding.headerView.rightTextButton.isVisible = true
            baseLayoutBinding.headerView.rightButton.isVisible = false
        }
    }

    fun setRightTextEnabled(enabled: Boolean) {
        baseLayoutBinding.headerView.rightTextButton.isEnabled = enabled
    }

    fun setRightTextIcon(@DrawableRes drawableId: Int?) {
        if (drawableId == null)
            baseLayoutBinding.headerView.rightTextButton.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                null,
                null
            )
        else
            baseLayoutBinding.headerView.rightTextButton.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                getDrawable(drawableId),
                null
            )
    }

    fun setSubRightButton(drawableId: Int? = null, onClick: () -> Unit) {
        drawableId?.let {
            baseLayoutBinding.headerView.subRightButton.setImageResource(it)
            baseLayoutBinding.headerView.subRightButton.visibility = View.VISIBLE
            baseLayoutBinding.headerView.subRightButton.setOnClickListener {
                onClick.invoke()
            }
        }
    }

    protected fun playTranslationYAnimation(view: View) {
        val animator = AnimatorSet()
        animator.playTogether(
            ObjectAnimator.ofFloat(view, "alpha", 0.5f, 1f),
            ObjectAnimator.ofFloat(view, "translationY", 64f, 0f)
        )
        animator.duration = 250
        animator.interpolator = FastOutLinearInInterpolator()
        animator.start()
    }

    protected fun playSlideDownToUpAnimation(view: View, viewH: Int) {
        val animator = AnimatorSet()
        animator.playTogether(
            ObjectAnimator.ofFloat(view, "translationY", viewH.toFloat(), 0f)
        )
        animator.duration = 200
        animator.interpolator = FastOutLinearInInterpolator()
        animator.start()
    }

    fun showProgressDialog(title: String = "") {
        hideAllDialog()
        LanguageHelper.loadLocale(this)
        progressDialogFrag = ProgressPopupFragment.newInstance(title)
        progressDialogFrag?.show(supportFragmentManager, LoadingPopupFragment.TAG)
        dialogs.add(progressDialogFrag)
        isProgressShow = true
    }

    fun updateProgressDialogProgress(progress: Int) {
        (progressDialogFrag as? ProgressPopupFragment)?.updateProgress(progress)
    }

    fun showLoadingDialog() {
        hideAllDialog()
        LanguageHelper.loadLocale(this)
        loadingDialogFrag = LoadingPopupFragment.newInstance()
        loadingDialogFrag?.show(supportFragmentManager, LoadingPopupFragment.TAG)
        dialogs.add(loadingDialogFrag)
        isLoadingShow = true
    }

    fun dismissLoadingDialog() {
        if (isLoadingShow) {
            loadingDialogFrag?.dismissAllowingStateLoss()
        }
        isLoadingShow = false
    }

    protected fun showExportDialog(isEditVideo: Boolean = false, callback: (Int, Int) -> Unit) {
        LanguageHelper.loadLocale(this)
        val exportVideoFragment = ExportQualityVideoSelectionFragment.newInstance(isEditVideo)
        exportVideoFragment.exportSelectedHandle = callback
        exportVideoFragment.show(supportFragmentManager, null)
    }

    private lateinit var yesNoPopup: Dialog
    protected fun showYesNoDialog(title: String, onClickYes: () -> Unit) {
        if (isYesNoDialogShow) return
        LanguageHelper.loadLocale(this)
        yesNoPopup = Dialog(this, R.style.DialogTheme)
        val dialogBinding = LayoutYesNoDialogBinding.inflate(layoutInflater)
        yesNoPopup.setContentView(dialogBinding.root)
        yesNoPopup.show()
        dialogBinding.dialogTitle.text = title
        dialogBinding.noButton.setOnClickListener {
            dismissYesNoDialog()

        }
        dialogBinding.yesButton.setOnClickListener {
            dismissYesNoDialog()
            onClickYes.invoke()
        }
        dialogBinding.bgBlackOnYesNo.setOnClickListener {
            dismissYesNoDialog()
        }
        isYesNoDialogShow = true
    }

    protected fun showYesNoDialog(
        title: String,
        onClickYes: () -> Unit,
        onClickNo: (() -> Unit)? = null
    ): View? {
        if (isYesNoDialogShow) return null
        LanguageHelper.loadLocale(this)
        if (!this::yesNoPopup.isInitialized) {
            yesNoPopup = Dialog(this, R.style.DialogTheme)
        }
        val dialogBinding = LayoutYesNoDialogBinding.inflate(layoutInflater)
        yesNoPopup.setContentView(dialogBinding.root)
        yesNoPopup.show()
        dialogBinding.dialogTitle.text = title
        dialogBinding.noButton.setOnClickListener {
            dismissYesNoDialog()
            onClickNo?.invoke()
        }
        dialogBinding.yesButton.setOnClickListener {
            dismissYesNoDialog()
            onClickYes.invoke()
        }
        dialogBinding.bgBlackOnYesNo.setOnClickListener {
            dismissYesNoDialog()
        }

        scaleAnimation(dialogBinding.dialogContentOnYesNo)
        alphaInAnimation(dialogBinding.bgBlackOnYesNo)
        isYesNoDialogShow = true
        return dialogBinding.root
    }

    protected fun showYesNoDialog(
        title: String,
        onClickYes: () -> Unit,
        onClickNo: (() -> Unit)? = null,
        onClickBg: (() -> Unit)? = null
    ): View? {
        if (isYesNoDialogShow) return null
        LanguageHelper.loadLocale(this)
        if (!this::yesNoPopup.isInitialized) {
            yesNoPopup = Dialog(this, R.style.DialogTheme)
        }
        val dialogBinding = LayoutYesNoDialogBinding.inflate(layoutInflater)
        yesNoPopup.setContentView(dialogBinding.root)
        yesNoPopup.show()
        dialogBinding.dialogTitle.text = title
        dialogBinding.noButton.setOnClickListener {
            dismissYesNoDialog()
            onClickNo?.invoke()
        }
        dialogBinding.yesButton.setOnClickListener {
            dismissYesNoDialog()
            onClickYes.invoke()
        }
        dialogBinding.bgBlackOnYesNo.setOnClickListener {
            dismissYesNoDialog()
            onClickBg?.invoke()
        }

        scaleAnimation(dialogBinding.dialogContentOnYesNo)
        alphaInAnimation(dialogBinding.bgBlackOnYesNo)
        isYesNoDialogShow = true
        return dialogBinding.root
    }

    protected fun showYesNoDialogForOpenSetting(
        title: String,
        onClickYes: () -> Unit,
        onClickNo: (() -> Unit)? = null,
        onClickBg: (() -> Unit)? = null
    ) {
        if (isYesNoDialogShow) return
        LanguageHelper.loadLocale(this)
        if (!this::yesNoPopup.isInitialized) {
            yesNoPopup = Dialog(this, R.style.DialogTheme)
        }
        val dialogBinding = LayoutYesNoDialogBinding.inflate(layoutInflater)
        yesNoPopup.setContentView(dialogBinding.root)
        yesNoPopup.show()
        dialogBinding.dialogTitle.text = title
        dialogBinding.yesButton.text = getString(R.string.regular_ok)
        dialogBinding.noButton.setOnClickListener {
            dismissYesNoDialog()
            onClickNo?.invoke()
        }
        dialogBinding.yesButton.setOnClickListener {
            dismissYesNoDialog()
            onClickYes.invoke()
        }
        dialogBinding.bgBlackOnYesNo.setOnClickListener {
            //  dismissYesNoDialog()
            //    onClickBg?.invoke()
        }

        scaleAnimation(dialogBinding.dialogContentOnYesNo)
        alphaInAnimation(dialogBinding.bgBlackOnYesNo)
        isYesNoDialogShow = true
        return
    }

    protected fun dismissYesNoDialog() {
        if (isYesNoDialogShow) {
            if (this::yesNoPopup.isInitialized) {
                yesNoPopup.dismiss()
            }
            isYesNoDialogShow = false
        }


    }

    protected fun dismissExportDialog() {
        if (isExportDialogShow) {
            baseLayoutBinding.baseRootView.removeViewAt(baseLayoutBinding.baseRootView.childCount - 1)
            isExportDialogShow = false
        }


    }


    private var isAutoShowRating = false
    private lateinit var ratePopup: Dialog
    protected fun showRatingDialog(
        showExit: Boolean = false
    ) {
        if (isRateDialogShow) return
        LanguageHelper.loadLocale(this)
        isRateDialogShow = true
        ratePopup = Dialog(this, R.style.DialogTheme)
        val rateDialogBinding = LayoutRateDialogBinding.inflate(layoutInflater)
        ratePopup.setContentView(rateDialogBinding.root)
        ratePopup.show()
        rateDialogBinding.bgBlackViewInRate.setOnClickListener {
            dismissRatingDialog()
        }

        rateDialogBinding.mainRatingContentLayout.setOnClickListener {

        }

        rateDialogBinding.layoutRateDialogExitButton.isVisible = showExit
        rateDialogBinding.btnClose.isVisible = !showExit
        rateDialogBinding.btnClose.setOnClickListener {
            dismissRatingDialog()
        }

        rateDialogBinding.bgBlackViewInRate.fadeInAnimation()
        rateDialogBinding.layoutRateDialogMainContentGroup.scaleAnimation()

        rateDialogBinding.layoutRateDialogRateUsButton.setOnClickListener {
            if (userRating == 0) {
                Toast.makeText(
                    this,
                    R.string.dialog_rating_toast_no_rating,
                    Toast.LENGTH_LONG
                )
                    .show()
            } else if (userRating > 4) {
                openAppInStore()
                RatingManager.getInstance().setRated()
            } else {
                sendEmail(
                    getString(R.string.feedback_email),
                    getString(R.string.feedback_email_subject, BuildConfig.VERSION_NAME)
                )
                RatingManager.getInstance().setRated()
            }
            dismissRatingDialog()

        }

        rateDialogBinding.btnStar1.setOnClickListener {
            rate(1, rateDialogBinding.root)
        }
        rateDialogBinding.btnStar2.setOnClickListener {
            rate(2, rateDialogBinding.root)
        }
        rateDialogBinding.btnStar3.setOnClickListener {
            rate(3, rateDialogBinding.root)
        }
        rateDialogBinding.btnStar4.setOnClickListener {
            rate(4, rateDialogBinding.root)
        }
        rateDialogBinding.btnStar5.setOnClickListener {
            rate(5, rateDialogBinding.root)
        }

        rateDialogBinding.layoutRateDialogNoThankButton.setOnClickListener {
            RatingManager.getInstance().setRated()
            dismissRatingDialog()

        }

        rateDialogBinding.layoutRateDialogLaterButton.setOnClickListener {
            dismissRatingDialog()
        }
        rateDialogBinding.layoutRateDialogExitButton.setOnClickListener {
            finish()
        }

    }

    var userRating = 0
    private fun rate(rate: Int, view: View) {
        userRating = rate
        val listAllRateStars = listOf(
            view.btnStar1,
            view.btnStar2,
            view.btnStar3,
            view.btnStar4,
            view.btnStar5,
        )

        listAllRateStars.forEachIndexed { index, imv ->
            if (index < rate) {
                imv.setImageResource(R.drawable.icon_star_filled)
            } else {
                imv.setImageResource(R.drawable.icon_star_outline)
            }
        }

    }


    private fun highlightStar(targetIndex: Int, groupStar: ArrayList<LottieAnimationView>) {
        for (index in 0..targetIndex) {
            groupStar[index].progress = 1f
        }
        object : CountDownTimer(500, 500) {
            override fun onFinish() {
                runOnUiThread { dismissRatingDialog() }

            }

            override fun onTick(millisUntilFinished: Long) {

            }

        }.start()

    }

    private fun playAnimator(
        startValue: Float,
        endValue: Float,
        delay: Long,
        duration: Long,
        onUpdate: (Float) -> Unit,
        onEnd: () -> Unit
    ) {
        val valueAnimator = ValueAnimator.ofFloat(startValue, endValue)
        valueAnimator.addUpdateListener {
            val value = it.animatedFraction

            onUpdate.invoke(value)
        }

        valueAnimator.addListener(object : AnimatorListener {
            override fun onAnimationRepeat(animation: Animator) {

            }

            override fun onAnimationEnd(animation: Animator) {
                onEnd.invoke()
            }

            override fun onAnimationCancel(animation: Animator) {

            }

            override fun onAnimationStart(animation: Animator) {

            }

        })
        valueAnimator.duration = duration
        //  animator.interpolator = FastOutLinearInInterpolator()
        valueAnimator.startDelay = delay
        valueAnimator.start()
    }

    protected fun dismissRatingDialog() {
        if (isRateDialogShow) {
            if (this::ratePopup.isInitialized) {
                ratePopup.dismiss()
            }
        }
        isRateDialogShow = false

    }

    abstract fun getContentResId(): Int
    abstract fun initViews()
    abstract fun initActions()
    override fun onBackPressed() {
        hideKeyboard()
        if (isRateDialogShow) return
        if (idDownloadDialogShow) {
            return
        }

        if (!isRateAvailable) return
        if (isLoadingShow) return

        if (isExportDialogShow) {
            dismissExportDialog()
            return
        }
        if (isRateDialogShow) {
            dismissRatingDialog()
            return
        }
        if (isYesNoDialogShow) {
            dismissYesNoDialog()
            return
        }
        if (canShowDialog) {
            /*            Logger.e("is home = $isHome")
                        if (isHome) {

                            val rated = RatingManager.getInstance().isRated()
                            Logger.e("rated = $rated")
                            if (!rated) {
                                showRatingDialog()
                                return
                            }
                        }*/
            showYesNoDialog(comebackStatus) {
                super.onBackPressed()
            }


        } else {
            super.onBackPressed()
        }

    }

    private fun checkRate(): Boolean {
        val isRated = RatingManager.getInstance().isRated()
        if (isRated) return false
        val timeShow = RatingManager.getInstance().getTimeShowRating()
        Logger.e("time show = $timeShow")
        if (timeShow <= System.currentTimeMillis() || timeShow < 0) {
            // showRatingDialog(true)
            return true
        }
        return false
    }

    protected enum class VideoQuality {
        NORMAL, HD, FULL_HD, NONE
    }

    fun scaleAnimation(view: View) {
        val scaleXValue = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f, 1f)
        val scaleYValue = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f, 1f)
        ObjectAnimator.ofPropertyValuesHolder(view, scaleXValue, scaleYValue).apply {
            interpolator = LinearOutSlowInInterpolator()
            duration = 250
        }.start()
    }

    fun alphaInAnimation(view: View) {
        val alphaValue = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
        ObjectAnimator.ofPropertyValuesHolder(view, alphaValue).apply {
            interpolator = LinearOutSlowInInterpolator()
            duration = 250
        }.start()
    }

    fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

    }


    fun hideRightButton() {
        baseLayoutBinding.headerView.rightButton.visibility = View.GONE
    }

    fun hideSubRightButton() {
        baseLayoutBinding.headerView.subRightButton.visibility = View.GONE
    }

    fun showRightButton() {
        baseLayoutBinding.headerView.rightButton.visibility = View.VISIBLE
    }

    fun showSubRightButton() {
        baseLayoutBinding.headerView.subRightButton.visibility = View.VISIBLE
    }

    fun doSendBroadcast(filePath: String) {
        sendBroadcast(
            Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(File(filePath))
            )
        )
    }

    override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
        reloadBanner()
        try {
            hideKeyboard()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }


    protected val bannerListenerCallback = object : VioAdmobCallback() {
        override fun onAdClicked() {
            super.onAdClicked()
            reloadBanner()
        }
    }
    open fun reloadBanner() {
        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "reload banner", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManager?.toggleSoftInputFromWindow(
            baseLayoutBinding.baseRootView.applicationWindowToken,
            InputMethodManager.SHOW_FORCED,
            1
        )
    }

    private fun hideKeyboard() {

        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        inputMethodManager?.hideSoftInputFromWindow(baseLayoutBinding.baseRootView.applicationWindowToken, 0)
    }

    fun showFullAds() {

    }

    private val downloadViewHashMap = HashMap<String, View?>()
    var idDownloadDialogShow = false
    fun onDownloadTheme(
        link: String,
        fileName: String,
        idTheme: String,
        lottieFileLink: String,
        lottieFileName: String,
        onComplete: () -> Unit,
        view: View? = null
    ) {
        val lottieComplete = CompletableDeferred<Error?>()

        PRDownloader
            .download(lottieFileLink, FileHelper.themeFolderPath, lottieFileName)
            .build()
            .setOnProgressListener {
                val progress = it.currentBytes.toFloat() / it.totalBytes
            }
            .start(
                object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        lottieComplete.complete(null)
                        Timber.tag("onDownloadTheme")
                            .d("----> lottieFileLink=$lottieFileLink, lottieFileName=$lottieFileName --> DONE")
                    }

                    override fun onError(error: Error?) {
                        lottieComplete.complete(error)
                        Timber.tag("onDownloadTheme")
                            .d("----> lottieFileLink=$lottieFileLink, lottieFileName=$lottieFileName --> ERROR $error")
                    }
                }
            )

        PRDownloader.download(link, FileHelper.themeFolderPath, fileName)
            .setHeader("Accept-Encoding", "identity")
            .build()
            .setOnProgressListener {
                val progress = it.currentBytes.toFloat() / it.totalBytes
                if (isProgressShow) {
                    (progressDialogFrag as? ProgressPopupFragment)?.updateProgress((progress * 100).toInt())
                }

            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    lifecycleScope.launch {
                        val bug = lottieComplete.await()
                        Timber.tag("onDownloadTheme").d("onDownloadComplete error=$bug")

                        if (bug == null) {
                            ZipPageLoader(
                                idTheme = idTheme,
                                File(FileHelper.themeFolderPath).resolve(fileName),
                                this@BaseActivity
                            ).unzipEffect {
                                Timber.tag("Download").d("onDownloadComplete")
                                downloadViewHashMap.remove(link)
                                onComplete.invoke()
                            }
                        } else {
                            onError(bug)
                        }
                    }
                }

                override fun onError(error: Error?) {
                    runOnUiThread {
                        hideLoadingDialog()
                        if (!Utils.isInternetAvailable()) {
                            showToast(getString(R.string.internet_error_warning))
                        } else {
                            hideProgressDialog()
                            showToast(getString(R.string.some_thing_went_wrong_try_again_please))
                        }
                    }

                }
            })
    }

    fun onDownloadFrame(
        frameLinkData: FrameLinkInfo,
        onComplete: () -> Unit,
        view: View? = null
    ) {
        val (link, fileName) = frameLinkData.getDownloadLinkAndName()
        PRDownloader.download(link, FileHelper.frameFolderPath, fileName)
            .setHeader("Accept-Encoding", "identity")
            .build()
            .setOnProgressListener {
                val progress = it.currentBytes.toFloat() / it.totalBytes
                updateProgressDialogProgress((progress * 100).toInt())
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    runOnUiThread {
                        Timber.tag("Download").d("onDownloadComplete")
                        downloadViewHashMap.remove(link)
                        if (fileName.endsWith(".zip")) {
                            FileHelper.unpackZip(FileHelper.frameFolderPath + "/", fileName)
                        }

                        onComplete.invoke()
                    }
                }

                override fun onError(error: Error?) {
                    runOnUiThread {
                        Timber.tag("Download")
                            .d("onError isConnectionError=${error?.isConnectionError} isServerError=${error?.isServerError}")
                        Timber.tag("Download")
                            .d("onError serverErrorMessage=${error?.serverErrorMessage} connectionException=${error?.connectionException}")

                        hideLoadingDialog()
                        if (!Utils.isInternetAvailable()) {
                            showToast(getString(R.string.internet_error_warning))
                        } else {
                            hideProgressDialog()
                            showToast(getString(R.string.some_thing_went_wrong_try_again_please))
                        }
                    }

                }
            })
    }

    fun onDownloadFilter(
        filterLinkInfo: FilterLinkInfo,
        onComplete: () -> Unit,
        view: View? = null
    ) {
        val (link, fileName) = filterLinkInfo.getDownloadLinkAndName()
        PRDownloader.download(link, FileHelper.filterFolderPath, fileName)
            .setHeader("Accept-Encoding", "identity")
            .build()
            .setOnProgressListener {
                val progress = it.currentBytes.toFloat() / it.totalBytes
                Timber.tag("Download").d("===> $link ==> ${(progress * 100f).roundToInt()}")
                updateProgressDialogProgress((progress * 100).toInt())
            }
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    runOnUiThread {
                        Timber.tag("Download").d("onDownloadComplete")

                        downloadViewHashMap.remove(link)
                        if (fileName.endsWith(".zip")) {
                            FileHelper.unpackZip(FileHelper.filterFolderPath + "/", fileName)
                        }

                        onComplete.invoke()
                    }
                }

                override fun onError(error: Error?) {
                    runOnUiThread {
                        Timber.tag("Download")
                            .d("onError isConnectionError=${error?.isConnectionError} isServerError=${error?.isServerError}")
                        Timber.tag("Download")
                            .d("onError serverErrorMessage=${error?.serverErrorMessage} connectionException=${error?.connectionException}")

                        hideLoadingDialog()
                        if (!Utils.isInternetAvailable()) {
                            showToast(getString(R.string.internet_error_warning))
                        } else {
                            hideProgressDialog()
                            showToast(getString(R.string.some_thing_went_wrong_try_again_please))
                        }
                    }

                }
            })
    }

    private var loadingDialogFrag: DialogFragment? = null
    private var progressDialogFrag: DialogFragment? = null
    private var confirmDialogFrag: DialogFragment? = null
    private var errorDialogFrag: DialogFragment? = null

    fun showConfirmDialog(
        title: String = "",
        content: String,
        cancelText: String = getString(R.string.regular_cancel),
        okText: String = "OK",
        confirmCallback: () -> Unit,
        cancelCallBack: (() -> Unit)? = null,
    ) {
        LanguageHelper.loadLocale(this)
        confirmDialogFrag = ConfirmPopupFragment.Builder()
            .setTitle(title)
            .setContent(content)
            .setCancelText(cancelText)
            .setOkText(okText)
            .setOkCallBack(confirmCallback)
            .setCancelCallBack(cancelCallBack)
            .build()

        confirmDialogFrag?.show(supportFragmentManager, "")
        dialogs.add(confirmDialogFrag)
    }

    fun showInternetErrorPopup() {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return
        hideAllDialog()
        errorDialogFrag = ErrorPopupFragment.Builder()
            .setDialogType(ErrorPopupFragment.ErrorType.INTERNET)
            .build()
        errorDialogFrag?.show(supportFragmentManager, "")
        dialogs.add(errorDialogFrag)
    }

    fun showAPIErrorPopup() {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            return
        }
        hideAllDialog()
        errorDialogFrag = ErrorPopupFragment.Builder()
            .setDialogType(ErrorPopupFragment.ErrorType.API)
            .build()
        errorDialogFrag?.show(supportFragmentManager, "")
        dialogs.add(errorDialogFrag)
    }

    fun dismissDownloadDialog() {
        if (idDownloadDialogShow) {
            idDownloadDialogShow = false
        }
    }

    fun showLoadingDialog(withTitle: String = "") {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            return
        }
        showLoadingDialogUnsafe(withTitle)
    }

    fun showLoadingDialogUnsafe(withTitle: String = "") {
        hideAllDialog()
        loadingDialogFrag = LoadingPopupFragment.newInstance(withTitle)
        loadingDialogFrag?.show(supportFragmentManager, LoadingPopupFragment.TAG)
        dialogs.add(loadingDialogFrag)
    }

    fun hideProgressDialog() {
        progressDialogFrag?.dismissAllowingStateLoss()
        isProgressShow = false
    }

    fun hideLoadingDialog() {
        isLoadingShow = false
        loadingDialogFrag?.dismissAllowingStateLoss()
    }

    fun hideAllDialog() {
        dialogs.forEach {
            it?.dismissAllowingStateLoss()
        }
        dialogs.clear()
    }

    fun hideNavigationBar() {
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        window.decorView.systemUiVisibility = flags
        window.decorView.setOnSystemUiVisibilityChangeListener {
            if ((it and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                window.decorView.systemUiVisibility = flags
            }
        }
    }
}