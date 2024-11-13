package com.bloodpressure.app.screen.updateapp

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.bloodpressure.app.R

@Composable
fun AppUpdateDialog(
    title: String,
    message: String,
    isCancellable: Boolean,
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = title, style = MaterialTheme.typography.titleSmall) },
        text = { Text(text = message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(onClick = { onConfirmClick() }) {
                Text(text = stringResource(id = R.string.update_now))
            }
        },
        dismissButton = {
            if (isCancellable) {
                Button(onClick = { onDismissRequest() }) {
                    Text(text = stringResource(id = R.string.later))
                }
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = isCancellable,
            dismissOnClickOutside = isCancellable
        )
    )
}
