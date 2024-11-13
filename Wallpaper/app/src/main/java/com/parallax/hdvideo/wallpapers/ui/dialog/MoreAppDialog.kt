package com.parallax.hdvideo.wallpapers.ui.dialog

import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.ads.AdsManager
import com.parallax.hdvideo.wallpapers.data.model.MoreAppModel
import com.parallax.hdvideo.wallpapers.databinding.ItemSuggestionAppDialogBinding
import com.parallax.hdvideo.wallpapers.databinding.PopupMoreAppBinding
import com.parallax.hdvideo.wallpapers.extension.IntentSupporter
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.ui.base.adapter.BaseAdapterList
import com.parallax.hdvideo.wallpapers.ui.base.dialog.BaseDialogFragment
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.dp2Px
import kotlin.math.ceil

class MoreAppDialog: BaseDialogFragment() {

    override val resLayoutId: Int
        get() = R.layout.popup_more_app

    private val moreAppAdapter =  BaseAdapterList<MoreAppModel, ItemSuggestionAppDialogBinding>({ R.layout.item_suggestion_app_dialog },
        onBind = { binding, model, _ -> binding.item = model })
    private var callbackOnClickYes: (() -> Unit)? = null
    private val heightOfItem = 94f
    private val spanCount = 2
    private var reloadNativeAd = false
    private val _nativeAdLiveData = AdsManager.loadNativeAdOnExitDialog()
    private lateinit var binding: PopupMoreAppBinding
    override fun init(view: View) {
        view.alpha = 0f
        binding = PopupMoreAppBinding.bind(view)
        view.animate().alpha(1f).setStartDelay(100).setDuration(300).start()
        binding.yesBtn.setOnClickListener {
            reloadNativeAd = false
            callbackOnClickYes?.invoke()
        }
        binding.noBtn.setOnClickListener {
            reloadNativeAd = true
            dismiss()
        }
        if(!moreAppAdapter.dataEmpty) {
            val moreAppRecyclerView = binding.dialogMoreAppRecycleView
            val layoutManager = GridLayoutManager(view.context, spanCount, GridLayoutManager.VERTICAL, false)
            moreAppRecyclerView.setHasFixedSize(true)
            moreAppRecyclerView.layoutManager = layoutManager
            moreAppAdapter.onClickedItemcallBack = { _, data ->
                IntentSupporter.openById(data.url)
                dismiss()
            }

        } else {
            binding.lineView2.isHidden = true
            binding.lineView1.isHidden = true
            binding.dialogMoreAppRecycleView.isHidden = true
            val textView = binding.dialogMoreAppTitle
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            textView.setText(R.string.app_name)
        }
        view.doOnPreDraw {
            viewDidLoad(it)
        }
    }

    private fun viewDidLoad(view: View) {
        if (!moreAppAdapter.dataEmpty) {
            val moreAppRecyclerView = binding.dialogMoreAppRecycleView
            val maxHeightRecyclerView = AppConfiguration.heightScreenValue * 45 / 100 - view.findViewById<NativeAdView>(R.id.nativeAdView).height
            if (ceil(moreAppAdapter.dataSize * dp2Px(heightOfItem) / 2f) > maxHeightRecyclerView) {
                val layoutParams = moreAppRecyclerView.layoutParams
                layoutParams.height = maxHeightRecyclerView
                moreAppRecyclerView.layoutParams = layoutParams
            }
            moreAppRecyclerView.adapter = moreAppAdapter
        }
        if (!AdsManager.isVipUser)
            _nativeAdLiveData.observe(this, {
                bindDataNativeView(rootView?.findViewById(R.id.nativeAdView), it)
            })
        else view.findViewById<NativeAdView>(R.id.nativeAdView).isHidden = true
    }

    private fun bindDataNativeView(nativeView: NativeAdView?, nativeAd: NativeAd?) {
        if (nativeView == null) return
        if (nativeAd != null) {
            nativeView.starRatingView = nativeView.findViewById<RatingBar>(R.id.adStars).apply {
                val starRate = nativeAd.starRating
                if (starRate == null) {
                    isHidden = true
                } else {
                    rating = starRate.toFloat()
                }
            }
            nativeView.headlineView = nativeView.findViewById<TextView>(R.id.adHeadline).apply {
                text = nativeAd.headline
            }
            nativeView.iconView = nativeView.findViewById<com.google.android.material.imageview.ShapeableImageView>(R.id.adAppIcon).apply {
                setImageDrawable(nativeAd.icon?.drawable)
            }
            nativeView.callToActionView = nativeView.findViewById<TextView>(R.id.adCallToAction).apply {
                text = nativeAd.callToAction
            }
            nativeView.setNativeAd(nativeAd)
        }
    }
    override fun onStart() {
        super.onStart()
        setLayout(AppConfiguration.widthScreenValue * 85 / 100, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        _nativeAdLiveData.removeObservers(this)
        AdsManager.destroyNativeAdOnExitDialog()
        if (reloadNativeAd) AdsManager.loadNativeAdOnExitDialog()
        rootView?.findViewById<NativeAdView>(R.id.nativeAdView)?.destroy()
        super.onDestroyView()
    }

    override fun onDetach() {
        callbackOnClickYes = null
        super.onDetach()
    }

    companion object {
        fun show(manger: FragmentManager,
                 data: List<MoreAppModel>,
                 callbackYes: (() -> Unit)? = null) {
            synchronized(this) {
                val tag = MoreAppDialog::javaClass.name
                if (manger.findFragmentByTag(tag) != null) {
                    return
                }
                val fg = MoreAppDialog()
                fg.moreAppAdapter.setData(data)
                fg.callbackOnClickYes = callbackYes
                fg.show(manger, tag)
            }

        }
    }
}