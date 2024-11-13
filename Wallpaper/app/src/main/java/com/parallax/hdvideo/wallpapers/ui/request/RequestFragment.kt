package com.parallax.hdvideo.wallpapers.ui.request

import android.annotation.SuppressLint
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.FragmentRequestScreenBinding
import com.parallax.hdvideo.wallpapers.extension.isNullOrEmptyOrBlank
import com.parallax.hdvideo.wallpapers.services.log.EventSetting
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragmentBinding
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RequestFragment : BaseFragmentBinding<FragmentRequestScreenBinding, RequestViewModel>() {
    override val resLayoutId: Int
        get() = R.layout.fragment_request_screen

    private lateinit var listTextField : List<EditText>
    private lateinit var binding: FragmentRequestScreenBinding
    @SuppressLint("SetTextI18n")
    override fun init(view: View) {
        super.init(view)
        binding = FragmentRequestScreenBinding.bind(view)
        binding.llContainer.setOnClickListener { hideSoftKeyBoard() }
        delayedAmount = 300
        binding.emailLabel.text = "" + binding.emailLabel.text + "*"
        binding.subjectLabel.text = "" + binding.subjectLabel.text + "*"
        binding.appBarLayout.layoutParams.height =
            AppConfiguration.statusBarSize + resources.getDimensionPixelOffset(R.dimen.height_header)
        listTextField = listOf(
            dataBinding.emailEDT.editText!!,
            dataBinding.subjectEDT.editText!!,
            dataBinding.detailedInfoEDT.editText!!
        )
        dataBinding.submitButton.setOnClickListener {
            val canProcess = dataBinding.emailEDT.error == null &&
                    !(dataBinding.emailEDT.editText?.text.toString().isNullOrEmptyOrBlank()) &&
                    !(dataBinding.subjectEDT.editText?.text.toString().isNullOrEmptyOrBlank()) &&
                    dataBinding.subjectEDT.error == null
            if (canProcess) {
                viewModel.submit(
                    dataBinding.emailEDT.editText?.text.toString(),
                    dataBinding.subjectEDT.editText?.text.toString(),
                    dataBinding.detailedInfoEDT.editText?.text.toString()
                )
            } else {
                if (dataBinding.emailEDT.error == null &&
                    dataBinding.emailEDT.editText?.text.toString().isNullOrEmptyOrBlank())
                    dataBinding.emailEDT.error = getString(R.string.enter_email_invalid_message)
                if (dataBinding.subjectEDT.error == null
                    && dataBinding.subjectEDT.editText?.text.toString().isNullOrEmptyOrBlank()
                )
                    dataBinding.subjectEDT.error = getString(R.string.enter_subject_empty_error_message)
            }
        }

        dataBinding.backButton.setOnClickListener {
            onBackPressed()
        }
        dataBinding.emailEDT.editText?.doOnTextChanged { text, _, _, _ ->
            updateView()
            dataBinding.emailEDT.error =
                if (text.toString().isNullOrEmptyOrBlank() || !text.toString().isNullOrEmptyOrBlank() && android.util.Patterns
                        .EMAIL_ADDRESS
                        .matcher(text)
                        .matches()
                )
                    null
                else
                    getString(R.string.enter_email_invalid_message)
        }
        dataBinding.subjectEDT.editText?.doOnTextChanged { text, _, _, _ ->
            updateView()
            dataBinding.subjectEDT.error = if (text.toString().isNullOrEmptyOrBlank())
                getString(R.string.enter_subject_empty_error_message)
            else
                null
        }
        val email = viewModel.emailRequest
        if (email.isEmpty()) {
            dataBinding.emailEDT.editText!!.requestFocus()
        } else {
            dataBinding.emailEDT.editText!!.setText(email)
            dataBinding.subjectEDT.editText!!.requestFocus()
        }
        postDelayed(this::initDelayed, delayedAmount)
        TrackingSupport.recordEvent(EventSetting.RequestNewWallpaper)
    }


    private fun updateView() {
        if (isValidate()) {
            dataBinding.submitButton.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_btn_send_request_re)
            dataBinding.submitButton.isEnabled = true
        } else {
            dataBinding.submitButton.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.bg_send_request_disable_re)
            dataBinding.submitButton.isEnabled = false
        }
    }

    private fun isValidate() = dataBinding.emailEDT.error == null &&
            !(dataBinding.emailEDT.editText?.text.toString().isNullOrEmptyOrBlank()) &&
            !(dataBinding.subjectEDT.editText?.text.toString().isNullOrEmptyOrBlank()) &&
            dataBinding.subjectEDT.error == null

    override fun showToast(id: Int) {
        super.showToast(id)
        onBackPressed()
    }

    override fun onBackPressed(): Boolean {
        hiddenKeyboard()
        return super.onBackPressed()
    }

    private fun initDelayed() {
        showKeyboard()
    }

    override fun onStop() {
        super.onStop()
        hideSoftKeyBoard()
    }

    companion object {
        const val TAGS = "RequestFragment"
    }

}
