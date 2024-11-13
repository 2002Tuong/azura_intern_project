package com.bloodpressure.app.screen.bmi.add

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ads.SmallNativeAd
import com.bloodpressure.app.screen.heartrate.add.AgePickerDialog
import com.bloodpressure.app.screen.heartrate.add.GenderPickerDialog
import com.bloodpressure.app.screen.heartrate.add.GenderType
import com.bloodpressure.app.screen.home.tracker.TrackerType
import com.bloodpressure.app.screen.record.DateTimeSelection
import com.bloodpressure.app.ui.component.BloodButton
import com.bloodpressure.app.ui.theme.GrayScale700
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.HeightUnit
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalRemoteConfig
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBmiScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onComplete: () -> Unit,
    onNavigateToAddNote: () -> Unit,
    title: String,
    viewModel: AddBmiScreenViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adsManager = LocalAdsManager.current
    val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()
    val shouldShowNative = LocalRemoteConfig.current.adsConfig.shouldShowAddRecordNativeAd

    LaunchedEffect(Unit) {
        adsManager.loadAddRecordFeatureInter(TrackerType.WEIGHT_BMI)
    }

    LaunchedEffect(key1 = uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            adsManager.showAddFeatureInterAds(TrackerType.WEIGHT_BMI) {
                onComplete()
            }
        }
    }

    LaunchedEffect(uiState.shouldNavigateUp) {
        if (uiState.shouldNavigateUp) {
            onNavigateUp()
        }
    }

    if (uiState.shouldShowDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.clearConfirmDelete() },
            text = { Text(text = stringResource(id = R.string.delete_record_des)) },
            title = { Text(text = stringResource(id = R.string.delete_record_title)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRecord()
                    viewModel.clearConfirmDelete()
                }) {
                    Text(text = stringResource(id = R.string.cw_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearConfirmDelete() }) {
                    Text(text = stringResource(id = R.string.cw_cancel))
                }
            }
        )
    }

    if (uiState.showAgeDialog || uiState.forceShowAgeDialog) {
        AgePickerDialog(
            value = uiState.age,
            onValueChanged = { viewModel.setAge(it) },
            onDismissRequest = { viewModel.showAgeDialog(false) },
            onValueSaved = {
                viewModel.setAge(it)
                viewModel.showAgeDialog(false)
                viewModel.forceShowAgeDialog(false)

                if (!uiState.isGenderInputted) {
                    viewModel.forceShowGenderDialog(true)
                }
            },
            dismissOnClickOutside = !uiState.forceShowAgeDialog
        )
    }

    if (uiState.showGenderDialog || uiState.forceShowGenderDialog) {
        GenderPickerDialog(
            value = uiState.genderType ?: GenderType.OTHERS,
            onValueChanged = { viewModel.setGender(it) },
            onDismissRequest = { viewModel.showGenderDialog(false) },
            onValueSaved = {
                viewModel.setGender(it)
                viewModel.showGenderDialog(false)
                viewModel.forceShowGenderDialog(false)
            },
            dismissOnClickOutside = !uiState.forceShowGenderDialog
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
    ) {
        TopAppBar(
            title = {
                Text(
                    text = title, style = TextStyle(
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
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.Close), contentDescription = null
                    )
                }
            },
            actions = {
                uiState.recordId?.let {
                    TextButton(onClick = { viewModel.confirmDelete() }) {
                        Text(
                            text = stringResource(R.string.delete),
                            style = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight(400),
                                color = Color(0xFFF95721),
                                textAlign = TextAlign.Right,
                            )
                        )
                    }
                }
            }
        )

        AddBmiContent(
            modifier = Modifier.weight(1f),
            uiState = uiState,
            onSaveClick = { viewModel.saveRecord() },
            onWeightChange = viewModel::setWeight,
            onHeightChange = viewModel::setHeight,
            onTimeChanged = viewModel::setTime,
            onDateChanged = viewModel::setDate,
            onSelectedNotesChanged = viewModel::setNotes,
            onAddNoteClick = onNavigateToAddNote,
            onEditAgeClick = { viewModel.showAgeDialog(true) },
            onEditGenderClick = { viewModel.showGenderDialog(true) },
            onWeightUnitChange = {viewModel.changeWeightUnit()},
            onHeightUnitChange = {viewModel.changeHeightUnit()},
            onWeightTextFieldCLick = viewModel::onWeightTextFieldCLick,
            onHeightTextFieldClick = viewModel::onHeightTextFieldClick,
            viewModel = viewModel
        )

        if (!uiState.isPurchased) {
            if (shouldShowNative) {
                val nativeAd by LocalAdsManager.current.addRecordNativeAd.collectAsStateWithLifecycle()
                if (nativeAd != null) {
                    SmallNativeAd(nativeAd = nativeAd!!, modifier = Modifier.fillMaxWidth())
                }
            } else if (adView != null){
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

@Composable
fun AddBmiContent(
    uiState: AddBmiScreenViewModel.UiState,
    onSaveClick: () -> Unit,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onTimeChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onSelectedNotesChanged: (Set<String>) -> Unit,
    onAddNoteClick: () -> Unit,
    onEditAgeClick: () -> Unit,
    onEditGenderClick: () -> Unit,
    onWeightUnitChange: () -> Unit,
    onHeightUnitChange: () -> Unit,
    onWeightTextFieldCLick: () -> Unit,
    onHeightTextFieldClick: () -> Unit,
    viewModel: AddBmiScreenViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                    }
                )
            }
    ) {
        var isScrollEnabled by remember { mutableStateOf(true) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(state = rememberScrollState(), enabled = isScrollEnabled)
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    BMITypeItem(bmiType = uiState.selectBmiType)
                    
                    BMITypeIndicator(
                        bmiType = uiState.selectBmiType,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    Row(
                        modifier = Modifier.padding(top = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WeightTextField(
                            modifier = Modifier
                                .weight(5f)
                                .padding(end = 8.dp),
                            uiState = uiState,
                            onWeightChange = onWeightChange,
                            onWeightUnitChange = onWeightUnitChange,
                            onWeightTextFieldCLick = onWeightTextFieldCLick,
                            onBmiChange = { viewModel.setBmi() }
                        )
                        HeightTextField(
                            modifier = Modifier
                                .weight(6f)
                                .padding(start = 8.dp),
                            uiState = uiState,
                            onHeightChange = onHeightChange,
                            onHeightUnitChange = onHeightUnitChange,
                            onHeightTextFieldClick = onHeightTextFieldClick,
                            onBmiChange = { viewModel.setBmi() },
                            viewModel = viewModel
                        )
                    }

                    DateTimeSelection(
                        onTimeChanged = onTimeChanged,
                        onDateChanged = onDateChanged,
                        onAddNoteClick = onAddNoteClick,
                        onSelectedNotesChanged = onSelectedNotesChanged,
                        title = stringResource(R.string.bmi_notes),
                        time = uiState.time,
                        date = uiState.date,
                        notes = uiState.notes,
                        modifier = Modifier.padding(top = 24.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                                .clickable { onEditAgeClick() },
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                stringResource(R.string.age_edit_btn_text, uiState.age), Modifier.padding(end = 4.dp),
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFF8C8E97),
                                )
                            )

                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = ""
                            )
                        }

                        Spacer(
                            modifier = Modifier
                                .width(1.dp)
                                .background(color = GrayScale700)
                        )

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                                .clickable { onEditGenderClick() },
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                modifier = Modifier.padding(end = 4.dp),
                                text = stringResource(
                                    R.string.gender_edit_btn_text,
                                    context.getString(uiState.genderType.nameRes)
                                ),
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    fontWeight = FontWeight(400),
                                    color = Color(0xFF8C8E97),
                                )
                            )

                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = ""
                            )
                        }
                    }

                    BloodButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.save),
                        onClick = onSaveClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }


            }
        }
    }
}

