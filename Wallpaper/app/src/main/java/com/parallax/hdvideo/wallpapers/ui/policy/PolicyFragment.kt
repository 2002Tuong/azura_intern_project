package com.parallax.hdvideo.wallpapers.ui.policy

import android.view.View
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.FragmentPolicyScreenBinding
import com.parallax.hdvideo.wallpapers.extension.fromHtml
import com.parallax.hdvideo.wallpapers.extension.getStringFromAssets
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragment
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.AppConstants


class PolicyFragment: BaseFragment() {

    override val resLayoutId: Int = R.layout.fragment_policy_screen
    private lateinit var binding: FragmentPolicyScreenBinding
    override fun init(view: View) {
        binding = FragmentPolicyScreenBinding.bind(view)
        binding.appBarLayout.layoutParams.height = AppConfiguration.statusBarSize + resources.getDimensionPixelOffset(R.dimen.height_header)
        binding.backButton.setOnClickListener { onBackPressed() }
        binding.textView.text = getStringFromAssets(AppConstants.FILE_NAME_POLICY)?.fromHtml
    }

    override fun viewIsVisible() {
    }
}