package com.bloodpressure.app.screen.comingsoon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bloodpressure.app.R
import com.bloodpressure.app.ui.theme.GrayScale600
import com.bloodpressure.app.ui.theme.GrayScale900
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComingSoonScreen(
    modifier: Modifier = Modifier,
    titleResId: Int? = null,
    onNavigateUp: () -> Unit,
) {

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = if (titleResId != null) stringResource(id = titleResId) else "",
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

        ComingSoonScreen(modifier = Modifier.fillMaxSize(), onNavigateUp = onNavigateUp)
    }
}

@Composable
private fun ComingSoonScreen(modifier: Modifier = Modifier, onNavigateUp: () -> Unit) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPaddingIfNeed()
            .background(Color.White), contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(modifier = Modifier.fillMaxWidth(0.8f), painter = painterResource(id = R.drawable.coming_soon_img), contentDescription = "")

            Text(
                text = stringResource(R.string.coming_soon),
                style = TextStyle(
                    fontSize = 20.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight(800),
                    color = Color(0xFF1892FA),
                    textAlign = TextAlign.Center,
                )
            )

            Text(
                text = stringResource(R.string.our_programmer_is_working_hard_on_development),
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF8C8E97),
                    textAlign = TextAlign.Center,
                )
            )

            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = Color(0xFF1892FA), shape = RoundedCornerShape(size = 8.dp)),
                onClick = onNavigateUp
            ) {
                Text(
                    text = stringResource(R.string.back),
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF1892FA),
                        textAlign = TextAlign.Center,
                    )
                )
            }
        }
    }
}