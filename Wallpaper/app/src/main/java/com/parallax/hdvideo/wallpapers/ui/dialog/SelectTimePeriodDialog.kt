package com.parallax.hdvideo.wallpapers.ui.dialog

import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.FragmentManager
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.PopupTimePeriodSelectorBinding
import com.parallax.hdvideo.wallpapers.ui.base.dialog.BaseDialogFragment
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration

class SelectTimePeriodDialog : BaseDialogFragment() {

    override var canceledWhenTouchOutside: Boolean = true

    override var resLayoutId: Int = R.layout.popup_time_period_selector
    override var dimension: Float = 0.7F
    private var delayedTime: Int? = null

    private var dialogCallback: ((text: String, duration: Int) -> Unit)? = null

    private val listIdTime = arrayOf(R.id.option15Minutes, R.id.option30Minutes,
        R.id.option1Hour, R.id.option2Hours,
        R.id.option6Hours, R.id.option12Hours,
        R.id.option1Day, R.id.option3Days)
    private lateinit var binding: PopupTimePeriodSelectorBinding
    override fun init(view: View) {
        super.init(view)
        binding = PopupTimePeriodSelectorBinding.bind(view)
        binding.dialogConfirmTitle.text = getString(R.string.title_wallpaper_automatically_change_time)
        binding.confirmButton.setOnClickListener {
            val duration = listDurations[listIdTime.indexOf(binding.radioOptions.checkedRadioButtonId)]
            val text = view.findViewById<RadioButton>(binding.radioOptions.checkedRadioButtonId).text.toString()
            dialogCallback?.invoke(text, duration)
            dismiss()
        }
        binding.radioOptions.check(listIdTime[listDurations.indexOf(delayedTime ?: 0)])
    }

    override fun onStart() {
        super.onStart()
        setLayout(
            (AppConfiguration.widthScreenValue * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        fun show(fm: FragmentManager?, delay: Int, callback: (text: String, duration: Int) -> Unit) {
            val tag = SelectTimePeriodDialog::class.java.name
            val manager = fm ?: return
            synchronized(tag) {
                if (manager.findFragmentByTag(tag) != null) return
                val timePeriodDialog = SelectTimePeriodDialog()
                timePeriodDialog.delayedTime = delay
                timePeriodDialog.dialogCallback = callback
                timePeriodDialog.show(fm, tag)
            }
        }

        val listDurations = arrayOf(15, 30, 60, 120, 360, 720, 1440, 4320)
    }
}