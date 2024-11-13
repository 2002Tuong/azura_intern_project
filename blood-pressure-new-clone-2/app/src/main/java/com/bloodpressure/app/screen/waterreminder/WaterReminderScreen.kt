package com.bloodpressure.app.screen.waterreminder

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ads.SmallNativeAd
import com.bloodpressure.app.screen.splash.EaseInOut
import com.bloodpressure.app.ui.component.AskSetAlarmDialog
import com.bloodpressure.app.ui.component.TopAppBarAction
import com.bloodpressure.app.ui.component.WaterLeverProgressView
import com.bloodpressure.app.ui.component.WaterRulerView
import com.bloodpressure.app.ui.theme.Blue
import com.bloodpressure.app.ui.theme.Blue80
import com.bloodpressure.app.ui.theme.GrayScale600
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.ui.theme.Orange
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalRemoteConfig
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterReminderScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onReminderItemClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: WaterReminderViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    var bottomSheetKeyValue by remember {
        mutableStateOf(false)
    }
    val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()

    if (uiState.isShowEditGoalDialog) {
        EditDailyGoalDialog(
            uiState.dailyGoalInDisplay,
            if (uiState.isMl) stringResource(id = R.string.limit_bottle_size_ml) else stringResource(
                id = R.string.limit_bottle_size_oz
            ),
            {
                if (uiState.isMl && it in 500..10000 || !uiState.isMl && it in 20..350) {
                    viewModel.setDailyGoal(it)
                    viewModel.showEditWaterGoalDialog(false)
                }
            },
            { viewModel.showEditWaterGoalDialog(false) },
            true
        )
    }

    if (uiState.showAskSetAlarmDialog) {
        AskSetAlarmDialog(
            onDismissRequest = { viewModel.showSetReminder(false) },
            onCancel = {
                viewModel.showAskSetAlarmDialog(false)
                onNavigateUp()
            },
            onAgreeSetAlarm = {
                onReminderItemClick.invoke()
                viewModel.showAskSetAlarmDialog(false)
            }
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
    ) {

        BottomSheetScaffold(
            sheetContent = {
                key(bottomSheetKeyValue) {
                    EditSizeOfCupBottomSheet(
                        value = viewModel.tempBottleSizeValue.takeIf { it != -1 }
                            ?: uiState.bottleSizeInDisplay,
                        isMl = uiState.isMl,
                        setVolumeType = { isMl, tempValue ->
                            viewModel.setVolumeType(isMl)
                            viewModel.setTempBottleSize(isMl, tempValue)
                        },
                        onValueSaved = {
                            viewModel.showEditSizeOfCup(false)
                            viewModel.setBottleSize(it)
                            viewModel.setTempBottleSize(false, -1)
                            scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                        },
                        onDismiss = {
                            viewModel.setTempBottleSize(false, -1)
                            viewModel.showEditSizeOfCup(false)
                            scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                        },
                    )
                }
            },
            sheetPeekHeight = 0.dp,
            scaffoldState = scaffoldState,
            sheetTonalElevation = 8.dp,
            sheetShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            sheetContainerColor = Color.White,
            sheetShadowElevation = 8.dp
        ) {

            val isDimVisible by remember {
                derivedStateOf {
                    scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded
                }
            }
            if (isDimVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(1f)
                        .background(color = Color(0x50191D30))
                        .clickable { }
                )
            } else {
                viewModel.setTempBottleSize(false, -1)
                bottomSheetKeyValue = !bottomSheetKeyValue
            }

            Column(modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.water_reminder),
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight(800),
                                color = GrayScale900,
                            )
                        )
                    },
                    actions = {
                        TopAppBarAction(
                            isPurchased = uiState.isPurchased,
                            onSetAlarmClick = onReminderItemClick,
                            onNavigateToPremium = onNavigateToPremium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (uiState.hasWaterReminderAlarm) {
                                onNavigateUp()
                            } else {
                                viewModel.showAskSetAlarmDialog(true)
                            }
                        }) {
                            Icon(
                                painter = rememberVectorPainter(image = Icons.Default.ArrowBack),
                                contentDescription = null
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    ),
                )

                Divider(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(color = Color.Gray)
                )

                WaterReminderContent(
                    modifier = Modifier
                        .background(Color.White)
                        .fillMaxSize()
                        .weight(1f),
                    onHistoryClick = onHistoryClick,
                    onEditDailyGoalClick = { viewModel.showEditWaterGoalDialog(true) },
                    onEditSizeOfCupClick = {
                        viewModel.showEditSizeOfCup(true)
                        scope.launch { scaffoldState.bottomSheetState.expand() }
                    },
                    uiState = uiState,
                    onWaterAdd = {
                        viewModel.increaseWaterCup()
                    },
                    onWaterDecrease = {
                        viewModel.decreaseWaterCup()
                    }
                )

                if (!uiState.isPurchased && adView != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxWidth(),
                            factory = {
                                adView!!.apply {
                                    (parent as? ViewGroup)?.removeView(this)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("Recycle")
@Composable
fun WaterReminderContent(
    modifier: Modifier = Modifier,
    onHistoryClick: () -> Unit,
    onEditDailyGoalClick: () -> Unit,
    onEditSizeOfCupClick: () -> Unit,
    uiState: WaterReminderViewModel.UiState,
    onWaterAdd: () -> Unit,
    onWaterDecrease: () -> Unit
) {
    var isShowWaterAddIndicator by remember {
        mutableStateOf(false)
    }
    var isWaterAddAnimStart by remember { mutableStateOf(false) }
    var waterAddValue by remember {
        mutableStateOf("+")
    }
    var scope = rememberCoroutineScope()

    fun setupAnimation(onAction: () -> Unit) {
        scope.launch {
            isShowWaterAddIndicator = false
            isWaterAddAnimStart = false
            delay(300)
            isWaterAddAnimStart = true
            isShowWaterAddIndicator = true
            onAction()
        }
    }

    val waterAddIndicatorPos by animateIntOffsetAsState(
        targetValue = if (isWaterAddAnimStart) IntOffset(0, -30.dp.toPx()) else IntOffset(
            0,
            50.dp.toPx()
        ),
        label = "",
        animationSpec = tween(
            durationMillis = if (isWaterAddAnimStart) 1000 else 100,
            easing = EaseInOut
        )
    )
    LaunchedEffect(key1 = uiState.actualWater, block = {
        delay(1000)
        isShowWaterAddIndicator = false
        isWaterAddAnimStart = false
    })
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(contentAlignment = Alignment.TopCenter) {
            WaterLeverProgressView(
                uiState
            )
            if (isShowWaterAddIndicator) {
                Text(
                    modifier = Modifier.offset {
                        if (isShowWaterAddIndicator)
                            waterAddIndicatorPos else IntOffset(
                            0,
                            50.dp.value.toInt()
                        )
                    },
                    text = uiState.getBottleSizeChanged(waterAddValue),
                    color = Blue80,
                    fontSize = 18.sp,
                    style = TextStyle(
                        fontWeight = FontWeight(700),
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Transparent, shape = RoundedCornerShape(8.dp))
                .border(
                    1.dp,
                    color = colorResource(id = R.color.gray_scale_600),
                    shape = RoundedCornerShape(8.dp)
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = R.string.daily_goal),
                        textAlign = TextAlign.Start,
                        style = TextStyle(fontSize = 15.sp)
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onEditDailyGoalClick()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "${uiState.dailyGoalInDisplay} ${uiState.unitInDisplay}",
                            textAlign = TextAlign.End,
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight(700))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Image(
                            painter = painterResource(id = R.drawable.ic_pencil),
                            contentDescription = ""
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = R.string.last_drink),
                        textAlign = TextAlign.Start,
                        style = TextStyle(fontSize = 15.sp)
                    )
                    Text(
                        modifier = Modifier.weight(1f),
                        text = if (uiState.lastDrink != 0) "${uiState.lastDrinkInDisplay} ${uiState.unitInDisplay}" else "-- ${uiState.unitInDisplay}",
                        textAlign = TextAlign.End,
                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight(700))
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(id = R.string.number_of_cup_title),
                        textAlign = TextAlign.Start,
                        style = TextStyle(fontSize = 15.sp)
                    )
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(
                            id = R.string.number_of_cup,
                            uiState.numberOfCup.toString()
                        ),
                        textAlign = TextAlign.End,
                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight(700))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        FloatingActionButton(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    color = colorResource(id = R.color.gray_scale_600),
                    shape = RoundedCornerShape(8.dp),
                )
                .height(48.dp),
            onClick = onHistoryClick,
            shape = RoundedCornerShape(8.dp),
            containerColor = Color.White
        ) {
            Text(
                text = stringResource(id = R.string.history),
                style = TextStyle(
                    color = Blue,
                    fontWeight = FontWeight.W700,
                    fontSize = 15.sp
                )
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.water_minus),
                    contentDescription = "",
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (uiState.actualWater > 0) {
                            setupAnimation {
                                waterAddValue = "-"
                                onWaterDecrease()
                            }
                        }
                    }
                )
                Image(
                    modifier = Modifier
                        .size(132.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            setupAnimation {
                                waterAddValue = "+"
                                onWaterAdd()
                            }
                        }
                        .clip(CircleShape),
                    painter = painterResource(id = R.drawable.ic_add_water),
                    contentDescription = ""
                )
                Column(
                    modifier = Modifier.clickable { onEditSizeOfCupClick() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.water_cup),
                        contentDescription = ""
                    )
                    Text(text = "${uiState.bottleSizeInDisplay} ${uiState.unitInDisplay}")
                }

            }

        }
    }
}

