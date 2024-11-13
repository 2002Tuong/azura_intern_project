package com.example.videoart.batterychargeranimation.ui.preview

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewbinding.ViewBinding
import by.kirich1409.viewbindingdelegate.viewBinding
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
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.example.videoart.batterychargeranimation.App
import com.example.videoart.batterychargeranimation.R
import com.example.videoart.batterychargeranimation.databinding.FragmentPreviewBinding
import com.example.videoart.batterychargeranimation.extension.FragmentDirections
import com.example.videoart.batterychargeranimation.helper.FileHelper
import com.example.videoart.batterychargeranimation.helper.ThemeManager
import com.example.videoart.batterychargeranimation.model.RemoteTheme
import com.example.videoart.batterychargeranimation.model.Theme
import com.example.videoart.batterychargeranimation.receiver.BatteryStateReceiver
import com.example.videoart.batterychargeranimation.ui.base.BaseFragment
import com.example.videoart.batterychargeranimation.ui.dialog.LoadingDialogFragment
import com.example.videoart.batterychargeranimation.ui.home.HomeFragment
import com.example.videoart.batterychargeranimation.ui.unlock.UnlockFragment
import com.example.videoart.batterychargeranimation.utils.AdsUtils
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.material.resources.TypefaceUtils
import com.slideshowmaker.slideshow.data.remote.DownloadState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import java.lang.IllegalArgumentException

class PreviewFragment : BaseFragment() {
    private lateinit var binding: FragmentPreviewBinding
    private var theme: RemoteTheme? = null
    private var selectTheme: Theme = Theme()
    private lateinit var batteryReceiver: BatteryStateReceiver
    private var imageLoader: ImageLoader? = null
    private val viewModel: PreviewViewModel by inject()
    private var isNativeShowSuccess = false
    private var onPreview = false
    override fun getViewBinding(): ViewBinding {
        binding = FragmentPreviewBinding.inflate(layoutInflater)
        return binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            theme = getClickTheme(it)
            selectTheme = theme!!.toTheme()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated() {
        Log.d("Theme", "${theme}")
        imageLoader = ImageLoader.Builder(requireContext())
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }.build()

        theme?.let {remoteTheme ->
            loadingDialog.show(requireActivity().supportFragmentManager, "")
            binding.preview.load(remoteTheme.thumbUrl, imageLoader!!)
            val imageRequest = ImageRequest.Builder(requireContext())
                .data(remoteTheme.animationUrl)
                .target(
                    onSuccess = {result ->
                        binding.preview.load(result)
                        viewModel.previewHasShow(true)
                    },
                    onError = {
                        viewModel.previewHasShow(true)
                    }
                )
                .build()
            imageLoader!!.enqueue(imageRequest)

            viewModel.downloadFont(remoteTheme.font)
            viewModel.downloadSound(remoteTheme.soundUrl)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { uiState ->
                    if(uiState.downloadSoundState !is DownloadState.Downloading &&
                        uiState.downloadFontState !is DownloadState.Downloading &&
                        uiState.previewShow) {
                        loadingDialog.dismiss()
                    }

                    if(uiState.downloadSoundState is DownloadState.Finished) {
                        musicPlayer.changeMusic(uiState.soundPath)
                        selectTheme = selectTheme.copy(sound = uiState.soundPath)
                    }

                    if(uiState.downloadFontState is DownloadState.Finished) {
                        val typeFace = Typeface.createFromFile(File(uiState.fontPath))
                        binding.batteryState.typeface = typeFace
                        selectTheme = selectTheme.copy(fontId = uiState.fontPath)
                    }

                    if(uiState.downloadAnimationState !is DownloadState.Downloading) {
                        selectTheme = selectTheme.copy(animation = uiState.animationPath)
                        val fontPath = ThemeManager.getInstance(requireContext()).createFontPath(selectTheme.id)
                        val soundPath = ThemeManager.getInstance(requireContext()).createSoundPath(selectTheme.id)

                        FileHelper.copyFileTo(uiState.fontPath, fontPath)
                        FileHelper.copyFileTo(uiState.soundPath, soundPath)
                        ThemeManager.getInstance(requireContext()).downloadThemeImages(selectTheme) {
                            if(it) {
                                ThemeManager.getInstance(requireContext()).saveThemeIdToPref(selectTheme.id)
                                requireActivity().runOnUiThread {
                                    val savedTheme = ThemeManager.getInstance(requireContext())
                                        .getTheme(themeId = selectTheme.id)
                                    loadingDialog.dismiss()
                                    val bundle = bundleOf(HomeFragment.CLICK_THEME to savedTheme, UnlockFragment.FROM_PREVIEW to true)
                                    val action =FragmentDirections.action(
                                        bundle,
                                        R.id.action_previewFragment_to_unlockFragment
                                    )
                                    try {
                                        getNavController().navigate(action)
                                    } catch (_: IllegalArgumentException) {

                                    }
                                }
                            }
                        }
                    }

                }
            }
        }

