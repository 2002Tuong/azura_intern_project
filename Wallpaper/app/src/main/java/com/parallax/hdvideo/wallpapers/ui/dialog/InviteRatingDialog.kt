package com.parallax.hdvideo.wallpapers.ui.dialog

import android.view.View
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.PopupInviteRateAppBinding
import com.parallax.hdvideo.wallpapers.extension.fromHtml
import com.parallax.hdvideo.wallpapers.services.log.EventRateApp
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.ui.base.dialog.BaseDialogFragment
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration

class InviteRatingDialog: BaseDialogFragment() {

    override var canceledWhenTouchOutside: Boolean = true
    override var dimension: Float = 0.7F
    override var resLayoutId: Int =  R.layout.popup_invite_rate_app
    private lateinit var binding: PopupInviteRateAppBinding
    override fun init(view: View) {
        super.init(view)
        binding = PopupInviteRateAppBinding.bind(view)
        view.layoutParams.apply {
            width = (AppConfiguration.displayMetrics.widthPixels * 0.8).toInt()
        }
        val appName = "<b>" + resources.getString(R.string.app_name) + "</b>"
        val inviteMessage = String.format(resources.getString(R.string.popup_invite_rate_titles),appName)
            .replace("\n", "<br/> <br/>")
        binding.txtInviteMsg.text = inviteMessage.fromHtml
        binding.btnLikeInvite.setOnClickListener {
            RatingDialog().show(requireActivity().supportFragmentManager, RatingDialog::class.java.name)
            TrackingSupport.recordEvent(EventRateApp.ClickedLike)
            dismiss()
        }

        binding.btnDislikeInvite.setOnClickListener {
            TrackingSupport.recordEvent(EventRateApp.ClickedDislike)
            dismiss()
        }
    }
}