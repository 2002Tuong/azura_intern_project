package com.bloodpressure.app.screen.premium

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ui.component.BloodButton
import com.bloodpressure.app.utils.LocalTextFormatter
import org.koin.androidx.compose.koinViewModel

@Composable
fun PremiumPopup(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    viewModel: PremiumViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(modifier = modifier.fillMaxWidth()) {
                IconButton(onClick = onDismissRequest, modifier = Modifier.align(Alignment.End)) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = null)
                }

                Image(
                    painter = painterResource(id = R.drawable.ic_vip),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )

                uiState.promotePremiumPlan?.let { premiumPlan ->
                    val title = if (premiumPlan.hasFreeTrial()) {
                        stringResource(
                            id = R.string.popup_premium_title,
                            premiumPlan.name,
                            premiumPlan.freeTrialPeriod
                        )
                    } else {
                        premiumPlan.name
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 40.dp, top = 12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_tick),
                            contentDescription = null
                        )

                        Text(
                            text = stringResource(id = R.string.remove_all_ads),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 40.dp, top = 12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_tick),
                            contentDescription = null
                        )

                        Text(
                            text = stringResource(id = R.string.cancel_anytime),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    BloodButton(
                        text = stringResource(id = R.string.cw_continue),
                        onClick = { viewModel.buyProduct(premiumPlan.productId) },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 20.dp)
                            .fillMaxWidth()
                    )

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
                        text = if (premiumPlan.hasFreeTrial()) {
                            stringResource(
                                id = R.string.popup_premium_free_trial_des,
                                premiumPlan.freeTrialPeriod,
                                pricePerPeriod
                            )
                        } else {
                            pricePerPeriod
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 24.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PremiumPopupPreview() {
    PremiumPopup(onDismissRequest = {})
}
