package com.artgen.app.ui.screen.premium

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artgen.app.R
import com.artgen.app.data.model.PremiumPlan
import com.artgen.app.ui.theme.ArtGenTheme
import com.artgen.app.utils.LocalShareController
import com.artgen.app.utils.navigationBarsPaddingIfNeed
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalTextApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    viewModel: PremiumViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showPremiumPopup by remember { mutableStateOf(false) }


    if(showPremiumPopup) {
        PremiumPopup( onDismissRequest =  {
            showPremiumPopup = false
            onNavigateUp()
        })
    }

    val backHandle = {
        if(uiState.isPremium) {
            onNavigateUp()
        } else {
            showPremiumPopup = true
        }
    }

    BackHandler(enabled = true) {
        backHandle()
    }

//    LaunchedEffect(uiState) {
//        if (uiState.isPremium) {
//            viewModel.shouldShowPremiumPopup(false)
//            onNavigateUp()
//        }
//    }

    val upgradeProState by viewModel.upgradeProState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    DisposableEffect(upgradeProState) {
        upgradeProState?.let {
            viewModel.launchBillingFlow(context, params = it)
            viewModel.onBillingFlowLaunched()
        }
        onDispose {  }
    }

    var selectedProductId by remember {
        mutableStateOf(uiState.premiumPlans.firstOrNull {
            it.premiumConfig?.isBest ?: false
        }?.product?.productId.orEmpty())
    }

    Box(
        modifier = modifier.fillMaxSize()
            .background(Color.Black)
    ) {
        Image(
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop,
            painter = painterResource(R.drawable.premium_bg),
            contentDescription = null,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPaddingIfNeed()
        ) {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                title = {},
                navigationIcon = {
                    IconButton(
                        modifier = Modifier
                            .background(
                                color = Color(0xA0282F4D),
                                shape = CircleShape
                            ),
                        onClick = {backHandle()}
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = null,
                                tint = Color(0xFF7884BA))
                        }
                    }
                }
            )

            Column(
                modifier = Modifier
                    .padding(top = 265.dp)
                    .fillMaxSize()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Text(
                        stringResource(R.string.artgen_pro),
                        style = TextStyle(
                            brush = Brush.linearGradient(
                                textGradientColor
                            ),
                        ),
                        fontSize = 24.sp,
                        fontWeight = FontWeight(700),
                        modifier = Modifier.padding(end = 9.dp)
                    )

                    Image(
                        painter = painterResource(R.drawable.ic_premium_title),
                        contentDescription = null
                    )
                }

                if(!uiState.isPremium) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.weight(6f)
                        ) {
                            BenefitSlogan(message = stringResource(R.string.no_ad_experience),
                                modifier = Modifier.padding(bottom = 4.dp))

                            BenefitSlogan(message = stringResource(R.string.unlimited_feature),
                                modifier = Modifier.padding(bottom = 4.dp))
                        }

                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.weight(5f)
                        ) {
                            BenefitSlogan(message = stringResource(R.string.high_speed_ai_draw),
                                modifier = Modifier.padding(bottom = 4.dp))

                            BenefitSlogan(message = stringResource(R.string.ultra_high_resolution),
                                modifier = Modifier.padding(bottom = 4.dp))
                        }
                    }

                    Column(
                        modifier = modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 10.dp, top = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        uiState.premiumPlans.forEach { premium ->
                            PremiumPlanItem(
                                premiumPlan = premium,
                                isSelected = selectedProductId == premium.productId,
                                onItemClick ={ selectedProductId = it.productId},
                                convertToPeriodText = {viewModel.convertToPeriodText(it)}
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.artgen_pro_unlock),
                        style = TextStyle(
                            brush = Brush.linearGradient(
                                textGradientColor
                            ),
                        ),
                        fontSize = 20.sp,
                        fontWeight = FontWeight(700),
                    )
                }
            }

            if(!uiState.isPremium) {
                PremiumActionPart(
                    selectedProductId = selectedProductId,
                    onContinue = {viewModel.buyProduct(it)}
                )
            }

        }

    }
}

@Composable
fun BenefitSlogan(
    modifier: Modifier = Modifier,
    message: String
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = rememberVectorPainter(image = Icons.Default.Done),
            tint = Color.White,
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(2.dp))

        Text(
            message,
            fontSize = 11.sp,
            fontWeight = FontWeight(400),
            color = Color.White
        )
    }
}

