package com.slideshowmaker.slideshow.ui.picker

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ads.control.ads.VioAdmob
import com.airbnb.epoxy.EpoxyTouchHelper
import com.airbnb.epoxy.EpoxyTouchHelper.DragCallbacks
import com.google.android.gms.ads.AdView
import com.ironsource.mediationsdk.IronSource
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.data.TrackingFactory
import com.slideshowmaker.slideshow.data.models.enum.MediaType
import com.slideshowmaker.slideshow.data.models.enum.VideoActionType
import com.slideshowmaker.slideshow.data.response.PhotoAlbum
import com.slideshowmaker.slideshow.data.response.SnapImage
import com.slideshowmaker.slideshow.databinding.ActivityImagePickerScreenBinding
import com.slideshowmaker.slideshow.ui.dialog.ConfirmPopupFragment
import com.slideshowmaker.slideshow.ui.dialog.PermissionPopupFragment
import com.slideshowmaker.slideshow.ui.join_video.JoinVideoActivity2
import com.slideshowmaker.slideshow.ui.picker.view.SelectedMediaEpoxyModel
import com.slideshowmaker.slideshow.ui.slide_show_v2.ImageSlideShowActivity
import com.slideshowmaker.slideshow.ui.trim_video.TrimVideoActivity
import com.slideshowmaker.slideshow.utils.AdsHelper
import com.slideshowmaker.slideshow.utils.LanguageHelper
import com.slideshowmaker.slideshow.utils.extentions.getEnumExtra
import com.slideshowmaker.slideshow.utils.extentions.orFalse
import com.slideshowmaker.slideshow.utils.extentions.putExtra
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

