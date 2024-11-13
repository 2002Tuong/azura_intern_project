package com.example.videoart.batterychargeranimation.ui.home

import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.ads.control.admob.Admob
import com.example.videoart.batterychargeranimation.MainActivity
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.data.remote.RemoteConfig
import com.example.videoart.batterychargeranimation.databinding.FragmentHomeBinding
import com.example.videoart.batterychargeranimation.extension.FragmentDirections
import com.example.videoart.batterychargeranimation.helper.ThemeManager
import com.example.videoart.batterychargeranimation.helper.localScreenRemoteThemes
import com.example.videoart.batterychargeranimation.model.RemoteTheme
import com.example.videoart.batterychargeranimation.model.Theme
import com.example.videoart.batterychargeranimation.receiver.BatteryStateReceiver
import com.example.videoart.batterychargeranimation.ui.base.BaseFragment
import com.example.videoart.batterychargeranimation.utils.AdsUtils
import com.google.android.material.chip.Chip

class HomeFragment : BaseFragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: HomeAdapter
    private lateinit var receiver: BatteryStateReceiver
    private var localThemes = localScreenRemoteThemes
    private val listTheme = mutableListOf<RemoteTheme>()

    override fun getViewBinding(): ViewBinding {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {
        setUpListItem()
        setUpRecycleView()
        setUpChip()
        AdsUtils.requestNativeHome(requireActivity())
        AdsUtils.requestNativeHomeList(requireActivity())
        AdsUtils.requestNativeBatteryInfo(requireActivity())
        binding.icHelp.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_helpFragment)
        }
        receiver = BatteryStateReceiver {
            binding.batteryState.text = "${it} %"
        }
        requireActivity().registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        binding.batteryInfoBanner.setOnClickListener {
            AdsUtils.forceShowInterBatteryInfo(requireContext()) {
                findNavController().navigate(R.id.action_mainFragment_to_batteryInfoFragment)
                AdsUtils.loadInterBatteryInfo(requireContext(), true)
            }
        }
    }

    override fun registerObservers() {
        AdsUtils.loadInterSelect(requireContext())
        AdsUtils.loadInterBatteryInfo(requireContext())
        AdsUtils.nativeHome.observe(viewLifecycleOwner) {
            adapter.apNativeAd = it
        }

        AdsUtils.nativeHomeLoadFail.observe(viewLifecycleOwner) {
            if(it) {
                adapter.apNativeAd = null
            }
        }

        AdsUtils.triggerRebind.observe(viewLifecycleOwner) {
            if(it && adapter.isWaitingToLoadAds) {
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(receiver)
    }

    private fun setUpListItem() {
        //get from remote
        val listRemote = RemoteConfig.remoteThemes
        val listCategory = RemoteConfig.category
        Log.d("remoteConfig", "${listRemote?.size}")
        Log.d("remoteConfig", "${listCategory?.size}")
        //local
        listTheme.addAll(listRemote ?: emptyList())
        listTheme.addAll(localThemes)
    }
    private fun setUpRecycleView() {
        adapter = HomeAdapter(requireActivity())
        adapter.setCallback(object : ThemeAdapterCallback {
            override fun onItemClicked(theme: RemoteTheme) {
                val isExist = ThemeManager.getInstance(requireContext()).themeFolderExists(themeId = theme.id)
                if(isExist) {
                    val temp = ThemeManager.getInstance(requireContext()).getTheme(theme.id)
                    goToUnlock(temp)
                }else {
                    goToPreview(theme)
                }
            }
        })

        adapter.updateItems(listTheme)
        adapter.setOriginalItems(listTheme)

        binding.apply {
            recyclerView.layoutManager =LinearLayoutManager(context)
            recyclerView.adapter = adapter
            recyclerView.setHasFixedSize(true)

        }
    }

    private fun setUpChip() {
        val categories = listOf(ALL_THEME)
            .plus(RemoteConfig.category!!.map { it.name }.distinct())
        Log.d("Chip", "${categories}")
        binding.categoryChipGroup.isVisible = categories.isNotEmpty()
        binding.categoryChipGroup.let {chipGroup ->
            categories.forEach {
                val chip = (layoutInflater.inflate(
                    R.layout.category_chip_layout,
                    chipGroup,
                    false
                ) as Chip).apply {
                    setChipBackgroundColorResource(R.color.chip_color_selector)
                    setTextColor(
                        resources.getColorStateList(
                            R.color.chip_text_color_selector,
                            null
                        )
                    )
                    setChipStrokeColorResource(R.color.chip_stroke_color_selector)
                    text = it
                    isCheckable = true
                    setOnCheckedChangeListener(null)
                    setOnCheckedChangeListener { view, isCheck ->
                        if (isCheck) {
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
                        } else if (chipGroup.indexOfChild(view) != 0) {
                            if (!chipGroup.children.any { item -> (item as? Chip)?.isChecked == true }) {
                                (chipGroup.getChildAt(0) as? Chip)?.isChecked = true
                            }
                        }
                        val selectedCategories = mutableListOf<String>()
                        chipGroup.children.forEach {
                            if ((it as Chip).isChecked) selectedCategories.add(it.text.toString())
                        }

                        filterThemeAccordingToCategories(selectedCategories)
                    }
                }
                chipGroup.addView(chip)
            }
            val allThemeId = chipGroup.getChildAt(0) as? Chip
            allThemeId?.isChecked = true
        }
    }


    private fun goToPreview(theme: RemoteTheme) {
        AdsUtils.forceShowInterSelect(requireContext()) {
            val args = bundleOf(CLICK_THEME to theme)
            Log.d("Fragment", "${args}")
            if (getNavController().currentDestination?.id == R.id.mainFragment) {
                val action = FragmentDirections.action(
                    args,
                    R.id.action_mainFragment_to_previewFragment
                )
                getNavController().navigate(action)
            }

            AdsUtils.loadInterSelect(requireContext(), true)
        }
    }

    private fun goToUnlock(theme: Theme?) {
        AdsUtils.forceShowInterSelect(requireContext()) {
            val args = bundleOf(CLICK_THEME to theme)
            Log.d("Fragment", "${args}")
            if (getNavController().currentDestination?.id == R.id.mainFragment) {
                val action = FragmentDirections.action(
                    args,
                    R.id.action_mainFragment_to_unlockFragment
                )
                getNavController().navigate(action)
            }
            AdsUtils.loadInterSelect(requireContext(), true)
        }
    }

    private fun filterThemeAccordingToCategories(selectedCategories: List<String>) {
        Log.d("Chip", "${adapter.getOriginalItems()}")
        Log.d("Chip", "${selectedCategories}")
        if (selectedCategories.contains(ALL_THEME)) {
            adapter.updateItems(adapter.getOriginalItems())
            return
        }

        if (selectedCategories.isNotEmpty()) {
            val filteredThemes = adapter.getOriginalItems().filter { theme ->
                theme.categoryId.let {
                    val category = RemoteConfig.category
                    val type = category?.filter { item -> item.id == it }
                    selectedCategories.contains(type?.get(0)?.name ?: ALL_THEME)
                }
            }
            adapter.updateItems(ArrayList(filteredThemes))
        } else {
            adapter.updateItems(adapter.getOriginalItems())
        }
    }

    companion object {
        const val CLICK_THEME = "CLICK_THEME"
        const val ALL_THEME = "All"
    }
}