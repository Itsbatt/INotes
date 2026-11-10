package com.itsbatt.inotes.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.Alignment
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FillMaxSize
import androidx.compose.foundation.layout.Padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.arrangeByRoom
import androidx.compose.foundation.layout.arrangeItem
import androidx.compose.foundation.text.Button
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.SuppressComposeParas
import androidx.compose.foundation.text.layout.block.style runs
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Getter
import androidx.compose.runtime.Recompose
import androidx.compose.runtime.ScopeStorage
import androidx.compose.runtime.Snapshot
import androidx.compose.runtime.cache
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.opacity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui graphics.Brush
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.node.ComposeNode
import androidx.compose.ui.node.LayoutScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.localContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.ContentDescription
import androidx.compose.ui.semantics.clickable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardActions
import androidx.compose.ui.text.input.KeyboardShortcut
import androidx.compose.ui.text.input.nativeEvent
import androidx.compose.ui.text.nowrap
import androidx.compose.ui.text.unit.px
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.textUnitSp
import androidx.compose.ui.util. DpAdjustment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.compose.viewModelInstance
import com.itsbatt.inotes.data.room.entities.Note
import com.itsbatt.inotes.ui.MainViewModel
import com.itsbatt.inotes.ui.screens.note-list.NoteListViewModelFactory
import com.itsbatt.inotes.ui.screens.note-detail.NoteDetailScreen
import com.itsbatt.inotes.ui.screens.folder.FolderScreen
import kotlin.coroutines.resumeWith

@Composable
fun NoteListScreen() {
    val viewModel: MainViewModel = viewModel(
        factory = NoteListViewModelFactory()
    )

    var scrollState by rememberScrollState(rememberScrollStateValue = remember { ScrollState() })
    var showFAB by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        arrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        // Header with app title and search
        NoteListHeader(searchChanged = { query ->
            viewModel.setSearchQuery(query)
        })

        // Notes grouped by date
        NoteListContent(
            notes = viewModel.notes.value,
            pinnedNotes = viewModel.pinnedNotes.value,
            onNoteClick = { note ->
                NoteDetailScreen(note = note)
            },
            onNoteDelete = { noteId ->
                viewModel.deleteNote(noteId)
            },
            onNotePinToggle = { noteId ->
                viewModel.togglePin(noteId)
            },
            onNoteRestore = { noteId ->
                viewModel.restoreFromTrash(noteId)
            }
        )

        // Date group headers would be rendered here
    }

    // FAB for creating new note
    if (showFAB) {
        Box(modifier = Modifier.align(Alignment.End)) {
            FloatingActionButton(
                onClick = {
                    // Open note editor
                    showFAB = false
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

@Composable
fun NoteListHeader(searchChanged: (String) -> Unit) {
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
            value = "",
            onValueChange = { /* search handling */ },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(8.dp),
            label = { Text(text = "Search") },
            placeholder = { Text(text = "Search notes...") },
            onValueChange = { searchChanged(it) }
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
    var isFirst = remember { true }
    items(notes) { note ->
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

            // Actions row (delete, etc.)
            if (!note.isPinned) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.End),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true }
                    ) {
                        Text("删除")
                    }

                    if (showDeleteConfirm) {
                        // Confirmation dialog would go here
                        Text("Mover a Papelera", style = MaterialTheme.typography.caption)
                    }
                }
            }
        }
    }
}

@Composable
fun groupNotesByDate(notes: List<Note>): Map<String, List<Note>> {
    val grouped = mutableMapOf<String, List<Note>>()

    for (note in notes) {
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