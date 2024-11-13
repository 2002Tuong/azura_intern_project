package com.artgen.app.ui.screen.genart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artgen.app.R
import com.artgen.app.ui.screen.premium.gradientColor
import com.artgen.app.ui.theme.ArtGenTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenArtBottomPopup(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onActionButtonClick: () -> Unit,
    onGoProButtonClick: () -> Unit,
    actionButtonTitle: String,
    actionButtonSubtitle: String,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = Color(0xFF343D65),
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        ActionButton(
            title = actionButtonTitle,
            subtitle = actionButtonSubtitle,
            onClick = onActionButtonClick
        )

        UnlockProButton {
            onGoProButtonClick()
        }
    }
}

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    title: String,
    subtitle: String,
) {
    Button(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .padding( bottom = 12.dp),
        onClick = {
            onClick()
        },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight(700)
            )

            Text(
                text = subtitle,
                color = Color(0xFFFFA8C2),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun UnlockProButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .padding(bottom = 30.dp),
        onClick = {
            onClick()
        },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF191D30)
        ),
        contentPadding = PaddingValues(start = 0.dp, end = 0.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = stringResource(R.string.unlock_pro),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight(700)
                )

                Text(
                    text = stringResource(R.string.no_ads_unlimited_access),
                    color = Color(0xFF8C8E97),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier =Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .background(
                        brush = Brush.linearGradient(gradientColor),
                        shape = RoundedCornerShape(20.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_premium_title),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).padding(start = 6.dp)
                )

                Text(
                    text = stringResource(R.string.pro),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 5.dp)

                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewButton() {
    ArtGenTheme {
        Column {
            ActionButton(
                onClick = {},
                title = "Generate Now",
                subtitle = "Watch video as"
            )
            UnlockProButton {  }
        }

    }
}