package com.itsbatt.inotes.ui.screens

import androidx.compose.foundation.text.TextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.common.copy
import androidx.compose.foundation.text.common.paste
import androidx.compose.foundation.text.common.cut
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
import androidx.compose.ui.input.key.keyChars
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.ContentDescription
import androidx.compose.ui.unit.dp
import com.itsbatt.inotes.data.room.entities.Note
import com.itsbatt.inotes.ui.MainViewModel

@Composable
fun NoteDetailScreen(note: Note, onSaved: (Note) -> Unit = {}) {
    var titleValue by remember { mutableStateOf(note.title) }
    var bodyValue by remember { mutableStateOf(note.body) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Toolbar with back button and share
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { /* go back */ }
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
            keyboardOptions = keyboardOptions(
                immEditorAction = "done"
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
                    // Save and dismiss
                }
            ),
            softKeys = KeyboardSoftKeys.Default
        )

        // Save button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            color = MaterialTheme.colorScheme.primary
        ) {
            Button(
                onClick = {
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