package com.example.claptofindphone.presenter.select

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.ads.control.ads.VioAdmob
import com.example.claptofindphone.R
import com.example.claptofindphone.ads.BannerAdsController
import com.example.claptofindphone.data.local.PreferenceSupplier
import com.example.claptofindphone.databinding.FragmentSelectBinding
import com.example.claptofindphone.presenter.base.BaseFragment
import com.example.claptofindphone.presenter.common.AudioStatus
import com.example.claptofindphone.presenter.common.MediaPlayerHelper
import com.example.claptofindphone.presenter.common.SoundRes
import com.example.claptofindphone.presenter.findphone.viewmodel.FindPhoneScreenViewModel
import com.example.claptofindphone.presenter.select.adapter.SoundListAdapter
import com.example.claptofindphone.presenter.select.model.SoundModel
import com.example.claptofindphone.presenter.select.viewmodel.SelectScreenViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import javax.inject.Inject


@AndroidEntryPoint
class SelectFragment : BaseFragment(R.layout.fragment_select), AudioStatus {
    override val label: String
        get() = getString(R.string.grant_permission_title)

    override val isBackButtonEnable: Boolean
        get() = true

    override val isBottomBarEnable: Boolean
        get() = false
    override val isFabEnable: Boolean
        get() = false

    private val mSelectBinding by viewBinding(FragmentSelectBinding::bind)

    override val isRightButtonEnable: Boolean
        get() = false

    override val isApplyButtonEnable: Boolean
        get() = true

    override val bannerPlacement: BannerAdsController.BannerPlacement
        get() = BannerAdsController.BannerPlacement.SELECT_SOUND

    override val viewModel: SelectScreenViewModel by viewModels()

    private val selectedNavArgs by navArgs<SelectFragmentArgs>()

    @Inject
    lateinit var mediaPlayerHelper: MediaPlayerHelper

    @Inject
    lateinit var preferenceSupplier: PreferenceSupplier


    private val durationsText = ArrayList<TextView>()

    private lateinit var iconListAdapter: SoundListAdapter
    private lateinit var soundList: ArrayList<SoundModel>
    private var soundSelectedIndex = 0

    private var shouldTurnOnFlash = false
    private var shouldVibration = false
    private var shouldPlaySound = false
    private var soundVolumeValue = 0
    private var soundDurationValue = 0
    private lateinit var soundCategory: SoundCategory
    private val findPhoneScreenViewModel: FindPhoneScreenViewModel by viewModels({ requireActivity() })


    override fun initView() {
        super.initView()
        setUpView()
        setUpSoundList()
        shouldTurnOnFlash = preferenceSupplier.isShouldTurnOnFlash()
        shouldVibration = preferenceSupplier.isShouldVibration()
        shouldPlaySound = preferenceSupplier.isShouldPlaySound()
        soundVolumeValue = preferenceSupplier.getVolume()
        soundDurationValue = preferenceSupplier.getDuration()
        initAds()
        initBanner()
    }

    private fun initMediaPlayer() {
        mediaPlayerHelper.initSound(SoundRes.getSound(soundCategory.value))
    }

    private fun setUpSoundList() {
        soundList = findPhoneScreenViewModel.setUpSoundTypeList()
        val soundListLayoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mSelectBinding.rclSounds.layoutManager = soundListLayoutManager
    }

    private fun onItemClick(
        soundType: SoundModel,
        position: Int
    ) {
        mSelectBinding.imgSoundAvatar.setImageResource(soundType.iconRes)
        soundSelectedIndex = position
        setTitle(requireContext().getString(soundType.soundNameRes))
        if (!MediaPlayerHelper.isCallToStartFromService) {
            mediaPlayerHelper.stopSound(this)
            this.soundCategory = soundType.soundCategory
            initMediaPlayer()
        }
        // VuCX request : change other audio type, the audio should not start.
//        GlobalScope.launch(Dispatchers.IO) {
//            myMediaPlayerHelper.playSound()
//        }
    }


    override fun bindViewModel() {
        super.bindViewModel()
        viewModel.initData(selectedNavArgs.SoundTypeModel)
        viewModel.soundModel.onEachWithLifecycleScope {
            mSelectBinding.imgSoundAvatar.setImageResource(it!!.iconRes)
            mSelectBinding.rclSounds.scrollToPosition(it.soundCategory.value)
            soundSelectedIndex = it.soundCategory.value
            iconListAdapter = SoundListAdapter(
                requireContext(),
                soundList,
                ::onItemClick,
                SoundListAdapter.ScreenType.Select,
                it.soundCategory.value
            )
            soundCategory = it.soundCategory
            mSelectBinding.rclSounds.adapter = iconListAdapter
            setTitle(requireContext().getString(it.soundNameRes))
        }

    }