@Composable
fun HeightTextField(
    modifier: Modifier = Modifier,
    uiState: AddBmiScreenViewModel.UiState,
    onHeightChange: (String) -> Unit,
    onHeightUnitChange: () -> Unit,
    onHeightTextFieldClick: () -> Unit,
    onBmiChange: () -> Unit,
    viewModel: AddBmiScreenViewModel,
) {
    val focusManager = LocalFocusManager.current
    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(text = stringResource(id = R.string.height),
                modifier = Modifier.wrapContentSize()
            )

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = uiState.heightUnit.value)

                Icon(
                    painter = painterResource(id = R.drawable.ic_refresh),
                    contentDescription = null,
                    modifier = Modifier.clickable { onHeightUnitChange()}
                )
            }
        }
        if(uiState.heightUnit == HeightUnit.CM) {
            OutlinedTextField(
                value = uiState.heightTextField,
                onValueChange = {newValue -> onHeightChange(newValue.text)},
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFECEDEF),
                    unfocusedBorderColor = Color(0xFFECEDEF)

                ),
                interactionSource = remember {
                    MutableInteractionSource()
                }.also { interactionSource ->
                    LaunchedEffect(key1 = interactionSource) {
                        interactionSource.interactions.collectLatest {
                            if(it is PressInteraction.Release) {
                                onHeightTextFieldClick()
                            }
                        }
                    }
                },
                modifier = Modifier.onFocusChanged {
                    if(!it.hasFocus) {
                        onBmiChange()
                    }
                },
                textStyle = TextStyle(
                    fontSize = 25.sp,
                    fontWeight = FontWeight(700),
                    color = Color(0xFF1892FA),
                    textAlign = TextAlign.Center,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                    }
                ),
                isError = uiState.isHeightInputWrongFormat,
                supportingText = {
                    if(uiState.isHeightInputWrongFormat) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                painter = painterResource(id = R.drawable.ic_warning),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.error,
                                text = stringResource(id = R.string.text_field_input_wrong_format),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }else {
                        Spacer(modifier = modifier.height(16.dp))
                    }
                }
            )
        } else {
            val (input1, input2) = viewModel.separateHeightInFtUnit(uiState.height)
            Row{
                OutlinedTextField(
                    value = input1,
                    onValueChange = {newValue ->
                        val input = "$newValue'$input2"
                        onHeightChange(input) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFECEDEF),
                        unfocusedBorderColor = Color(0xFFECEDEF)

                    ),
                    textStyle = TextStyle(
                        fontSize = 25.sp,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF1892FA),
                        textAlign = TextAlign.Center,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    suffix = {
                             Text("'",
                                 style = TextStyle(
                                     fontSize = 20.sp,
                                     fontWeight = FontWeight(700),
                                     color = Color(0xFF1892FA),
                                 )
                             )
                    },
                    modifier = Modifier
                        .weight(2f)
                        .onFocusChanged {
                            if (!it.hasFocus) {
                                onBmiChange()
                            }
                        },
                    isError = uiState.isHeightInputWrongFormat,
                    supportingText = {
                        if(uiState.isHeightInputWrongFormat) {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                painter = painterResource(id = R.drawable.ic_warning),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }else {
                            Spacer(modifier = modifier.height(16.dp))
                        }
                    }
                )

                OutlinedTextField(
                    value = input2,
                    onValueChange = {newValue ->
                        val input = "$input1'$newValue"
                        onHeightChange(input)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFECEDEF),
                        unfocusedBorderColor = Color(0xFFECEDEF)

                    ),
                    textStyle = TextStyle(
                        fontSize = 25.sp,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF1892FA),
                        textAlign = TextAlign.Center,
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    suffix = {
                        Text("''",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight(700),
                                color = Color(0xFF1892FA),
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(3f)
                        .padding(start = 8.dp)
                        .onFocusChanged {
                            if (!it.hasFocus) {
                                onBmiChange()
                            }
                        },
                    isError = uiState.isHeightInputWrongFormat,
                    supportingText = {
                        if(uiState.isHeightInputWrongFormat) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.error,
                                text = stringResource(id = R.string.text_field_input_wrong_format),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }else {
                            Spacer(modifier = modifier.height(16.dp))
                        }
                    }
                )
            }
        }

    }
}

