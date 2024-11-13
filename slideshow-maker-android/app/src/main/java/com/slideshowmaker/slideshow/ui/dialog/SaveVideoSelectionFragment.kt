package com.slideshowmaker.slideshow.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.data.RemoteConfigRepository
import com.slideshowmaker.slideshow.data.local.SharedPreferUtils
import com.slideshowmaker.slideshow.databinding.SaveVideoSelectionPopupBinding
import com.slideshowmaker.slideshow.ui.premium.PremiumActivity
import com.slideshowmaker.slideshow.utils.extentions.orFalse
import kotlinx.android.synthetic.main.save_video_selection_popup.rbStandardQuality


class ExportQualityVideoSelectionFragment : BottomSheetDialogFragment() {
    var exportSelectedHandle: ((Int, Int) -> Unit)? = null
    private var quality = QUALITY_720P
    private val onEditVideo by lazy {
        arguments?.getBoolean(ARG_IS_EDIT_VIDEO, false).orFalse()
    }

    private val qualityOption by lazy {
        listOf(
            dialogBinding.rbBestQuality,
            dialogBinding.rbStandardQuality,
            dialogBinding.rbHighQuality
        )
    }

    private val dialogBinding: SaveVideoSelectionPopupBinding by lazy {
        SaveVideoSelectionPopupBinding.inflate(layoutInflater)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetStyle)
        dialog.let {
            val sheet = it
            sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            sheet.behavior.skipCollapsed = true
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return dialogBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        eventSetup()
    }

    private fun setupUI() {
        dialogBinding.groupRatio.isVisible = onEditVideo.orFalse()
    }

    private fun eventSetup() {
        dialogBinding.ibClose.setOnClickListener { dismiss() }
        dialogBinding.vHighQuality.isVisible = !SharedPreferUtils.proUser
        dialogBinding.vStandardQuality.isVisible = !SharedPreferUtils.proUser

        dialogBinding.vStandardQuality.setOnClickListener {
            quality = QUALITY_480P
            invalidateQualityGroup()
        }

        dialogBinding.vHighQuality.setOnClickListener {
            quality = QUALITY_720P
            invalidateQualityGroup()
        }

        val highQuality =
            RemoteConfigRepository.videoQualityConfigs?.firstOrNull { it.quality == "720" }
        val normalQuality =
            RemoteConfigRepository.videoQualityConfigs?.firstOrNull { it.quality == "480" }
        quality =
            if (normalQuality?.isDefault.orFalse()) QUALITY_480P else QUALITY_720P

        dialogBinding.rbStandardQuality.isChecked = quality == QUALITY_480P
        dialogBinding.rbHighQuality.isChecked = quality == QUALITY_720P

        if (highQuality == null) {
            dialogBinding.vHighQuality.isVisible = true
            dialogBinding.textDescriptionHigh.text =
                getString(R.string.dialog_select_tier_high_quality_content_inter)
        } else {
            dialogBinding.vHighQuality.isVisible = highQuality.statusShow
            dialogBinding.textDescriptionHigh.setText(
                if (highQuality.typeAds == "inter")
                    R.string.dialog_select_tier_high_quality_content_inter else R.string.dialog_select_tier_high_quality_content
            )
        }

        dialogBinding.vStandardQuality.isVisible = normalQuality != null
        if (normalQuality != null) {
            dialogBinding.vStandardQuality.isVisible = normalQuality.statusShow
            dialogBinding.textDescriptionStandard.setText(
                if (normalQuality.typeAds == "inter")
                    R.string.dialog_select_tier_standard_quality_content else R.string.dialog_select_tier_standard_quality_content_video
            )
        }

        dialogBinding.vBestQuality.setOnClickListener {
            quality = QUALITY_1080P
            invalidateQualityGroup()
        }
        qualityOption.forEach {
            it.setOnCheckedChangeListener { _, checked ->
                if(checked) {
                    when(it.id) {
                        R.id.rbBestQuality -> quality = QUALITY_1080P
                        R.id.rbHighQuality -> quality = QUALITY_720P
                        R.id.rbStandardQuality -> quality = QUALITY_480P
                    }
                }
                invalidateQualityGroup()
            }
        }
        dialogBinding.btnExport.setOnClickListener {
            Log.d("Quality", quality.toString())
            if (quality == QUALITY_1080P && !SharedPreferUtils.proUser) {
                startActivity(
                    PremiumActivity.newIntent(
                        requireContext(),
                        PremiumActivity.ARG_SOURCE_EXPORT_KEY
                    )
                )
            } else {
                exportSelectedHandle?.invoke(quality, getSelectedRatio())
            }
            dismissAllowingStateLoss()
        }
        dialogBinding.btnCancel.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    private fun invalidateQualityGroup() {
        qualityOption.forEach { it.isChecked = false }
        when (quality) {
            QUALITY_480P -> dialogBinding.rbStandardQuality.isChecked = true
            QUALITY_720P -> dialogBinding.rbHighQuality.isChecked = true
            QUALITY_1080P -> dialogBinding.rbBestQuality.isChecked = true

        }
    }

    private fun getSelectedRatio() = when (dialogBinding.rgRatio.checkedRadioButtonId) {
        R.id.rbWideScreen -> RATIO_16_9
        R.id.rbVerticalScreen -> RATIO_9_16
        R.id.rbSquareScreen -> RATIO_1_1
        else -> RATIO_16_9
    }


    companion object {
        private const val ARG_IS_EDIT_VIDEO = "ARG_IMAGE_QUALITIES"
        internal const val TAG = "DownloadImageSelectionFragment"
        const val QUALITY_480P = 480
        const val QUALITY_720P = 720
        const val QUALITY_1080P = 1080
        const val RATIO_16_9 = 1
        const val RATIO_9_16 = 2
        const val RATIO_1_1 = 3
        fun newInstance(
            isEditVideo: Boolean
        ): ExportQualityVideoSelectionFragment {
            val args = Bundle()
            args.putBoolean(ARG_IS_EDIT_VIDEO, isEditVideo)
            val fragment = ExportQualityVideoSelectionFragment()
            fragment.arguments = args
            return fragment
        }
    }
}