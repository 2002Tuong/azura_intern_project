package com.artgen.app.ui.screen.genart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.artgen.app.R
import com.artgen.app.ui.theme.Neutral600
import com.artgen.app.ui.theme.Neutral900

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral900)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            LottieAnimation(
                composition,
                modifier = Modifier
                    .padding(top = 174.dp)
                    .size(170.dp),
                iterations = Int.MAX_VALUE
            )


            Text(
                text = stringResource(id = R.string.loading_gen_art_title),
                modifier = Modifier.padding(top = 24.dp),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )

            Text(
                text = stringResource(id = R.string.loading_gen_art_message),
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral600
            )
        }
    }
}