@Composable
fun PremiumScreenContent(
    modifier: Modifier = Modifier,
    uiState: PremiumViewModel.UiState,
    selectedProductId: String,
    onSelectedProductIdChanged: (String) -> Unit,
    onContinue:(String) -> Unit,
    viewModel: PremiumViewModel
) {
    Column(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        uiState.premiumPlans.forEach { premium ->
            PremiumPlanItem(
                premiumPlan = premium,
                isSelected = selectedProductId == premium.productId,
                onItemClick ={ onSelectedProductIdChanged(premium.productId)},
                convertToPeriodText = {viewModel.convertToPeriodText(it)}
            )
        }

        Button(
            modifier = Modifier
                .padding(top = 10.dp)
                .background(
                brush = Brush.linearGradient(gradientColor),
                shape = RoundedCornerShape(8.dp)
            ).fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            onClick = {onContinue(selectedProductId)},
        ) {
            Text(
                stringResource(R.string.text_continue),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = stringResource(R.string.cancel_anytime),
            modifier = Modifier.padding(top = 12.dp),
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight(400),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        )

        val shareController = LocalShareController.current
        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { shareController.openTermOfService() }) {
                Text(
                    text = stringResource(R.string.term_of_service),
                    color = Color(0xFF8C8E97),
                    textDecoration = TextDecoration.Underline
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(12.dp)
                    .background(color = Color(0xFF8C8E97))
            )

            TextButton(onClick = { shareController.openPrivacy() }) {
                Text(
                    text = stringResource(R.string.privacy),
                    color = Color(0xFF8C8E97),
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun PremiumPlanItem(
    modifier: Modifier = Modifier,
    premiumPlan: PremiumPlan,
    isSelected: Boolean,
    onItemClick: (PremiumPlan) -> Unit,
    convertToPeriodText: (String) -> String
) {
    Box(
        modifier = modifier
    ) {
        val bgColor = if(!isSelected) Color(0xFF191D30) else Color(0x1AFF2E6C)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable { onItemClick(premiumPlan) },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            border = if(isSelected){
                    BorderStroke(2.dp, brush = Brush.linearGradient(gradientColor))
                }else {
                    null
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(4f)
                        .padding(start = 20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    if(isSelected) {
                        Text(
                            text = premiumPlan.name,
                            style = TextStyle(
                                brush = Brush.linearGradient(textGradientColor),
                                fontSize = 16.sp,
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    } else {
                        Text(
                            text = premiumPlan.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }

                    if(premiumPlan.hasFreeTrial()) {
                        Text(
                            text = stringResource(R.string.premium_item_des, premiumPlan.freeTrialPeriod),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            modifier = Modifier.padding(end = 4.dp).align(Alignment.Start)
                        )
                    }
                }

                if(premiumPlan.hasDiscount) {
                    Box(
                        modifier = Modifier
                            .weight(5f)
                            .padding(end = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .wrapContentWidth()
                                .background(
                                    brush = Brush.linearGradient(textGradientColor),
                                    shape = RoundedCornerShape(8.dp)
                                ).align(Alignment.CenterStart)
                        ) {
                            Text(
                                text = stringResource(R.string.discount_off, premiumPlan.discountPercent),
                                modifier = Modifier.padding(vertical = 2.dp, horizontal = 4.dp).align(Alignment.CenterStart),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF604306)
                            )
                        }
                    }
                }

                val pricePerPeriod = remember(premiumPlan) {
                    if(premiumPlan.isProduct) {
                        premiumPlan.formattedBasePlanPrice
                    } else {
                        premiumPlan.formattedBasePlanPrice
                    }
                }
                Column(modifier = Modifier.padding(end = 10.dp).weight(5f)) {
                    Text(
                        text = pricePerPeriod,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = modifier.align(Alignment.End)
                    )

                    Text(
                        text = premiumPlan.formattedOriginalPrice,
                        color = Color(0xFF8C8E97),
                        style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumActionPart(
    modifier: Modifier = Modifier,
    selectedProductId: String,
    onContinue: (String) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
            .background(Color.Black)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            modifier = Modifier
                .padding(top = 10.dp)
                .background(
                    brush = Brush.linearGradient(gradientColor),
                    shape = RoundedCornerShape(8.dp)
                ).fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            onClick = {onContinue(selectedProductId)},
        ) {
            Text(
                stringResource(R.string.text_continue),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = stringResource(R.string.cancel_anytime),
            modifier = Modifier.padding(top = 12.dp),
            style = TextStyle(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight(400),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        )

        val shareController = LocalShareController.current
        Row(
            modifier = Modifier
                .padding(top = 0.dp)
                .align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { shareController.openTermOfService() }) {
                Text(
                    text = stringResource(R.string.term_of_service),
                    color = Color(0xFF8C8E97),
                    textDecoration = TextDecoration.Underline
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(12.dp)
                    .background(color = Color(0xFF8C8E97))
            )

            TextButton(onClick = { shareController.openPrivacy() }) {
                Text(
                    text = stringResource(R.string.privacy),
                    color = Color(0xFF8C8E97),
                    textDecoration = TextDecoration.Underline
                )
            }
        }
    }
}

val textGradientColor = listOf(
    Color(0xFFF2BB46),
    Color(0xFFFBE67D),
    Color(0xFFF5C45E)
)

val gradientColor = listOf(
    Color(0xFFFE9A38),
    Color(0xFFFF5F28),
    Color(0xFFA54C9D),
    Color(0xFF6A42E3),
    Color(0xFF4C83F7),
    Color(0xFF3CF8EA)
)
@Composable
@Preview
fun PremiumScreenPreview() {
    ArtGenTheme {
        PremiumScreen(onNavigateUp = {})
    }
}

@Composable
@Preview
fun PremiumPlanItemPreView() {
    ArtGenTheme {
        PremiumPlanItem(
            isSelected = true,
            onItemClick = {},
            premiumPlan = PremiumPlan(),
            convertToPeriodText = {"123"}
        )
    }
}