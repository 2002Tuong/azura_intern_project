package com.bloodpressure.app.screen.heartrate.measure

import android.graphics.SurfaceTexture
import android.view.TextureView
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.screen.alarm.SetAlarmDialog
import com.bloodpressure.app.screen.home.tracker.TrackerType
import com.bloodpressure.app.ui.component.TopAppBarAction
import com.bloodpressure.app.ui.component.rememberLifecycleEvent
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.Logger
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HeartRateMeasureScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onFinishMeasure: (Int, HeartRateRecord?) -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: HeartRateMeasureViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermissionState: PermissionState = rememberPermissionState(permission = android.Manifest.permission.CAMERA)
    var pendingCameraPermission by remember { mutableStateOf(false) }
    val lifecycleEvent = rememberLifecycleEvent()

    LaunchedEffect(lifecycleEvent) {

        if (uiState.screenState == HeartRateMeasureViewModel.ScreenState.BEGIN) {
            if (lifecycleEvent == Lifecycle.Event.ON_RESUME) {
                if (uiState.isMeasureFlashEnabled) {
                    viewModel.turnFlashOn(true)
                }
            } else if (lifecycleEvent == Lifecycle.Event.ON_PAUSE || lifecycleEvent == Lifecycle.Event.ON_DESTROY) {
                viewModel.turnFlashOn(false)
            }
        }
    }
    LaunchedEffect(cameraPermissionState.status.isGranted){
        if (pendingCameraPermission)
            viewModel.startMeasureScreen()
    }

    LaunchedEffect(uiState.finishMeasure) {
        if (uiState.finishMeasure) {

            if (uiState.heartRate in 40..220) {
                onFinishMeasure(uiState.heartRate, uiState.quickHeartRateRecord)
            } else {
                viewModel.showErrorDialog(true)
            }

        }
    }

    if (uiState.showSetAlarmDialog) {
        SetAlarmDialog(
            alarmType = AlarmType.HEART_RATE,
            onDismissRequest = { viewModel.showSetReminder(false) },
            onSave = { alarmRecord ->
                viewModel.insertRecord(alarmRecord)
                viewModel.showSetReminder(false)
            }
        )
    }

    if (uiState.showIntroDialog) {
        IntroductionDialog(onDismissRequest = { viewModel.showIntroDialog(false) })
    }

    if (uiState.showErrorDialog) {
        MeasureErrorDialog {
            viewModel.showErrorDialog(false)
            onNavigateUp()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.measure), style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                    )
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            navigationIcon = {
                IconButton(
                    onClick = {
                        when (uiState.screenState) {
                            HeartRateMeasureViewModel.ScreenState.INTRO -> onNavigateUp.invoke()

                            HeartRateMeasureViewModel.ScreenState.BEGIN -> {
                                viewModel.returnToIntroScreen()
                            }
                        }

                    }
                ) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.Close),
                        contentDescription = null
                    )
                }
            },
            actions = {
                TopAppBarAction(
                    isPurchased = uiState.isPurchased,
                    onSetAlarmClick = { viewModel.showSetReminder(true) },
                    onNavigateToPremium = onNavigateToPremium,
                    additionalAction = {

                        if (uiState.screenState == HeartRateMeasureViewModel.ScreenState.BEGIN) {
                            IconButton(onClick = viewModel::toggleSound) {
                                Image(
                                    painter = painterResource(id = if (uiState.isSoundOn) R.drawable.ic_sound_on else R.drawable.ic_sound_mute),
                                    contentDescription = null,
                                    modifier = Modifier.size(21.dp),
                                    contentScale = ContentScale.Inside
                                )
                            }
                            IconButton(onClick = viewModel::toggleFlash) {
                                Image(
                                    painter = painterResource(id = if (uiState.isMeasureFlashEnabled) R.drawable.ic_flash_on else R.drawable.ic_flash_off),
                                    contentDescription = null,
                                    modifier = Modifier.size(21.dp),
                                    contentScale = ContentScale.Inside
                                )
                            }
                        }
                    }
                )

            }
        )

        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(Color(0xFFECEDEF))
        )

        HeartRateMeasureContent(
            modifier = modifier
                .fillMaxSize()
                .weight(1f),
            uiState = uiState, onStartMeasure = {

                if (cameraPermissionState.status.isGranted) {
                    viewModel.startMeasureScreen()
                } else {
                    pendingCameraPermission = true
                    cameraPermissionState.launchPermissionRequest()
                }

            },
            onDismissDialog = { viewModel.dismissNoticeDialog() },
            onSurfaceTextureAvailable = { surface ->
                viewModel.addPreviewSurface(surface)
                viewModel.startFingerDetection()
            },
            onInstructionClick = {
                viewModel.showIntroDialog(true)
            }
        )

        val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()
        if (uiState.isAdsEnabled && adView != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPaddingIfNeed(),
                contentAlignment = Alignment.BottomCenter
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = {
                        adView!!.apply { (parent as? ViewGroup)?.removeView(this) }
                    }
                )
            }
        }
    }
}

