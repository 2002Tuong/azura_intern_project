package com.example.videoart.batterychargeranimation.ui.unlock

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.animation.Animation
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.load
import coil.request.ImageRequest
import com.ads.control.ads.VioAdmob
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.videoart.batterychargeranimation.App
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.data.local.PreferenceUtils
import com.example.videoart.batterychargeranimation.databinding.FragmentPreviewBinding
import com.example.videoart.batterychargeranimation.databinding.FragmentUnlockBinding
import com.example.videoart.batterychargeranimation.extension.FragmentDirections
import com.example.videoart.batterychargeranimation.helper.ThemeManager
import com.example.videoart.batterychargeranimation.model.Theme
import com.example.videoart.batterychargeranimation.receiver.BatteryStateReceiver
import com.example.videoart.batterychargeranimation.service.ChargerService
import com.example.videoart.batterychargeranimation.ui.base.BaseFragment
import com.example.videoart.batterychargeranimation.ui.dialog.CompleteDialog
import com.example.videoart.batterychargeranimation.ui.dialog.SelectClosingMethodBottomSheet
import com.example.videoart.batterychargeranimation.ui.dialog.SelectDurationBottomSheet
import com.example.videoart.batterychargeranimation.ui.dialog.SetAnimationDialog
import com.example.videoart.batterychargeranimation.ui.home.HomeFragment
import com.example.videoart.batterychargeranimation.utils.AdsUtils
import java.io.File

class UnlockFragment : BaseFragment() {
    private lateinit var binding: FragmentUnlockBinding
    private lateinit var theme: Theme
    private lateinit var batteryReceiver: BatteryStateReceiver
    private var isFromPreview = false
    private var isNativeShowSuccess = false
    private var onPreview = false

    private val completeDialog = CompleteDialog {
        AdsUtils.requestNativePreview(requireActivity(), true)
        if(PreferenceUtils.countSetAnimation % 2 != 0) {
            if(!ratingAppDialog.isAdded) ratingAppDialog.show(requireActivity())
        }
    }

    private val setAnimationDialog = SetAnimationDialog(
        soundCallback = {active -> PreferenceUtils.soundActive = active},
        durationCallback = {SelectDurationBottomSheet().show(requireActivity().supportFragmentManager, "duration bottom sheet")},
        closingMethodCallback = {SelectClosingMethodBottomSheet().show(requireActivity().supportFragmentManager, "select closing bottom sheet")},
        setAnimationCallback = {

            val count = PreferenceUtils.countSetAnimation
            PreferenceUtils.countSetAnimation = count + 1

            if (!loadingDialog.isAdded)
                loadingDialog.show(requireActivity().supportFragmentManager, "loading_dialog")

            ThemeManager.getInstance(requireContext()).downloadThemeImages(theme) {
                if(it) {
                    ThemeManager.getInstance(requireContext()).saveThemeIdToPref(theme.id)
                    ThemeManager.getInstance(requireContext()).currentThemeId = theme.id

                    requireActivity().runOnUiThread {
                        //Done show dialog
                        completeDialog.show(requireActivity().supportFragmentManager, "complete dialog")
                        val intent = Intent(requireContext(), ChargerService::class.java)
                        if(!ChargerService.isServiceRunning) {
                            requireActivity().startService(intent)
                        }
                    }
                }
            }
            loadingDialog.dismiss()
        }
    )
    override fun getViewBinding(): ViewBinding {
        binding = FragmentUnlockBinding.inflate(layoutInflater)
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            theme = it.getParcelable(HomeFragment.CLICK_THEME)!!
            isFromPreview = it.getBoolean(FROM_PREVIEW)
        }


    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated() {
        if(isFromPreview) {
            setAnimationDialog.show(requireActivity().supportFragmentManager, "set animation dialog")
        }
        val imageLoader = ImageLoader.Builder(requireContext())
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }.build()

        theme.let {
            //bind img
            loadingDialog.show(requireActivity().supportFragmentManager, "")
            binding.preview.load(it.thumbnail, imageLoader)
            val imageRequest = ImageRequest.Builder(requireContext())
                .data(it.animation)
                .target(
                    onSuccess = {result ->
                        binding.preview.load(result)
                        loadingDialog.dismiss()
                    },
                    onError = {
                        loadingDialog.dismiss()
                    }
                )
                .build()
            imageLoader.enqueue(imageRequest)
            //apply font
            if(it.fontId.isNotEmpty()) {
                binding.batteryState.typeface = Typeface.createFromFile(File(it.fontId))
            }

            //apply sound


            if(isFromPreview) {
                if(it.sound.isNotEmpty()) {
                    playAudio(it.sound)
                }
            }else {
                AdsUtils.isCloseInterSelect.observe(viewLifecycleOwner) {close ->
                    if(close) {
                        if(it.sound.isNotEmpty()) {
                            playAudio(it.sound)
                        }
                        AdsUtils.isCloseInterSelect.value = false
                    }
                }
            }

        }

        binding.backBtn.setOnClickListener {
            findNavController().navigate(R.id.action_unlockFragment_to_mainFragment)
        }

        binding.downloadBtn.setOnClickListener {
            setAnimationDialog.show(requireActivity().supportFragmentManager, "set animation dialog")
        }

        binding.previewBtn.setOnClickListener {
            binding.navBar.isVisible = false
            binding.frAds.isVisible = false
            onPreview = true
            AdsUtils.requestNativePreview(requireActivity(), true)
        }

        binding.root.setOnTouchListener { _, event ->
            if(event.action == MotionEvent.ACTION_DOWN && onPreview) {
                binding.navBar.isVisible = true
                if(isNativeShowSuccess) {
                    binding.frAds.isVisible = true
                }
                onPreview = false
            }
            true
        }


        batteryReceiver = BatteryStateReceiver {
            binding.batteryState.text = "${it} %"
        }
        requireActivity().registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun registerObservers() {
        AdsUtils.nativePreview.observe(viewLifecycleOwner) {
            if(it != null) {
                isNativeShowSuccess = true
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    it,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner
                )
                if(onPreview) {
                    binding.frAds.isVisible = false
                }
            }
        }

        AdsUtils.nativePreviewLoadFail.observe(viewLifecycleOwner) {
            if(it) {
                isNativeShowSuccess = false
                binding.frAds.isVisible = false
                AdsUtils.nativePreviewLoadFail.value = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        theme.let {
            if(it.sound.isNotEmpty()) {
                playAudio(it.sound)
            }

        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_unlockFragment_to_mainFragment)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }


    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(batteryReceiver)
    }


    companion object {
        const val FROM_PREVIEW = "FROM PREVIEW"
    }
}