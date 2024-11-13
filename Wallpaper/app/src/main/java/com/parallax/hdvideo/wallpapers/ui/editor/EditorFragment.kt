package com.parallax.hdvideo.wallpapers.ui.editor

import android.net.Uri
import android.view.View
import android.view.ViewTreeObserver
import com.alexvasilkov.gestures.animation.ViewPositionAnimator
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.parallax.hdvideo.wallpapers.R
import com.parallax.hdvideo.wallpapers.databinding.FragmentEditorScreenBinding
import com.parallax.hdvideo.wallpapers.extension.isHidden
import com.parallax.hdvideo.wallpapers.extension.isInvisible
import com.parallax.hdvideo.wallpapers.extension.popFragment2
import com.parallax.hdvideo.wallpapers.services.log.EventSetWall
import com.parallax.hdvideo.wallpapers.services.log.TrackingSupport
import com.parallax.hdvideo.wallpapers.ui.base.fragment.BaseFragmentBinding
import com.parallax.hdvideo.wallpapers.ui.details.PreviewView
import com.parallax.hdvideo.wallpapers.ui.dialog.CongratulationDialog
import com.parallax.hdvideo.wallpapers.ui.dialog.SetWallpaperDialog
import com.parallax.hdvideo.wallpapers.ui.main.MainActivity
import com.parallax.hdvideo.wallpapers.utils.AppConfiguration
import com.parallax.hdvideo.wallpapers.utils.file.FileUtils
import com.parallax.hdvideo.wallpapers.utils.other.Reflector
import com.parallax.hdvideo.wallpapers.utils.wallpaper.WallpaperHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditorFragment: BaseFragmentBinding<FragmentEditorScreenBinding, EditorViewModel>() {

    companion object {
        const val TAGS = "EditorFragment"
    }

    override val resLayoutId: Int
        get() = R.layout.fragment_editor_screen
    private var uri: Uri? = null
    private lateinit var requestManager: RequestManager

    override fun init(view: View) {
        super.init(view)
        uri = arguments?.getParcelable(TAGS) as? Uri
        dataBinding.containerTop.layoutParams.height = AppConfiguration.statusBarSize + resources.getDimensionPixelOffset(R.dimen.height_header)
        dataBinding.backButton.setOnClickListener { onBackPressed() }
        requestManager = Glide.with(this)
        requestManager.load(uri).into(dataBinding.imageViewFull)
        dataBinding.titleTv.text = FileUtils.getFileNameFromUri(uri)
        dataBinding.previewCheckbox.setOnCheckedChangeListener { _, isChecked ->
            setPreviewLayout(isChecked)
        }
        dataBinding.setWallpaperButton.setOnClickListener {
            SetWallpaperDialog.show(requireActivity().supportFragmentManager,callback = {
                viewModel.setWallpaper(
                    uri,
                    WallpaperHelper.WallpaperType.init(it)
                )
            })
        }
        dataBinding.imageViewFull.setOnClickListener {
            setupStatusView(!dataBinding.setWallpaperButton.isHidden)
        }
        setupStatusView(false)
        setUpObservers()
    }

    private fun setUpObservers() {
        viewModel.hasSetWallpaperLiveData.observe(viewLifecycleOwner) {
            if (it) {
                TrackingSupport.recordEvent(EventSetWall.SetWallSuccessFromDeviceImage)
                CongratulationDialog.show(parentFragmentManager)
                popFragment2(this)
            }
            else showToast(R.string.setting_wallpaper_error)
        }
    }

    private fun setPreviewLayout(isPreviewView: Boolean) {
        if (isPreviewView) {
            if (dataBinding.previewLayout.childCount > 0) return
            dataBinding.previewLayout.isInvisible = false
            val view = PreviewView(requireContext()).getView()
            dataBinding.previewLayout.translationX = dataBinding.previewLayout.width.toFloat()
            dataBinding.previewLayout.addView(view)
            dataBinding.previewLayout.animate().translationX(0f).setDuration(300).start()
            dataBinding.imageViewFull.controller.resetState()
        } else {
            dataBinding.previewLayout.removeAllViews()
            dataBinding.previewLayout.isInvisible = true
        }
        setupStatusView(isPreviewView)
    }

    private fun setupStatusView(isHidden: Boolean) {
        setStatusBar(!isHidden, isLight = true)
        dataBinding.setWallpaperButton.isHidden = isHidden
        dataBinding.containerTop.isHidden = isHidden
    }

    override fun onDestroyView() {
        try {
            if (isInitialized)
                Reflector.getDeclaredField<ViewPositionAnimator>(dataBinding.imageViewFull, "positionAnimator")?.also {
                    Reflector.getDeclaredField<ViewTreeObserver.OnPreDrawListener>(it, "toPosHolder")?.also { listener ->
                        dataBinding.imageViewFull.viewTreeObserver.removeOnPreDrawListener(listener)
                    }
                }
        }catch (e: Exception) { }
        setStatusBar(true, isLight = false)
        (activity as? MainActivity)?.pauseOrPlayVideo(true)
        super.onDestroyView()
    }
}