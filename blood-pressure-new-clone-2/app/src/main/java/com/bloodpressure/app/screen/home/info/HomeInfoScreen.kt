package com.bloodpressure.app.screen.home.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ui.component.TopAppBarAction
import com.bloodpressure.app.ui.theme.GrayScale900
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeInfoScreen(
    modifier: Modifier = Modifier,
    onPremiumClick: () -> Unit,
    onHomeInfoItemClick: (HomeInfoType) -> Unit,
    onAlarmClick: () -> Unit,
    viewModel: HomeInfoViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.cw_info),

                    style = TextStyle(
                        fontSize = 20.sp,
                        lineHeight = 30.sp,
                        fontWeight = FontWeight(800),
                        color = GrayScale900,
                    )
                )
            },
            actions = {
                TopAppBarAction(
                    isPurchased = uiState.isPurchased,
                    onSetAlarmClick = onAlarmClick,
                    onNavigateToPremium = onPremiumClick
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        Spacer(
            modifier = Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(Color(0xFFECEDEF))
        )

        Surface(modifier = Modifier.fillMaxSize()) {
            HomeInfoContent(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onInfoItemClick = onHomeInfoItemClick
            )
        }
    }
}

@Composable
fun HomeInfoContent(
    modifier: Modifier = Modifier,
    onInfoItemClick: (HomeInfoType) -> Unit,
    uiState: HomeInfoViewModel.UiState
) {
    Box(modifier = modifier
        .background(Color.White)
        .padding(16.dp)) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            items(uiState.homeInfoList) { homeInfoType ->

                val gradientBrush = Brush.verticalGradient(colors = homeInfoType.colors)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brush = gradientBrush, shape = RoundedCornerShape(8.dp))
                        .clickable {
                            onInfoItemClick(homeInfoType)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 23.dp, horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = stringResource(id = homeInfoType.titleRes),
                            style = TextStyle(
                                fontSize = 18.sp,
                                lineHeight = 28.sp,
                                fontWeight = FontWeight(700),
                                color = Color.White,
                            )
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Image(
                            modifier = Modifier.size(48.dp),
                            painter = painterResource(id = homeInfoType.iconRes),
                            contentDescription = "",
                            contentScale = ContentScale.None
                        )
                    }
                }
            }
        }
    }
}