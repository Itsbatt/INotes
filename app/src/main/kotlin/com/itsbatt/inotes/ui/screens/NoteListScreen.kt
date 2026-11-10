package com.itsbatt.inotes.ui.screens

import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FillMaxSize
import androidx.compose.foundation.layout.Padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Getter
import androidx.compose.runtime.Recompose
import androidx.compose.runtime.cache
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.opacity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp as dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.compose.viewModelInstance
import com.itsbatt.inotes.data.room.entities.Note
import com.itsbatt.inotes.ui.MainViewModel
import com.itsbatt.inotes.ui.screens.note-detail.NoteDetailScreen
import com.itsbatt.inotes.ui.screens.folder.FolderScreen

@Composable
fun NoteListScreen() {
    val viewModel: MainViewModel = viewModel(
        factory = NoteListViewModelFactory()
    )

    var scrollState by rememberScrollState(rememberScrollStateValue = remember { ScrollState() })
    var showFAB by remember { mutableStateOf(true) }
    var searchText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        arrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Header with app title and search
        NoteListHeader(
            searchText = searchText,
            onSearchChanged = { viewModel.setSearchQuery(it) }
        )

        // Notes grouped by date
        NoteListContent(
            notes = filterNotes(viewModel.notes.value),
            pinnedNotes = viewModel.pinnedNotes.value,
            onNoteClick = { note ->
                NoteDetailScreen(note = note) { savedNote ->
                    // Handle note save - update in viewModel
                    viewModel.createNote(savedNote.title, savedNote.body)
                }
            },
            onNoteDelete = { noteId ->
                viewModel.deleteNote(noteId)
            },
            onNotePinToggle = { noteId ->
                viewModel.togglePin(noteId)
            },
            onNoteRestore = { noteId ->
                // Restore from trash
                viewModel.restoreFromTrash(noteId)
            }
        )

        // FAB for creating new note
        if (showFAB) {
            Box(modifier = Modifier.align(Alignment.End)) {
                FloatingActionButton(
                    onClick = {
                        // Open note editor
                    }
                )
                {
                    Image(
                        painter = painterResource(id = "ic_add"),
                        contentDescription = "Create new note"
                    )
                }
            }
        }
    }
}

@Composable
fun NoteListHeader(searchText: String, onSearchChanged: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp),
        arrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "INotes",
            style = MaterialTheme.typography.h5,
            maxLines = 1,
            style = MaterialTheme.typography.subtitle1
        )

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it; onSearchChanged(it) },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(8.dp),
            label = { Text("Buscar") },
            placeholder = { Text("Buscar notas...") }
        )
    }
}

@Composable
fun NoteListContent(
    notes: List<Note>,
    pinnedNotes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onNoteDelete: (Int) -> Unit,
    onNotePinToggle: (Int) -> Unit,
    onNoteRestore: (Int) -> Unit
) {
    // Group notes by date
    val groupedNotes = groupNotesByDate(notes)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Render date groups
        itemsIndexed(groupedNotes) { index, (date, notesForDate) ->
            renderDateGroup(
                date = date,
                notes = notesForDate,
                isPinnedSection = index == 0,
                onNoteClick = onNoteClick,
                onNoteDelete = onNoteDelete,
                onNotePinToggle = onNotePinToggle,
                onNoteRestore = onNoteRestore
            )
        }
    }
}

@Composable
fun renderDateGroup(
    date: String,
    notes: List<Note>,
    isPinnedSection: Boolean,
    onNoteClick: (Note) -> Unit,
    onNoteDelete: (Int) -> Unit,
    onNotePinToggle: (Int) -> Unit,
    onNoteRestore: (Int) -> Unit
) {
    // Date header
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp, 8.dp),
        elevation = 1.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.subtitle2,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    // Render notes - pinned first, then regular
    // Sort: pinned first, then by recency
    val allNotes = notes.sortedBy { if (it.isPinned) 0 else 1 }
    items(allNotes) { note ->
        NoteItem(
            note = note,
            onClick = onNoteClick,
            onDelete = onNoteDelete,
            onPinToggle = onNotePinToggle,
            onRestore = onNoteRestore,
            isPinned = note.isPinned
        )
    }
}

@Composable
fun NoteItem(
    note: Note,
    onClick: (Note) -> Unit,
    onDelete: (Int) -> Unit,
    onPinToggle: (Int) -> Unit,
    onRestore: (Int) -> Unit,
    isPinned: Boolean
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp)),
        elevation = if (isPinned) 4.dp else 1.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            // Title line - large at top, compacts on scroll
            if (isPinned) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.h6,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = note.body.take(80) ?: "",
                style = MaterialTheme.typography.body2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Actions row (delete, etc.) - shown when swiping
            if (showDeleteConfirm) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.End),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { /* handle delete */ }
                    ) {
                        Text("Eliminar")
                    }

                    Text(
                        text = "Mover a Papelera",
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        }
    }
}

private fun List<Note>.filterNotes(searchQuery: String): List<Note> {
    if (searchQuery.isEmpty()) return this
    return filter { note ->
        note.title.contains(searchQuery, ignoreCase = true) ||
        note.body.contains(searchQuery, ignoreCase = true)
    }
}

private fun List<Note>.groupNotesByDate(): Map<String, List<Note>> {
    val grouped = mutableMapOf<String, List<Note>>()

    for (note in this) {
        val dateKey = when {
            note.updatedAt >= System.currentTimeMillis() - 24 * 60 * 60 * 1000 -> "Hoy"
            note.updatedAt >= System.currentTimeMillis() - 48 * 60 * 60 * 1000 -> "Ayer"
            note.updatedAt >= System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000 -> "Esta semana"
            note.updatedAt >= System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000 -> "Este mes"
            else -> "Año ${java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault()).format(note.updatedAt)}"
        }
        grouped.putIfAbsent(dateKey, emptyList())
        grouped[dateKey]!!.add(note)
    }

    return grouped
}