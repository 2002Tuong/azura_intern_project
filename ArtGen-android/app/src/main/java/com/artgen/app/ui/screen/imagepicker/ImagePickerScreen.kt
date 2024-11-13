package com.artgen.app.ui.screen.imagepicker

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.rememberAsyncImagePainter
import com.artgen.app.R
import com.artgen.app.ads.BannerAdsManager
import com.artgen.app.ads.LoadState
import com.artgen.app.ads.MediumNativeAdWrapper
import com.artgen.app.ads.NativeAdWrapper
import com.artgen.app.extension.getDrawable
import com.artgen.app.ui.theme.Neutral800
import com.artgen.app.ui.theme.Neutral900
import com.artgen.app.utils.LocalAdsManager
import com.artgen.app.utils.LocalOpenAdsManager
import com.artgen.app.utils.navigationBarsPaddingIfNeed
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.koin.androidx.compose.koinViewModel
import snapedit.app.remove.data.Image

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ImagePickerScreen(
    canSkipable: Boolean,
    onNavigateUp: () -> Unit,
    onSkipClick: () -> Unit,
    onNavigateToCrop: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ImagePickerViewModel = koinViewModel(),
) {

    val adsManager = LocalAdsManager.current
    val nativeAd by adsManager.imagePickerNativeAd.collectAsStateWithLifecycle()
    val openAdsManager = LocalOpenAdsManager.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionState = rememberMultiplePermissionsState(
        permissions = getPermissions()
    )
    val isPermissionGranted = permissionState.allPermissionsGranted
    BackHandler(true) {
        adsManager.loadImagePickerNativeAd(true)
        onNavigateUp()
    }
    LaunchedEffect(isPermissionGranted) {
        if (isPermissionGranted) {
            viewModel.loadAlbums()
        } else {
            permissionState.launchMultiplePermissionRequest()
        }
    }
    LaunchedEffect(uiState.shouldLoadAds) {
        if (uiState.shouldLoadAds) {
            adsManager.loadBannerAd(BannerAdsManager.BannerAdPlacement.CROP_PHOTO)
            adsManager.loadBannerAd(BannerAdsManager.BannerAdPlacement.GEN_ART)
            viewModel.adLoaded()
        }
    }
    LaunchedEffect(Unit) {
        adsManager.loadImagePickerNativeAd()
    }

    val pagingData = remember(uiState.selectedAlbum) {
        uiState.selectedAlbum?.let { viewModel.getImages(it.id) }
    }
    val images = pagingData?.collectAsLazyPagingItems()

    var captureUri: Uri? = remember { null }
    val context = LocalContext.current
    val cameraCaptureResult = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { isSuccess ->
            if (isSuccess) {
                captureUri?.let { onNavigateToCrop(it) }
            }
        }
    )

    ImagePicker(
        canSkipable = canSkipable,
        isPermissionGranted = isPermissionGranted,
        uiState = uiState,
        nativeAd = nativeAd,
        onNavigateUp = {
            onNavigateUp()
            adsManager.loadImagePickerNativeAd(true)
        },
        onSkipClick = {
            onSkipClick()
            adsManager.loadImagePickerNativeAd(true)
            adsManager.loadRewardedVideoAds()
        },
        onAlbumItemSelected = viewModel::setSelectedAlbum,
        images = images,
        modifier = modifier,
        onImageSelected = viewModel::setSelectedImage,
        onCameraClick = {
            openAdsManager.setShouldShowAppOpenAds(false)
            adsManager.loadImagePickerNativeAd(true)
            captureUri = ArtGenFileProvider.getImageUri(context)
            cameraCaptureResult.launch(captureUri)
        },
        onNavigateToCrop = {
            adsManager.loadImagePickerNativeAd(true)
            onNavigateToCrop(it)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePicker(
    modifier: Modifier = Modifier,
    canSkipable: Boolean,
    isPermissionGranted: Boolean,
    uiState: ImagePickerViewModel.UiState,
    nativeAd: NativeAdWrapper?,
    images: LazyPagingItems<Image>?,
    onNavigateUp: () -> Unit,
    onSkipClick: () -> Unit,
    onAlbumItemSelected: (ImageAlbum) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onCameraClick: () -> Unit,
    onNavigateToCrop: (Uri) -> Unit,
) {
    val context = LocalContext.current
    var showAlbums by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
    ) {
        CenterAlignedTopAppBar(
            title = {
                AnimatedVisibility(visible = isPermissionGranted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxHeight()
                            .clickable { showAlbums = !showAlbums }
                    ) {
                        Text(text = uiState.selectedAlbum?.name.orEmpty())

                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateUp) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = null)
                }
            },
            actions = {
                if (canSkipable) {
                    TextButton(onClick = onSkipClick) {
                        Text(text = stringResource(R.string.skip))
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Neutral900
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize(),
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(124.dp)
                            .clickable { onCameraClick() },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Camera,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )

                            Text(text = stringResource(R.string.camera))
                        }
                    }
                }

                items(images?.itemCount ?: 0) { index ->
                    images?.get(index)?.let {
                        val isSelected = it.uri == uiState.selectedImage
                        Image(
                            painter = rememberAsyncImagePainter(model = it.uri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(124.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onImageSelected(it.uri) },
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                item {

                    if (images?.itemCount == null || images.itemCount <= 0) {

                        val sampleUri = context.getDrawable("sample")
                        val isSelected = uiState.selectedImage == sampleUri
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = sampleUri),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(124.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onImageSelected(sampleUri) },
                                contentScale = ContentScale.Crop
                            )

                            Text(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 4.dp),
                                text = stringResource(R.string.sample),
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight(500),
                                    color = Color.White,
                                )
                            )
                        }

                    }
                }
            }

            if (showAlbums) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Neutral900),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(uiState.allAlbums) {
                        AlbumItem(
                            album = it,
                            onClick = {
                                showAlbums = false
                                onAlbumItemSelected(it)
                            }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Neutral900)
        ) {
            Icon(
                modifier = Modifier
                    .size(32.dp)
                    .background(Neutral800)
                    .clickable {
                        if (uiState.selectedImage != null)
                            onNavigateToCrop(uiState.selectedImage)
                    }
                    .align(Alignment.CenterEnd)
                    .padding(5.dp),
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = "",
                tint = if (uiState.selectedImage != null) Color.White else Color.White.copy(
                    alpha = 0.4f
                ),
            )
        }

        if (nativeAd != null && nativeAd.loadState != LoadState.FAILED) {
            MediumNativeAdWrapper(
                nativeAd = nativeAd,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 4.dp)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                onLoadAd = {}
            )
        }
    }
}

@Composable
private fun AlbumItem(
    album: ImageAlbum,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = album.thumbnailUri),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                .size(72.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp)
        ) {
            Text(
                text = album.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = album.imageCount.toString(),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

private fun getPermissions(): List<String> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }
}