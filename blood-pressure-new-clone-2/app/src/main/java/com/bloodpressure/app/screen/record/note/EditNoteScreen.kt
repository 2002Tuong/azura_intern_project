package com.bloodpressure.app.screen.record.note

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.data.model.Note
import com.bloodpressure.app.ui.component.BloodButton
import com.bloodpressure.app.utils.navigationBarsPaddingIfNeed
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    viewModel: NoteViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.cw_note)) },
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

        var showInputTag by remember { mutableStateOf(false) }
        if (showInputTag) {
            InputTag(
                onDoneClick = viewModel::createNote,
                onDismissRequest = { showInputTag = false }
            )
        }
        EditNoteContent(
            uiState = uiState,
            onDeleteClick = viewModel::delete,
            onAddNoteClick = { showInputTag = true },
            onSaveClick = onNavigateUp
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditNoteContent(
    modifier: Modifier = Modifier,
    uiState: NoteViewModel.UiState,
    onDeleteClick: (Note) -> Unit,
    onAddNoteClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    var deletingNote by remember { mutableStateOf<Note?>(null) }
    if (deletingNote != null) {
        AlertDialog(
            onDismissRequest = { deletingNote = null },
            text = { Text(text = stringResource(id = R.string.delete_tag_des)) },
            title = { Text(text = stringResource(id = R.string.delete_tag_title)) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteClick(deletingNote!!)
                    deletingNote = null
                }) {
                    Text(text = stringResource(id = R.string.cw_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingNote = null }) {
                    Text(text = stringResource(id = R.string.cw_cancel))
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPaddingIfNeed()
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            uiState.notes.forEach { note ->
                FilterChip(
                    selected = false,
                    onClick = { deletingNote = note },
                    label = { Text(text = note.name) },
                    modifier = Modifier.padding(start = 12.dp),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_remove_note),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    }
                )
            }
        }

        BloodButton(
            text = stringResource(id = R.string.add_new),
            onClick = onAddNoteClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.primary)
        )

        BloodButton(
            text = stringResource(id = R.string.cw_save),
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun InputTag(
    modifier: Modifier = Modifier,
    onDoneClick: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(true) {
        delay(100L)
        focusRequester.requestFocus()
        keyboard?.show()
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .clickable { onDismissRequest() },
            contentAlignment = Alignment.BottomCenter
        ) {
            var textInput by remember { mutableStateOf("") }
            var isError by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { text ->
                        isError = text.length > 20
                        textInput = text
                    },
                    supportingText = {
                        if (isError) {
                            Text(text = stringResource(id = R.string.error_limit_character_des))
                        }
                    },
                    shape = CircleShape,
                    isError = isError,
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.input_hint),
                            color = Color(0xFFC4CACF)
                        )
                    },
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                        .weight(1f)
                        .focusRequester(focusRequester)
                )

                IconButton(
                    onClick = { textInput = "" },
                    modifier = Modifier.padding(end = 2.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_clear_text),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }

                val isButtonEnabled = !isError && textInput.isNotEmpty()
                IconButton(
                    onClick = {
                        onDoneClick(textInput)
                        onDismissRequest()
                    },
                    modifier = Modifier.padding(end = 10.dp),
                    enabled = isButtonEnabled
                ) {
                    val iconRes = if (isButtonEnabled) {
                        R.drawable.ic_btn_done
                    } else {
                        R.drawable.ic_btn_done_disabled
                    }
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}
