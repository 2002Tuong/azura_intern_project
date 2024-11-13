package com.parallax.hdvideo.wallpapers.ui.splash.welcom

import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.ads.AdsManager
import com.parallax.hdvideo.wallpapers.databinding.FragmentWelcomeScreenBinding
import com.parallax.hdvideo.wallpapers.di.storage.frefs.LocalStorage
import com.parallax.hdvideo.wallpapers.extension.*
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragmentBinding
import com.parallax.hdvideo.wallpapers.ui.custom.ExoPlayVideo
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class WelcomeFragment : BaseFragmentBinding<FragmentWelcomeScreenBinding, MainViewModel>() {

    @Inject
    lateinit var localStorage: LocalStorage

    private val mainViewModel: MainViewModel by activityViewModels()
    private lateinit var sloganArray: Array<String>
    private lateinit var imageArray: Array<String>
    private lateinit var imageAndSloganPair: Pair<String, String>
    private val isFirstAppOpen get() = !localStorage.firstOpenComplete
    private lateinit var exoPlayVideo: ExoPlayVideo
    private var appNameText = ""
    private var positionVideoIntro = 0

    override val resLayoutId = R.layout.fragment_welcome_screen

    override fun init(view: View) {
        super.init(view)
        isRunningSplashWelcome = true
        imageAndSloganPair = get4DImageAndSlogan()

        switchWelcomeMode(isFirstAppOpen)
        //bindingAction()
        initObserver()


    }

    override fun onResume() {
        super.onResume()
        AdsManager.checkShowInterSplashWhenFail(requireActivity() as AppCompatActivity) {
            Log.d("Splash", "Load fail call")
            goHomeScreen(0)
        }
    }

    private fun initObserver() {
        Log.d("AdsSpalsh", "${AdsManager.stopLoadAds()}")
        if(AdsManager.stopLoadAds()) {
            goHomeScreen(3000L)
            return
        }
        mainViewModel.configState.observe(viewLifecycleOwner) {
//            goHomeScreen(3000)
//            if (isFirstAppOpen) {
//                dataBinding.btnStart.isInvisible = false
//                dataBinding.progressBar.isHidden = true
//
//                dataBinding.btnStart.setOnClickListener {
//                    if (!openNetworkSettings()) {
//                        popFragment2(this)
//                        checkIntent()
//                    }
//                }
//            }
            Log.d("AdsSpalsh", "Call")
            AdsManager.loadInterSplash(requireActivity() as AppCompatActivity) {
                goHomeScreen(0)
            }
        }

    }

    private fun bindingAction() {
        if (!isFirstAppOpen) {
            goHomeScreen(3000)
        }
    }

    private fun switchWelcomeMode(isFirstOpenApp: Boolean) {
        if (isFirstOpenApp) {
            dataBinding.tvSlogan.text = getString(R.string.welcome_msg, appNameText).fromHtml
        } else {
            dataBinding.tvSlogan.text = imageAndSloganPair.first.fromHtml
        }
//        initVideo()
    }

//    private fun initVideo() {
//        if (!AppConfig.isLowMemory) {
//            if (isFirstOpenApp) {
//                GlideHelper.load(dataBinding.ivBackground, R.drawable.splash_welcome_first)
//                exoPlayVideo = ExoPlayVideo(requireContext())
//                exoPlayVideo.playRawFile(dataBinding.videoView, R.raw.demosplash)
//            } else {
//                GlideHelper.load(dataBinding.ivBackground, getVideoIntro().second)
//                exoPlayVideo = ExoPlayVideo(requireContext())
//                exoPlayVideo.playRawFile(dataBinding.videoView, getVideoIntro().first)
//            }
//        }
//    }
//
//    @RawRes
//    private fun getVideoIntro(): Pair<Int, Int> {
//        val timeNow = Calendar.getInstance().timeInMillis
//        positionVideoIntro = localStorage.currentVideoIntro
//
//        if ((timeNow - localStorage.lastDayChangeVideoIntro) > (60 * 1000)) {
//            positionVideoIntro++
//            localStorage.currentVideoIntro = positionVideoIntro
//            localStorage.lastDayChangeVideoIntro = timeNow
//        }
//        if (positionVideoIntro == 5) {
//            positionVideoIntro = 0
//            localStorage.currentVideoIntro = positionVideoIntro
//            localStorage.lastDayChangeVideoIntro = timeNow
//        }
//
//        return when (positionVideoIntro) {
//            0 -> Pair(R.raw.video_intro_1, R.drawable.thumb_video_intro_1)
//            1 -> Pair(R.raw.video_intro_2, R.drawable.thumb_video_intro_2)
//            2 -> Pair(R.raw.video_intro_3, R.drawable.thumb_video_intro_3)
//            3 -> Pair(R.raw.video_intro_4, R.drawable.thumb_video_intro_4)
//            else -> Pair(R.raw.video_intro_5, R.drawable.thumb_video_intro_5)
//        }
//    }

    private fun get4DImageAndSlogan(): Pair<String, String> {
        sloganArray = context?.resources?.getStringArray(R.array.slogan) as Array<String>
        appNameText = "<b>" + resources.getString(R.string.app_name) + "</b>"
        var curSlogan = ""
        imageArray = listOf(
            "https://icdn.dantri.com.vn/thumb_w/640/2020/12/16/ngam-dan-hot-girl-xinh-dep-noi-bat-nhat-nam-2020-docx-1608126694049.jpeg",
            "https://icdn.dantri.com.vn/thumb_w/640/2020/12/16/ngam-dan-hot-girl-xinh-dep-noi-bat-nhat-nam-2020-docx-1608126694049.jpeg"
        ).toTypedArray()
        var newPositionSlogan = localStorage.curSloganPosition

        // change slogan after 3 days
        val curTime = Calendar.getInstance().timeInMillis

        if (isFirstAppOpen) {
            localStorage.lastDayModifySlogan = curTime
            localStorage.lastDayModifyVideoIntro = curTime
        }

        if ((curTime - localStorage.lastDayModifySlogan) > (24 * 60 * 60 * 1000 * 2)) {
            newPositionSlogan++
            localStorage.lastDayModifySlogan = curTime
        }
        if (newPositionSlogan == sloganArray.size) {
            newPositionSlogan = 0
            localStorage.lastDayModifySlogan = curTime
        }
        val name =
            if ((!localStorage.lastName.isNullOrEmpty() || !localStorage.accountName.isNullOrEmpty())) {
                localStorage.lastName + " " + localStorage.accountName
            } else {
                ""
            }

        curSlogan = if (name.isNotEmpty()) {
            String.format(sloganArray[newPositionSlogan], "<b>$name</b>", appNameText)
        } else {
            when (newPositionSlogan) {
                0 -> String.format(getString(R.string.welcome_user_first_not_name), appNameText)
                1 -> String.format(getString(R.string.welcome_user_second_not_name), appNameText)
                else -> String.format(getString(R.string.welcome_user_first_not_name), appNameText)
            }
        }
        localStorage.curSloganPosition = newPositionSlogan
        return Pair(curSlogan, imageArray[newPositionSlogan])
    }

    @Synchronized
    private fun goHomeScreen(delay: Long) {
//        if (!openNetworkSettings()) {
//
//        }
        postDelayed({
            if (isRunningSplashWelcome && mainViewModel.configState.value == true ) {
                isRunningSplashWelcome = false
                Log.d("Welcome", "Call")
                popFragment2(this)
                checkIntent()
            }
        }, delay)
    }

    private fun checkIntent() {
        (activity as? MainActivity)?.let { it.checkIntent(it.intent) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isRunningSplashWelcome = false
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    companion object {
        var isRunningSplashWelcome = false
        const val TAG = "SplashWelcomeFragment"
    }

}