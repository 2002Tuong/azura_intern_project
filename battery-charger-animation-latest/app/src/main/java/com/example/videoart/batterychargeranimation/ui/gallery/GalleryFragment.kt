package com.example.videoart.batterychargeranimation.ui.gallery

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.databinding.FragmentGalleryBinding
import com.example.videoart.batterychargeranimation.ui.base.BaseFragment
import com.example.videoart.batterychargeranimation.ui.main.MainViewPagerAdapter
import com.example.videoart.batterychargeranimation.ui.mytheme.MyThemeFragment
import com.example.videoart.batterychargeranimation.ui.mytheme.MyThemeViewModel
import org.koin.android.ext.android.inject

class GalleryFragment : BaseFragment() {
    private lateinit var binding: FragmentGalleryBinding
    private lateinit var adapter: MainViewPagerAdapter
    private val listFragment = mutableListOf<Fragment>()
    private val usedFragment: MyThemeFragment = MyThemeFragment(GALLERY_TYPE.USED)
    private val uploadedFragment: MyThemeFragment = MyThemeFragment(GALLERY_TYPE.UPLOAD)
    private val viewModel: MyThemeViewModel by inject()
    override fun getViewBinding(): ViewBinding {
        binding = FragmentGalleryBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {
        Log.d("Call", "onViewCreated galleryFragment")
        listFragment.add(usedFragment)
        listFragment.add(uploadedFragment)

        adapter = MainViewPagerAdapter(requireActivity(), listFragment)
        binding.apply {
            viewPager.adapter = adapter
            viewPager.offscreenPageLimit = 2
            viewPager.isSaveEnabled = false
            viewPager.isUserInputEnabled = false

            usedBtn.setOnClickListener {
                usedBtn.setTextColor(resources.getColor(R.color.white, null))
                usedBtn.setBackgroundResource(R.drawable.bg_gallery_btn)
                uploadedBtn.setBackgroundColor(resources.getColor(R.color.transparent, null))
                uploadedBtn.setTextColor(resources.getColor(R.color.galley_disable_color, null))
                indicator.setSelectedIndex(0)
                viewPager.setCurrentItem(0, true)

            }

            uploadedBtn.setOnClickListener {
                uploadedBtn.setTextColor(resources.getColor(R.color.white, null))
                uploadedBtn.setBackgroundResource(R.drawable.bg_gallery_btn)
                usedBtn.setBackgroundColor(resources.getColor(R.color.transparent, null))
                usedBtn.setTextColor(resources.getColor(R.color.galley_disable_color, null))
                indicator.setSelectedIndex(1)
                viewPager.setCurrentItem(1, true)
            }
        }
    }

    override fun registerObservers() {

    }
}

enum class GALLERY_TYPE {
    USED, UPLOAD
}