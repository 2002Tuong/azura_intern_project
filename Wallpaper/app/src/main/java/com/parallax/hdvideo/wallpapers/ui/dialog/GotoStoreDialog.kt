package com.parallax.hdvideo.wallpapers.ui.dialog

import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.PopupRatingOnStoreBinding
import com.parallax.hdvideo.wallpapers.extension.IntentSupporter
import com.parallax.hdvideo.wallpapers.services.log.EventRateApp
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.ui.base.dialog.BaseDialogFragment
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration


class GotoStoreDialog: BaseDialogFragment() {

    override var canceledWhenTouchOutside: Boolean = true
    override var resLayoutId: Int = R.layout.popup_rating_on_store
    override var dimension: Float = 0.7F
    private lateinit var binding: PopupRatingOnStoreBinding
    override fun init(view: View) {
        super.init(view)
        binding = PopupRatingOnStoreBinding.bind(view)
        binding.textView.text = getString(R.string.popup_rate_play_store_msg)
        binding.ratingButton.setOnClickListener {
            TrackingSupport.recordEvent(EventRateApp.OpenStore)
            IntentSupporter.openById(requireContext().packageName)
            dismiss()
        }
        binding.btnOk.setOnClickListener {
            dismiss()
        }
        Toast.makeText(requireActivity(), R.string.thanks_for_using, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        setLayout(AppConfiguration.widthScreenValue * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}