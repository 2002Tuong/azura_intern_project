package com.slideshowmaker.slideshow.ui.picker

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.ads.control.ads.VioAdmob
import com.airbnb.epoxy.EpoxyTouchHelper
import com.facebook.ads.Ad
import com.ironsource.mediationsdk.IronSource
import com.slideshowmaker.slideshow.R
import com.slideshowmaker.slideshow.VideoMakerApplication
import com.slideshowmaker.slideshow.data.TrackingFactory
import com.slideshowmaker.slideshow.data.models.enum.MediaType
import com.slideshowmaker.slideshow.data.models.enum.VideoActionType
import com.slideshowmaker.slideshow.data.response.SnapImage
import com.slideshowmaker.slideshow.databinding.ActivityImageConfirmScreenBinding
import com.slideshowmaker.slideshow.ui.HomeActivity
import com.slideshowmaker.slideshow.ui.join_video.JoinVideoActivity2
import com.slideshowmaker.slideshow.ui.picker.view.ConfirmMediaEpoxyModel
import com.slideshowmaker.slideshow.ui.slide_show_v2.ImageSlideShowActivity
import com.slideshowmaker.slideshow.utils.AdsHelper
import com.slideshowmaker.slideshow.utils.LanguageHelper
import org.kodein.di.KodeinAware

