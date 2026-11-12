package com.itsbatt.inotes.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsbatt.inotes.data.room.entities.Note
import com.itsbatt.inotes.domain.ChecklistUtils
import com.itsbatt.inotes.domain.DateGroupingUtils
import com.itsbatt.inotes.ui.AndroidUtils
import com.itsbatt.inotes.ui.IOSYellow
import com.itsbatt.inotes.ui.LiquidGlassPill
import com.itsbatt.inotes.ui.LocalLiquidGlassConfig
import com.itsbatt.inotes.ui.MainViewModel
import com.itsbatt.inotes.ui.NoteSortOrder
import com.itsbatt.inotes.ui.iosPressScale
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

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
    val sortOrder by viewModel.sortOrder.collectAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()
    val smartFolders by viewModel.allSmartFolders.collectAsState()
    val allFolders by viewModel.allFolders.collectAsState()

    val displayFolderTitle = remember(selectedFolder, smartFolders) {
        if (selectedFolder.startsWith("smart:")) {
            val id = selectedFolder.removePrefix("smart:").toLongOrNull()
            smartFolders.firstOrNull { it.id == id }?.name ?: "Carpeta inteligente"
        } else {
            selectedFolder
        }
    }

    val listState = rememberLazyListState()

    // Title compacts on scroll (Requirement 2)
    val isScrolled by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 20 }
    }

    var notePendingDelete by remember { mutableStateOf<Note?>(null) }
    var notePendingMove by remember { mutableStateOf<Note?>(null) }
    var activeNoteForOptions by remember { mutableStateOf<Note?>(null) }
    var showEmptyTrashDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var isGalleryView by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedNoteIds by remember { mutableStateOf(setOf<Long>()) }
    val backdrop = rememberLayerBackdrop()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .layerBackdrop(backdrop)
                .statusBarsPadding()
        ) {
            // Top spacer for floating back pill
            Spacer(modifier = Modifier.height(56.dp))

            // Big iOS Title (smoothly compacts when scrolled)
            AnimatedVisibility(
                visible = !isScrolled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)) {
                    Text(
                        text = displayFolderTitle,
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
                    if (selectedTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(selectedTags.toList()) { tag ->
                                Surface(
                                    shape = CircleShape,
                                    color = IOSYellow.copy(alpha = 0.22f),
                                    modifier = Modifier.clickable { viewModel.toggleTagFilter(tag) }
                                ) {
                                    Text(
                                        text = "$tag ×",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = IOSYellow,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
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

                if (isGalleryView) {
                    // iOS 26 Gallery View (2-column rounded cards)
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val rows = notes.chunked(2)
                        itemsIndexed(rows) { _, pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                pair.forEach { note ->
                                    val isSel = selectedNoteIds.contains(note.id)
                                    GalleryNoteCard(
                                        note = note,
                                        isSelectionMode = isSelectionMode,
                                        isSelected = isSel,
                                        onClick = {
                                            if (isSelectionMode) {
                                                selectedNoteIds = if (isSel) selectedNoteIds - note.id else selectedNoteIds + note.id
                                            } else {
                                                onOpenNote(note.id)
                                            }
                                        },
                                        onLongClick = { activeNoteForOptions = note },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(92.dp)) }
                    }
                } else {
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
                                        val isSel = selectedNoteIds.contains(note.id)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.combinedClickable(
                                                onClick = {
                                                    if (isSelectionMode) {
                                                        selectedNoteIds = if (isSel) selectedNoteIds - note.id else selectedNoteIds + note.id
                                                    } else {
                                                        onOpenNote(note.id)
                                                    }
                                                },
                                                onLongClick = { activeNoteForOptions = note }
                                            )
                                        ) {
                                            if (isSelectionMode) {
                                                SelectionCircle(
                                                    selected = isSel,
                                                    modifier = Modifier.padding(start = 14.dp)
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                NoteItem(
                                                    note = note,
                                                    onClick = {
                                                        if (isSelectionMode) {
                                                            selectedNoteIds = if (isSel) selectedNoteIds - note.id else selectedNoteIds + note.id
                                                        } else {
                                                            onOpenNote(note.id)
                                                        }
                                                    },
                                                    onSwipeDelete = { notePendingDelete = note },
                                                    onSwipePin = if (!isTrashMode) { { viewModel.togglePin(note) } } else null,
                                                    onSwipeMove = if (!isTrashMode) { { notePendingMove = note } } else null,
                                                    showDivider = index < pinnedNotes.size - 1
                                                )
                                            }
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
                                            val isSel = selectedNoteIds.contains(note.id)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.combinedClickable(
                                                    onClick = {
                                                        if (isSelectionMode) {
                                                            selectedNoteIds = if (isSel) selectedNoteIds - note.id else selectedNoteIds + note.id
                                                        } else {
                                                            onOpenNote(note.id)
                                                        }
                                                    },
                                                    onLongClick = { activeNoteForOptions = note }
                                                )
                                            ) {
                                                if (isSelectionMode) {
                                                    SelectionCircle(
                                                        selected = isSel,
                                                        modifier = Modifier.padding(start = 14.dp)
                                                    )
                                                }
                                                Box(modifier = Modifier.weight(1f)) {
                                                    NoteItem(
                                                        note = note,
                                                        onClick = {
                                                            if (isSelectionMode) {
                                                                selectedNoteIds = if (isSel) selectedNoteIds - note.id else selectedNoteIds + note.id
                                                            } else {
                                                                onOpenNote(note.id)
                                                            }
                                                        },
                                                        onSwipeDelete = { notePendingDelete = note },
                                                        onSwipePin = if (!isTrashMode) { { viewModel.togglePin(note) } } else null,
                                                        onSwipeMove = if (!isTrashMode) { { notePendingMove = note } } else null,
                                                        showDivider = index < sectionNotes.size - 1
                                                    )
                                                }
                                                if (isTrashMode && !isSelectionMode) {
                                                    Box(
                                                        modifier = Modifier
                                                            .padding(end = 10.dp)
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .clickable {
                                                                viewModel.restoreFromTrash(note.id)
                                                                android.widget.Toast
                                                                    .makeText(context, "Nota restaurada", android.widget.Toast.LENGTH_SHORT)
                                                                    .show()
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Restore,
                                                            contentDescription = "Restaurar nota",
                                                            tint = IOSYellow,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(92.dp))
                        }
                    }
                }
            }
        }

        // Floating Top Navigation row (iOS 26 Liquid Glass back pill)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LiquidGlassPill(
                backdrop = backdrop,
                modifier = Modifier.height(38.dp),
                shape = CircleShape,
                onClick = onNavigateToFolders
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Volver",
                        tint = IOSYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Carpetas",
                        color = IOSYellow,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            AnimatedVisibility(
                visible = isScrolled,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = displayFolderTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (isTrashMode && notes.isNotEmpty()) {
                if (isSelectionMode) {
                    LiquidGlassPill(
                        backdrop = backdrop,
                        modifier = Modifier.height(38.dp),
                        shape = CircleShape,
                        onClick = {
                            isSelectionMode = false
                            selectedNoteIds = emptySet()
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "OK",
                                color = IOSYellow,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LiquidGlassPill(
                            backdrop = backdrop,
                            modifier = Modifier.height(38.dp),
                            shape = CircleShape,
                            onClick = {
                                isSelectionMode = true
                                selectedNoteIds = emptySet()
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Seleccionar",
                                    color = IOSYellow,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        LiquidGlassPill(
                            backdrop = backdrop,
                            modifier = Modifier.height(38.dp),
                            shape = CircleShape,
                            onClick = { showEmptyTrashDialog = true }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Vaciar",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            } else if (!isTrashMode) {
                if (isSelectionMode) {
                    LiquidGlassPill(
                        backdrop = backdrop,
                        modifier = Modifier.height(38.dp),
                        shape = CircleShape,
                        onClick = {
                            isSelectionMode = false
                            selectedNoteIds = emptySet()
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "OK",
                                color = IOSYellow,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    LiquidGlassPill(
                        backdrop = backdrop,
                        modifier = Modifier.size(38.dp),
                        shape = CircleShape,
                        onClick = { showSortMenu = !showSortMenu }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Opciones",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        // Floating Bottom Bar (iOS 26 Liquid Glass)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LiquidGlassPill(
                backdrop = backdrop,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(IOSYellow),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Buscar",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 16.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { viewModel.setSearchQuery("") }
                        )
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

            if (isSelectionMode) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isTrashMode) {
                        LiquidGlassPill(
                            backdrop = backdrop,
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            onClick = {
                                if (selectedNoteIds.isNotEmpty()) {
                                    selectedNoteIds.forEach { viewModel.restoreFromTrash(it) }
                                    android.widget.Toast
                                        .makeText(context, "Notas restauradas", android.widget.Toast.LENGTH_SHORT)
                                        .show()
                                    selectedNoteIds = emptySet()
                                    isSelectionMode = false
                                }
                            }
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = "Restaurar seleccionadas",
                                    tint = if (selectedNoteIds.isNotEmpty()) IOSYellow else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                    LiquidGlassPill(
                        backdrop = backdrop,
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        onClick = {
                            if (selectedNoteIds.isNotEmpty()) {
                                if (isTrashMode) {
                                    selectedNoteIds.forEach { viewModel.deletePermanently(it) }
                                } else {
                                    selectedNoteIds.forEach { viewModel.moveToTrash(it) }
                                }
                                selectedNoteIds = emptySet()
                                isSelectionMode = false
                            }
                        }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = if (isTrashMode) "Eliminar definitivamente" else "Eliminar seleccionadas",
                                tint = if (selectedNoteIds.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            } else if (!isTrashMode) {
                LiquidGlassPill(
                    backdrop = backdrop,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    onClick = onCreateNote
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Nueva nota",
                            tint = IOSYellow,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else if (notes.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LiquidGlassPill(
                        backdrop = backdrop,
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        onClick = {
                            notes.forEach { viewModel.restoreFromTrash(it.id) }
                            android.widget.Toast
                                .makeText(context, "Papelera restaurada", android.widget.Toast.LENGTH_SHORT)
                                .show()
                        }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "Restaurar todo",
                                tint = IOSYellow,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    LiquidGlassPill(
                        backdrop = backdrop,
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        onClick = { showEmptyTrashDialog = true }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

        // iOS 26 Liquid Glass List Options Popover (Gallery/List, Select, Sort)
        IOS26ListOptionsPopover(
            visible = showSortMenu,
            backdrop = backdrop,
            isGalleryView = isGalleryView,
            currentSort = sortOrder,
            onDismiss = { showSortMenu = false },
            onToggleGallery = {
                isGalleryView = !isGalleryView
                showSortMenu = false
            },
            onStartSelection = {
                isSelectionMode = true
                selectedNoteIds = emptySet()
                showSortMenu = false
            },
            onSelectSort = { order ->
                viewModel.setSortOrder(order)
                showSortMenu = false
            }
        )
    }

    // iOS 26 Liquid Glass Confirmation Modal for Delete
    IOS26ConfirmGlassModal(
        visible = notePendingDelete != null,
        backdrop = backdrop,
        title = if (isTrashMode) "¿Eliminar definitivamente?" else "¿Mover a la papelera?",
        message = notePendingDelete?.let { note ->
            if (isTrashMode)
                "La nota '${note.title.ifBlank { "sin título" }}' se eliminará de forma permanente."
            else
                "La nota '${note.title.ifBlank { "sin título" }}' permanecerá en la papelera durante 30 días."
        } ?: "",
        confirmLabel = "Eliminar",
        onDismiss = { notePendingDelete = null },
        onConfirm = {
            notePendingDelete?.let { note ->
                if (isTrashMode) viewModel.deletePermanently(note.id) else viewModel.moveToTrash(note.id)
            }
            notePendingDelete = null
        }
    )

    // iOS 26 Liquid Glass Context Modal for long-pressed note
    IOS26NoteContextModal(
        note = activeNoteForOptions,
        backdrop = backdrop,
        isTrashMode = isTrashMode,
        onDismiss = { activeNoteForOptions = null },
        onTogglePin = { note ->
            viewModel.togglePin(note)
            activeNoteForOptions = null
        },
        onShare = { note ->
            AndroidUtils.shareText(context, "${note.title}\n\n${note.body}")
            activeNoteForOptions = null
        },
        onRestore = { note ->
            viewModel.restoreFromTrash(note.id)
            activeNoteForOptions = null
        },
        onDelete = { note ->
            notePendingDelete = note
            activeNoteForOptions = null
        }
    )

    // iOS 26 Liquid Glass Confirmation Modal for Empty Trash
    IOS26ConfirmGlassModal(
        visible = showEmptyTrashDialog,
        backdrop = backdrop,
        title = "¿Vaciar papelera?",
        message = "Se eliminarán todas las notas de la papelera definitivamente.",
        confirmLabel = "Vaciar",
        onDismiss = { showEmptyTrashDialog = false },
        onConfirm = {
            viewModel.emptyTrash()
            showEmptyTrashDialog = false
        }
    )

    // Quick Move to Folder dialog from left-swipe
    notePendingMove?.let { movingNote ->
        AlertDialog(
            onDismissRequest = { notePendingMove = null },
            title = { Text("Mover a carpeta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val folderNames = allFolders.map { it.name }.ifEmpty { listOf("Notas") }
                    folderNames.forEach { fname ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.moveNoteToFolder(movingNote.id, fname)
                                    android.widget.Toast
                                        .makeText(context, "Movida a $fname", android.widget.Toast.LENGTH_SHORT)
                                        .show()
                                    notePendingMove = null
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = IOSYellow, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(fname, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { notePendingMove = null }) {
                    Text("Cancelar", color = IOSYellow)
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
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

@Composable
private fun SelectionCircle(selected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (selected) IOSYellow else Color.Transparent)
            .border(
                1.8.dp,
                if (selected) IOSYellow else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun GalleryNoteCard(
    note: Note,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(160.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title.ifBlank { "Nueva nota" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelectionMode) {
                        SelectionCircle(selected = isSelected, modifier = Modifier.padding(start = 6.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                val context = LocalContext.current
                val isLocked = remember(note.id) {
                    context.getSharedPreferences("inotes_locks", android.content.Context.MODE_PRIVATE)
                        .getBoolean("lock_${note.id}", false)
                }
                val preview = if (isLocked) "🔒 Nota bloqueada" else ChecklistUtils.formatPreview(note.body)
                Text(
                    text = preview.ifBlank { "Sin texto adicional" },
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = DateGroupingUtils.formatNoteDate(note.updatedAt),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun IOS26ListOptionsPopover(
    visible: Boolean,
    backdrop: LayerBackdrop,
    isGalleryView: Boolean,
    currentSort: NoteSortOrder,
    onDismiss: () -> Unit,
    onToggleGallery: () -> Unit,
    onStartSelection: () -> Unit,
    onSelectSort: (NoteSortOrder) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val config = LocalLiquidGlassConfig.current

    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .statusBarsPadding()
                .padding(top = 56.dp, end = 16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(
                    initialScale = 0.82f,
                    transformOrigin = TransformOrigin(0.92f, 0f),
                    animationSpec = spring(0.65f, Spring.StiffnessMediumLow)
                ) + fadeIn(),
                exit = scaleOut(targetScale = 0.88f, transformOrigin = TransformOrigin(0.92f, 0f)) + fadeOut()
            ) {
                val shape = RoundedCornerShape(28.dp)
                val borderBrush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (isDark) 0.30f else 0.96f),
                        (if (isDark) Color.White else Color(0xFFC7C7CC)).copy(alpha = if (isDark) 0.08f else 0.40f)
                    )
                )
                val dividerColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)

                Column(
                    modifier = Modifier
                        .width(250.dp)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                        .shadow(22.dp, shape)
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { shape },
                            effects = {
                                if (config.enableVibrancy) vibrancy()
                                blur(20.dp.toPx())
                                if (config.enableLens) {
                                    lens(16.dp.toPx(), 26.dp.toPx(), chromaticAberration = config.chromaticAberration)
                                }
                            },
                            onDrawSurface = {
                                val bg = if (isDark) Color(0xFF262629) else Color(0xFFFDFDFE)
                                drawRect(bg.copy(alpha = if (isDark) 0.78f else 0.82f))
                            }
                        )
                        .border(BorderStroke(1.dp, borderBrush), shape)
                        .padding(vertical = 10.dp)
                ) {
                    // Top quick bar: Gallery/List + Select
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(onClick = onToggleGallery)
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (isGalleryView) Icons.Default.List else Icons.Default.GridView,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(21.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isGalleryView) "Lista" else "Galería",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(onClick = onStartSelection)
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(21.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Seleccionar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    Text(
                        text = "Ordenar por",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                    )

                    NoteSortOrder.entries.forEach { order ->
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val pillBg = if (isPressed) {
                            if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f)
                        } else Color.Transparent

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 2.dp)
                                .iosPressScale(interactionSource, pressedScale = 0.97f)
                                .clip(CircleShape)
                                .background(pillBg)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = { onSelectSort(order) }
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = order.label,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (currentSort == order) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = IOSYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IOS26ConfirmGlassModal(
    visible: Boolean,
    backdrop: LayerBackdrop,
    title: String,
    message: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val config = LocalLiquidGlassConfig.current
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.40f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(initialScale = 0.86f, animationSpec = spring(0.65f, Spring.StiffnessMediumLow)) + fadeIn(),
                exit = scaleOut(targetScale = 0.90f) + fadeOut()
            ) {
                val shape = RoundedCornerShape(30.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                        .shadow(22.dp, shape)
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { shape },
                            effects = {
                                if (config.enableVibrancy) vibrancy()
                                blur(18.dp.toPx())
                                if (config.enableLens) {
                                    lens(16.dp.toPx(), 26.dp.toPx(), chromaticAberration = config.chromaticAberration)
                                }
                            },
                            onDrawSurface = {
                                val bg = if (isDark) Color(0xFF262629) else Color.White
                                drawRect(bg.copy(alpha = if (isDark) 0.82f else 0.86f))
                            }
                        )
                        .border(1.dp, Color.White.copy(alpha = if (isDark) 0.28f else 0.92f), shape)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(message, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        LiquidGlassPill(backdrop = backdrop, modifier = Modifier.weight(1f).height(44.dp), onClick = onDismiss) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Cancelar", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        LiquidGlassPill(
                            backdrop = backdrop,
                            modifier = Modifier.weight(1f).height(44.dp),
                            tintOverride = Color(0xFFFF3B30),
                            onClick = onConfirm
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(confirmLabel, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IOS26NoteContextModal(
    note: Note?,
    backdrop: LayerBackdrop,
    isTrashMode: Boolean,
    onDismiss: () -> Unit,
    onTogglePin: (Note) -> Unit,
    onShare: (Note) -> Unit,
    onRestore: (Note) -> Unit,
    onDelete: (Note) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val config = LocalLiquidGlassConfig.current
    AnimatedVisibility(visible = note != null, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.40f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            note?.let { target ->
                val shape = RoundedCornerShape(30.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                        .shadow(22.dp, shape)
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { shape },
                            effects = {
                                if (config.enableVibrancy) vibrancy()
                                blur(18.dp.toPx())
                                if (config.enableLens) {
                                    lens(16.dp.toPx(), 26.dp.toPx(), chromaticAberration = config.chromaticAberration)
                                }
                            },
                            onDrawSurface = {
                                val bg = if (isDark) Color(0xFF262629) else Color.White
                                drawRect(bg.copy(alpha = if (isDark) 0.82f else 0.86f))
                            }
                        )
                        .border(1.dp, Color.White.copy(alpha = if (isDark) 0.28f else 0.92f), shape)
                        .padding(18.dp)
                ) {
                    Text(
                        text = target.title.ifBlank { "Nota" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!isTrashMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .clickable { onTogglePin(target) }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PushPin, contentDescription = null, tint = IOSYellow)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(if (target.isPinned) "Desfijar" else "Fijar arriba", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .clickable { onShare(target) }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = IOSYellow)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Compartir nota", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .clickable { onDelete(target) }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF3B30))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Mover a la papelera", fontSize = 16.sp, color = Color(0xFFFF3B30))
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .clickable { onRestore(target) }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, tint = IOSYellow)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Recuperar nota", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .clickable { onDelete(target) }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF3B30))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Eliminar definitivamente", fontSize = 16.sp, color = Color(0xFFFF3B30))
                        }
                    }
                }
            }
        }
    }
}
