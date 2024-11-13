package com.example.videoart.batterychargeranimation.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.viewbinding.ViewBinding
import com.example.videoart.batterychargeranimation.MainActivity
import com.example.videoart.batterychargeranimation.helper.BatteryHelper
import com.example.videoart.batterychargeranimation.helper.FileHelper
import com.example.videoart.batterychargeranimation.model.MusicReturnInfo
import com.example.videoart.batterychargeranimation.ui.dialog.ErrorPopupFragment
import com.example.videoart.batterychargeranimation.ui.dialog.ExitAppDialog
import com.example.videoart.batterychargeranimation.ui.dialog.LoadingDialogFragment
import com.example.videoart.batterychargeranimation.ui.dialog.RatingAppDialog
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.slideshowmaker.slideshow.modules.music_player.MusicPlayer
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

abstract class BaseFragment : Fragment(), LifecycleObserver {

    private var rootView: View? = null
    private var needLoadData = false
    private var audioPath: String = ""
    protected var myActivity: AppCompatActivity? = null
    protected val musicPlayer: MusicPlayer by inject()
    protected val batteryHelper: BatteryHelper by inject()

    val loadingDialog = LoadingDialogFragment()
    val ratingAppDialog = RatingAppDialog()
    val exitAppDialog = ExitAppDialog()
    var errorDialogFrag = ErrorPopupFragment {
        requireActivity().finish()
    }
    abstract fun getViewBinding(): ViewBinding

    abstract fun onViewCreated()

    abstract fun registerObservers()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.lifecycle?.addObserver(this)

        if (context is BaseActivity) {
            myActivity = context
        }
    }

    override fun onDetach() {
        activity?.lifecycle?.removeObserver(this)
        super.onDetach()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (rootView == null) {
            rootView = getViewBinding().root
            needLoadData = true
        } else {
            needLoadData = false
        }
        return rootView as View
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (needLoadData) {
            onViewCreated()
            registerObservers()
        }

        needLoadData = true
    }


    override fun onPause() {
        super.onPause()
        pauseAudio()
    }

    fun getNavController(): NavController {
        val mainActivity = activity as MainActivity
        return mainActivity.navController
    }

    protected fun playAudio(filePath: String) {
        musicPlayer.changeMusic(filePath)
        musicPlayer.play()
    }

    protected fun pauseAudio() {
        musicPlayer.pause()
    }

    fun showInternetErrorPopup() {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return
        errorDialogFrag = ErrorPopupFragment.Builder()
            .setDialogType(ErrorPopupFragment.ErrorType.INTERNET)
            .setOnClick { requireActivity().finish()}
            .build()
        errorDialogFrag?.show(requireActivity().supportFragmentManager, "")
    }


}