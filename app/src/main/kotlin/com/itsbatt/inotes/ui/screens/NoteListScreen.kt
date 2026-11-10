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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
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
    val isGridView by viewModel.isGridView.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedNoteIds by viewModel.selectedNoteIds.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val allFolders by viewModel.allFolders.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var activeNoteForOptions by remember { mutableStateOf<Note?>(null) }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var showMoveToFolderDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (isSelectionMode) {
                // Bottom Bar in Selection Mode (Screenshot 2: Folder, Tag, Trash)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Move to folder button
                        IconButton(
                            onClick = { showMoveToFolderDialog = true },
                            enabled = selectedNoteIds.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DriveFileMove,
                                contentDescription = "Mover a carpeta",
                                tint = if (selectedNoteIds.isNotEmpty()) IOSYellow else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Tag button
                        IconButton(
                            onClick = {},
                            enabled = selectedNoteIds.isNotEmpty()
                        ) {
                            Text(
                                text = "#",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedNoteIds.isNotEmpty()) IOSYellow else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }

                        // Trash button
                        IconButton(
                            onClick = { viewModel.deleteSelectedNotes() },
                            enabled = selectedNoteIds.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar seleccionadas",
                                tint = if (selectedNoteIds.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            } else {
                // Standard Floating Bottom Bar (Screenshot 5 & 7: Search pill + Compose button)
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

                        // Floating circular New Note compose button
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
            if (isSelectionMode) {
                // Top bar in Selection Mode (Screenshot 2)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.selectAllNotes() },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp
                    ) {
                        Text(
                            text = "Seleccionar todo",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    // Yellow check button to exit selection mode
                    Surface(
                        modifier = Modifier
                            .size(34.dp)
                            .clickable { viewModel.clearSelection() },
                        shape = CircleShape,
                        color = IOSYellow
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Listo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            } else {
                // Standard Top bar: Back to Folders and More options '...' (Screenshot 5 & 8)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
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

                    // Options '...' menu button (Screenshot 5)
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Surface(
                                modifier = Modifier.size(30.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MoreHoriz,
                                        contentDescription = "Opciones",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isGridView) "Ver como lista" else "Ver como galería") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    viewModel.toggleGridView()
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Seleccionar notas") },
                                leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) },
                                onClick = {
                                    viewModel.setSelectionMode(true)
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sincronizar con Google") },
                                leadingIcon = { Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color(0xFF1A73E8)) },
                                onClick = {
                                    viewModel.syncWithGoogle()
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Big iOS Title & Note count (Screenshot 2, 5 & 8)
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)) {
                val headerTitle = if (isSelectionMode) {
                    "${selectedNoteIds.size} seleccionadas"
                } else if (selectedTag != null) {
                    selectedTag!!
                } else {
                    selectedFolder
                }

                Text(
                    text = headerTitle,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                val countText = "${notes.size} ${if (notes.size == 1) "nota" else "notas"}"
                Text(
                    text = countText,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Tag screen horizontal tags selector (Screenshot 8)
                if (selectedTag != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allTags) { tag ->
                            val isSelected = tag == selectedTag
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.selectTag(if (isSelected) null else tag)
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) IOSYellow else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mostrar notas que coincidan con la etiqueta seleccionada: $selectedTag.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Notes Content
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

                if (isGridView) {
                    // GALLERY / GRID VIEW (Screenshot 5)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (pinnedNotes.isNotEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                SectionHeader(title = "Fijadas")
                            }
                            items(pinnedNotes) { note ->
                                NoteGridItem(
                                    note = note,
                                    onClick = { onOpenNote(note.id) },
                                    isSelectionMode = isSelectionMode,
                                    isSelected = selectedNoteIds.contains(note.id),
                                    onSelectToggle = { viewModel.toggleSelectNote(note.id) }
                                )
                            }
                        }

                        dateSections.forEach { (sectionTitle, sectionNotes) ->
                            if (sectionNotes.isNotEmpty()) {
                                item(span = { GridItemSpan(2) }) {
                                    SectionHeader(title = sectionTitle)
                                }
                                items(sectionNotes) { note ->
                                    NoteGridItem(
                                        note = note,
                                        onClick = { onOpenNote(note.id) },
                                        isSelectionMode = isSelectionMode,
                                        isSelected = selectedNoteIds.contains(note.id),
                                        onSelectToggle = { viewModel.toggleSelectNote(note.id) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // LIST VIEW (Screenshot 2)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        if (pinnedNotes.isNotEmpty()) {
                            item {
                                SectionHeader(title = "Fijadas")
                            }
                            item {
                                GroupedCard {
                                    pinnedNotes.forEachIndexed { index, note ->
                                        Box(
                                            modifier = Modifier.combinedClickable(
                                                onClick = {
                                                    if (isSelectionMode) {
                                                        viewModel.toggleSelectNote(note.id)
                                                    } else {
                                                        onOpenNote(note.id)
                                                    }
                                                },
                                                onLongClick = {
                                                    if (!isSelectionMode) {
                                                        activeNoteForOptions = note
                                                    }
                                                }
                                            )
                                        ) {
                                            NoteItem(
                                                note = note,
                                                onClick = { onOpenNote(note.id) },
                                                showDivider = index < pinnedNotes.size - 1,
                                                isSelectionMode = isSelectionMode,
                                                isSelected = selectedNoteIds.contains(note.id),
                                                onSelectToggle = { viewModel.toggleSelectNote(note.id) }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }

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
                                                    onClick = {
                                                        if (isSelectionMode) {
                                                            viewModel.toggleSelectNote(note.id)
                                                        } else {
                                                            onOpenNote(note.id)
                                                        }
                                                    },
                                                    onLongClick = {
                                                        if (!isSelectionMode) {
                                                            activeNoteForOptions = note
                                                        }
                                                    }
                                                )
                                            ) {
                                                NoteItem(
                                                    note = note,
                                                    onClick = { onOpenNote(note.id) },
                                                    showDivider = index < sectionNotes.size - 1,
                                                    isSelectionMode = isSelectionMode,
                                                    isSelected = selectedNoteIds.contains(note.id),
                                                    onSelectToggle = { viewModel.toggleSelectNote(note.id) }
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
    }

    // Move to folder dialog
    if (showMoveToFolderDialog) {
        AlertDialog(
            onDismissRequest = { showMoveToFolderDialog = false },
            title = { Text("Mover notas a carpeta", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn {
                    items(allFolders) { folder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.moveSelectedNotesToFolder(folder.name)
                                    showMoveToFolderDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = IOSYellow)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(folder.name, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMoveToFolderDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
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
