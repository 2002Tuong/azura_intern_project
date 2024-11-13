package com.bloodpressure.app.screen.bloodsugar.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodpressure.app.R
import com.bloodpressure.app.screen.bloodsugar.type.StateType
import com.bloodpressure.app.screen.bloodsugar.type.getStringAnnotation
import com.bloodpressure.app.ui.theme.Blue80
import com.bloodpressure.app.ui.theme.GrayScale600
import com.bloodpressure.app.ui.theme.GrayScale900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T: StateType> SelectState(
    modifier: Modifier = Modifier,
    state: T,
    onSaveState: (T) -> Unit,
    itemList: List<T>
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val stateSelected by remember { mutableStateOf(state) }
    val configuration = LocalConfiguration.current

    val screenHeight = configuration.screenHeightDp
    val bottomSheetHeight = (screenHeight * 0.7)

    if (showBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier
                .height(bottomSheetHeight.dp),
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = Color.White,
            content = {
                ModalBottomSheetContent(
                    bottomSheetHeight = bottomSheetHeight,
                    currentState = stateSelected,
                    onSaveState = {
                        showBottomSheet = false
                        onSaveState.invoke(it)
                    },
                    itemList = itemList
                )
            },
        )
    }
    Row(
        modifier = modifier
            .fillMaxHeight()
            .border(
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(width = 1.dp, color = GrayScale600)
            )
            .clickable { showBottomSheet = true }
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = state.getStringAnnotation(),
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight(400),
        )

        Icon(
            painter = rememberVectorPainter(image = Icons.Filled.KeyboardArrowDown),
            contentDescription = null
        )

    }
}

@Composable
private fun <T: StateType> ModalBottomSheetContent(
    modifier: Modifier = Modifier,
    bottomSheetHeight: Double,
    currentState: T,
    itemList: List<T>,
    onSaveState: (T) -> Unit
) {
    val (selectedState, setState) = remember { mutableStateOf(currentState) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(id = R.string.select_state),
            style = TextStyle(
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight(700),
                color = GrayScale900,
            )
        )

        LazyColumn(
            modifier = Modifier.height((bottomSheetHeight * 0.7).dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(itemList) { _, stateType ->
                Column(
                    modifier = Modifier
                        .border(
                            width = 2.dp,
                            color = if (stateType == selectedState) Blue80 else GrayScale600,
                            shape = RoundedCornerShape(size = 8.dp)
                        )
                        .clickable {
                            setState.invoke(stateType)
                        }
                        .padding(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = if (stateType == selectedState)
                                    painterResource(R.drawable.ic_radio_checked_default)
                                else
                                    painterResource(R.drawable.ic_radio_default),
                                contentDescription = null
                            )
                            Text(
                                text = stateType.getStringAnnotation(),
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    fontWeight = FontWeight(700),
                                    color = GrayScale900,
                                )
                            )
                        }
                    }
                }
            }
        }
    }
    Button(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(48.dp),
        onClick = {
            onSaveState.invoke(selectedState)
        },
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(text = stringResource(id = R.string.done))
    }
}