package com.bloodpressure.app.screen.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.bloodpressure.app.R
import com.bloodpressure.app.ads.LargeNativeAd
import com.google.android.gms.ads.nativead.NativeAd

@Composable
fun ConfirmExitDialog(
    nativeAd: NativeAd?,
    onDismissRequest: () -> Unit,
    onConfirmButtonClick: () -> Unit,
    onDismissButtonClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.exit_popup_title),
                    style = MaterialTheme.typography.headlineSmall,
                )

                Text(
                    text = stringResource(id = R.string.exit_popup_message),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )

                nativeAd?.let {
                    LargeNativeAd(nativeAd = it, modifier = Modifier.padding(top = 16.dp))
                }

                Row(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.End)
                        .wrapContentSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        onDismissRequest()
                        onDismissButtonClick()
                    }) {
                        Text(
                            text = stringResource(id = R.string.no_exit),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    TextButton(
                        onClick = {
                            onDismissRequest()
                            onConfirmButtonClick()
                        }, modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.rate_app),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
