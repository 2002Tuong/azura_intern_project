package com.bloodpressure.app.screen.heartrate.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bloodpressure.app.R
import com.bloodpressure.app.ui.component.Picker2
import com.bloodpressure.app.ui.theme.GrayScale900

@Composable
fun GenderPickerDialog(
    modifier: Modifier = Modifier,
    value: GenderType,
    onValueChanged: (GenderType) -> Unit,
    onValueSaved: (GenderType) -> Unit,
    onDismissRequest: () -> Unit,
    dismissOnClickOutside: Boolean = true
) {

    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = dismissOnClickOutside),
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(
                    color = Color.White, shape = RoundedCornerShape(8.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = stringResource(R.string.edit_your_gender),
                    style = TextStyle(
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight(700),
                        color = GrayScale900,
                        textAlign = TextAlign.Center,
                    )
                )

                Card(
                    modifier = Modifier.wrapContentWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Picker2(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        currentValue = value,
                        onValueChanged = onValueChanged,
                        items = GenderType.values().toList(),
                        selectedTextColor = Color(0xFF1892FA),
                        itemKey = { it.toString() },
                        itemText = { context.getString(it.nameRes) }
                    )
                }

                TextButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(shape = RoundedCornerShape(8.dp), color = Color(0xFF1892FA)),
                    onClick = { onValueSaved(value) }
                ) {
                    Text(
                        text = stringResource(R.string.save_update),
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight(700),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                        )
                    )
                }
            }
        }
    }
}