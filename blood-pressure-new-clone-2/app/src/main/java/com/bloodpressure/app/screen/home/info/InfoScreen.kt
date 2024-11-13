package com.bloodpressure.app.screen.home.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ads.LargeNativeAd
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.LocalAdsManager
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(
    modifier: Modifier = Modifier,
    onNavigateToInfoDetail: (String) -> Unit,
    onNavigateUp: () -> Unit,
    infoType: InfoItemType,
    viewModel: InfoViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val titleResId = when (infoType) {
        InfoItemType.BLOOD_PRESSURE -> R.string.blood_pressure
        InfoItemType.HEART_RATE -> R.string.heart_rate
        InfoItemType.WEIGHT_BMI -> R.string.weight_bmi
        InfoItemType.BLOOD_SUGAR -> R.string.blood_sugar
        InfoItemType.WATER_REMINDER -> R.string.water_reminder
        else -> throw IllegalArgumentException("Unsupported info type")
    }
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(title = {
            Text(
                text = stringResource(titleResId), style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight(700),
                    color = GrayScale900,
                )
            )
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        ), navigationIcon = {
            IconButton(onClick = { onNavigateUp() }) {
                Icon(
                    painter = rememberVectorPainter(image = Icons.Default.ArrowBack),
                    contentDescription = null
                )
            }
        })

        InfoContent(
            onItemClick = { onNavigateToInfoDetail(it.id) },
            uiState = uiState
        )
    }
}

@Composable
fun InfoContent(
    modifier: Modifier = Modifier,
    onItemClick: (InfoItemData) -> Unit,
    uiState: InfoViewModel.UiState
) {
    val nativeAd by LocalAdsManager.current.infoNativeAd.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = modifier.navigationBarsPaddingIfNeed(),
        contentPadding = PaddingValues(16.dp)
    ) {
        uiState.items.forEachIndexed { index, itemData ->
            item {
                InfoItem(
                    infoItemData = itemData,
                    onClick = onItemClick,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (index == 1 && !uiState.isPurchased && nativeAd != null) {
                item {
                    LargeNativeAd(
                        nativeAd = nativeAd!!,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoItem(
    modifier: Modifier = Modifier,
    infoItemData: InfoItemData,
    onClick: (InfoItemData) -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(74.dp)
            .clickable { onClick(infoItemData) }
    ) {
        Image(
            painter = painterResource(id = infoItemData.bgRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(id = infoItemData.titleRes),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = infoItemData.titleColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Image(
                painter = painterResource(id = infoItemData.imageRes),
                contentDescription = null,
                modifier = Modifier.padding(end = 24.dp)
            )
        }
    }
}