        binding.backBtn.setOnClickListener {
            getNavController().navigateUp()
        }

        binding.downloadBtn.setOnClickListener {
            if(!loadingDialog.isAdded) {
                loadingDialog.show(requireActivity().supportFragmentManager, "loading_dialog")
            }
            viewModel.downloadAnimation(theme?.animationUrl ?: "")
            AdsUtils.requestNativePreview(requireActivity(), true)
        }

        binding.previewBtn.setOnClickListener {
            binding.navBar.visibility = View.INVISIBLE
            binding.frAds.visibility = View.GONE
            onPreview = true
            AdsUtils.requestNativePreview(requireActivity(), true)
        }

        binding.root.setOnTouchListener { _, event ->
            if(event.action == MotionEvent.ACTION_DOWN && onPreview) {
                binding.navBar.visibility = View.VISIBLE
                if(isNativeShowSuccess) {
                    Log.d("AdsUtil", "call from set on touch")
                    binding.frAds.visibility = View.VISIBLE
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


    override fun onResume() {
        super.onResume()
//        Log.d("Preview", "onResume call")
//        theme?.let {
//            if(it.soundUrl.isNotEmpty()) {
//                musicPlayer.play()
//            }
//
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(batteryReceiver)
    }

    override fun registerObservers() {
        AdsUtils.nativePreview.observe(viewLifecycleOwner) {
            if(it != null) {

                Log.d("AdsUtil", "call to this observe nativePreview ")
                isNativeShowSuccess = true
                VioAdmob.getInstance().populateNativeAdView(
                    requireActivity(),
                    it,
                    binding.frAds,
                    binding.includeNative.shimmerContainerBanner
                )

                if(onPreview) {
                    binding.frAds.visibility = View.GONE
                }
            }
        }

        AdsUtils.nativePreviewLoadFail.observe(viewLifecycleOwner) {
            if(it) {
                Log.d("AdsUtil", "call to this observe nativePreviewLoadFail ")
                isNativeShowSuccess = false
                binding.frAds.visibility = View.GONE
                AdsUtils.nativePreviewLoadFail.value = false
            }
        }

        AdsUtils.isCloseInterSelect.observe(viewLifecycleOwner) {
            if(it) {
                lifecycleScope.launch {
                    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.uiState.collectLatest {state ->
                            if(state.downloadSoundState is DownloadState.Finished) {
                                musicPlayer.play()
                                AdsUtils.isCloseInterSelect.value = false
                            }
                        }
                    }
                }
            }
        }
    }

    fun getClickTheme(bundle: Bundle): RemoteTheme? {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(HomeFragment.CLICK_THEME, RemoteTheme::class.java)
        }else {
            bundle.getParcelable(HomeFragment.CLICK_THEME)
        }
    }

}