@Composable
fun WeightTextField(
    modifier: Modifier = Modifier,
    uiState: AddBmiScreenViewModel.UiState,
    onWeightChange: (String) -> Unit,
    onWeightUnitChange: () -> Unit,
    onWeightTextFieldCLick: () -> Unit,
    onBmiChange: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Column(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text(text = stringResource(id = R.string.weight),
                modifier = Modifier.wrapContentSize()
            )

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = uiState.weightUnit.value)

                Icon(
                    painter = painterResource(id = R.drawable.ic_refresh),
                    contentDescription = null,
                    modifier = Modifier.clickable { onWeightUnitChange()}
                )

            }
        }
        OutlinedTextField(
            value = uiState.weightTextField,
            onValueChange = {newValue -> onWeightChange(newValue.text)},
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFECEDEF),
                unfocusedBorderColor = Color(0xFFECEDEF)
            ),
            textStyle = TextStyle(
                fontSize = 25.sp,
                fontWeight = FontWeight(700),
                color = Color(0xFF1892FA),
                textAlign = TextAlign.Center,
            ),
            interactionSource = remember {
                MutableInteractionSource()
            }.also { interactionSource ->
                LaunchedEffect(key1 = interactionSource) {
                       interactionSource.interactions.collectLatest {
                           if(it is PressInteraction.Release) {
                               onWeightTextFieldCLick()
                           }
                       }
                   }
            },
            modifier = Modifier
                .onFocusChanged {
                    if (!it.hasFocus) {
                        onBmiChange()
                    }
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            isError = uiState.isWeightInputWrongFormat,
            supportingText = {
                if(uiState.isWeightInputWrongFormat) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(id = R.drawable.ic_warning),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.error,
                            text = stringResource(id = R.string.text_field_input_wrong_format),
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }else {
                    Spacer(modifier = modifier.height(16.dp))
                }
            }
        )

    }
}

@Composable
fun BMITypeItem(
    modifier: Modifier = Modifier,
    bmiType: BMIType,
) {
    var shouldShowBmiTypeInfos by remember { mutableStateOf(false) }
    if (shouldShowBmiTypeInfos) {
        BMITypeDialog { shouldShowBmiTypeInfos = false }
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { shouldShowBmiTypeInfos = true },
        colors = CardDefaults.cardColors(containerColor = bmiType.color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = bmiType.nameRes),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight(700),
            )

            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.padding(end = 12.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun BMITypeIndicator(
    modifier: Modifier = Modifier,
    bmiType: BMIType,
) {
    val bmiTypes by remember { mutableStateOf(BMIType.values()) }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val text = bmiType.getAnnotatedString()
        Text(text = text)

        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        ) {
            bmiTypes.forEach {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (it == bmiType) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_bottom),
                            contentDescription = null
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(8.dp)
        ) {
            bmiTypes.map { it.color }.forEachIndexed { index, color ->
                val shape = when (index) {
                    0 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                    BMIType.values().size - 1 -> RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                    else -> RoundedCornerShape(0.dp)
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(color, shape)
                )
            }
        }
    }
}
