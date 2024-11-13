package com.calltheme.app.ui.picklanguage

import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.calltheme.app.ui.base.BaseFragment
import com.screentheme.app.databinding.FragmentPickLanguageBinding
import com.screentheme.app.utils.extensions.getLanguage
import com.screentheme.app.utils.helpers.SharePreferenceHelper
import org.koin.android.ext.android.inject


class UpdateLanguageFragment : BaseFragment() {

    private lateinit var binding: FragmentPickLanguageBinding

    private lateinit var adapter: LanguageAdapter


    override fun getViewBinding(): ViewBinding {
        binding = FragmentPickLanguageBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {
        adapter = LanguageAdapter(requireContext()) {}
        adapter.onItemClicked { language ->
            pickLanguageViewModel.languageLiveData.postValue(language)
        }

        binding.apply {
            languagesRecyclerView.adapter = adapter
            languagesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            goBackButton.setOnClickListener {
                findNavController().popBackStack()
            }

            confirmButton.setOnClickListener {

                setFragmentResult(PickLanguageFragment.REQUEST_PICKUP_LANGUAGE_KEY, bundleOf("language" to pickLanguageViewModel.languageLiveData.value))

                findNavController().navigateUp()
            }
        }

    }

    override fun registerObservers() {
        pickLanguageViewModel.listOfAllLanguages.observe(this) {
            adapter.updateItems(it)
            if (it.isNotEmpty()) {

                val currentAppLanguage = SharePreferenceHelper.getLanguage(requireContext())

                if (currentAppLanguage != null) {
                    adapter.setSelectedLanguage(currentAppLanguage)
                }
            }
        }

        pickLanguageViewModel.languageLiveData.observe(this) {
        }
    }

}