package com.example.videoart.batterychargeranimation.ui.mytheme

import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewbinding.ViewBinding
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.databinding.FragmentMyThemeBinding
import com.example.videoart.batterychargeranimation.extension.FragmentDirections
import com.example.videoart.batterychargeranimation.helper.ThemeManager
import com.example.videoart.batterychargeranimation.model.Theme
import com.example.videoart.batterychargeranimation.ui.base.BaseActivity
import com.example.videoart.batterychargeranimation.ui.base.BaseFragment
import com.example.videoart.batterychargeranimation.ui.gallery.GALLERY_TYPE
import com.example.videoart.batterychargeranimation.ui.home.HomeFragment
import com.example.videoart.batterychargeranimation.utils.AdsUtils
import org.koin.android.ext.android.inject
import java.util.UUID

class MyThemeFragment(
    private val type: GALLERY_TYPE = GALLERY_TYPE.USED,
    private val listTheme: MutableList<Theme> = mutableListOf()
) : BaseFragment() {
    private lateinit var binding: FragmentMyThemeBinding
    private val viewModel: MyThemeViewModel by inject()
    private lateinit var adapter: MyThemeAdapter

    init {
        Log.d("Call", "init my theme Fragment")
    }
    override fun getViewBinding(): ViewBinding {
        binding = FragmentMyThemeBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {
        Log.d("Call", "init my theme Fragment adapter")
        adapter = MyThemeAdapter(listTheme)
        binding.listItem.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.listItem.adapter = adapter
        adapter.setItemCallback {
            val bundle = bundleOf(HomeFragment.CLICK_THEME to it)
            val action = FragmentDirections.action(
                bundle,
                R.id.action_mainFragment_to_unlockFragment
            )

            findNavController().navigate(action)
        }
        binding.sortBtn.setOnClickListener {
            adapter?.sortList()
        }

        binding.selectBtn.setOnClickListener {
            Log.d("Mode", "call")
            if(viewModel.myThemes.value.isNullOrEmpty()) {
                return@setOnClickListener
            }
            viewModel.changeMode(Mode.EDIT)
        }

        binding.cancelBtn.setOnClickListener {
            viewModel.changeMode(Mode.NORMAL)
            adapter.clearAllSelect()
        }

        binding.selectAllBtn.setOnClickListener {
            adapter.selectAll()
        }

        binding.deleteBtn.setOnClickListener {
            val deleteList = adapter?.getDeleteList()
            val currentId = ThemeManager.getInstance(requireContext()).currentThemeId
            for(item in deleteList ?: emptyList()) {
                if(item.id == currentId) {
                    ThemeManager.getInstance(requireContext()).currentThemeId = ThemeManager.DEFAULT_THEME_ID
                }
                ThemeManager.getInstance(requireContext()).removeTheme(item.id)
            }

            viewModel.myThemes.value?.let {
                if(it.isEmpty()) {
                    viewModel.changeMode(Mode.NORMAL)
                }
            }
        }

        binding.addNewTheme.setOnClickListener {
            (requireActivity() as BaseActivity).pickImageFromGallery {
                Log.d("PickMedia", "Hello")
                it?.let {uri ->
                    val newTheme = Theme(
                        id = UUID.randomUUID().toString(),
                        thumbnail = uri.toString(),
                        animation = "",
                        sound = "",
                        fontId = "",
                        category = "Local",
                        fromRemote = false
                    )
                    Log.d("PickMedia", newTheme.toString())
                    val navController = getNavController()
                    Log.d("PickMedia", "${navController.currentDestination?.id}")
                    val bundle = bundleOf(HomeFragment.CLICK_THEME to newTheme)
                    if(navController.currentDestination?.id == R.id.mainFragment) {
                        val action = FragmentDirections.action(
                            bundle,
                            R.id.action_mainFragment_to_unlockFragment
                        )
                        navController.navigate(action)
                    }
                }
            }
        }
    }

    override fun registerObservers() {
        viewModel.observeData(requireContext(), this)
        viewModel.myThemes.observe(this) {
            val themes = if(type == GALLERY_TYPE.UPLOAD) {
                it.filter { theme -> !theme.fromRemote }
            }else it
            updateListData(themes)
            if(it.isEmpty()) {
                binding.listItem.visibility = View.INVISIBLE
            }else {
                binding.listItem.visibility = View.VISIBLE
            }
        }

        viewModel.currentThemesId.observe(this) {
            updateCurrentItem(it)
        }

        viewModel.mode.observe(this) {
            adapter.changeMode(it)
            Log.d("Mode", "current Mode${it}")
            if(it == Mode.NORMAL) {
                binding.listBtn1.isVisible = true
                binding.listBtn2.isVisible = false
                binding.addNewTheme.isVisible =true
                binding.deleteBtn.isVisible = false
            }else {
                binding.listBtn1.isVisible = false
                binding.listBtn2.isVisible = true
                binding.addNewTheme.isVisible = false
                binding.deleteBtn.isVisible = true
            }
        }
    }

    private fun updateListData(themes: List<Theme>) {
        listTheme.clear()
        listTheme.addAll(themes)
        adapter.updateData(themes)
    }

    private fun updateCurrentItem(themeId: String) {
        adapter.updateCurrent(themeId)
    }
}