@Composable
private fun HeartRateMeasureContent(
    modifier: Modifier = Modifier,
    uiState: HeartRateMeasureViewModel.UiState,
    onStartMeasure: () -> Unit,
    onDismissDialog: () -> Unit,
    onSurfaceTextureAvailable: (SurfaceTexture) -> Unit,
    onInstructionClick: () -> Unit
) {

    when (uiState.screenState) {
        HeartRateMeasureViewModel.ScreenState.INTRO -> {
            HeartRateMeasureIntro(
                modifier = modifier,
                uiState = uiState,
                onStartMeasure = onStartMeasure,
                onDismissDialog = onDismissDialog,
                onInstructionClick = onInstructionClick
            )
        }

        HeartRateMeasureViewModel.ScreenState.BEGIN -> {
            HeartRateMeasureBegin(
                modifier = modifier,
                uiState = uiState,
                onSurfaceTextureAvailable = onSurfaceTextureAvailable
            )
        }
    }
}

@Composable
private fun HeartRateMeasureIntro(
    modifier: Modifier = Modifier,
    uiState: HeartRateMeasureViewModel.UiState,
    onStartMeasure: () -> Unit,
    onDismissDialog: () -> Unit,
    onInstructionClick: () -> Unit
) {

    if (uiState.showNoticeDialog) {
        NoticeDialog(onDismissRequest = onDismissDialog)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPaddingIfNeed(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier
                .fillMaxWidth(0.6f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(120.dp))
                .clickable { onStartMeasure() }) {
                Image(
                    modifier = Modifier.fillMaxSize(), painter = painterResource(id = R.drawable.heart_rate_measure_img), contentDescription = ""
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(modifier = Modifier.size(56.dp), painter = painterResource(id = R.drawable.ic_fingerprint), contentDescription = "")
                    Text(
                        modifier = Modifier.padding(top = 10.dp), text = stringResource(R.string.start_measure), style = TextStyle(
                            fontSize = 18.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight(700),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        if (uiState.screenState == HeartRateMeasureViewModel.ScreenState.INTRO) {
            IconButton(onClick = onInstructionClick) {
                Image(
                    painter = painterResource(id = R.drawable.ic_question_circle),
                    contentDescription = null,
                    modifier = Modifier.size(21.dp),
                    contentScale = ContentScale.Inside
                )
            }
        }

        Image(
            modifier = Modifier.fillMaxWidth(0.7f), painter = painterResource(id = R.drawable.measure_instruct_img), contentDescription = ""
        )
    }
}

@Composable
private fun HeartRateMeasureBegin(
    modifier: Modifier = Modifier,
    uiState: HeartRateMeasureViewModel.UiState,
    onSurfaceTextureAvailable: (SurfaceTexture) -> Unit
) {

    val context = LocalContext.current

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.heart_beat_animation))

    val clipSpecs = LottieClipSpec.Progress(
        min = if (uiState.progress in 1..99) 0.0f else 1.0f,
        max = if (uiState.progress in 1..99) 1.0f else 0.0f
    )

    val cameraPreviewTextureView = remember { TextureView(context) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(Color.White)
            .navigationBarsPaddingIfNeed(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(RoundedCornerShape(41.dp))
                        .border(1.dp, Color(0xFFC4CACF), shape = RoundedCornerShape(41.dp)),
                    contentAlignment = Alignment.Center
                ) {

                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .clip(RoundedCornerShape(41.dp))
                    ) {
                        AndroidView(
                            modifier = Modifier.background(Color.Black),
                            factory = { context ->
                                cameraPreviewTextureView.apply {
                                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                                        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                                            onSurfaceTextureAvailable(surface)
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
                    }
                }

                Text(
                    text = "${uiState.heartRate}", style = TextStyle(
                        fontSize = 48.sp,
                        lineHeight = 48.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                        textAlign = TextAlign.Center,
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Image(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(id = R.drawable.ic_heart_filled),
                        contentDescription = "",
                        contentScale = ContentScale.None
                    )

                    Text(
                        text = "bpm", style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(400),
                            color = Color(0xFF8C8E97),
                            textAlign = TextAlign.Center,
                        )
                    )
                }

            }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .align(Alignment.BottomCenter),
                text = stringResource(if (uiState.isFingerDetected) R.string.measuring else R.string.place_your_finger_on_camera),
                style = TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight(700),
                    color = GrayScale900,
                )
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                ) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(color = Color(0xFFDEEDE1))
                    )
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = painterResource(id = R.drawable.measure_progress_bg),
                        contentDescription = "",
                        contentScale = ContentScale.FillWidth
                    )
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth(uiState.progress / 100f)
                            .height(2.dp)
                            .graphicsLayer(alpha = 0.3f),
                        color = Color(0xFF62A970),
                        progress = uiState.progress / 100f,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.progress), style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(400),
                            color = GrayScale900,
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "${uiState.progress}%", style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(400),
                            color = GrayScale900,
                            textAlign = TextAlign.Right,
                        )
                    )
                }
            }

            LottieAnimation(
                modifier = Modifier
                    .fillMaxWidth(),
                composition = composition,
                iterations = Int.MAX_VALUE,
                isPlaying = true,
                clipSpec = clipSpecs
            )
        }

    }
}

