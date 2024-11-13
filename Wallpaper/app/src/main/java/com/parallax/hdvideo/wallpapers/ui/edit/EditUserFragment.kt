package com.parallax.hdvideo.wallpapers.ui.edit

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.FragmentEditUserScreenBinding
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.isNullOrEmptyOrBlank
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragment
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.ui.personalization.PersonalizationFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditUserFragment : BaseFragment() {

    @Inject
    lateinit var localStorage: LocalStorage
    lateinit var dataBinding: FragmentEditUserScreenBinding

    override val resLayoutId: Int
        get() = R.layout.fragment_edit_user_screen
    private lateinit var binding: FragmentEditUserScreenBinding
    override fun init(view: View) {
        super.init(view)
        binding = FragmentEditUserScreenBinding.bind(view)
        binding.llContainer.setOnClickListener { hideSoftKeyBoard() }
        dataBinding = FragmentEditUserScreenBinding.bind(view)
        dataBinding.edtName.editText?.setText(localStorage.accountName)
        dataBinding.edtLastName.editText?.setText(localStorage.lastName)
        initAction()
    }

    private fun initAction() {
        dataBinding.backButton.setOnClickListener { onBackPressed() }
        dataBinding.edtName.editText?.doOnTextChanged { text, _, _, _ ->
            updateView()
        }
        dataBinding.edtLastName.editText?.doOnTextChanged { text, _, _, _ ->
            updateView()
        }
        dataBinding.submitButton.setOnClickListener {
            if (isValidate()) {
                localStorage.accountName = dataBinding.edtName.editText?.text.toString()
                localStorage.lastName = dataBinding.edtLastName.editText?.text.toString()
                onBackPressed()
                val appSuggestionFragment =
                    (activity as MainActivity).topFragment as PersonalizationFragment
                appSuggestionFragment.updateNameProfile(localStorage.lastName.toString() + " " + localStorage.accountName.toString())
            }
        }
        dataBinding.cancelButton.setOnClickListener {
            onBackPressed()
        }
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

    private fun isValidate() = !(dataBinding.edtName.editText?.text.toString().isNullOrEmptyOrBlank() && dataBinding.edtLastName.editText?.text.toString().isNullOrEmptyOrBlank())

    override fun onStop() {
        super.onStop()
        hideSoftKeyBoard()
    }
}