package com.example.claptofindphone.presenter.setting

import android.os.Build
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.RequiresApi
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.example.claptofindphone.R
import com.example.claptofindphone.ads.BannerAdsController
import com.example.claptofindphone.data.local.PreferenceSupplier
import com.example.claptofindphone.databinding.FragmentSettingBinding
import com.example.claptofindphone.presenter.ClapToFindApplication
import com.example.claptofindphone.presenter.base.BaseFragment
import com.example.claptofindphone.presenter.language.LanguageActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SettingFragment : BaseFragment(R.layout.fragment_setting) {
    override val label: String
        get() = getString(R.string.setting)

    override val isBackButtonEnable: Boolean
        get() = false

    override val isBottomBarEnable: Boolean
        get() = false
    override val isFabEnable: Boolean
        get() = false

    override val isRightButtonEnable: Boolean
        get() = false
    override val isApplyButtonEnable: Boolean
        get() = false
    override val isHowToUseButtonEnable: Boolean
        get() = true
    override val bannerPlacement: BannerAdsController.BannerPlacement
        get() = BannerAdsController.BannerPlacement.SETTING
    private val layoutBinding by viewBinding(FragmentSettingBinding::bind)

    @Inject
    lateinit var preferenceSupplier: PreferenceSupplier

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun initView() {
        super.initView()

        setUpSensitivityView()
        setUpExtensionView()
        setUpFlashMode()
        setUpVibrationMode()
        layoutBinding.settingLanguage.root.setOnClickListener {
            LanguageActivity.start(requireActivity(), true)
        }

        layoutBinding.txtFindPhone.isSelected = false
        layoutBinding.txtFindPhone.clicksWithLifecycleScope {
            it.findNavController().apply {
                if (currentDestination?.id != R.id.settingFragment) navigateUp()
                navigate(SettingFragmentDirections.globalToFindPhoneFragment())
            }
        }
        layoutBinding.txtSetting.isSelected = true

        initBanner()
    }

    private fun setUpSensitivityView() {
        layoutBinding.sensitivity.seekBarSensitivity.max = 100
        layoutBinding.sensitivity.seekBarSensitivity.min = 0
        layoutBinding.sensitivity.seekBarSensitivity.progress =
            preferenceSupplier.getSensitivity()
        layoutBinding.sensitivity.seekBarSensitivity.setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                preferenceSupplier.setSensitivity(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun setUpExtensionView() {
        layoutBinding.extension.switchExtension.isChecked =
            preferenceSupplier.isShouldLightUpScreen()
        layoutBinding.extension.switchExtension.setOnCheckedChangeListener { _, isChecked ->
            preferenceSupplier.setShouldLightUpScreen(isChecked)
        }
    }

    private fun setUpFlashMode() {
        val flashModeRadiosBtn = ArrayList<RadioButton>()
        flashModeRadiosBtn.add(layoutBinding.flashMode.settingFlashModeDefault.getRadioButton())
        flashModeRadiosBtn.add(layoutBinding.flashMode.settingFlashModeDisco.getRadioButton())
        flashModeRadiosBtn.add(layoutBinding.flashMode.settingFlashModeSOS.getRadioButton())
        for (i in flashModeRadiosBtn.indices) {
            if (i == preferenceSupplier.getFlashMode()) {
                flashModeRadiosBtn[i].isChecked = true
            }
            flashModeRadiosBtn[i].setOnClickListener {
                flashModeRadiosBtn[preferenceSupplier.getFlashMode()].isChecked = false
                preferenceSupplier.setFlashMode(i)
                // preview flash time is 3s
                ClapToFindApplication.get().turnOnFlash(i, 3)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setUpVibrationMode() {
        val vibrationModeRadiosBtn = ArrayList<RadioButton>()
        vibrationModeRadiosBtn.add(layoutBinding.vibrationMode.settingVibrationModeDefault.getRadioButton())
        vibrationModeRadiosBtn.add(layoutBinding.vibrationMode.settingVibrationModeStrong.getRadioButton())
        vibrationModeRadiosBtn.add(layoutBinding.vibrationMode.settingVibrationModeHeartBeat.getRadioButton())
        vibrationModeRadiosBtn.add(layoutBinding.vibrationMode.settingVibrationModeTicktock.getRadioButton())
        for (j in vibrationModeRadiosBtn.indices) {
            if (j == preferenceSupplier.getVibrationMode()) {
                vibrationModeRadiosBtn[j].isChecked = true
            }
            vibrationModeRadiosBtn[j].setOnClickListener {
                vibrationModeRadiosBtn[preferenceSupplier.getVibrationMode()].isChecked = false
                preferenceSupplier.setVibrationMode(j)
                ClapToFindApplication.get().startVibrate(
                    preferenceSupplier.getVibrationMode(), false
                )
            }
        }
    }

    private fun initBanner() {
        adsHelper.bannerAdViewSetting.distinctUntilChanged().observe(viewLifecycleOwner) {
            it?.let {
                (it.parent as? ViewGroup)?.removeView(it)
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                layoutBinding.bottomLayout.addView(it)
            }
        }
    }
}