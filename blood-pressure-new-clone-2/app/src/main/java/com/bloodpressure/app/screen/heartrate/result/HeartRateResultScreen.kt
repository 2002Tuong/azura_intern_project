package com.bloodpressure.app.screen.heartrate.result

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.HeartRateRecord
import com.bloodpressure.app.screen.heartrate.add.HeartRateTypeItem
import com.bloodpressure.app.screen.heartrate.detail.TrendsBarChart
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateResultScreen(
    modifier: Modifier = Modifier,
    heartRateRecord: HeartRateRecord? = null,
    onNavigateUp: () -> Unit,
    onNavigateToDetail: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: HeartRateResultViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.shouldNavigateUp) {
        if (uiState.shouldNavigateUp) {
            onNavigateUp()
        }
    }

    LaunchedEffect(heartRateRecord) {
        if (heartRateRecord != null) {
            viewModel.setHeartRateRecord(heartRateRecord)
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


    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.result),
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                    )
                )
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

                if (uiState.recordId == null) {
                    IconButton(onClick = {
                        viewModel.save()
                        onNavigateToDetail()
                    }) {
                        Icon(
                            painter = rememberVectorPainter(image = Icons.Default.Check),
                            contentDescription = null
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            navigationIcon = {
                IconButton(onClick = { onNavigateUp() }) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.ArrowBack),
                        contentDescription = null
                    )
                }
            }
        )

        HeartRateResultContent(
            modifier = modifier
                .fillMaxSize()
                .weight(1f),
            uiState = uiState,
            onNavigateToHistory = onNavigateToHistory
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeartRateResultContent(
    modifier: Modifier = Modifier,
    uiState: HeartRateResultViewModel.UiState,
    onNavigateToHistory: () -> Unit
) {

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
    ) {
        var isScrollEnabled by remember { mutableStateOf(true) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(state = rememberScrollState(), enabled = isScrollEnabled)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Card(
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        HeartRateTypeItem(heartRateType = uiState.selectedHeartRateType)

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.your_heart_rate),
                            style = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight(400),
                                color = GrayScale900,
                                textAlign = TextAlign.Center,
                            )
                        )

                        Row(
                            modifier = Modifier
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Spacer(modifier = modifier.weight(1f))

                            Box(
                                modifier = Modifier
                                    .border(width = 1.dp, color = Color(0xFF8BC9FD), shape = RoundedCornerShape(size = 8.dp))
                                    .background(color = Color(0xFFE8F4FE), shape = RoundedCornerShape(size = 8.dp))
                                    .width(88.dp)
                                    .height(56.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${uiState.heartRateRecord?.heartRate}",
                                    style = TextStyle(
                                        fontSize = 28.sp,
                                        lineHeight = 40.sp,
                                        fontWeight = FontWeight(700),
                                        color = Color(0xFF1892FA),
                                        textAlign = TextAlign.Center,
                                    )
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 16.dp)
                                    .fillMaxHeight()
                            ) {
                                Text(
                                    text = stringResource(R.string.bpm),
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        lineHeight = 28.sp,
                                        fontWeight = FontWeight(700),
                                        color = GrayScale900,
                                    )
                                )
                            }

                        }

                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "${uiState.heartRateRecord?.date}, ${uiState.heartRateRecord?.time}",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight(400),
                                    color = GrayScale900,
                                )
                            )

                            if (uiState.heartRateRecord != null) {
                                NotesFlowRow(notes = uiState.heartRateRecord.notes)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {

                                Text(
                                    "Age: ${uiState.heartRateRecord?.age}", Modifier.padding(end = 4.dp),
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        fontWeight = FontWeight(400),
                                        color = GrayScale900,
                                    )
                                )

                                Spacer(
                                    modifier = Modifier
                                        .padding(0.dp)
                                        .width(1.dp)
                                        .height(20.dp)
                                        .background(color = Color(0xFFC4CACF))
                                )

                                Text(
                                    modifier = Modifier.padding(end = 4.dp),
                                    text = "Gender: ${if (uiState.heartRateRecord != null) context.getString(uiState.heartRateRecord?.genderType!!.nameRes) else "--"}",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp,
                                        fontWeight = FontWeight(400),
                                        color = GrayScale900,
                                    )
                                )
                            }
                        }

                    }
                }

                if (uiState.records.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {

                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(id = R.string.trends),
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        lineHeight = 28.sp,
                                        fontWeight = FontWeight(700),
                                        color = GrayScale900,
                                    )
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                                    TextButton(onClick = onNavigateToHistory) {
                                        Text(
                                            text = stringResource(id = R.string.history),
                                            style = TextStyle(
                                                fontSize = 15.sp,
                                                lineHeight = 22.sp,
                                                fontWeight = FontWeight(400),
                                                color = Color(0xFF1892FA),
                                                textAlign = TextAlign.Right,
                                                textDecoration = TextDecoration.Underline,
                                            )
                                        )
                                    }
                                }
                            }

                            TrendsBarChart(
                                modifier = Modifier.height(200.dp),
                                records = uiState.records,
                                onRecordSelected = { }
                            )
                        }
                    }
                }
            }

        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotesFlowRow(modifier: Modifier = Modifier, notes: Set<String>) {

    FlowRow(modifier = modifier.fillMaxWidth()) {
        notes.forEach { note ->
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .background(color = Color(0xFFECEDEF), shape = RoundedCornerShape(size = 8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = note,
                    style = TextStyle(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight(400),
                        color = GrayScale900,
                    )
                )
            }
        }
    }
}