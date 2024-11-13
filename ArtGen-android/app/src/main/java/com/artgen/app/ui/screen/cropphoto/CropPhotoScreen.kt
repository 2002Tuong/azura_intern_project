package com.artgen.app.ui.screen.cropphoto

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artgen.app.R
import com.artgen.app.ads.BannerAdView
import com.artgen.app.ads.BannerAdsManager
import com.artgen.app.extension.loadImageBitmapFromUri
import com.artgen.app.ui.theme.Neutral900
import com.artgen.app.ui.theme.Purple800
import com.artgen.app.utils.LocalAdsManager
import com.artgen.app.utils.navigationBarsPaddingIfNeed
import com.google.android.gms.ads.AdView
import com.smarttoolfactory.cropper.ImageCropper
import com.smarttoolfactory.cropper.model.AspectRatio
import com.smarttoolfactory.cropper.model.OutlineType
import com.smarttoolfactory.cropper.model.RectCropShape
import com.smarttoolfactory.cropper.settings.CropDefaults
import com.smarttoolfactory.cropper.settings.CropOutlineProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropPhotoScreen(
    modifier: Modifier = Modifier,
    uri: Uri,
    onNavigateUp: () -> Unit,
    onNavigateToGenArt: () -> Unit,
    viewModel: CropPhotoViewModel = koinViewModel()
) {

    val adsManager = LocalAdsManager.current
    val adView by LocalAdsManager.current.cropPhotoBannerAd.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.shouldLoadAds) {
        if (uiState.shouldLoadAds) {
            adsManager.loadRewardedVideoAds()
            viewModel.onAdsLoaded()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.crop_photo),
                    style = TextStyle(
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        fontFamily = FontFamily(Font(R.font.roboto)),
                        fontWeight = FontWeight(700),
                        color = Color.White,
                    ),
                    modifier = Modifier
                        .weight(1f),
                    textAlign = TextAlign.Center
                )
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Neutral900
            ), navigationIcon = {
                IconButton(onClick = {
                    onNavigateUp()
                }) {
                    Icon(
                        modifier = Modifier.size(14.dp),
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            })

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            CropPhotoContent(
                uiState = uiState,
                adView = adView,
                uri = uri,
                onNavigateToGenArt = { image ->
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            viewModel.saveCroppedPhoto(image = image)
                        }
                        onNavigateToGenArt()
                    }
                }
            )
        }
    }
}

@Composable
fun CropPhotoContent(
    modifier: Modifier = Modifier,
    uiState: CropPhotoViewModel.UiState,
    adView: AdView?,
    uri: Uri,
    onNavigateToGenArt: (ImageBitmap) -> Unit
) {

    val context = LocalContext.current

    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var originalAspectRatio by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(uri) {
        val bitmap = context.loadImageBitmapFromUri(uri)
        imageBitmap = bitmap

        originalAspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
    }


    var croppedImage by remember { mutableStateOf<ImageBitmap?>(null) }

    var crop by remember { mutableStateOf(false) }

    var cropStyle by remember { mutableStateOf(CropDefaults.style()) }

    val handleSize: Float = LocalDensity.current.run { 20.dp.toPx() }
    var cropProperties by remember {
        mutableStateOf(
            CropDefaults.properties(
                cropOutlineProperty = CropOutlineProperty(
                    OutlineType.Rect,
                    RectCropShape(0, "Rect")
                ),
                handleSize = handleSize
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
    ) {

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {

            imageBitmap?.let {
                ImageCropper(
                    modifier = Modifier.fillMaxWidth(),
                    imageBitmap = it,
                    contentDescription = stringResource(R.string.image_cropper),
                    cropStyle = cropStyle,
                    cropProperties = cropProperties,
                    crop = crop,
                    onCropStart = {
                    },
                    onCropSuccess = { image ->
                        croppedImage = image
                        onNavigateToGenArt(image)
                        crop = false
                    }
                )
            }

            if (uiState.isCroppingPhoto) {
                CircularProgressIndicator()
            }
        }

        SelectCropRatio(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) { cropAspectRatio ->

            cropProperties =
                if (cropAspectRatio == ORIGINAL_ASPECT_RATIO && originalAspectRatio != null) {
                    cropProperties.copy(
                        aspectRatio = AspectRatio(originalAspectRatio!!),
                        fixedAspectRatio = true
                    )
                } else if (cropAspectRatio == FREE_ASPECT_RATIO) {
                    cropProperties.copy(
                        aspectRatio = AspectRatio(cropAspectRatio.aspectRatio.value),
                        fixedAspectRatio = false
                    )
                } else {
                    cropProperties.copy(
                        aspectRatio = AspectRatio(cropAspectRatio.aspectRatio.value),
                        fixedAspectRatio = true
                    )
                }
        }

        TextButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(
                    color = Purple800,
                    shape = RoundedCornerShape(size = 8.dp)
                ),
            onClick = {
                crop = true
            }
        ) {
            Text(
                text = stringResource(R.string.text_continue),
                style = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily(Font(R.font.roboto)),
                    fontWeight = FontWeight(700),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            )
        }

        if (adView != null) {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .navigationBarsPaddingIfNeed()
                    .fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                BannerAdView(adView = adView)
            }
        }

    }

}

@Composable
@Preview
fun PreViewCropPhoto() {
    CropPhotoScreen(uri = Uri.parse(""), onNavigateUp = {}, onNavigateToGenArt = {})
}
