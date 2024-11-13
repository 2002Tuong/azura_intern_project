package com.bloodpressure.app.screen.heartrate.add

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bloodpressure.app.R
import com.bloodpressure.app.ui.theme.GrayScale900
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map

@Composable
fun AgePickerDialog(
    modifier: Modifier = Modifier,
    value: Int,
    onValueChanged: (Int) -> Unit,
    onValueSaved: (Int) -> Unit,
    onDismissRequest: () -> Unit,
    dismissOnClickOutside: Boolean = true
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = dismissOnClickOutside),
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = stringResource(R.string.edit_your_age),
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                        textAlign = TextAlign.Center,
                    )
                )

                Card(
                    modifier = Modifier.wrapContentWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    AgePicker(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        currentValue = value,
                        onValueChanged = onValueChanged,
                        items = (2..110).toList(),
                        selectedTextColor = Color(0xFF1892FA),
                        itemKey = { it.toString() },
                        itemText = { it.toString() }
                    )
                }

                TextButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(shape = RoundedCornerShape(8.dp), color = Color(0xFF1892FA)),
                    onClick = { onValueSaved(value) }
                ) {
                    Text(
                        text = stringResource(R.string.save_update),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgePicker(
    modifier: Modifier = Modifier,
    items: List<Int>,
    currentValue: Int,
    onValueChanged: (Int) -> Unit,
    selectedTextColor: Color,
    itemText: (Int) -> String,
    itemKey: (Int) -> Any,
) {
    val listStartIndex = items.indexOf(currentValue)

    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val visibleItem by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex
        }
    }

    LaunchedEffect(listState) {
        if (!listState.isScrollInProgress && listStartIndex >= 0 && listStartIndex < items.size) {
            listState.scrollToItem(listStartIndex)
        }
        snapshotFlow { visibleItem }
            .drop(1)
            .map { index ->
                if (index in items.indices) {
                    items[index]
                } else {
                    null
                }
            }
            .distinctUntilChanged()
            .collect { item ->
                if (item != null) {
                    onValueChanged(item)
                }
            }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 12.dp)
                .fillMaxWidth()
                .height(154.dp)
        ) {
            item(key = "first_empty_item") {
                Box(
                    modifier = Modifier
                        .width(68.dp)
                        .height(52.dp)
                )
            }

            items(items = items, key = { itemKey(it) }) { value ->
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .height(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = itemText(value),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontSize = if (currentValue == value) 28.sp else 24.sp,
                            lineHeight = 40.sp,
                            fontWeight = FontWeight(700),
                            color = if (currentValue == value) selectedTextColor else Color(0xFFECEDEF),
                            textAlign = TextAlign.Center,
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            item(key = "second_empty_item") {
                Box(
                    modifier = Modifier
                        .width(68.dp)
                        .height(52.dp)
                )
            }
        }

        Column(modifier = Modifier.width(60.dp)) {
            Divider(
                color = Color(0xFFF4F4F5),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(52.dp))

            Divider(
                color = Color(0xFFF4F4F5),
                modifier = Modifier.fillMaxWidth()
            )
        }

    }
}