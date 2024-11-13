package com.bloodpressure.app.screen.barcodescan

import android.Manifest
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.TextureView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ui.theme.BloodPressureAndroidTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun BarcodeScanScreen(
    modifier: Modifier = Modifier,
    viewModel: BarcodeScanViewModel = koinViewModel(),
    onNavigateUp: () -> Unit = {}
) {

    val context = LocalContext.current
    val cameraPreviewTextureView = remember { TextureView(context) }

    val configuration = LocalConfiguration.current

    val cameraPermissionState: PermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(cameraPermissionState.status.isGranted) {
        if(cameraPermissionState.status.isGranted) {
            viewModel.startDetectScreen()
        }
    }

    BackHandler(true) {
        if(uiState.screenState == ScreenState.INTRO) {
            onNavigateUp()
        }else {
            viewModel.stopDetectCode()
            onNavigateUp()
        }
    }
    BarcodeScanContent(
        uiState = uiState,
        onRequestPermission = {
            cameraPermissionState.launchPermissionRequest()
        },
        onSurfaceTextureAvailable = { surface, width, height ->
            viewModel.setSurfaceSize(width, height)
            viewModel.addPreviewSurface(surface)
            viewModel.startDetectCode()
        },
        textureView = cameraPreviewTextureView,
        scanningHeight = configuration.screenWidthDp.dp* 4 / 9,
        scanningWidth = configuration.screenWidthDp.dp* 2 / 3,
        onNavigationBack = {
            if(uiState.screenState == ScreenState.INTRO) {
                onNavigateUp()
            }else {
                viewModel.stopDetectCode()
                onNavigateUp()
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScanContent(
    uiState: BarcodeScanViewModel.UiState,
    textureView: TextureView,
    onRequestPermission: () -> Unit = {},
    onNavigationBack: () -> Unit= {},
    onSurfaceTextureAvailable: (SurfaceTexture, Int, Int) -> Unit = { _: SurfaceTexture, _: Int, _: Int -> },
    scanningWidth: Dp,
    scanningHeight: Dp,
) {
    when(uiState.screenState) {
        ScreenState.INTRO -> BarcodeScanIntro(uiState= uiState,
            onButtonClick = onRequestPermission,
            onNavigationBack = onNavigationBack)
        ScreenState.BEGIN -> BarcodeScanMain(
            cameraPreviewTextureView = textureView,
            onNavigationBack = onNavigationBack,
            onSurfaceTextureAvailable = onSurfaceTextureAvailable,
            scanningWidth = scanningWidth,
            scanningHeight = scanningHeight,
            uiState = uiState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScanIntro(
    modifier: Modifier = Modifier,
    onNavigationBack:() -> Unit={},
    onButtonClick: () -> Unit = {},
    uiState: BarcodeScanViewModel.UiState
) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = {},
            navigationIcon = {
                IconButton(
                    onClick = {onNavigationBack()},
                ) { Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null) }
            }
        )
        Icon(
            modifier = Modifier.size(60.dp),
            imageVector = Icons.Filled.Camera,
            contentDescription = null
        )

        Spacer(modifier = Modifier.size(30.dp))
        Text(
            text = stringResource(R.string.camera_permission),
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.size(30.dp))
        Text(
            modifier = Modifier.padding(horizontal = 30.dp),
            text = stringResource(R.string.camera_permission_request),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.size(20.dp))
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.5f),
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(229,103,23),
                contentColor = Color.White
            )
        ) {
            Text(
                text = stringResource(R.string.enable).uppercase(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScanMain(
    modifier: Modifier = Modifier,
    cameraPreviewTextureView: TextureView,
    onNavigationBack: () -> Unit = {},
    onSurfaceTextureAvailable: (SurfaceTexture, Int, Int) -> Unit = {_: SurfaceTexture, _: Int, _: Int -> },
    scanningWidth: Dp,
    scanningHeight: Dp,
    uiState: BarcodeScanViewModel.UiState
) {

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded)
    )
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            DetectResult(
                modifier = Modifier
                    .fillMaxWidth(),
                uiState = uiState
            )
        },
        sheetPeekHeight = 0.dp,
        sheetSwipeEnabled = false,
        sheetContainerColor = Color.White,
    ) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(
                title = {},
                navigationIcon = { IconButton(onClick = {onNavigationBack()} ) {
                    Icon(
                        imageVector =  Icons.Filled.ArrowBack,
                        contentDescription = "Back to last screen"
                    )
                }
                },
                actions = {
                    TextButton(onClick = {},
                        modifier = Modifier.background(Color.Transparent),
                    ) {
                        Text(stringResource(R.string.history))
                    }
                }
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                AndroidView(
                    modifier = Modifier.background(Color.Black),
                    factory = {
                        cameraPreviewTextureView.apply {
                            surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                                override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                                    surface.setDefaultBufferSize(width, height)
                                    Log.d("Scancode", "Surface texture size: ${width} | ${height}")
                                    onSurfaceTextureAvailable(surface, width, height)
                                }

                                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                                }

                                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                                    return true
                                }

                                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                                }
                            }
                        }
                    }
                )

                ScanningView(
                    modifier = Modifier.padding(top = 10.dp),
                    stopScanning = uiState.detectState != DetectResult.UNDETECT,
                    width = scanningWidth,//configuration.screenWidthDp.dp* 2 / 3,
                    height = scanningHeight //configuration.screenWidthDp.dp* 4 / 9
                )

            }
        }
    }
}
@Composable
private fun DetectResult(
    modifier: Modifier = Modifier,
    uiState: BarcodeScanViewModel.UiState
) {
    Column(
        modifier = modifier.height(330.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(uiState.detectState == DetectResult.UNDETECT) {
            Text(
                text = stringResource(R.string.scan_barcode),
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )

            Image(
                painter = painterResource(R.drawable.qr_code_scanner),
                contentDescription = null
            )

            Text(
                modifier = Modifier.fillMaxWidth(0.7f),
                text = stringResource(R.string.scan_barcode_attention),
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
        } else {
            Text(
                text = if (uiState.detectState == DetectResult.FAIL)  stringResource(R.string.cannot_find_data) else uiState.detectResult,
                textAlign = TextAlign.Center,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview
@Composable
private fun BarcodeScreenPreview(
) {
    BloodPressureAndroidTheme {
        //BarcodeScanScreen()
        //BarcodeScanIntro()
        DetectResult(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            uiState = BarcodeScanViewModel.UiState()
        )
    }
}

