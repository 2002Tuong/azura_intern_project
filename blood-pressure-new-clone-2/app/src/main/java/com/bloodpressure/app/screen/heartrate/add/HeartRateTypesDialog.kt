package com.bloodpressure.app.screen.heartrate.add

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bloodpressure.app.R
import com.bloodpressure.app.screen.record.BpType
import com.bloodpressure.app.ui.component.BloodButton
import com.bloodpressure.app.ui.theme.GrayScale900

@Composable
fun HeartRateTypesDialog(
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            val types = remember { HeartRateType.values() }

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                text = stringResource(R.string.type),
                style = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight(700),
                    color = GrayScale900,
                    textAlign = TextAlign.Center,
                )
            )

            types.forEach {
                HeartRateTypeInfo(
                    type = it,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                )
            }

            BloodButton(
                text = stringResource(R.string.got_it),
                onClick = onDismissRequest,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun HeartRateTypeInfo(modifier: Modifier = Modifier, type: HeartRateType) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(width = 1.dp, color = Color(0xFFF4F4F5))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .width(4.dp)
                    .height(28.dp)
                    .background(color = type.color, shape = RoundedCornerShape(8.dp))
            )

            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = stringResource(id = type.nameRes),
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = type.getAnnotatedString(),
                    color = Color(0xFF8C8E97),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}