package com.calltheme.app.ui.mydesign

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.calltheme.app.ui.base.BaseFragment
import com.screentheme.app.databinding.FragmentApplyThemeBinding
import com.screentheme.app.models.ThemeConfig
import org.koin.android.ext.android.inject

class ApplyThemeFragment : BaseFragment() {

    private lateinit var binding: FragmentApplyThemeBinding
    private val applyThemeViewModel: ApplyThemeViewModel by inject()

    override fun getViewBinding(): ViewBinding {
        binding = FragmentApplyThemeBinding.inflate(layoutInflater)
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {

            var themeInBundle = it.getParcelable("themeConfig") as? ThemeConfig

            if (themeInBundle != null) {
                applyThemeViewModel.themeConfigLiveData.postValue(themeInBundle)
            }
        }
    }

    override fun onViewCreated() {
    }

    override fun registerObservers() {

        applyThemeViewModel.themeConfigLiveData.observe(this) { theme ->

            binding.apply {
                Glide.with(requireActivity())
                    .load(theme.background)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(callBackground)

                Glide.with(requireActivity())
                    .load(theme.avatar)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(callerAvatar)

                Glide.with(requireActivity())
                    .load(theme.declineCallIcon)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(callEnd)

                Glide.with(requireActivity())
                    .load(theme.acceptCallIcon)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(callAccept)
            }
        }
    }

}