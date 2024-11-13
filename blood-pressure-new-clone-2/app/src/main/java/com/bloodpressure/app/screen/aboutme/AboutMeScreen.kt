package com.bloodpressure.app.screen.aboutme

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.screen.heartrate.add.GenderType
import com.bloodpressure.app.ui.component.Picker2
import com.bloodpressure.app.ui.theme.GrayScale600
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutMeScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    viewModel: AboutMeViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val adView by LocalAdsManager.current.homeBannerAd.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()
        .navigationBarsPaddingIfNeed()) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.about_me),
                    style = TextStyle(
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
                IconButton(onClick = { onNavigateUp() }) {
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.ArrowBack),
                        contentDescription = null
                    )
                }
            }
        )

        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(GrayScale600)
        )

        AboutMeContent(
            modifier = Modifier.fillMaxWidth().weight(1f),
            uiState = uiState,
            onAgeChanged = viewModel::setAge,
            onGenderChanged = viewModel::setGender
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
private fun AboutMeContent(
    modifier: Modifier = Modifier,
    uiState: AboutMeViewModel.UiState,
    onAgeChanged: (Int) -> Unit,
    onGenderChanged: (GenderType) -> Unit,
) {

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White, shape = RoundedCornerShape(8.dp))
                .weight(0.3f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.age),
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                        textAlign = TextAlign.Start
                    )
                )

                Picker2(
                    modifier = Modifier,
                    currentValue = uiState.age,
                    onValueChanged = onAgeChanged,
                    items = (2..110).toList(),
                    selectedTextColor = Color(0xFF1892FA),
                    itemKey = { it.toString() },
                    itemText = { it.toString() }
                )
            }

        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White, shape = RoundedCornerShape(8.dp))
                .weight(0.3f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.gender),
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                        textAlign = TextAlign.Start
                    )
                )

                Picker2(
                    modifier = Modifier,
                    currentValue = uiState.gender,
                    onValueChanged = onGenderChanged,
                    items = GenderType.values().toList(),
                    selectedTextColor = Color(0xFF1892FA),
                    itemKey = { it.toString() },
                    itemText = { context.getString(it.nameRes) }
                )
            }

        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
        )

//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .fillMaxHeight(0.3f),
//            shape = RoundedCornerShape(8.dp),
//            colors = CardDefaults.cardColors(containerColor = Color.Red)
//        ) {
//            Picker2(
//                modifier = Modifier.align(Alignment.CenterHorizontally),
//                currentValue = 0,
//                onValueChanged = { },
//                items = (2..110).toList(),
//                selectedTextColor = Color(0xFF1892FA),
//                itemKey = { it.toString() },
//                itemText = { it.toString() }
//            )
//        }
//
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .fillMaxHeight(0.3f),
//            shape = RoundedCornerShape(8.dp),
//            colors = CardDefaults.cardColors(containerColor = Color.Red)
//        ) {
//            Picker2(
//                modifier = Modifier.align(Alignment.CenterHorizontally),
//                currentValue = GenderType.OTHERS,
//                onValueChanged = { },
//                items = GenderType.values().toList(),
//                selectedTextColor = Color(0xFF1892FA),
//                itemKey = { it.toString() },
//                itemText = { context.getString(it.nameRes) }
//            )
//        }
    }
}