    override fun applySelect() {
        super.applySelect()
        preferenceSupplier.setSoundType(soundSelectedIndex)
        preferenceSupplier.setShouldTurnOnFlash(shouldTurnOnFlash)
        preferenceSupplier.setShouldVibration(shouldVibration)
        preferenceSupplier.setShouldPlaySound(shouldPlaySound)
        preferenceSupplier.setVolume(soundVolumeValue)
        preferenceSupplier.setDuration(soundDurationValue)
        // change request
        // after touch to apply, if the mode is deactive, let's active it.
        //preferenceProvider.setFindPhoneActivated(true)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun setUpView() {
        // flash
        mSelectBinding.settingFlash.getSwitchButton().isChecked =
            preferenceSupplier.isShouldTurnOnFlash()
        mSelectBinding.settingFlash.getSwitchButton()
            .setOnCheckedChangeListener { _, isChecked ->
                shouldTurnOnFlash = isChecked
            }
        //vibration
        mSelectBinding.settingVibration.getSwitchButton().isChecked =
            preferenceSupplier.isShouldVibration()
        mSelectBinding.settingVibration.getSwitchButton()
            .setOnCheckedChangeListener { _, isChecked ->
                shouldVibration = isChecked
            }
        //play sound
        mSelectBinding.settingSound.getSwitchButton().isChecked =
            preferenceSupplier.isShouldPlaySound()
        mSelectBinding.settingSound.getSwitchButton()
            .setOnCheckedChangeListener { _, isChecked ->
                shouldPlaySound = isChecked
            }

        //volume
        mSelectBinding.settingVolume.getSeekBarVolume().max =
            mediaPlayerHelper.getMaxAudioVolume()
        mSelectBinding.settingVolume.getSeekBarVolume()
            .setProgress(preferenceSupplier.getVolume(), true)
        mSelectBinding.settingVolume.getSeekBarVolume()
            .setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    mediaPlayerHelper.setVolume(progress)
                    soundVolumeValue = progress
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }
            })
        // duration
        durationsText.add(mSelectBinding.settingDuration.txtSoundDuration2m)
        durationsText.add(mSelectBinding.settingDuration.txtSoundDuration1m)
        durationsText.add(mSelectBinding.settingDuration.txtSoundDuration30s)
        durationsText.add(mSelectBinding.settingDuration.txtSoundDuration15s)
        when (preferenceSupplier.getDuration()) {
            Duration.TwoMinute.value -> {
                mSelectBinding.settingDuration.txtSoundDuration2m.isSelected = true
            }

            Duration.OneMinute.value -> {
                mSelectBinding.settingDuration.txtSoundDuration1m.isSelected = true
            }

            Duration.ThirtySecond.value -> {
                mSelectBinding.settingDuration.txtSoundDuration30s.isSelected = true
            }

            Duration.FifteenSecond.value -> {
                mSelectBinding.settingDuration.txtSoundDuration15s.isSelected = true
            }
        }
        mSelectBinding.settingDuration.txtSoundDuration2m.setOnClickListener {
            for (durationView in durationsText) {
                durationView.isSelected = false
            }
            it.isSelected = true
            soundDurationValue = Duration.TwoMinute.value
        }
        mSelectBinding.settingDuration.txtSoundDuration1m.setOnClickListener {
            for (durationView in durationsText) {
                durationView.isSelected = false
            }
            it.isSelected = true
            soundDurationValue = Duration.OneMinute.value
        }
        mSelectBinding.settingDuration.txtSoundDuration30s.setOnClickListener {
            for (durationView in durationsText) {
                durationView.isSelected = false
            }
            it.isSelected = true
            soundDurationValue = Duration.ThirtySecond.value
        }
        mSelectBinding.settingDuration.txtSoundDuration15s.setOnClickListener {
            for (durationView in durationsText) {
                durationView.isSelected = false
            }
            it.isSelected = true
            soundDurationValue = Duration.FifteenSecond.value
        }
        mSelectBinding.imgPlayPausePreviewSound.isSelected = false
        mSelectBinding.imgPlayPausePreviewSound.setOnClickListener {
            if (!MediaPlayerHelper.isCallToStartFromService) {
                if (!it.isSelected) {
                    initMediaPlayer()
                    mediaPlayerHelper.playSound(
                        volume = preferenceSupplier.getVolume(),
                        duration = preferenceSupplier.getDuration(),
                        audioStatus = this,
                        isPlayFromService = false
                    )
                } else {
                    mediaPlayerHelper.stopSound(this)
                }
            } else {
                Toast.makeText(
                    context,
                    "Finding audio is playing. Cannot play preview sound.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onAudioFinish() {
        mSelectBinding.imgPlayPausePreviewSound.isSelected = false
    }

    override fun onDuringPlaying() {
        mSelectBinding.imgPlayPausePreviewSound.isSelected = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!MediaPlayerHelper.isCallToStartFromService) {
            mediaPlayerHelper.stopSound(this)
        }
    }

    enum class Duration(val value: Int) {
        TwoMinute(120),
        OneMinute(60),
        ThirtySecond(30),
        FifteenSecond(15);

        companion object {
            fun fromInt(value: Int) = values().first { it.ordinal == value }
        }

    }

    private fun initAds() {
        return
        adsHelper.selectApNativeAdLoadFail.observe(viewLifecycleOwner) {
            if(it) {
                mSelectBinding.frAdsNative.visibility = View.GONE
                adsHelper.selectApNativeAdLoadFail.value = false
            }
        }

        adsHelper.selectApNativeAd.observe(viewLifecycleOwner) {
            it?.let { nativeAd ->
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    nativeAd,
                    mSelectBinding.frAdsNative,
                    mSelectBinding.includeNative.shimmerContainerBanner
                )
            }
        }
    }

    private fun initBanner() {
        adsHelper.bannerAdViewSelectSound.distinctUntilChanged().observe(viewLifecycleOwner) {
            if(it == null) {
                mSelectBinding.bannerShimmer.visibility = View.GONE
            }
            it?.let {
                (it.parent as? ViewGroup)?.removeView(it)
                it.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                        gravity = Gravity.BOTTOM or Gravity.CENTER
                }
                mSelectBinding.root.addView(it)
            }
        }
    }

    enum class SoundCategory(val value: Int) {
        Police(0),
        Hello(1),
        Doorbell(2),
        Laughing(3),
        Alarm(4),
        Harp(5),
        Piano(6),
        Rooster(7),
        Sneeze(8),
        Train(9),
        WindChimes(10),
        Whistle(11);

        companion object {
            fun fromInt(value: Int) = values().first { it.ordinal == value }
        }

    }
}