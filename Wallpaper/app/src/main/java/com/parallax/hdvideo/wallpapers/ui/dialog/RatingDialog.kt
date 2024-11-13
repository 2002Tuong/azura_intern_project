package com.parallax.hdvideo.wallpapers.ui.dialog

import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.PopupRatingBinding
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.services.log.EventRateApp
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.ui.base.dialog.BaseDialogFragment
import com.parallax.hdvideo.wallpapers.ui.custom.RatingBar
import com.parallax.hdvideo.wallpapers.utils.dpToPx
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.reflect.jvm.jvmName

@AndroidEntryPoint
class RatingDialog: BaseDialogFragment() {

    private val listId = arrayOf(
        R.string.rating_hate_status,
        R.string.rating_dislike_status, R.string.rating_just_ok_status,
        R.string.rating_like_status, R.string.rating_very_like_status
    )
    private val arrayColors = arrayOf(
        R.color.colorHate, R.color.colorHate,
        R.color.colorNormal, R.color.colorVeryNice, R.color.colorVeryNice
    )
    private val listIdImage = arrayOf(
        R.drawable.img_hate,
        R.drawable.img_dislike, R.drawable.img_normal,
        R.drawable.img_very_like, R.drawable.img_very_like
    )

    override var resLayoutId: Int = R.layout.popup_rating

    override var canceledWhenTouchOutside: Boolean = true
    private lateinit var ratingBtn: Button
    private lateinit var ratingBar: RatingBar
    private lateinit var textView: TextView
    private lateinit var binding: PopupRatingBinding

    @Inject
    lateinit var localStorage: LocalStorage

    override fun init(view: View) {
        super.init(view)
        binding = PopupRatingBinding.bind(view)
        ratingBtn = binding.ratingButton
        setEnableButton(false)
        ratingBtn.setOnClickListener {
            if (ratingBar.rating < 5) {
                FeedbackDialog(ratingBar.rating).show(
                    requireActivity().supportFragmentManager,
                    FeedbackDialog::class.jvmName
                )
                TrackingSupport.recordEvent(EventRateApp.ShowFeedback)
            } else {
                GotoStoreDialog().show(
                    requireActivity().supportFragmentManager,
                    GotoStoreDialog::class.jvmName
                )
                localStorage.ratingApp = true
            }
            TrackingSupport.recordRateAppEvent(EventRateApp.E2RatePopupRate, ratingBar.rating)
            localStorage.countRating = ratingBar.rating
            dismiss()
        }
        binding.btnExit.setOnClickListener {
            TrackingSupport.recordEvent(EventRateApp.ClickedNotNow)
            dismiss()
        }
        textView = binding.tvTitle
        textView.text = getString(R.string.popup_rate_msg).plus("\n\n")
            .plus(getString(R.string.popup_rate_msg_2))
        ratingBar = binding.ratingBar

        ratingBar.onClickedStarCallback = { rating ->
            val index = rating - 1
            textView.gravity = Gravity.CENTER
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            textView.setTextColor(ContextCompat.getColor(requireContext(), arrayColors[index]))
            textView.setText(listId[index])
            binding.ivEmotionRating.isHidden = false
            binding.ivEmotionRating.setImageResource(listIdImage[index])
            setEnableButton(true)
            binding.ivHand.x = ratingBar.getChildAt(rating - 1).x + dpToPx(33f) * 2
        }
    }

    private fun setEnableButton(isEnabled: Boolean) {
        ratingBtn.isEnabled = isEnabled
        ratingBtn.alpha = if (isEnabled) 1f else 0.5f
    }

    override fun onStart() {
        super.onStart()
        val layoutParams = dialog?.window?.attributes
        if (layoutParams != null) {
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        }
        if (layoutParams != null) {
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        }
        dialog?.window?.attributes = layoutParams
    }

}