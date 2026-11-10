package com.itsbatt.inotes.ui.screens

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.common.copy
import androidx.compose.foundation.text.common.paste
import androidx.compose.foundation.text.common.cut
import androidx.compose.foundation.text.TextField
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Getter
import androidx.compose.runtime.Repaint
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyCode
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.ContentDescription
import androidx.compose.ui.unit.dp
import com.itsbatt.inotes.data.room.entities.Note
import com.itsbatt.inotes.ui.MainViewModel

@Composable
fun NoteDetailScreen(
    note: Note,
    viewModel: MainViewModel,
    onSaved: (Note) -> Unit = {},
    onDelete: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var titleValue by remember { mutableStateOf(note.title) }
    var bodyValue by remember { mutableStateOf(note.body) }
    var saveLaunched by remember { mutableStateOf(false) }

    // Debounced auto-save
    LaunchedEffect(titleValue, bodyValue) {
        delay(1500)
        if (!saveLaunched) {
            saveLaunched = true
            val updatedNote = note.copy(
                title = titleValue,
                body = bodyValue,
                updatedAt = System.currentTimeMillis()
            )
            viewModel.createNote(updatedNote.title, updatedNote.body)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Toolbar with back button and share
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack
            ) {
                Icon(
                    imageVector = androidx.compose.ui.vectors.family.FamilyVector.Companion.dpBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = "INotes",
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(end = 24.dp)
            )

            IconButton(
                onClick = {
                    // Share note
                    val text = "Title: $titleValue\n\n$bodyValue"
                    AndroidUtils.shareText(text)
                }
            ) {
                Icon(
                    imageVector = androidx.compose.ui.vectors.family.FamilyVector.Companion.dpShare,
                    contentDescription = "Share"
                )
            }
        }

        // Title field - large at top, compacts on scroll
        TextField(
            value = titleValue,
            onValueChange = { titleValue = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(bottom = 4.dp),
            style = MaterialTheme.typography.h6,
            label = { Text("Titulo") },
            keyboardOptions = androidx.compose.ui.input.keyboard.KeyboardOptions(
                imEditorAction = "done"
            ),
            singleLine = true,
            maxLines = 1
        )

        // Body editor
        TextField(
            value = bodyValue,
            onValueChange = { bodyValue = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            style = MaterialTheme.typography.body1,
            label = { Text("Cuerpo") },
            keyboardActions = KeyboardActions(
                onDone = {
                    // Save and dismiss - the debounced effect will handle it
                }
            ),
            softKeys = androidx.compose.ui.softkeyboard.KeyboardSoftKeys.Default
        )

        // Save button (manual save)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Button(
                onClick = {
                    // Immediate save on button click
                    saveLaunched = true // Prevent debounced save
                    val updatedNote = note.copy(
                        title = titleValue,
                        body = bodyValue,
                        updatedAt = System.currentTimeMillis()
                    )
                    onSaved(updatedNote)
                }
            ) {
                Text("Guardar")
            }
        }
    }
}