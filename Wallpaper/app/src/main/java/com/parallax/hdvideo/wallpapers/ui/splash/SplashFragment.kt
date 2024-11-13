package com.parallax.hdvideo.wallpapers.ui.splash

import android.view.View
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.FragmentSplashScreenBinding
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.popFragment2
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragment
import com.parallax.hdvideo.wallpapers.ui.custom.ExoPlayVideo
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.ui.splash.welcom.WelcomeFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SplashFragment : BaseFragment() {

    override var resLayoutId: Int = R.layout.fragment_splash_screen
    private lateinit var exoPlay: ExoPlayVideo
    private lateinit var mView: View
    private lateinit var binding: FragmentSplashScreenBinding
    @Inject
    lateinit var localStorage: LocalStorage

    override fun init(view: View) {
        binding = FragmentSplashScreenBinding.bind(view)
        mView = view
        binding.introText.text = getString(R.string.app_name)
        goWelcomeFragment()
    }

    @Synchronized
    private fun goWelcomeFragment() {
        postDelayed({
            (activity as? MainActivity)?.navigationController?.pushFragment(
                WelcomeFragment(),
                null,
                WelcomeFragment.TAG,
                singleton = true
            )
            popFragment2(this)
        }, 1000)
    }

    override fun onDestroyView() {
        if (this::exoPlay.isInitialized) exoPlay.release()
        super.onDestroyView()
    }

    override fun onBackPressed(): Boolean {
        activity?.finish()
        return false
    }

    companion object {
        const val TAG = "SplashFragment"
    }

}