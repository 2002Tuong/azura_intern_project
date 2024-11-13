package com.parallax.hdvideo.wallpapers.ui.dialog

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.PopupFeedbackBinding
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.services.log.EventRateApp
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.ui.base.dialog.BaseDialogFragment
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import java.text.DecimalFormat

class FeedbackDialog constructor(private val rating: Int) : BaseDialogFragment() {

    override var canceledWhenTouchOutside: Boolean = false
    override var resLayoutId: Int = R.layout.popup_feedback
    override var dimension: Float = 0.7F
    private lateinit var binding: PopupFeedbackBinding
    private val arrayCheckBox by lazy {
        val view = rootView ?: return@lazy arrayOf<CheckBox>()
        return@lazy arrayOf<CheckBox>(
            binding.checkBox1,
            binding.checkBox2, binding.checkBox3, binding.checkBox5
        )
    }
    lateinit var editText: EditText

    override fun init(view: View) {
        super.init(view)
        binding = PopupFeedbackBinding.bind(view)
        editText = binding.editText
        binding.ratingButton.setOnClickListener {
            (requireActivity() as MainActivity).viewModel.sendFeedback(getFeedbackForm())
            handleFeedBack(view)
            TrackingSupport.recordEvent(EventRateApp.SubmitFeedback)
            dismiss()
        }
        binding.btnOk.setOnClickListener {
            dismiss()
        }

        binding.checkBox2.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.detailForCheckBox2.isHidden = !isChecked
        }

        binding.checkBox3.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.editText.isHidden = isChecked
        }
    }

    private fun handleFeedBack(view: View) {

        if (binding.checkBox3.isChecked && !binding.checkBox4.isChecked) {
            HardUseFeedbackDialog().show(
                requireActivity().supportFragmentManager,
                HardUseFeedbackDialog::class.java.name
            )
        } else {
            RequestWallDialog.newInstance(binding.checkBox4.isChecked).show(
                requireActivity().supportFragmentManager,
                RequestWallDialog::class.java.name
            )
        }

    }

    override fun onStart() {
        super.onStart()
        setLayout(AppConfiguration.widthScreenValue * 80 / 100, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun getFeedbackForm(): String {
        val nameOfApp = getString(R.string.app_name)
        var vn = "Unknown"
        var verCode = -1L
        try {
            requireContext().packageManager?.getPackageInfo(requireContext().packageName, 0)
                ?.apply {
                    vn = versionName
                    verCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        longVersionCode
                    } else this.versionCode.toLong()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val starFeedbackText: String = rating.toString() + if (rating == 1) " star" else " stars"
        val defaultFeedback = arrayCheckBox.filter { it.isChecked }
            .fold("") { acc, checkbox -> acc + "\n" + checkbox.text }
        val feedbackForm = resources.getString(
            R.string.feedback_form, nameOfApp, vn,
            verCode, starFeedbackText, rating.toString(),
            defaultFeedback, editText.text.toString()
        )

        // Memory info
        val activityManager =
            requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalMem = memoryInfo.totalMem
        val freeMem = memoryInfo.availMem

        val deviceInfo = "\nVERSION.RELEASE : ${Build.VERSION.RELEASE}" +
                "\nVERSION.SDK.NUMBER : ${Build.VERSION.SDK_INT}" +
                "\nCPU : " + Build.SUPPORTED_ABIS.reduce { acc, s -> "$acc;$s" } +
                "\nHardware : ${Build.HARDWARE}" +
                "\nModel : ${Build.MODEL}" +
                "\nManufacturer : ${Build.MANUFACTURER}" +
                "\nScreen size : ${AppConfiguration.widthScreenValue}x${AppConfiguration.heightScreenValue}" +
                "\nScreen density : ${AppConfiguration.displayMetrics.density}" +
                "\nTotal memory : ${getMemorySizeHumanized(totalMem)}" +
                "\nFree memory : ${getMemorySizeHumanized(freeMem)}" +
                "\nTotal internal storage : ${getTotalInternalMemorySize()}" +
                "\nFree internal storage : ${getAvailableInternalMemorySize()}"
        return feedbackForm + deviceInfo
    }

    private fun getAvailableInternalMemorySize(): String {
        val dir = Environment.getDataDirectory()
        val stat = StatFs(dir.path)
        val blockSizeInLong = stat.blockSizeLong
        val availableBlocksInLong = stat.availableBlocksLong
        return getMemorySizeHumanized(availableBlocksInLong * blockSizeInLong)
    }

    private fun getTotalInternalMemorySize(): String {
        val dir = Environment.getDataDirectory()
        val stat = StatFs(dir.path)
        val blockSizeInLong = stat.blockSizeLong
        val totalBlocksInLong = stat.blockCountLong
        return getMemorySizeHumanized(totalBlocksInLong * blockSizeInLong)
    }

    private fun getMemorySizeHumanized(memory: Long): String {
        val twoDecimalForm = DecimalFormat("#.##")
        val kb = memory / 1024.0
        val mb = memory / 1048576.0
        val gb = memory / 1073741824.0
        val tb = memory / 1099511627776.0
        return when {
            tb > 1 -> twoDecimalForm.format(tb) + " TB"
            gb > 1 -> twoDecimalForm.format(gb) + " GB"
            mb > 1 -> twoDecimalForm.format(mb) + " MB"
            kb > 1 -> twoDecimalForm.format(mb) + " KB"
            else -> twoDecimalForm.format(memory) + " Bytes"
        }
    }

}