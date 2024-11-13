package com.bloodpressure.app.screen.bloodsugar.add

import android.util.Range
import android.view.ViewGroup
import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.util.toRange
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.screen.bloodsugar.SCOPE_ID
import com.bloodpressure.app.screen.bloodsugar.type.BloodSugarRateType
import com.bloodpressure.app.screen.bloodsugar.type.TargetRange
import com.bloodpressure.app.screen.bloodsugar.type.getStringAnnotation
import com.bloodpressure.app.ui.theme.GrayScale600
import com.bloodpressure.app.ui.theme.GrayScale700
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.ui.theme.Green800
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.getKoin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodSugarTargetsScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    title: String,
    viewModel: BloodSugarDetailViewModel = getKoin().getScope(SCOPE_ID).get()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()
    Column(modifier = modifier.fillMaxSize().navigationBarsPaddingIfNeed()) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = title,
                        style = TextStyle(fontSize = 16.sp)
                    )
                    Text(
                        text = uiState.bloodSugarUnit.value,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight(400),
                            color = GrayScale900,
                        )
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            actions = {
                TextButton(onClick = { viewModel.resetTarget() }) {
                    Text(text = stringResource(id = R.string.reset))
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    onNavigateUp()
                }) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.ArrowBack),
                        contentDescription = null
                    )
                }
            }
        )

        BloodSugarTargetsContent(
            modifier = Modifier.fillMaxSize().weight(1f),
            uiState = uiState,
            onDiabetesUpdate = viewModel::setDiabetes,
            onNormalRangeMinUpdate = viewModel::setNormalRangeMin,
            onNormalRangeMaxUpdate = viewModel::setNormalRangeMax,
            onCheckedUpdate = viewModel::setChecked,
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

@Composable
fun BloodSugarTargetsContent(
    modifier: Modifier = Modifier,
    onDiabetesUpdate: (String, Int) -> Boolean,
    onNormalRangeMinUpdate: (String, Int) -> Boolean,
    onNormalRangeMaxUpdate: (String, Int) -> Boolean,
    onCheckedUpdate: (Boolean, Int) -> Unit,
    uiState: BloodSugarDetailViewModel.UiState
) {
    Column(
        modifier = modifier
    ) {

        LazyColumn(modifier = modifier.padding(16.dp)) {

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .background(Green800, shape = RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.blood_sugar_warning),
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(700),
                            color = Color.White,
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            itemsIndexed(uiState.targetRanges) { index: Int, item: TargetRange ->
                if (index == 0) {
                    Text(
                        text = stringResource(id = R.string.default_range),
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(700),
                            color = GrayScale900,
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (index == 1) {
                    Text(
                        text = stringResource(id = R.string.specified_state),
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(700),
                            color = GrayScale900,
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                DetailTargetItem(
                    title = item.bloodSugarStateType.getStringAnnotation(),
                    isShowLabel = index != 0,
                    item = item,
                    onDiabetesUpdate = { onDiabetesUpdate.invoke(it, index) },
                    onNormalRangeMinUpdate = { onNormalRangeMinUpdate.invoke(it, index) },
                    onNormalRangeMaxUpdate = { onNormalRangeMaxUpdate.invoke(it, index) },
                    onCheckedUpdate = { onCheckedUpdate.invoke(it, index) }
                )
            }
        }
    }
}

@Composable
fun DetailTargetItem(
    modifier: Modifier = Modifier,
    title: String,
    isShowLabel: Boolean = false,
    item: TargetRange,
    onDiabetesUpdate: (String) -> Boolean,
    onNormalRangeMinUpdate: (String) -> Boolean,
    onNormalRangeMaxUpdate: (String) -> Boolean,
    onCheckedUpdate: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        if (isShowLabel) {
            LabelTarget(title = title, item.isChecked, onCheckedUpdate)
            if (item.isChecked) {
                Divider(
                    color = Color.Gray,
                )
                ItemRangeValue(
                    modifier = modifier,
                    item = item,
                    onDiabetesUpdate = onDiabetesUpdate,
                    onNormalRangeMinUpdate = onNormalRangeMinUpdate,
                    onNormalRangeMaxUpdate = onNormalRangeMaxUpdate
                )
            } else {
                Text(
                    text = stringResource(id = R.string.use_default_range),
                    style = TextStyle(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight(400),
                        color = GrayScale700,
                    )
                )
            }
        } else {
            ItemRangeValue(
                modifier = modifier,
                item = item,
                onDiabetesUpdate = onDiabetesUpdate,
                onNormalRangeMinUpdate = onNormalRangeMinUpdate,
                onNormalRangeMaxUpdate = onNormalRangeMaxUpdate
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun ItemRangeValue(
    modifier: Modifier,
    item: TargetRange,
    onDiabetesUpdate: (String) -> Boolean,
    onNormalRangeMinUpdate: (String) -> Boolean,
    onNormalRangeMaxUpdate: (String) -> Boolean
) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxHeight()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            StaticTarget(
                bloodSugarRateType = BloodSugarRateType.LOW,
                value = item.normalRangeMin,
                rangeValue = null
            )

            Spacer(modifier = Modifier.height(24.dp))

            StaticTarget(
                bloodSugarRateType = BloodSugarRateType.PRE_DIABETES,
                value = null,
                rangeValue = (item.normalRangeMax..item.diabetesValue).toRange()
            )
        }
        Column(
            modifier = Modifier
                .padding(10.dp)
                .weight(2f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val focusRequester = remember { FocusRequester() }
            Row {
                Icon(
                    painter = rememberVectorPainter(image = Icons.Filled.Circle),
                    tint = BloodSugarRateType.NORMAL.color,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(id = BloodSugarRateType.NORMAL.nameRes),
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight(400),
                        color = GrayScale900,
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TargetRangeText(
                    modifier = modifier
                        .heightIn(min = 36.dp)
                        .weight(1f)
                        .focusRequester(focusRequester),
                    text = item.normalRangeMin.toString(),
                    onTextChange = onNormalRangeMinUpdate
                )
                Text(
                    modifier = Modifier.padding(4.dp),
                    text = "~",
                    style = TextStyle(
                        fontSize = 20.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight(800),
                        color = GrayScale900,
                    )
                )
                TargetRangeText(
                    modifier = modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    text = item.normalRangeMax.toString(),
                    onTextChange = onNormalRangeMaxUpdate
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row {
                Icon(
                    painter = rememberVectorPainter(image = Icons.Filled.Circle),
                    tint = Color.Red,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(id = R.string.diabetes))
            }
            Spacer(modifier = Modifier.height(8.dp))
            TargetRangeText(
                modifier = modifier
                    .heightIn(min = 36.dp)
                    .focusRequester(focusRequester),
                text = "≥ ${item.diabetesValue}",
                onTextChange = {
                    onDiabetesUpdate.invoke(it.removePrefix("≥ "))
                }
            )
        }

    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun TargetRangeText(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Boolean
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val (text1, setValue) = remember { mutableStateOf(text) }

    OutlinedTextField(
        value = text1,
        onValueChange = {
            setValue.invoke(it)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        keyboardActions = KeyboardActions(
            onDone = {
                if (onTextChange.invoke(text1)) {
                    keyboardController?.hide()
                }
            }
        ),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GrayScale900,
            unfocusedBorderColor = GrayScale600,
        ),
        textStyle = TextStyle(
            fontSize = 16.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight(800),
            color = GrayScale900,
            textAlign = TextAlign.Center
        ),
        isError = !onTextChange.invoke(text1),
        singleLine = true,
        modifier = modifier
    )
}

@Composable
fun LabelTarget(
    title: String,
    checkedState: Boolean,
    setCheckedValue: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight(700),
                color = GrayScale900,
            )
        )

        Switch(
            checked = checkedState,
            onCheckedChange = setCheckedValue,
        )
    }

}

@Composable
private fun StaticTarget(
    bloodSugarRateType: BloodSugarRateType,
    value: Float?,
    @FloatRange
    rangeValue: Range<Float>?,
) {
    Row {
        Icon(
            painter = rememberVectorPainter(image = Icons.Filled.Circle),
            tint = bloodSugarRateType.color,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(id = bloodSugarRateType.nameRes),
            style = TextStyle(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight(400),
                color = GrayScale900,
            )
        )
    }
    val text = if (value != null) {
        "$value"
    } else {
        "${rangeValue?.lower}~${rangeValue?.upper}"
    }
    Text(
        text = text,
        style = TextStyle(
            fontSize = 20.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight(800),
            color = GrayScale900,
        )
    )
}