open class ImagePickerActivity :
    AppCompatActivity(),
    ImagePickerController.OnItemSelectedListener,
    AlbumPickerController.OnAlbumSelectedListener,
    KodeinAware, SelectedMediaController.OnItemSelectedListener {
    private val layoutBinding: ActivityImagePickerScreenBinding
        get() = _binding!!
    private var _binding: ActivityImagePickerScreenBinding? = null
    private lateinit var selectedMediaControllerObj: SelectedMediaController
    private lateinit var imagePickerControllerObj: ImagePickerController
    private lateinit var albumPickerControllerObj: AlbumPickerController
    override val kodein by lazy { (application as VideoMakerApplication).kodein }

    val viewModel: ImagePickerViewModel by instance()
    private var loadImageJob: Job? = null
    private var loadAlbumJob: Job? = null
    lateinit var curConstraintSet: ConstraintSet

    private var permissionPopup: PermissionPopupFragment? = null
    private lateinit var adView: AdView
    private var initialLayoutComplete = false

    private val premiumPlanLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                viewModel.selectedUri?.let { uri -> handleImageSelected(uri) }
            }
        }

    private val kindOfMedia by lazy {
        MediaType.valueOf(intent.getStringExtra("MediaKind") ?: MediaType.PHOTO.name)
    }
    private val typeOfvideoAction by lazy {
        VideoActionType.valueOf(
            intent.getStringExtra(ARG_VIDEO_ACTION_KIND) ?: VideoActionType.SLIDE.name
        )
    }
    private val canAddMore by lazy {
        intent.getBooleanExtra(ARG_ADD_MORE, false)
    }

    private val fromScreen by lazy {
        intent.getEnumExtra<FromScreen>() ?: FromScreen.HOME
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.loadLocale(this)
        super.onCreate(savedInstanceState)
        TrackingFactory.ImagePicker.openImagePicker().track()
        _binding = ActivityImagePickerScreenBinding.inflate(layoutInflater)
        setContentView(layoutBinding.root)
        setupUi()
        setupLoader()
        if (checkStoragePermission()) {
            askForPermission()
        }
        _binding?.let { layoutBinding ->
            AdsHelper.requestNativeChooseImage(
                this,
                onLoaded = {
                    VioAdmob.getInstance().populateNativeAdView(
                        this,
                        it,
                        layoutBinding.frAds,
                        layoutBinding.includeNative.shimmerContainerBanner
                    )
                },
                onLoadFail = {
                    layoutBinding.frAds.visibility = View.GONE
                })
        }
        hideNavigationBar()
    }

    override fun onResume() {
        super.onResume()
        IronSource.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }

    private fun checkStoragePermission(): Boolean { //true if GRANTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return when (kindOfMedia) {
                MediaType.PHOTO -> {
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED
                }

                MediaType.VIDEO -> {
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_VIDEO
                    ) == PackageManager.PERMISSION_GRANTED
                }
            }
        } else {
            return ActivityCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun setupUi() {
        layoutBinding.tvTitle.text = if (kindOfMedia == MediaType.PHOTO) {
            getString(R.string.image_picker_section_all_photos)
        } else {
            getString(R.string.image_picker_section_all_videos)
        }
        if (!canAddMore && kindOfMedia == MediaType.PHOTO) {
            layoutBinding.tvPhotoGuideline.text =
                "${layoutBinding.tvPhotoGuideline.text}\n${getString(R.string.photo_picker_minimum_guideline)}"
        }
        curConstraintSet = ConstraintSet()
        curConstraintSet.clone(layoutBinding.root)
        imagePickerControllerObj = ImagePickerController(this)
        selectedMediaControllerObj = SelectedMediaController()
        selectedMediaControllerObj.handleCallback = this
        albumPickerControllerObj = AlbumPickerController()
        albumPickerControllerObj.handleCallback = this

        layoutBinding.rvSelectedMedia.setItemSpacingRes(R.dimen.margin_1)
        layoutBinding.rvSelectedMedia.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        layoutBinding.rvSelectedMedia.setController(selectedMediaControllerObj)

        layoutBinding.rvGallery.setController(imagePickerControllerObj)
        layoutBinding.rvGallery.setItemSpacingRes(R.dimen.space_tiny)
        layoutBinding.rvGallery.layoutManager = GridLayoutManager(this, 3)
        layoutBinding.rvAlbum.layoutManager = LinearLayoutManager(this)
        layoutBinding.rvAlbum.setController(albumPickerControllerObj)
        layoutBinding.layoutSelectedAlbum.setOnClickListener {
            animateAlbum(!layoutBinding.layoutAlbum.isVisible)
        }
        layoutBinding.layoutAlbum.setOnClickListener {
            animateAlbum(false)
        }
        layoutBinding.ibBack.setOnClickListener {
            onBackPressed()
        }
        imagePickerControllerObj.addLoadStateListener {
            if (it.append.endOfPaginationReached) {
                imagePickerControllerObj.requestModelBuild()
            }
        }
        layoutBinding.ibClearAll.setOnClickListener {
            ConfirmPopupFragment.Builder()
                .setTitle(getString(R.string.popup_title_delete_item))
                .setContent(getString(R.string.popup_content_delete_item))
                .setOkText(getString(R.string.regular_delete))
                .setCancelText(getString(R.string.regular_cancel))
                .setOkCallBack {
                    imagePickerControllerObj.clear()
                    selectedMediaControllerObj.clear()
                }
                .build()
                .show(supportFragmentManager, "ConfirmDialogFragment")
        }

        EpoxyTouchHelper.initDragging(selectedMediaControllerObj)
            .withRecyclerView(layoutBinding.rvSelectedMedia)
            .forHorizontalList()
            .withTarget(SelectedMediaEpoxyModel::class.java)
            .andCallbacks(object : DragCallbacks<SelectedMediaEpoxyModel>() {
                override fun onModelMoved(
                    fromPosition: Int,
                    toPosition: Int,
                    modelBeingMoved: SelectedMediaEpoxyModel?,
                    itemView: View?
                ) {
                    selectedMediaControllerObj.swapModel(fromPosition, toPosition)
                }
            })
        layoutBinding.btnNext.isVisible = typeOfvideoAction != VideoActionType.TRIM
        val listImage = ArrayList(imagePickerControllerObj.selectedImages)

        layoutBinding.btnNext.isEnabled =
            (listImage.isNotEmpty() && kindOfMedia != MediaType.PHOTO) || (kindOfMedia == MediaType.PHOTO && listImage.size >= 2) || (canAddMore && listImage.isNotEmpty())
        layoutBinding.layoutSelectedMedia.isVisible = typeOfvideoAction != VideoActionType.TRIM
        layoutBinding.btnNext.setOnClickListener {
            val listImagesPath =
                ArrayList(imagePickerControllerObj.selectedImages.map { it.filePath.orEmpty() })
            val listImage = ArrayList(imagePickerControllerObj.selectedImages)
            if (canAddMore) {
                if(fromScreen == FromScreen.SLIDESHOW) {
                    val intent = Intent().apply {
                        putStringArrayListExtra(ImageSlideShowActivity.imagePickedListKey, listImagesPath)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }else if(fromScreen == FromScreen.CONFIRM) {
                    Log.d("Confirm", "call to this")
                    Log.d("Confirm", "${listImage.size}")
                    val intent = Intent().apply {
                        putParcelableArrayListExtra(ImageConfirmActivity.ADD_IMAGE_KEY, listImage)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            } else if (kindOfMedia == MediaType.PHOTO) {
                val intent = Intent(this, ImageConfirmActivity::class.java)
                Log.d("Confirm", "${listImage.size}")
                intent.putParcelableArrayListExtra(ImageConfirmActivity.IMAGE_LIST, listImage)

                startActivity(intent)
            } else {
                if (canAddMore) {
                    val intent = Intent().apply {
                        putStringArrayListExtra("Video picked list", listImagesPath)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else if (typeOfvideoAction == VideoActionType.JOIN) {
                    JoinVideoActivity2.gotoActivity(this, listImagesPath)
                }
            }
        }
    }

    private fun setupLoader() {
        if (loadAlbumJob?.isCancelled == true) {
            loadAlbumJob?.cancel()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (loadAlbumJob != null && !loadAlbumJob?.isCancelled.orFalse() && !loadAlbumJob?.isActive.orFalse()) {
                    loadAlbumJob?.start()
                } else {
                    TrackingFactory.ImagePicker.startLoadAlbums().track()
                    loadAlbumJob = launch {
                        viewModel.getAlbums(kindOfMedia).collect { it ->
                            albumPickerControllerObj.mediAlbum = it
                            albumPickerControllerObj.requestModelBuild()
                            it.firstOrNull()?.let {
                                startLoadImages(it)
                                imagePickerControllerObj.photoAlbum = it
                            }
                        }
                    }
                }
            }
        }
    }


//    private fun showNativeAds(ads: NativeAd) {
//        if (_binding == null) return
//        val adView = binding.adView.root
//        adView.headlineView = binding.adView.tvHeadline
//        adView.bodyView = binding.adView.tvBody
//        adView.callToActionView = binding.adView.btnAction
//        adView.iconView = binding.adView.imvIcon
//        binding.adView.groupContent.isVisible = true
//        (adView.headlineView as TextView).text = ads.headline
//        (adView.bodyView as TextView).text = ads.body
//        (adView.callToActionView as Button).text = ads.callToAction
//        (adView.iconView as ImageView).setImageDrawable(ads.icon?.drawable)
//        adView.setNativeAd(ads)
//    }

    private fun animateAlbum(isVisible: Boolean) {
        layoutBinding.layoutAlbum.isVisible = isVisible
        val rotFrom = if (isVisible) 0f else -180f
        val rotTo = if (isVisible) -180f else 0f
        val rotateAnimation = RotateAnimation(
            rotFrom,
            rotTo,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
        )
        rotateAnimation.duration = 200
        rotateAnimation.fillAfter = true
    }

    private fun startLoadImages(album: PhotoAlbum) {
        if (loadImageJob?.isCancelled == true) {
            loadImageJob?.cancel()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                TrackingFactory.ImagePicker.startLoadImages().track()
                loadImageJob = launch {
                    viewModel.getImages(album.id, kindOfMedia).collect {
                        imagePickerControllerObj.submitData(it)
                    }
                }
            }
        }
    }

    private fun askForPermission() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (kindOfMedia) {
                MediaType.PHOTO -> {
                    listOf(
                        READ_MEDIA_IMAGES
                    )
                }

                MediaType.VIDEO -> {
                    listOf(
                        READ_MEDIA_VIDEO
                    )
                }
            }
        } else {
            listOf(
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE
            )
        }

        Dexter.withContext(this)
            .withPermissions(permissions)
            .withListener(
                object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        if (p0?.areAllPermissionsGranted().orFalse()) {
                            setupLoader()
                        } else if (p0?.isAnyPermissionPermanentlyDenied.orFalse()) {
                            showPermissionDialog()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                        TrackingFactory.ImagePicker.onPermissionRationaleShouldBeShown().track()
                        showPermissionDialog()
                    }

                })
    }

    private fun showPermissionDialog() {
        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return

        permissionPopup = PermissionPopupFragment()
        permissionPopup?.show(supportFragmentManager, "")
        permissionPopup?.goSettingHandle = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onImageSelected(uri: SnapImage, isSample: Boolean) {
        if (typeOfvideoAction == VideoActionType.TRIM) {
            TrimVideoActivity.gotoActivity(this, uri.filePath.orEmpty())
            return
        }
        imagePickerControllerObj.toggleSelectedImage(uri)
        updateSelectedMedia(uri)
        updateLayoutState(imagePickerControllerObj.selectedImages)
    }

    private fun updateSelectedMedia(uri: SnapImage) {
        selectedMediaControllerObj.updateUri(uri)
        layoutBinding.groupMedia.isVisible = selectedMediaControllerObj.snapImages.isNotEmpty()
        layoutBinding.groupEmptyView.isVisible = selectedMediaControllerObj.snapImages.isEmpty()
        layoutBinding.tvSelectedMediaCount.text = buildSpannedString {
            color(ContextCompat.getColor(this@ImagePickerActivity, R.color.orange_900)) {
                append(selectedMediaControllerObj.snapImages.size.toString())
            }
            append(" ")
            append(getString(R.string.photo_selected_label))
        }
    }

    private fun handleImageSelected(uri: Uri) {
        if (intent.getBooleanExtra(ARG_SHOULD_SEND_RESULT, false)) {
        } else {
        }
    }

    override fun syncSelectedMedias(uris: List<SnapImage>) {
        imagePickerControllerObj.syncSelectedMedias(uris)
        layoutBinding.groupMedia.isVisible = uris.isNotEmpty()
        layoutBinding.groupEmptyView.isVisible = uris.isEmpty()
        updateLayoutState(uris)
    }

    private fun updateLayoutState(selectedImageUris: List<SnapImage>) {
        layoutBinding.btnNext.isEnabled =
            (selectedImageUris.isNotEmpty() && kindOfMedia != MediaType.PHOTO) || (kindOfMedia == MediaType.PHOTO && selectedImageUris.size >= 2) || (canAddMore && selectedImageUris.isNotEmpty())
        if (selectedImageUris.isEmpty()) {
            layoutBinding.btnNext.setText(R.string.button_next_label)
        } else {
            layoutBinding.btnNext.text =
                getString(R.string.button_next_format_label, selectedImageUris.size)
        }
    }

    private fun hideNavigationBar() {
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        window.decorView.systemUiVisibility = flags
        window.decorView.setOnSystemUiVisibilityChangeListener {
            if ((it and View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                window.decorView.systemUiVisibility = flags
            }
        }
    }

    override fun onCameraSelected() {

    }

    override fun onBackPressed() {
        if (layoutBinding.layoutAlbum.isVisible) {
            animateAlbum(false)
            return
        }
        super.onBackPressed()
    }

    override fun onAlbumSelected(album: PhotoAlbum) {
        animateAlbum(false)
        imagePickerControllerObj.photoAlbum = album
        layoutBinding.tvTitle.text = album.name
        startLoadImages(album)
    }

    companion object {
        const val FEATURE = "FEATURE"
        const val EXTRA_CLOSE = "extra_close"
        const val EXTRA_ACTION = "action"
        const val ARG_ADD_MORE = "ARG_ADD_MORE"
        const val ARG_SHOULD_SEND_RESULT = "SHOULD_SEND_RESULT"
        const val ARG_SELECTED_MEDIAS = "ARG_SELECTED_MEDIAS"
        const val ARG_VIDEO_ACTION_KIND = "ARG_VIDEO_ACTION_KIND"
        const val ADD_MORE_VIDEO_REQUEST_CODE = 2003
        const val ADD_MORE_PHOTO_REQUEST_CODE = 2004
        const val ADD_MORE_PHOTO_REQUEST_CODE_IN_CONFIRM = 2005
        const val ARG_FROM_SCREEN = "ARG_FROM_SCREEN"


        internal fun newIntent(
            context: Context,
            mediaKind: MediaType,
            addMore: Boolean,
            from: FromScreen = FromScreen.HOME
        ) = Intent(context, ImagePickerActivity::class.java).apply {
            putExtra("MediaKind", mediaKind.toString())
            putExtra(ARG_ADD_MORE, addMore)
            putExtra(from)
        }

        internal fun newIntent(
            context: Context,
            videoActionType: VideoActionType, addMore: Boolean
        ) = Intent(context, ImagePickerActivity::class.java).apply {
            putExtra("MediaKind", MediaType.VIDEO.toString())
            putExtra(ARG_VIDEO_ACTION_KIND, videoActionType.toString())
            putExtra(ARG_ADD_MORE, addMore)
        }
    }
}

enum class FromScreen {
    HOME, CONFIRM, SLIDESHOW
}