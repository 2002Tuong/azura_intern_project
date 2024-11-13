package com.bloodpressure.app.screen.bloodsugar.add

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ads.SmallNativeAd
import com.bloodpressure.app.screen.alarm.AlarmType
import com.bloodpressure.app.screen.alarm.SetAlarmDialog
import com.bloodpressure.app.screen.bloodsugar.SCOPE_ID
import com.bloodpressure.app.screen.bloodsugar.SCOPE_NAME
import com.bloodpressure.app.screen.heartrate.add.AgePickerDialog
import com.bloodpressure.app.screen.heartrate.add.GenderPickerDialog
import com.bloodpressure.app.screen.home.tracker.TrackerType
import com.bloodpressure.app.ui.component.TopAppBarAction
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.LocalRemoteConfig
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.getKoin
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BloodSugarDetailScreen(
    modifier: Modifier = Modifier,
    recordId: Long? = null,
    onNavigateUp: () -> Unit,
    onTargetSelected: () -> Unit,
    onNavigateToAddNote: () -> Unit,
    title: String,
    onNavigateToPremium: () -> Unit,
    onSaveRecord: () -> Unit
) {
    val viewModelScope = getKoin().getOrCreateScope(SCOPE_ID, named(SCOPE_NAME))
    val viewModel = viewModelScope.get<BloodSugarDetailViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adsManager = LocalAdsManager.current
    val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()
    val shouldShowNative = LocalRemoteConfig.current.adsConfig.shouldShowAddRecordNativeAd

    LaunchedEffect(Unit) {
        adsManager.loadAddRecordFeatureInter(TrackerType.BLOOD_SUGAR)
    }

    LaunchedEffect(key1 = recordId, block = {
        if (recordId != null && recordId != -1L) {
            viewModel.getRecord(recordId)
        } else {
            viewModel.setDefault()
        }
    })

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
            value = uiState.genderType,
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

    if (uiState.shouldShowDeleteDialog && recordId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearConfirmDelete() },
            text = { Text(text = stringResource(id = R.string.delete_record_des)) },
            title = { Text(text = stringResource(id = R.string.delete_record_title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecord(recordId)
                        viewModel.clearConfirmDelete()
                        leaveScreen(viewModelScope, onNavigateUp)
                    }
                ) {
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

    if (uiState.showSetAlarmDialog) {
        SetAlarmDialog(
            alarmType = AlarmType.BLOOD_SUGAR,
            onDismissRequest = { viewModel.showSetReminder(false) },
            onSave = { alarmRecord ->
                viewModel.insertRecord(alarmRecord)
                viewModel.showSetReminder(false)
            }
        )
    }

    Column(
        modifier = modifier
        .navigationBarsPaddingIfNeed()
    ) {
        TopAppBar(
            title = { Text(text = title) },
            actions = {
                TopAppBarAction(
                    isPurchased = uiState.isPurchased,
                    onSetAlarmClick = {
                        viewModel.showSetReminder(true)
                    },
                    onNavigateToPremium = onNavigateToPremium,
                    additionalAction = {
                        uiState.recordId?.let {
                            TextButton(onClick = { viewModel.confirmDelete() }) {
                                Text(text = stringResource(id = R.string.cw_delete))
                            }
                        }
                    }
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            ),
            navigationIcon = {
                IconButton(
                    onClick = {
                        leaveScreen(viewModelScope, onNavigateUp)
                    }
                ) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.ArrowBack),
                        contentDescription = null
                    )
                }
            }
        )
        BloodSugarDetailContent(
            modifier = Modifier.weight(1f),
            onSaveRecord = {
                adsManager.showAddFeatureInterAds(TrackerType.BLOOD_SUGAR) {
                    leaveScreen(viewModelScope, onSaveRecord)
                    viewModel.saveRecord()
                }
            },
            onAddNoteClick = onNavigateToAddNote,
            onTimeChanged = viewModel::setTime,
            onDateChanged = viewModel::setDate,
            onSelectedNotesChanged = viewModel::setNotes,
            onTargetSelected = onTargetSelected,
            onBloodSugarRateValueChange = viewModel::setBloodSugarRate,
            onSaveState = viewModel::setSate,
            updateMeasuredUnitToMol = viewModel::updateMeasureUnit,
            onSetLastValue = viewModel::setLastValue,
            onEditAgeClick = { viewModel.showAgeDialog(true) },
            onEditGenderClick = { viewModel.showGenderDialog(true) },
            uiState = uiState
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

private fun leaveScreen(scope: Scope, action: () -> Unit) {
    scope.close()
    action.invoke()
}