class ImageConfirmActivity: AppCompatActivity(),
    KodeinAware, ConfirmPhotoController.OnItemSelectedListener {
    private val layoutBinding: ActivityImageConfirmScreenBinding
        get() = _binding!!
    private var _binding: ActivityImageConfirmScreenBinding? = null
    override val kodein by lazy { (application as VideoMakerApplication).kodein }

    lateinit var currentConstraintSet: ConstraintSet
    private lateinit var selectedMediaController: ConfirmPhotoController
    private var listSnapImage = mutableListOf<SnapImage>()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.loadLocale(this)
        super.onCreate(savedInstanceState)
        TrackingFactory.ImagePicker.openImagePicker().track()
        _binding = ActivityImageConfirmScreenBinding.inflate(layoutInflater)
        setContentView(layoutBinding.root)
        setupUi()
        _binding?.let { layoutBinding ->
            AdsHelper.requestNativeArrangeImage(
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

    private fun setupUi() {
        selectedMediaController = ConfirmPhotoController()
        val listImages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(IMAGE_LIST, SnapImage::class.java)
        } else intent.getParcelableArrayListExtra(IMAGE_LIST)
        listSnapImage = listImages ?: mutableListOf()
        listSnapImage.forEach {
            selectedMediaController.updateUri(it)
        }
        layoutBinding.btnDone.isEnabled =
            (listSnapImage.isNotEmpty() && kindOfMedia != MediaType.PHOTO) || (kindOfMedia == MediaType.PHOTO && listSnapImage.size >= 2) || (canAddMore && listSnapImage.isNotEmpty())
        currentConstraintSet = ConstraintSet()
        currentConstraintSet.clone(layoutBinding.root)
        selectedMediaController.handleCallback = this

        layoutBinding.rvSelectedMedia.setItemSpacingRes(R.dimen.space_tiny)
        layoutBinding.rvSelectedMedia.layoutManager =
            GridLayoutManager(this, 3)
        layoutBinding.rvSelectedMedia.setController(selectedMediaController)
        layoutBinding.ibBack.setOnClickListener {
            finish()
        }

        EpoxyTouchHelper.initDragging(selectedMediaController)
            .withRecyclerView(layoutBinding.rvSelectedMedia)
            .forHorizontalList()
            .withTarget(ConfirmMediaEpoxyModel::class.java)
            .andCallbacks(object : EpoxyTouchHelper.DragCallbacks<ConfirmMediaEpoxyModel>() {
                override fun onModelMoved(
                    fromPosition: Int,
                    toPosition: Int,
                    modelBeingMoved: ConfirmMediaEpoxyModel?,
                    itemView: View?
                ) {
                    selectedMediaController.swapModel(fromPosition, toPosition)
                }
            })
        layoutBinding.btnDone.isVisible = typeOfvideoAction != VideoActionType.TRIM
        layoutBinding.layoutSelectedMedia.isVisible = typeOfvideoAction != VideoActionType.TRIM
        layoutBinding.btnDone.setOnClickListener {
            val listImagePath =
                ArrayList(selectedMediaController.images.map { it.filePath.orEmpty() })
            if (canAddMore) {
                val intent = Intent().apply {
                    putStringArrayListExtra(ImageSlideShowActivity.imagePickedListKey, listImagePath)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            } else if (kindOfMedia == MediaType.PHOTO) {
                val intent = Intent(this, ImageSlideShowActivity::class.java)

                intent.putStringArrayListExtra(
                    ImageSlideShowActivity.imagePickedListKey,
                    listImagePath
                )
                startActivity(intent)
                finish()
            } else {
                if (canAddMore) {
                    val intent = Intent().apply {
                        putStringArrayListExtra("Video picked list", listImagePath)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else if (typeOfvideoAction == VideoActionType.JOIN) {
                    JoinVideoActivity2.gotoActivity(this, listImagePath)
                }
            }
        }
        layoutBinding.btnAdd.setOnClickListener {
//            val intent = Intent(this, HomeActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//                putExtra("play-splash", false)
//            }
//            startActivity(intent)
            val intent = ImagePickerActivity.newIntent(this, MediaType.PHOTO, true, FromScreen.CONFIRM)
            startActivityForResult(intent, ImagePickerActivity.ADD_MORE_PHOTO_REQUEST_CODE)
        }
    }

    override fun onResume() {
        super.onResume()
        IronSource.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        IronSource.onPause(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ImagePickerActivity.ADD_MORE_PHOTO_REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                data?.let {
                    val listImages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        it.getParcelableArrayListExtra(ADD_IMAGE_KEY, SnapImage::class.java)
                    } else it.getParcelableArrayListExtra(ADD_IMAGE_KEY)
                    Log.d("Confirm", "${listImages?.size}")
                    if(listImages == null || listImages.isEmpty() ) {
                        return@let
                    }
                    //listSnapImage.addAll(listImages)
                    listImages.forEach {image->
                        if(selectedMediaController.images.contains(image)) {
                            return@forEach
                        }
                        listSnapImage.add(image)
                        Log.d("Check", "calll")
                        selectedMediaController.updateUri(image)
                    }

                    updateLayoutState(selectedMediaController.images)
                }
            }
        }
    }

    override fun syncSelectedMedias(uris: List<SnapImage>) {
        layoutBinding.groupMedia.isVisible = uris.isNotEmpty()
        updateLayoutState(uris)
    }

    private fun updateLayoutState(selectedImageUris: List<SnapImage>) {
        layoutBinding.btnDone.isClickable =
            (selectedImageUris.isNotEmpty() && kindOfMedia != MediaType.PHOTO) || (kindOfMedia == MediaType.PHOTO && selectedImageUris.size >= 2) || (canAddMore && selectedImageUris.isNotEmpty())
        if (layoutBinding.btnDone.isClickable) {
            layoutBinding.btnDone.setTextColor(resources.getColor(R.color.white, null))
        } else {
            layoutBinding.btnDone.setTextColor(resources.getColor(R.color.b0bec5, null))
        }

        layoutBinding.btnAdd.isClickable = selectedImageUris.isNotEmpty()
        if (layoutBinding.btnAdd.isClickable) {
            layoutBinding.btnAdd.setTextColor(resources.getColor(R.color.white, null))
        } else {
            layoutBinding.btnAdd.setTextColor(resources.getColor(R.color.b0bec5, null))
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

    companion object {
        const val ARG_ADD_MORE = "ARG_ADD_MORE"
        const val ARG_VIDEO_ACTION_KIND = "ARG_VIDEO_ACTION_KIND"
        const val IMAGE_LIST = "image_list"
        const val ADD_IMAGE_KEY = "ADD_IMAGE_KEY"
    }
}