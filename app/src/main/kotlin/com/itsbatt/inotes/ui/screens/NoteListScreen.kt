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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.itsbatt.inotes.ui.AndroidUtils
import com.itsbatt.inotes.ui.IOSYellow
import com.itsbatt.inotes.ui.MainViewModel
import java.util.Calendar

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

    var activeNoteForOptions by remember { mutableStateOf<Note?>(null) }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateToFolders) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Carpetas",
                            tint = IOSYellow,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    val noteCountText = "${notes.size} ${if (notes.size == 1) "nota" else "notas"}"
                    Text(
                        text = noteCountText,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!isTrashMode) {
                        IconButton(onClick = onCreateNote) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Nueva nota",
                                tint = IOSYellow,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    } else {
                        IconButton(onClick = { showEmptyTrashDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Vaciar papelera",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(26.dp)
                            )
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

                if (isTrashMode && notes.isNotEmpty()) {
                    TextButton(onClick = { showEmptyTrashDialog = true }) {
                        Text("Vaciar", color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
                    }
                }
            }

            // Big iOS Title
            Text(
                text = selectedFolder,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
            )

            // iOS-like Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Buscar",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notes grouped by section
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
                val dateSections = groupNotesByDateCategory(unpinnedNotes)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
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
                                            showDivider = index < pinnedNotes.size - 1
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Date grouped sections
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

    // Context options dialog/sheet for selected note
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
                            Text(if (note.isPinned) "Desfijar" else "Fijar nota")
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
                                    viewModel.moveToTrash(note.id)
                                    activeNoteForOptions = null
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
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
                            Text("Restaurar nota")
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.deletePermanently(note.id)
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
            text = { Text("Se eliminarán todas las notas de la papelera permanentemente.") },
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
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
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

fun groupNotesByDateCategory(notes: List<Note>): List<Pair<String, List<Note>>> {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val todayStart = cal.timeInMillis

    cal.add(Calendar.DAY_OF_YEAR, -1)
    val yesterdayStart = cal.timeInMillis

    cal.add(Calendar.DAY_OF_YEAR, -6)
    val last7DaysStart = cal.timeInMillis

    cal.timeInMillis = todayStart
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val thisMonthStart = cal.timeInMillis

    val hoy = mutableListOf<Note>()
    val ayer = mutableListOf<Note>()
    val ultimos7 = mutableListOf<Note>()
    val esteMes = mutableListOf<Note>()
    val anteriores = mutableListOf<Note>()

    for (note in notes) {
        when {
            note.updatedAt >= todayStart -> hoy.add(note)
            note.updatedAt >= yesterdayStart -> ayer.add(note)
            note.updatedAt >= last7DaysStart -> ultimos7.add(note)
            note.updatedAt >= thisMonthStart -> esteMes.add(note)
            else -> anteriores.add(note)
        }
    }

    val result = mutableListOf<Pair<String, List<Note>>>()
    if (hoy.isNotEmpty()) result.add("Hoy" to hoy)
    if (ayer.isNotEmpty()) result.add("Ayer" to ayer)
    if (ultimos7.isNotEmpty()) result.add("Últimos 7 días" to ultimos7)
    if (esteMes.isNotEmpty()) result.add("Este mes" to esteMes)
    if (anteriores.isNotEmpty()) result.add("Anteriores" to anteriores)

    return result
}
