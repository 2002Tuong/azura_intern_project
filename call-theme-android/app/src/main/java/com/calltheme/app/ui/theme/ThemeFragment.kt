package com.calltheme.app.ui.theme

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewbinding.ViewBinding
import com.calltheme.app.ui.activity.MainActivity
import com.calltheme.app.ui.base.BaseFragment
import com.calltheme.app.utils.AdsUtils
import com.google.android.material.chip.Chip
import com.screentheme.app.R
import com.screentheme.app.data.remote.config.AppRemoteConfig
import com.screentheme.app.databinding.FragmentThemeBinding
import com.screentheme.app.models.RemoteTheme
import com.screentheme.app.utils.extensions.FragmentDirections
import com.screentheme.app.utils.helpers.BillingClientProvider
import com.screentheme.app.utils.helpers.CountTimeHelper
import com.screentheme.app.utils.helpers.localScreenRemoteThemes
import org.koin.android.ext.android.inject

class ThemeFragment(
    private var listSelectedCategory: MutableList<String>
) : BaseFragment() {

    companion object {
        const val ALL_THEME = "All Theme"
    }

    private var _binding: FragmentThemeBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ThemeAdapter
    private var localThemes = localScreenRemoteThemes
    private val scrollPosition = hashMapOf<String, Int>()
    private var currentChip: String? = null
    private var source: RemoteTheme? = null
    private var argumentRead = false
    private var firstSetupApp: Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!argumentRead) {
            requireActivity().intent?.extras?.let {
                val source: RemoteTheme? = it.getParcelable("SOURCE")
                this.source = source
            }
            argumentRead = true
        }

        Log.d("Category", "call on create")
        Log.d("Category", "list theme category ${listSelectedCategory.toString()}")

    }
    override fun getViewBinding(): ViewBinding {
        _binding = FragmentThemeBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {

        setupRecyclerView()

        myActivity?.let {
            AdsUtils.loadInterTheme(it)
            AdsUtils.loadInterCategory(it)}
        binding.apply {
            goProButton.visibility = View.GONE
            goProButton.setOnClickListener {
                try {
                    getNavController().navigate(R.id.action_navigation_home_to_navigation_subscription)
                } catch (exception: Exception) {

                }

            }
        }
        if (source != null) {
            validateTheme(source!!)
        }
        AdsUtils.requestNativeSetCallTheme(requireActivity())
    }

    override fun registerObservers() {
        val themes =
            themeManager.getResources(AppRemoteConfig.callThemeConfigs(), RemoteTheme::class.java) as ArrayList<RemoteTheme>

        adapter.setOriginalItems(themes)
        adapter.updateItems(themes)
        filterThemeAccordingToCategories(listSelectedCategory)
        setUpCategoryChipGroup()

        AdsUtils.triggerRebind.observe(viewLifecycleOwner) {
            if (it && adapter.isWaitingToLoadAds) {
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun setUpCategoryChipGroup() {
        val themeCategories = listOf(ALL_THEME)
            .plus(
                AppRemoteConfig
                    .callThemeConfigs()
                    .screen_themes
                    .plus(localThemes)
                    .map { it.category }
                    .distinct())
        themeCategories.forEach { scrollPosition[it] = 0 }


        _binding?.categoryChipGroup?.visibility =
            if (themeCategories.isNotEmpty()) View.VISIBLE else View.GONE

        _binding?.categoryChipGroup?.let { chipGroup ->

            themeCategories.forEach {
                val chip = (layoutInflater.inflate(
                    R.layout.category_chip_layout,
                    chipGroup,
                    false
                ) as Chip).apply {
                    setChipBackgroundColorResource(R.color.color_chip_selector)
                    setTextColor(
                        resources.getColorStateList(
                            R.color.color_chip_text_selector,
                            null
                        )
                    )
                    text = it
                    isCheckable = true
                    setOnCheckedChangeListener(null)
                    setOnCheckedChangeListener { view, isCheck ->
                        if (isCheck) {
                            Log.d("Category", "Call ")
                            if (chipGroup.indexOfChild(this) == 0) {
                                chipGroup.children
                                    .filter { item -> chipGroup.indexOfChild(item) != 0 }
                                    .forEach { item -> (item as? Chip)?.isChecked = false }
                                (chipGroup.getChildAt(0) as? Chip)?.run {
                                    isClickable = false
                                }

                            } else {
                                (chipGroup.getChildAt(0) as? Chip)?.run {
                                    isChecked = false
                                    isClickable = true
                                }
                                chipGroup.children
                                    .filter { item -> item != view }
                                    .forEach { item ->
                                        (item as? Chip)?.isChecked = false
                                        (item as? Chip)?.isClickable = true
                                    }
                            }
                            currentChip = it

                        } else if (chipGroup.indexOfChild(view) != 0) {
                            if (!chipGroup.children.any { item -> (item as? Chip)?.isChecked == true }) {
                                (chipGroup.getChildAt(0) as? Chip)?.isChecked = true
                            }
                        }
                        val selectedCategories = mutableListOf<String>()
                        chipGroup.children.forEach {
                            if ((it as Chip).isChecked) selectedCategories.add(it.text.toString())
                        }

                        if(firstSetupApp) {
                            firstSetupApp = false
                            return@setOnCheckedChangeListener
                        }
                        val activity = requireActivity() as MainActivity
                        AdsUtils.requestNativeHome(activity, true)
                        if(activity.interSelectCategoryEnable  ) {
                            myActivity?.let {
                                AdsUtils.forceShowInterCategory(it) {
                                    //filterThemeAccordingToCategories(listSelectedCategory)
                                    activity.interSelectCategoryEnable = false
                                    activity.startCategoryTimer()

                                    val bundle = Bundle()
                                    bundle.putStringArrayList("selected_categories", ArrayList(selectedCategories))

                                    val action = FragmentDirections.action(
                                        bundle,
                                        R.id.action_navigation_home_self
                                    )
                                    (it as MainActivity).navController.navigate(action)
                                }
                            }
                        } else {
//                            filterThemeAccordingToCategories(listSelectedCategory)
                            val bundle = Bundle()
                            bundle.putStringArrayList("selected_categories", ArrayList(selectedCategories))

                            val action = FragmentDirections.action(
                                bundle,
                                R.id.action_navigation_home_self
                            )
                            findNavController().navigate(action)

                        }

                    }
                }
                chipGroup.addView(chip)
            }

            if(listSelectedCategory.isNotEmpty()) {
                val clickChip = chipGroup.children.first {
                        item -> (item as? Chip)?.text == listSelectedCategory[0]
                }
                (clickChip as? Chip)?.isChecked = true
                (clickChip as? Chip)?.isClickable = false
            } else {
                val allThemeId = chipGroup.getChildAt(0) as? Chip
                allThemeId?.isChecked = true
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ThemeAdapter()
        adapter.setCallback(object : ThemeAdapterCallback {
            override fun onItemClicked(theme: RemoteTheme) {
                validateTheme(theme)
            }
        })

        binding.apply {
            val layoutManager = GridLayoutManager(context, 2)
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(pos: Int): Int {
                    return when (adapter.getItemViewType(pos)) {
                        ThemeAdapter.ADS -> {
                            2
                        }
                        else -> {
                            1
                        }
                    }
                }
            }
            recyclerView.layoutManager = layoutManager

            recyclerView.adapter = adapter
            recyclerView.setHasFixedSize(true)
            adapter.updateItems(localThemes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun validateTheme(theme: RemoteTheme) {
        if (BillingClientProvider.getInstance(requireActivity()).isPurchased) {
            goToTheme(theme)
        } else {
            when {
                theme.isPremium ->
                    try {
                        findNavController().navigate(R.id.action_navigation_home_to_navigation_subscription)
                    } catch (exception: Exception) {

                    }

                else -> {
                    goToTheme(theme)
                }
            }
        }
    }

    private fun goToTheme(theme: RemoteTheme) {
        if (!loadingDialog.isAdded)
            loadingDialog.show(requireActivity().supportFragmentManager, "loading_dialog")

        themeManager.downloadThemeImages(theme) {
            if (it) {
                themeManager.saveThemeIdToPref(theme.id)

                requireActivity().runOnUiThread {
                    val savedTheme = themeManager
                        .getThemeConfig(themeId = theme.id)

                    loadingDialog.dismiss()

                    val args = Bundle()
                    args.putParcelable("themeConfig", savedTheme)

                    val action = FragmentDirections.action(
                        args,
                        R.id.action_navigation_home_to_navigation_set_call_theme
                    )
                    try {
                        myActivity?.let { mActivity ->
                            AdsUtils.forceShowInterTheme(mActivity) {
                                findNavController().navigate(action)
                            }
                        }
                    } catch (e: IllegalArgumentException) {

                    }
                }

            }

        }
    }

    private fun filterThemeAccordingToCategories(selectedCategories: List<String>) {
        if (selectedCategories.contains(ALL_THEME)) {
            adapter.updateItems(adapter.getOriginalItems())
            return
        }

        if (selectedCategories.isNotEmpty()) {
            val filteredThemes = adapter.getOriginalItems().filter { theme ->
                theme.category.let {
                    selectedCategories.contains(it)
                }
            }
            adapter.updateItems(ArrayList(filteredThemes))
        } else {
            adapter.updateItems(adapter.getOriginalItems())
        }
    }

}