@Composable
fun NoticeDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    color = Color.White, shape = RoundedCornerShape(8.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                        text = stringResource(R.string.heart_rate_measure_notice),
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight(400),
                            color = GrayScale900,
                        )
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = onDismissRequest) {
                        Text(
                            text = stringResource(id = R.string.got_it), style = TextStyle(
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight(700),
                                color = Color(0xFF1892FA),
                                textAlign = TextAlign.Right,
                            )
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun IntroductionDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    color = Color.White, shape = RoundedCornerShape(8.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    modifier = Modifier.padding(bottom = 24.dp),
                    text = stringResource(R.string.instruction), style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                        textAlign = TextAlign.Center,
                    )
                )

                Row {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFC4CACF), shape = RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1", style = TextStyle(
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight(700),
                                color = GrayScale900,
                                textAlign = TextAlign.Center,
                            )
                        )
                    }

                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp),
                        text = stringResource(R.string.heart_rate_first_instruction),
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(400),
                            color = GrayScale900,
                        )
                    )
                }



                Row {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFC4CACF), shape = RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "2", style = TextStyle(
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight(700),
                                color = GrayScale900,
                                textAlign = TextAlign.Center,
                            )
                        )
                    }

                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp),
                        text = stringResource(R.string.stay_still_until_the_measuring_is_done),
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(400),
                            color = GrayScale900,
                        )
                    )
                }

                Row {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFC4CACF), shape = RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "3", style = TextStyle(
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight(700),
                                color = GrayScale900,
                                textAlign = TextAlign.Center,
                            )
                        )
                    }

                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp),
                        text = stringResource(R.string.heart_rate_last_instruction),
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(400),
                            color = GrayScale900,
                        )
                    )
                }

                TextButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .background(color = Color(0xFF1892FA), shape = RoundedCornerShape(8.dp)),
                    onClick = onDismissRequest
                ) {
                    Text(
                        text = stringResource(id = R.string.got_it), style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(700),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                    )
                }

            }
        }
    }
}