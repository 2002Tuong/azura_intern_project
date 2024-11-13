package com.example.videoart.batterychargeranimation.ui.splash

import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.ads.control.admob.AppOpenManager
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.data.local.PreferenceUtils
import com.example.videoart.batterychargeranimation.data.remote.RemoteConfig
import com.example.videoart.batterychargeranimation.databinding.FragmentSplashBinding
import com.example.videoart.batterychargeranimation.extension.isInternetAvailable
import com.example.videoart.batterychargeranimation.helper.ThemeManager
import com.example.videoart.batterychargeranimation.ui.base.BaseFragment
import com.example.videoart.batterychargeranimation.utils.ConsentHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.android.ext.android.inject

class SplashFragment : BaseFragment()
{
    private lateinit var binding: FragmentSplashBinding
    private val viewModel: SplashViewModel by inject()
    override fun getViewBinding(): ViewBinding {
        binding = FragmentSplashBinding.inflate(layoutInflater)
        return binding
    }

    private val timer = object : CountDownTimer(30000L, 1000L) {
        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {
            Toast.makeText(requireContext(), getString(R.string.load_error), Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
    }

    override fun onViewCreated() {
        AppOpenManager.getInstance().disableAppResume()
        Log.d("call", "Splash call")
        if(!requireContext().isInternetAvailable()) {
            errorDialogFrag.show(requireActivity().supportFragmentManager, "error")
        }
        timer.start()
        lifecycleScope.launch {
            Log.d("RemoteConfig", "cmpRequired ${RemoteConfig.cmpRequire}")
            (requireActivity() as AppCompatActivity).let {
                ConsentHelper.obtain(
                    context = it,
                    loadAds = {viewModel.startSplash(it)},
                    onDone = { it.finish() }
                )
            }
        }


        val defaultTheme = ThemeManager.getInstance(requireContext()).getTheme(ThemeManager.DEFAULT_THEME_ID)
        if(defaultTheme == null) {
            ThemeManager.getInstance(requireContext()).saveDefault()
        }
    }

    override fun registerObservers() {
        viewModel.nextScreenRoute.observe(viewLifecycleOwner) {
            lifecycleScope.launch {
                if(it == SplashViewModel.ROUTE_HOME) {
                    if(PreferenceUtils.storagePermissionGranted && PreferenceUtils.displayOverAppGranted) {
                        findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
                    } else {
                        findNavController().navigate(R.id.action_splashFragment_to_permissionFragment)
                    }

                } else {
                    findNavController().navigate(R.id.action_splashFragment_to_languageFragment)
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkShowInterSplashWhenFail(requireActivity() as AppCompatActivity)
    }

    override fun onStop() {
        super.onStop()
        timer.cancel()
    }
}