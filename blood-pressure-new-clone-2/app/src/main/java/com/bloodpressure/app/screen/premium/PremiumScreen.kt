package com.bloodpressure.app.screen.premium

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.PremiumPlan
import com.bloodpressure.app.utils.LocalBillingClientLifecycle
import com.bloodpressure.app.utils.LocalShareController
import com.bloodpressure.app.utils.LocalTextFormatter
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    viewModel: PremiumViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showPremiumPopup by remember { mutableStateOf(false) }
    if (showPremiumPopup) {
        PremiumPopup(
            onDismissRequest = {
                showPremiumPopup = false
                onNavigateUp()
            }
        )
    }
    val onBack = { showPremiumPopup = true }
    BackHandler(enabled = true) {
        onBack()
    }

    LaunchedEffect(uiState) {
        if (uiState.isPremium) {
            showPremiumPopup = false
            onNavigateUp()
        }
    }

    val upgradeProState by viewModel.upgradeProState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val billingClientLifecycle = LocalBillingClientLifecycle.current
    DisposableEffect(upgradeProState) {
        upgradeProState?.let {
            billingClientLifecycle.launchBillingFlow(context, it)
            viewModel.onBillingFlowLaunched()
        }
        onDispose {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_premium),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .navigationBarsPaddingIfNeed()
        ) {
            TopAppBar(
                title = {
                    Text(text = "")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = { onBack() }) {
                        Icon(
                            painter = rememberVectorPainter(image = Icons.Default.Close),
                            contentDescription = null
                        )
                    }
                }
            )

            var selectedProductId by remember {
                mutableStateOf(uiState.premiumPlans.firstOrNull()?.productId.orEmpty())
            }
            PremiumContent(
                uiState = uiState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                selectedProductId = selectedProductId,
                onSelectedProductIdChanged = { selectedProductId = it }
            )

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp),
                onClick = { viewModel.buyProduct(selectedProductId) },
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.btn_unlock),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                text = stringResource(id = R.string.cancel_anytime),
                color = Color(0xFF8C8E97),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )

            val shareController = LocalShareController.current
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { shareController.openTermOfService() }) {
                    Text(text = stringResource(R.string.term_of_service))
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(12.dp)
                        .background(color = Color(0xFF8C8E97))
                )

                TextButton(onClick = { shareController.openPrivacy() }) {
                    Text(text = stringResource(R.string.privacy))
                }
            }
        }
    }
}

@Composable
fun PremiumContent(
    modifier: Modifier = Modifier,
    uiState: PremiumViewModel.UiState,
    selectedProductId: String,
    onSelectedProductIdChanged: (String) -> Unit
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_premium_heart),
            contentDescription = null,
            modifier = Modifier.padding(top = 60.dp)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Tracker Pro",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Image(
                painter = painterResource(id = R.drawable.ic_premium_title),
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Text(
            text = stringResource(id = R.string.premium_des),
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 32.dp),
            textAlign = TextAlign.Center
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                uiState.premiumPlans.forEach { premium ->
                    PremiumPlanItem(
                        onItemClick = { onSelectedProductIdChanged(it.productId) },
                        premiumPlan = premium,
                        isSelected = selectedProductId == premium.productId
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.TopCenter))
            }
        }
    }
}

@Composable
fun PremiumPlanItem(
    modifier: Modifier = Modifier,
    onItemClick: (PremiumPlan) -> Unit,
    premiumPlan: PremiumPlan,
    isSelected: Boolean
) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { onItemClick(premiumPlan) },
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = if (isSelected) {
                BorderStroke(2.dp, color = MaterialTheme.colorScheme.primary)
            } else {
                BorderStroke(2.dp, color = Color(0xFFECEDEF))
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onItemClick(premiumPlan) },
                    modifier = Modifier.padding(start = 4.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Text(
                        text = premiumPlan.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF191D30),
                        fontWeight = FontWeight.Bold
                    )

                    if (premiumPlan.hasFreeTrial()) {
                        Text(
                            text = stringResource(
                                id = R.string.premium_item_des,
                                premiumPlan.freeTrialPeriod
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }

                Column(modifier = Modifier.padding(end = 14.dp)) {
                    if (premiumPlan.hasDiscount) {
                        Text(
                            text = premiumPlan.formattedOriginalPrice,
                            color = Color(0xFF8C8E97),
                            style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }

                    val textFormatter = LocalTextFormatter.current
                    val pricePerPeriod = remember(premiumPlan) {
                        if (premiumPlan.isProduct) {
                            premiumPlan.formattedBasePlanPrice
                        } else {
                            val period = textFormatter.convertToPeriodText(
                                premiumPlan.basePlanPeriod
                            )
                            "${premiumPlan.formattedBasePlanPrice}/$period"
                        }
                    }
                    Text(
                        text = pricePerPeriod,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (premiumPlan.hasDiscount) {
            val bgColor = if (isSelected) Color(0xFFF95721) else Color(0xFF8C8E97)
            Box(
                modifier = Modifier
                    .padding(end = 32.dp)
                    .background(
                        color = bgColor,
                        shape = CircleShape
                    )
                    .align(Alignment.TopEnd)
            ) {
                Text(
                    text = "${premiumPlan.discountPercent}% OFF",
                    modifier = Modifier.padding(vertical = 2.dp, horizontal = 6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
