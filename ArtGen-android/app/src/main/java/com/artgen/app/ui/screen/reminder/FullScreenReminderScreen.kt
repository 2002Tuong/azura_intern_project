package com.artgen.app.ui.screen.reminder

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.artgen.app.R
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun FullScreenReminderScreenType2(
    modifier: Modifier = Modifier,
    showUploadImage: Boolean = false,
    openApp: () -> Unit,
    viewModel: FullScreenReminderViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var currentTimeMillis by remember {
        mutableStateOf(System.currentTimeMillis())
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = currentTimeMillis) {
        while (true) {
            delay(1000L)
            currentTimeMillis = System.currentTimeMillis()
            calendar.timeInMillis = currentTimeMillis
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(top = 72.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = calendar.getTimeFormattedString("HH:mm"), style = TextStyle(
                    fontSize = 72.sp,
                    fontWeight = FontWeight(700),
                    color = Color(0xFFFFFFFF),
                    textAlign = TextAlign.Center,
                ), modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = calendar.getTimeFormattedString("EEEE dd MMMM"),
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = TextStyle(
                    fontSize = 20.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFFFFFFFF),
                    textAlign = TextAlign.Center,
                )
            )
        }
        Column(
            modifier = Modifier
                .padding(bottom = 56.dp)
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.Center,
        ) {
            FilledIconButton(
                onClick = {
                    context.getActivity()?.finish()
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 4.dp)
                    .size(24.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.Gray, contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }

            Card(
                modifier = Modifier
                    .wrapContentHeight()
                    .clickable {
                        viewModel.setSelectStyle(uiState.style)
                        openApp()
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF232943))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(color = Color(0xFF191D30)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(
                                    start = 16.dp, top = 12.dp, bottom = 12.dp, end = 6.dp
                                )
                                .size(24.dp)
                                .align(Alignment.CenterVertically)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF2935DD), Color(0xFF38ACFA)
                                        )
                                    ), shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Image(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .size(32.dp),
                                painter = painterResource(id = R.drawable.toolbar_logo),
                                contentDescription = null,
                            )
                        }

                        Text(
                            modifier = Modifier,
                            text = stringResource(id = R.string.app_name),
                            color = Color.White,
                            textAlign = TextAlign.Start,
                            fontWeight = FontWeight(700),
                            fontSize = 18.sp,
                        )

                    }

                    Text(
                        text = stringResource(id = R.string.last_updated_style),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp),
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight(700),
                            color = Color(0xFFFFFFFF),
                            letterSpacing = 0.1.sp,
                        )
                    )
                    Text(
                        text = stringResource(id = R.string.let_create_your_magic),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .padding(horizontal = 16.dp),
                        style = TextStyle(
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight(400),
                            color = Color(0xFFFFFFFF),
                            letterSpacing = 0.1.sp,
                        )
                    )

                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 24.dp)
                            .wrapContentHeight()
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            AsyncImage(
                                model = if (showUploadImage) uiState.style?.styledUrl else uiState.style?.originalUrl,
                                contentScale = ContentScale.FillBounds,
                                contentDescription = "",
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .aspectRatio(1f, true)
                            )

                            Spacer(modifier = Modifier.size(16.dp))

                            AsyncImage(
                                model = if (showUploadImage) R.drawable.ic_add_photo else uiState.style?.styledUrl,
                                contentScale = ContentScale.FillBounds,
                                contentDescription = "",
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .aspectRatio(1f, true)
                            )
                        }
                    }
                    Button(
                        onClick = {
                            viewModel.setSelectStyle(uiState.style)
                            openApp()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                            Color(0xFFFF2E6C)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.try_this_style),
                            color = Color.White,
                            fontWeight = FontWeight(700),
                            fontSize = 18.sp
                        )
                    }

                }
            }

        }
    }
}

fun Calendar.getTimeFormattedString(simpleDateFormatString: String): String {
    val format = SimpleDateFormat(simpleDateFormatString, Locale.getDefault())

    return format.format(time)
}

fun Context.getActivity(): AppCompatActivity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is AppCompatActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}
