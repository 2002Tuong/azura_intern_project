package com.bloodpressure.app.screen.record.note

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bloodpressure.app.R
import com.bloodpressure.app.ui.component.BloodButton
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NoteDialog(
    onDismissRequest: () -> Unit,
    onNotesChanged: (Set<String>) -> Unit,
    onAddNoteClick: () -> Unit,
    title: String,
    notes: Set<String>,
    viewModel: NoteViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedNotes = remember(notes) { mutableStateListOf(*notes.toTypedArray()) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = null)
                }
            }

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp, start = 12.dp)
                        .height(32.dp)
                        .background(
                            Color(0xFFE8F4FE),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .drawBehind {
                            drawRoundRect(
                                color = Color(0xFF8BC9FD),
                                style = Stroke(
                                    width = 2f,
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(10f, 10f),
                                        0f
                                    )
                                ),
                                cornerRadius = CornerRadius(4.dp.toPx())
                            )
                        }
                        .clickable { onAddNoteClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit_blue),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                    )

                    Text(
                        text = stringResource(id = R.string.edit_add),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 6.dp, end = 12.dp)
                    )
                }
                uiState.notes.forEach { note ->
                    FilterChip(
                        selected = selectedNotes.contains(note.name),
                        onClick = {
                            if (selectedNotes.contains(note.name)) {
                                selectedNotes.remove(note.name)
                            } else {
                                selectedNotes.add(note.name)
                            }
                        },
                        label = { Text(text = note.name) },
                        border = null,
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFFECEDEF),
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            labelColor = Color(0xFF191D30),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }

            BloodButton(
                text = stringResource(id = R.string.cw_save),
                onClick = { onNotesChanged(selectedNotes.toSet()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}
