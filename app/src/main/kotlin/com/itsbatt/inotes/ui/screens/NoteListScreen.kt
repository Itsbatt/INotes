package com.itsbatt.inotes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsbatt.inotes.data.room.entities.Note
import com.itsbatt.inotes.domain.DateGroupingUtils
import com.itsbatt.inotes.ui.AndroidUtils
import com.itsbatt.inotes.ui.IOSYellow
import com.itsbatt.inotes.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteListScreen(
    viewModel: MainViewModel,
    onNavigateToFolders: () -> Unit,
    onOpenNote: (Long) -> Unit,
    onCreateNote: () -> Unit
) {
    val context = LocalContext.current
    val notes by viewModel.displayedNotes.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isTrashMode by viewModel.isTrashMode.collectAsState()

    val listState = rememberLazyListState()

    // Title compacts on scroll (Requirement 2)
    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 20 }
    }

    var notePendingDelete by remember { mutableStateOf<Note?>(null) }
    var activeNoteForOptions by remember { mutableStateOf<Note?>(null) }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            // Floating bottom bar with Search pill and Compose button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Search Bar pill
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            TextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = {
                                    Text(
                                        text = "Buscar",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 16.sp
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Limpiar",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voz",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    if (!isTrashMode) {
                        Surface(
                            modifier = Modifier
                                .size(44.dp)
                                .clickable(onClick = onCreateNote),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Nueva nota",
                                    tint = IOSYellow,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    } else {
                        Surface(
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { showEmptyTrashDialog = true },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Vaciar papelera",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Top Navigation row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onNavigateToFolders)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Volver",
                        tint = IOSYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Carpetas",
                        color = IOSYellow,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                if (isScrolled) {
                    Text(
                        text = selectedFolder,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (isTrashMode && notes.isNotEmpty()) {
                    TextButton(onClick = { showEmptyTrashDialog = true }) {
                        Text("Vaciar", color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Big iOS Title (compacts when scrolled)
            if (!isScrolled) {
                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)) {
                    Text(
                        text = selectedFolder,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    val noteCountText = "${notes.size} ${if (notes.size == 1) "nota" else "notas"}"
                    Text(
                        text = noteCountText,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Content: Grouped by date (Hoy, Ayer, Esta semana, Mes y año)
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No hay resultados" else "No hay notas",
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val pinnedNotes = notes.filter { it.isPinned }
                val unpinnedNotes = notes.filter { !it.isPinned }
                val dateSections = DateGroupingUtils.groupNotesByDate(unpinnedNotes)

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    // Pinned notes section
                    if (pinnedNotes.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Fijadas")
                        }
                        item {
                            GroupedCard {
                                pinnedNotes.forEachIndexed { index, note ->
                                    Box(
                                        modifier = Modifier.combinedClickable(
                                            onClick = { onOpenNote(note.id) },
                                            onLongClick = { activeNoteForOptions = note }
                                        )
                                    ) {
                                        NoteItem(
                                            note = note,
                                            onClick = { onOpenNote(note.id) },
                                            onSwipeDelete = { notePendingDelete = note },
                                            showDivider = index < pinnedNotes.size - 1
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Date grouped sections: Hoy, Ayer, Esta semana, Mes y año
                    dateSections.forEach { (sectionTitle, sectionNotes) ->
                        if (sectionNotes.isNotEmpty()) {
                            item {
                                SectionHeader(title = sectionTitle)
                            }
                            item {
                                GroupedCard {
                                    sectionNotes.forEachIndexed { index, note ->
                                        Box(
                                            modifier = Modifier.combinedClickable(
                                                onClick = { onOpenNote(note.id) },
                                                onLongClick = { activeNoteForOptions = note }
                                            )
                                        ) {
                                            NoteItem(
                                                note = note,
                                                onClick = { onOpenNote(note.id) },
                                                onSwipeDelete = { notePendingDelete = note },
                                                showDivider = index < sectionNotes.size - 1
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for Swipe-to-delete (Requirement 7)
    notePendingDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { notePendingDelete = null },
            title = { Text(if (isTrashMode) "¿Eliminar nota definitivamente?" else "¿Mover a la papelera?") },
            text = {
                Text(
                    if (isTrashMode)
                        "La nota '${note.title.ifBlank { "sin título" }}' se eliminará de forma permanente."
                    else
                        "La nota '${note.title.ifBlank { "sin título" }}' permanecerá en la papelera durante 30 días antes del borrado definitivo."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isTrashMode) {
                            viewModel.deletePermanently(note.id)
                        } else {
                            viewModel.moveToTrash(note.id)
                        }
                        notePendingDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { notePendingDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Context options dialog for long-pressed note
    activeNoteForOptions?.let { note ->
        AlertDialog(
            onDismissRequest = { activeNoteForOptions = null },
            title = {
                Text(
                    text = if (note.title.isNotBlank()) note.title else "Nota",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    if (!isTrashMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.togglePin(note)
                                    activeNoteForOptions = null
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PushPin, contentDescription = null, tint = IOSYellow)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(if (note.isPinned) "Desfijar" else "Fijar nota arriba")
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val shareText = "${note.title}\n\n${note.body}"
                                    AndroidUtils.shareText(context, shareText)
                                    activeNoteForOptions = null
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = IOSYellow)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Compartir nota")
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    notePendingDelete = note
                                    activeNoteForOptions = null
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Mover a la papelera", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.restoreFromTrash(note.id)
                                    activeNoteForOptions = null
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, tint = IOSYellow)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Recuperar nota")
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    notePendingDelete = note
                                    activeNoteForOptions = null
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Eliminar definitivamente", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { activeNoteForOptions = null }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showEmptyTrashDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyTrashDialog = false },
            title = { Text("¿Vaciar papelera?") },
            text = { Text("Se eliminarán todas las notas de la papelera definitivamente.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.emptyTrash()
                        showEmptyTrashDialog = false
                    }
                ) {
                    Text("Vaciar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyTrashDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp)
    )
}

@Composable
fun GroupedCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}
