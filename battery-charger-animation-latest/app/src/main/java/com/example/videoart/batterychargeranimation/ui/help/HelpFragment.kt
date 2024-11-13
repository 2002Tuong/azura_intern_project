package com.example.videoart.batterychargeranimation.ui.help

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.data.local.PreferenceUtils
import com.example.videoart.batterychargeranimation.databinding.FragmentHelpBinding
import com.example.videoart.batterychargeranimation.ui.base.BaseFragment
import com.example.videoart.batterychargeranimation.ui.premission.PermissionFragment

class HelpFragment : BaseFragment() {


    private lateinit var binding: FragmentHelpBinding
    private val viewModel: HelpViewModel by viewModels()
    override fun getViewBinding(): ViewBinding {
        binding = FragmentHelpBinding.inflate(layoutInflater)
        return binding
    }

    override fun onViewCreated() {
        viewModel.setDisplayOver(isDisplayOverOtherApp())
        viewModel.setIsIgnoreBatteryOptimization(isIgnoringBatteryOptimizations())

        binding.swOverlayPermission.setOnClickListener {
            if(!isDisplayOverOtherApp()) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + requireContext().packageName))
                startActivityForResult(intent, PermissionFragment.DISPLAY_OVER_APP_PERMISSION)
            }
        }

//        binding.swBatteryOptimizationPermission.setOnClickListener {
//            if(!isIgnoringBatteryOptimizations()) {
//                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
//                    Uri.parse("package:" + requireContext().packageName))
//                startActivityForResult(intent, IGNORE_BATTERY_OPTIMIZATION)
//            }
//        }

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun registerObservers() {
        viewModel.permissionDisplayOverOtherApps.observe(viewLifecycleOwner) {
            if(it) {
                binding.swOverlayPermission.setImageResource(R.drawable.check_circle)
                binding.swOverlayPermission.isEnabled = false
            }/* else {
                binding.swOverlayPermission.setImageResource(R.drawable.icon_switch_off)
                binding.swOverlayPermission.isEnabled = true
            }*/
        }

//        viewModel.isIgnoreBatteryOptimization.observe(viewLifecycleOwner) {
//            if(it) {
//                binding.swBatteryOptimizationPermission.setImageResource(R.drawable.icon_switch_on)
//                binding.swBatteryOptimizationPermission.isEnabled = false
//            } else {
//                binding.swBatteryOptimizationPermission.setImageResource(R.drawable.icon_switch_off)
//                binding.swBatteryOptimizationPermission.isEnabled = true
//            }
//        }
    }


    fun isIgnoringBatteryOptimizations(): Boolean {
        val pwrm = requireContext().applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        val name = requireContext().applicationContext.packageName

        return pwrm.isIgnoringBatteryOptimizations(name)
    }

    fun isDisplayOverOtherApp(): Boolean = Settings.canDrawOverlays(requireContext())

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PermissionFragment.DISPLAY_OVER_APP_PERMISSION && resultCode == Activity.RESULT_OK) {
            if(isDisplayOverOtherApp()) {
                viewModel.setDisplayOver(true)
                PreferenceUtils.setDisplayOverPermission(true)
            }
        }

        if(requestCode == IGNORE_BATTERY_OPTIMIZATION && resultCode == Activity.RESULT_OK) {
            if(isIgnoringBatteryOptimizations()) {
                viewModel.setIsIgnoreBatteryOptimization(true)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if(isIgnoringBatteryOptimizations()) {
            viewModel.setIsIgnoreBatteryOptimization(true)
        }

        if(isDisplayOverOtherApp()) {
            viewModel.setDisplayOver(true)
            PreferenceUtils.setDisplayOverPermission(true)
        }
    }

    companion object {
        const val IGNORE_BATTERY_OPTIMIZATION = 101
    }
}