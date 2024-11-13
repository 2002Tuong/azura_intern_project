package com.parallax.hdvideo.wallpapers.ui.dialog

import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.view.doOnPreDraw
import androidx.core.view.isInvisible
import androidx.fragment.app.FragmentManager
import com.google.android.gms.ads.nativead.NativeAd
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.ads.AdsManager
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.extension.isNullOrEmptyOrBlank
import com.parallax.hdvideo.wallpapers.extension.setGradientTextView
import com.parallax.hdvideo.wallpapers.ui.base.dialog.BaseDialogFragment
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CongratulationDialog : BaseDialogFragment() {

    private var congratulationCallback: (() -> Unit)? = null
    private var isForDownload: Boolean = false
    private var isVipUser: Boolean = AdsManager.isVipUser
    override val resLayoutId: Int get() = if (isVipUser || isForDownload) R.layout.popup_set_successful_wall else
        R.layout.popup_set_success_wall_no_vip
    private val _nativeAdLiveData = AdsManager.loadNativeAdOnExitDialog()
    private var is4DImage = false

    @Inject
    lateinit var localStorage : LocalStorage
    override fun init(view: View) {
        if (isVipUser || isForDownload) {
            if (isForDownload) {
                view.findViewById<com.airbnb.lottie.LottieAnimationView?>(R.id.ivSuccessful)?.apply {
                    setAnimation(R.raw.download_success)
                }
                view.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.subtitle).text = getString(R.string.msg_download_wall_successful)
                view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnOk).setGradientTextView(true)
                view.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.title).setGradientTextView(true)
            }
            view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnOk).setOnClickListener {
                if (isForDownload) congratulationCallback?.invoke()
                dismissAllowingStateLoss()
            }
        } else setupView(view)
    }

    override fun onResume() {
        super.onResume()
        val widthView = if (isVipUser || isForDownload) {
            (AppConfiguration.displayMetrics.widthPixels * 0.9).toInt()
        } else ViewGroup.LayoutParams.MATCH_PARENT
        setLayout(widthView, if (isVipUser || isForDownload) ViewGroup.LayoutParams.WRAP_CONTENT else ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onDestroyView() {
        _nativeAdLiveData.removeObservers(this)
        AdsManager.destroyNativeAdOnExitDialog()
        super.onDestroyView()
    }

    override fun onDetach() {
        super.onDetach()
        congratulationCallback = null
    }

    private fun setupView(view: View) {
        view.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.congratulationTv).setGradientTextView(true)
        view.apply {
            findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.titleTv).text = if (is4DImage) {
                getString(R.string.msg_set_parallax_success)
            } else {
                getString(R.string.msg_setup_wall_successful)
            }
        }
        view.findViewById<androidx.appcompat.widget.AppCompatImageView>(R.id.ivClose).setOnClickListener {
            congratulationCallback?.invoke()
            dismiss()
        }
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnInviteVip).setOnClickListener {
//            localStorage.hadActivelyOpenedIAP = true
            /*TrackingHelper.recordEventOnlyFirebase(IapEvent.E0IapClickVipFrom.eventName + "dialog_set_success")
            TPMetricsLoggerHelper.recordIapViewVip(iapFrom = TPMetricsLoggerHelper.SET, userType = RemoteConfig.userType.name)
            val mainActivity = activity as MainActivity?
            mainActivity?.showIAPScreen(true, iapFrom = TPMetricsLoggerHelper.SET)*/
            dismissAllowingStateLoss()
        }
        view.doOnPreDraw {
            _nativeAdLiveData.observe(this) {
                bindDataNativeView(it)
            }
        }
    }

    private fun bindDataNativeView(nativeAd: NativeAd?) {
        val nativeAdView = rootView?.findViewById<com.google.android.gms.ads.nativead.NativeAdView>(R.id.nativeAdView) ?: return
        nativeAd ?: return
        nativeAdView.mediaView = nativeAdView.findViewById(R.id.adMedia)
        nativeAdView.headlineView = nativeAdView.findViewById(R.id.adHeadline)
        nativeAdView.callToActionView = nativeAdView.findViewById(R.id.adCallToAction)
        nativeAdView.iconView = nativeAdView.findViewById(R.id.adAppIcon)

        (nativeAdView.headlineView as? TextView)?.text = nativeAd.headline

        val adCallToAction = nativeAdView.findViewById<com.google.android.material.button.MaterialButton>(R.id.adCallToAction)
        val adAppIcon = nativeAdView.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.adAppIcon)
        val adStars = nativeAdView.findViewById<RatingBar>(R.id.adStars)
        val adAdvertiser = nativeAdView.findViewById<TextView>(R.id.adAdvertiser)
        if (nativeAd.callToAction == null) {
            adCallToAction.isHidden = true
        } else {
            adCallToAction.isHidden = false
            adCallToAction.text = nativeAd.callToAction
        }
        if (nativeAd.icon == null) {
            adAppIcon.isHidden = true
        } else {
            adAppIcon.setImageDrawable(nativeAd.icon!!.drawable)
            adAppIcon.isHidden = false
        }
        nativeAdView.starRatingView = adStars
        nativeAdView.advertiserView = adAdvertiser
        if (nativeAd.starRating == null) {
            adStars.isHidden = true
            if (nativeAd.advertiser.isNullOrEmptyOrBlank()) {
                adAdvertiser.isInvisible = true
                adAdvertiser.text = ""
            } else {
                adAdvertiser.text = nativeAd.advertiser
                adAdvertiser.isInvisible = false
            }
        } else {
            adStars.isHidden = false
            adStars.rating = nativeAd.starRating?.toFloat() ?: 0f
            adAdvertiser.isInvisible = true
            adAdvertiser.text = ""
        }

        nativeAdView.setNativeAd(nativeAd)
    }

    companion object {
        fun show(
            fm: FragmentManager?,
            isForDownload: Boolean = false,
            is4DImage:Boolean = false,
            callback: (() -> Unit)? = null,
        ) {
            val manager = fm ?: return
            val dialog = CongratulationDialog()
            dialog.congratulationCallback = callback
            dialog.isForDownload = isForDownload
            dialog.canceledWhenTouchOutside = true
            dialog.is4DImage = is4DImage
            dialog.show(manager, "CongratulationDialog")
        }
    }
}