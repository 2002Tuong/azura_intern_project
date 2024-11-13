package com.artgen.app.ui.screen.result

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.artgen.app.R
import com.artgen.app.ui.theme.ArtGenTheme

@Composable
fun SaveDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick:() -> Unit,
    isPremium:Boolean
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF343D65)
            ),
        ) {
            Column (
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { onDismissRequest()},
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.End)
                        .background(
                            color = Color(0xA0282F4D),
                            shape = CircleShape
                        ).size(28.dp),
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        tint = Color(0xFF7884BA))
                }

                Text(
                    text = stringResource(R.string.hasnot_save_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = stringResource(R.string.do_you_really_want_to_exit),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight(400),
                    color = Color(0xFF7884BA),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                Row(
                    modifier = Modifier.height(IntrinsicSize.Min).padding(horizontal = 24.dp)
                        .padding(bottom = 30.dp)
                ) {
                    Button(
                        onClick = onCancelClick,
                        modifier = Modifier.weight(1f).padding(end = 10.dp).fillMaxHeight(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF282F4D)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.lose_it),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.height(24.dp),
                            color = Color(0xFF5A69AA),
                        )
                    }

                    Button(
                        onClick = onSaveClick,
                        modifier = Modifier.weight(1f).padding(start = 10.dp).fillMaxHeight(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if(!isPremium) {
                                Image(
                                    painter = painterResource(R.drawable.ic_ads),
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }

                            Text(
                                text = stringResource(R.string.save),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SaveDialogPreview() {
    ArtGenTheme {
        SaveDialog(
            onDismissRequest = {},
            onSaveClick = {},
            isPremium = true,
            onCancelClick = {}
        )
    }
}