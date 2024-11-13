package com.bloodpressure.app.screen.bmi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bloodpressure.app.data.model.BMIOptionType
import com.bloodpressure.app.screen.bmi.listfeature.BMIListFeatureScreen

@Composable
fun BMIScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    onItemClick:(BMIOptionType) -> Unit,
    onNavigateToPremium: () -> Unit
) {

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        BMIListFeatureScreen(
            onNavigateUp = { onNavigateUp() },
            onBMIOptionClick = {
                onItemClick(it)
            },
            onNavigateToPremium = onNavigateToPremium
        )
    }
}