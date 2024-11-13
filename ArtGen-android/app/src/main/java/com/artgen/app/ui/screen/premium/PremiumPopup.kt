package com.artgen.app.ui.screen.premium

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artgen.app.R
import com.artgen.app.ui.theme.ArtGenTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalTextApi::class)
@Composable
fun PremiumPopup(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    viewModel: PremiumViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF232943)
            ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.TopCenter
            ) {
                Image(
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Crop,
                    painter = painterResource(R.drawable.premium_popup_bg),
                    contentDescription = null
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = { onDismissRequest()},
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.End)
                            .background(
                                color = Color(0xA0282F4D),
                                shape = CircleShape)
                    ) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = null,
                            tint = Color(0xFF7884BA))
                    }

                    Image(
                        painter = painterResource(id = R.drawable.ic_premium_title),
                        contentDescription = null,
                        modifier = Modifier
                            .size(68.dp)
                            .padding(top = 8.dp)
                    )

                   uiState.promotePremiumPlan?.let {
                        val title = if(it.hasFreeTrial()) {
                            stringResource(R.string.popup_premium_title, it.name, it.freeTrialPeriod)
                        }else {
                            stringResource(R.string.start_with, it.name)
                        }

                       Text(
                           text = title,
                           style = TextStyle(
                               brush = Brush.linearGradient(textGradientColor)
                           ),
                           fontSize = 18.sp,
                           fontWeight = FontWeight.Bold,
                           textAlign = TextAlign.Center,
                           modifier = Modifier
                               .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
                       )
                   }

                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.wrapContentHeight()
                    ) {
                        BenefitSlogan(message = stringResource(R.string.unlimited_feature),
                            modifier = Modifier.padding(bottom = 12.dp))
                        BenefitSlogan(message = stringResource(R.string.remove_all_ads),
                            modifier = Modifier.padding(bottom = 12.dp))
                        BenefitSlogan(message = stringResource(R.string.cancel_any_time),
                            modifier = Modifier.padding(bottom = 12.dp))
                    }

                    uiState.promotePremiumPlan?.let { premiumPlan ->
                        if(!uiState.isPremium) {
                            Button(
                                modifier = Modifier
                                    .padding(top = 20.dp)
                                    .padding(horizontal = 16.dp)
                                    .background(
                                        brush = Brush.linearGradient(gradientColor),
                                        shape = RoundedCornerShape(8.dp)
                                    ).fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                onClick = {viewModel.buyProduct(premiumPlan.productId)},
                            ) {
                                Text(
                                    stringResource(R.string.text_continue),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            val pricePerPeriod = remember(premiumPlan) {
                                if(premiumPlan.isProduct) {
                                    premiumPlan.formattedBasePlanPrice
                                } else {
                                    val period = viewModel.convertToPeriodText(
                                        premiumPlan.basePlanPeriod
                                    )
                                    "${premiumPlan.formattedBasePlanPrice}/$period"
                                }
                            }
                            Text(
                                text = if(premiumPlan.hasFreeTrial()) {
                                    stringResource(
                                        R.string.popup_premium_free_trial_des,
                                        premiumPlan.freeTrialPeriod,
                                        pricePerPeriod)
                                }
                                else {
                                    stringResource(R.string.then_pay, pricePerPeriod)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun PremiumPopupPreview() {
    ArtGenTheme {
        PremiumPopup(
            onDismissRequest = {}
        )
    }
}