@Composable
private fun Dp.toPx(): Int {
    return with(LocalDensity.current) { toPx().toInt() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDailyGoalDialog(
    value: Int,
    placeHolderValue: String,
    onValueSaved: (Int) -> Unit,
    onDismiss: () -> Unit,
    dismissOnClickOutside: Boolean
) {

    var text by remember { mutableStateOf("$value") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = dismissOnClickOutside
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(color = Color.White, shape = RoundedCornerShape(8.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {


            Text(
                text = stringResource(R.string.edit_daily_goal),
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight(700),
                    color = GrayScale900,
                    textAlign = TextAlign.Center,
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextField(
                value = text,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = GrayScale600,
                        shape = RoundedCornerShape(8.dp)
                    ),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                onValueChange = { text = it },
                shape = RoundedCornerShape(8.dp),
                placeholder = { Text(placeHolderValue, style = TextStyle(color = Color.LightGray)) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth()) {

                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .background(shape = RoundedCornerShape(8.dp), color = Orange),
                    onClick = { onDismiss() }
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(700),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .background(shape = RoundedCornerShape(8.dp), color = Blue),
                    onClick = {
                        onValueSaved(text.toInt())
                    }
                ) {
                    Text(
                        text = stringResource(R.string.save),
                        style = TextStyle(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSizeOfCupBottomSheet(
    value: Int,
    isMl: Boolean,
    setVolumeType: (Boolean, Int) -> Unit,
    onValueSaved: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var waterPerCup by remember {
        mutableStateOf(value)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
            )
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.your_bottle_size),
            style = TextStyle(fontWeight = FontWeight(700), fontSize = 18.sp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row {
            FilterChip(
                label = { Text(stringResource(id = R.string.ml)) },
                shape = RoundedCornerShape(16.dp),
                selected = isMl,
                colors = FilterChipDefaults.filterChipColors(
                    disabledContainerColor = Color.White,
                    selectedContainerColor = Blue
                ),
                onClick = {
                    if (!isMl)
                        setVolumeType(true, waterPerCup)
                }
            )

            Spacer(modifier = Modifier.width(10.dp))

            FilterChip(
                label = { Text(stringResource(id = R.string.oz)) },
                shape = RoundedCornerShape(16.dp),
                selected = !isMl,
                colors = FilterChipDefaults.filterChipColors(
                    disabledContainerColor = Color.White,
                    selectedContainerColor = Blue
                ),
                onClick = {
                    if (isMl)
                        setVolumeType(false, waterPerCup)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = waterPerCup.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Blue80
        )

        WaterRulerView(
            currentValue = value,
            isUseMlUnit = isMl,
            onValueChange = {
                waterPerCup = it
            },
            containerPadding = 16.dp
        )

        Spacer(modifier = Modifier.height(20.dp))

        TextButton(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .background(shape = RoundedCornerShape(8.dp), color = Blue),
            onClick = {
                onValueSaved(waterPerCup)
                onDismiss()
            }
        ) {
            Text(
                text = stringResource(R.string.done),
                style = TextStyle(
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
