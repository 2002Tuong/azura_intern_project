package com.calltheme.app.ui.language

import androidx.viewbinding.ViewBinding
import com.calltheme.app.ui.base.BaseFragment
import com.screentheme.app.databinding.FragmentChooseLanguageBinding

class ChooseLanguageFragment : BaseFragment() {

    private lateinit var viewModel: ChooseLanguageViewModel
    private lateinit var binding: FragmentChooseLanguageBinding

    override fun getViewBinding(): ViewBinding {
        binding = FragmentChooseLanguageBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {
    }

    override fun registerObservers() {
    }

}