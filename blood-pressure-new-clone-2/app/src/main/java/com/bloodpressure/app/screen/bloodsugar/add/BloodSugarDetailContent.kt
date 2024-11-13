package com.bloodpressure.app.screen.bloodsugar.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodpressure.app.R
import com.bloodpressure.app.screen.bloodsugar.type.BloodSugarRateType
import com.bloodpressure.app.screen.bloodsugar.type.BloodSugarStateType
import com.bloodpressure.app.screen.record.DateTimeSelection
import com.bloodpressure.app.ui.component.BloodButton
import com.bloodpressure.app.ui.theme.GrayScale600
import com.bloodpressure.app.ui.theme.GrayScale700
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.BloodSugarUnit
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

@Composable
fun BloodSugarDetailContent(
    modifier: Modifier = Modifier,
    onSaveRecord: () -> Unit = {},
    onTimeChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onAddNoteClick: () -> Unit,
    onTargetSelected: () -> Unit,
    onSelectedNotesChanged: (Set<String>) -> Unit,
    onBloodSugarRateValueChange: (Float) -> Unit,
    onSaveState: (BloodSugarStateType) -> Unit,
    onEditAgeClick: () -> Unit,
    onEditGenderClick: () -> Unit,
    updateMeasuredUnitToMol: (BloodSugarUnit) -> Unit,
    onSetLastValue: (Float?) -> Unit,
    uiState: BloodSugarDetailViewModel.UiState
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        var isScrollEnabled by remember { mutableStateOf(true) }

        Card(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                .weight(1f)
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState(), enabled = isScrollEnabled),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = stringResource(id = R.string.select_state),
                    style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                        textAlign = TextAlign.Center,
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                SelectState(
                    modifier = Modifier.fillMaxWidth(),
                    onSaveState = onSaveState,
                    state = uiState.stateSelected,
                    itemList = BloodSugarStateType.values().toList()
                )

                Spacer(modifier = Modifier.height(16.dp))

                BloodSugarTypeItem(
                    bloodSugarRateType = uiState.getBloodSugarRateType()
                )

                BloodSugarTypeIndicator(
                    bloodSugarRateType = uiState.getBloodSugarRateType(),
                    modifier = Modifier.padding(top = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            val currentContext = currentCoroutineContext()
                            awaitPointerEventScope {
                                while (currentContext.isActive) {
                                    awaitPointerEvent()
                                    isScrollEnabled =
                                        currentEvent.type == PointerEventType.Release
                                }
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = modifier.weight(1f))

                    BloodSugarSpinnerValueSelection(
                        modifier = Modifier
                            .wrapContentHeight()
                            .width(100.dp),
                        value = uiState.lastValue ?: uiState.bloodSugarValue,
                        onValueChanged = onBloodSugarRateValueChange,
                        range = uiState.maxRange,
                        selectedTextColor = Color(0xFF1892FA)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TextButton(
                            modifier = if (uiState.bloodSugarUnit == BloodSugarUnit.MILLIGRAMS_PER_DECILITER) {
                                Modifier.background(
                                    GrayScale600,
                                    shape = RoundedCornerShape(32.dp)
                                )
                            } else Modifier,
                            onClick = {
                                onSetLastValue.invoke(null)
                                updateMeasuredUnitToMol.invoke(BloodSugarUnit.MILLIGRAMS_PER_DECILITER)
                            }
                        ) {
                            Text(
                                text = BloodSugarUnit.MILLIGRAMS_PER_DECILITER.value,
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    lineHeight = 28.sp,
                                    fontWeight = FontWeight(700),
                                    color = GrayScale900,
                                )
                            )
                        }
                        TextButton(
                            modifier = if (uiState.bloodSugarUnit == BloodSugarUnit.MILLIMOLES_PER_LITRE) {
                                Modifier.background(GrayScale600, shape = RoundedCornerShape(32.dp))
                            } else Modifier,
                            onClick = {
                                onSetLastValue.invoke(null)
                                updateMeasuredUnitToMol.invoke(BloodSugarUnit.MILLIMOLES_PER_LITRE)
                            }
                        ) {
                            Text(
                                text = BloodSugarUnit.MILLIMOLES_PER_LITRE.value,
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    lineHeight = 28.sp,
                                    fontWeight = FontWeight(700),
                                    color = GrayScale900,
                                )
                            )
                        }
                    }

                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.5f)
                        )
                        .clickable {
                            onSetLastValue.invoke(uiState.bloodSugarValue)
                            onTargetSelected.invoke()
                        }
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(id = R.string.edit_target_range),
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight(700),
                    )

                    Icon(
                        painter = rememberVectorPainter(image = Icons.Filled.KeyboardArrowRight),
                        contentDescription = null
                    )

                }

                DateTimeSelection(
                    modifier = Modifier.padding(top = 24.dp),
                    onTimeChanged = onTimeChanged,
                    onDateChanged = onDateChanged,
                    onSelectedNotesChanged = onSelectedNotesChanged,
                    onAddNoteClick = onAddNoteClick,
                    time = uiState.time,
                    date = uiState.date,
                    notes = uiState.notes,
                    title = "BS Notes"
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
                                stringResource(id = uiState.genderType.nameRes)
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
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = stringResource(id = R.string.cw_save),
                    onClick = onSaveRecord,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

    }
}

@Composable
fun BloodSugarTypeItem(
    modifier: Modifier = Modifier,
    bloodSugarRateType: BloodSugarRateType
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = CardDefaults.cardColors(containerColor = bloodSugarRateType.color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = bloodSugarRateType.nameRes),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight(700),
            )
        }
    }
}


@Composable
fun BloodSugarSpinnerValueSelection(
    modifier: Modifier = Modifier,
    value: Float,
    onValueChanged: (Float) -> Unit,
    range: List<Float>,
    selectedTextColor: Color
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFECEDEF), shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            BloodSugarPicker(
                currentValue = value,
                onValueChanged = onValueChanged,
                items = range,
                selectedTextColor = selectedTextColor,
                itemText = { it.toString() },
                itemKey = { it.toString() }
            )
        }
    }
}

@Composable
fun BloodSugarTypeIndicator(
    modifier: Modifier = Modifier,
    bloodSugarRateType: BloodSugarRateType
) {
    val bloodSugarRateTypes by remember { mutableStateOf(BloodSugarRateType.values()) }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        ) {
            bloodSugarRateTypes.forEach {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (it == bloodSugarRateType) {
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
                .fillMaxWidth()
                .height(8.dp)
        ) {
            bloodSugarRateTypes.map { it.color }.forEachIndexed { index, color ->
                val shape = when (index) {
                    0 -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                    BloodSugarRateType.values().size - 1 -> RoundedCornerShape(
                        topEnd = 8.dp,
                        bottomEnd = 8.dp
                    )

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
