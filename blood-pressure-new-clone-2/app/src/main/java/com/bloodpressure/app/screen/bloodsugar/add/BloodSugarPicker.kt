package com.bloodpressure.app.screen.bloodsugar.add

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodpressure.app.utils.Logger
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BloodSugarPicker(
    modifier: Modifier = Modifier,
    items: List<Float>,
    currentValue: Float,
    onValueChanged: (Float) -> Unit,
    selectedTextColor: Color,
    itemText: (Float) -> String,
    itemKey: (Float) -> Any,
) {
    val listStartIndex = items.indexOf(currentValue)

    val listStartIndex1 = remember { mutableStateOf(listStartIndex) }
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(key1 = items, block = {
        onValueChanged.invoke(currentValue)
    })

    val visibleItem by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex
        }
    }
    LaunchedEffect(currentValue) {
        if (!listState.isScrollInProgress && listStartIndex >= 0 && listStartIndex < items.size) {
            listState.scrollToItem(listStartIndex)
        }
        snapshotFlow { visibleItem }
            .drop(1)
            .map { index ->
                listStartIndex1.value = index
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