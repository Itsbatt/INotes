package com.itsbatt.inotes.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsbatt.inotes.data.room.entities.Folder
import com.itsbatt.inotes.ui.IOSYellow
import com.itsbatt.inotes.ui.LiquidGlassPill
import com.itsbatt.inotes.ui.LocalLiquidGlassConfig
import com.itsbatt.inotes.ui.MainViewModel
import com.itsbatt.inotes.ui.iosPressScale
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderScreen(
    viewModel: MainViewModel,
    onFolderSelected: (String) -> Unit,
    onTrashSelected: () -> Unit,
    onCreateNote: () -> Unit,
    onSearchClicked: () -> Unit,
    onOpenGlassLab: () -> Unit = {}
) {
    val folders by viewModel.allFolders.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val deletedNotes by viewModel.deletedNotes.collectAsState()

    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    var folderToRename by remember { mutableStateOf<Folder?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var folderToDelete by remember { mutableStateOf<Folder?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    var isDeviceSectionExpanded by remember { mutableStateOf(true) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (isDeviceSectionExpanded) 0f else -90f,
        label = "chevronRotation"
    )
    val backdrop = rememberLayerBackdrop()

    val detectedTags = remember(allNotes) {
        val tagRegex = Regex("#[\\p{L}\\w_]+")
        allNotes.flatMap { note ->
            tagRegex.findAll("${note.title} ${note.body}").map { it.value }
        }.distinct()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .layerBackdrop(backdrop)
                .statusBarsPadding()
        ) {
            // Top spacer for floating action pills
            Spacer(modifier = Modifier.height(56.dp))

            // Big iOS Title: "Carpetas"
            Text(
                text = "Carpetas",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 14.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // 1. Quick Access Card (Notas rápidas & Compartidas)
                item {
                    GroupedCard {
                        QuickAccessRow(
                            icon = Icons.Default.NoteAlt,
                            iconBackground = IOSYellow,
                            title = "Notas rápidas",
                            count = allNotes.take(4).size,
                            onClick = { onFolderSelected("Notas rápidas") },
                            showDivider = true
                        )

                        QuickAccessRow(
                            icon = Icons.Default.People,
                            iconBackground = Color(0xFF34AADC),
                            title = "Compartidas",
                            count = 0,
                            onClick = {},
                            showDivider = false
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 2. Section Header: "En mi dispositivo"
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isDeviceSectionExpanded = !isDeviceSectionExpanded }
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "En mi dispositivo",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(chevronRotation)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Folders Card
                item {
                    AnimatedVisibility(visible = isDeviceSectionExpanded) {
                        GroupedCard {
                            FolderItemRow(
                                name = "Todas las notas",
                                count = allNotes.size,
                                icon = Icons.Outlined.Folder,
                                iconTint = IOSYellow,
                                onClick = { if (!isEditMode) onFolderSelected("Todas las notas") },
                                showDivider = folders.isNotEmpty()
                            )

                            folders.forEachIndexed { index, folder ->
                                val count = allNotes.count { it.folder.equals(folder.name, ignoreCase = true) }
                                val isEditableFolder = !folder.name.equals("Notas", ignoreCase = true)
                                Box(
                                    modifier = Modifier.combinedClickable(
                                        onClick = {
                                            if (isEditMode && isEditableFolder) {
                                                folderToRename = folder
                                                renameInput = folder.name
                                            } else {
                                                onFolderSelected(folder.name)
                                            }
                                        },
                                        onLongClick = {
                                            if (isEditableFolder) {
                                                folderToRename = folder
                                                renameInput = folder.name
                                            }
                                        }
                                    )
                                ) {
                                    FolderItemRow(
                                        name = folder.name,
                                        count = count,
                                        icon = Icons.Outlined.Folder,
                                        iconTint = IOSYellow,
                                        onClick = {
                                            if (isEditMode && isEditableFolder) {
                                                folderToRename = folder
                                                renameInput = folder.name
                                            } else {
                                                onFolderSelected(folder.name)
                                            }
                                        },
                                        showDivider = index < folders.size - 1,
                                        isEditMode = isEditMode && isEditableFolder,
                                        onRenameClick = {
                                            folderToRename = folder
                                            renameInput = folder.name
                                        },
                                        onDeleteClick = {
                                            folderToDelete = folder
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(22.dp))
                }

                // 3. Section: "Etiquetas" (Tags) - only if notes contain hashtags
                if (detectedTags.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Etiquetas",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(detectedTags) { tag ->
                                val tagInteraction = remember { MutableInteractionSource() }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier
                                        .iosPressScale(tagInteraction)
                                        .clickable(
                                            interactionSource = tagInteraction,
                                            indication = null,
                                            onClick = {
                                                viewModel.setSearchQuery(tag)
                                                onFolderSelected("Todas las notas")
                                            }
                                        )
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(22.dp))
                    }
                }

                // 4. Smart Folders ("Carpetas inteligentes")
                item {
                    val startOfDay = remember {
                        java.util.Calendar.getInstance().apply {
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }.timeInMillis
                    }
                    val tasksCount = allNotes.count { it.body.contains("- [ ] ") || it.body.contains("- [x] ") }
                    val mediaCount = allNotes.count { it.body.contains("[drawing:") || it.body.contains("[image:") }
                    val todayCount = allNotes.count { it.updatedAt >= startOfDay }

                    SectionHeader(title = "Carpetas inteligentes")
                    GroupedCard {
                        FolderItemRow(
                            name = "Con tareas",
                            count = tasksCount,
                            icon = Icons.Default.CheckCircle,
                            iconTint = Color(0xFF34C759),
                            onClick = { onFolderSelected("Con tareas") },
                            showDivider = true
                        )
                        FolderItemRow(
                            name = "Con dibujos y fotos",
                            count = mediaCount,
                            icon = Icons.Default.Image,
                            iconTint = Color(0xFF5856D6),
                            onClick = { onFolderSelected("Con dibujos y fotos") },
                            showDivider = true
                        )
                        FolderItemRow(
                            name = "De hoy",
                            count = todayCount,
                            icon = Icons.Default.Today,
                            iconTint = Color(0xFF007AFF),
                            onClick = { onFolderSelected("De hoy") },
                            showDivider = false
                        )
                    }
                    Spacer(modifier = Modifier.height(22.dp))
                }

                // 5. Section: "Eliminado recientemente" (Papelera)
                item {
                    SectionHeader(title = "Eliminado recientemente")
                    GroupedCard {
                        FolderItemRow(
                            name = "Papelera",
                            count = deletedNotes.size,
                            icon = Icons.Default.Delete,
                            iconTint = MaterialTheme.colorScheme.error,
                            onClick = onTrashSelected,
                            showDivider = false
                        )
                    }
                    Spacer(modifier = Modifier.height(92.dp))
                }
            }
        }

        // Floating Top Action Pills (iOS 26 Liquid Glass)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiquidGlassPill(
                backdrop = backdrop,
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                onClick = onOpenGlassLab
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Liquid Glass Lab",
                        tint = IOSYellow,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            LiquidGlassPill(
                backdrop = backdrop,
                modifier = Modifier.size(38.dp),
                shape = CircleShape,
                onClick = { showNewFolderDialog = true }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = "Nueva carpeta",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            LiquidGlassPill(
                backdrop = backdrop,
                modifier = Modifier.height(38.dp),
                shape = CircleShape,
                onClick = { isEditMode = !isEditMode }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEditMode) "OK" else "Editar",
                        fontSize = 15.sp,
                        fontWeight = if (isEditMode) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isEditMode) IOSYellow else MaterialTheme.colorScheme.onSurface
                    )
                }
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
                    .height(48.dp),
                shape = CircleShape,
                onClick = onSearchClicked
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
                    Text(
                        text = "Buscar",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voz",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

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
        }
    }

    // iOS 26 Liquid Glass Modal: Create Folder
    LiquidGlassModal(
        visible = showNewFolderDialog,
        backdrop = backdrop,
        title = "Nueva carpeta",
        onDismiss = {
            showNewFolderDialog = false
            newFolderName = ""
        },
        onConfirm = {
            if (newFolderName.isNotBlank()) {
                viewModel.createFolder(newFolderName)
            }
            showNewFolderDialog = false
            newFolderName = ""
        },
        confirmEnabled = newFolderName.isNotBlank()
    ) {
        IOSGlassTextField(
            value = newFolderName,
            onValueChange = { newFolderName = it },
            placeholder = "Nombre"
        )
    }

    // iOS 26 Liquid Glass Modal: Rename / Delete Folder
    LiquidGlassModal(
        visible = folderToRename != null,
        backdrop = backdrop,
        title = "Editar carpeta",
        onDismiss = { folderToRename = null },
        onConfirm = {
            folderToRename?.let { folder ->
                if (renameInput.isNotBlank()) {
                    viewModel.renameFolder(folder.id, renameInput)
                }
            }
            folderToRename = null
        },
        confirmEnabled = renameInput.isNotBlank()
    ) {
        IOSGlassTextField(
            value = renameInput,
            onValueChange = { renameInput = it },
            placeholder = "Nuevo nombre"
        )
        Spacer(modifier = Modifier.height(14.dp))
        LiquidGlassPill(
            backdrop = backdrop,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = CircleShape,
            onClick = {
                folderToDelete = folderToRename
                folderToRename = null
            }
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Eliminar carpeta",
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }

    // iOS 26 Liquid Glass Modal: Delete Confirmation
    LiquidGlassModal(
        visible = folderToDelete != null,
        backdrop = backdrop,
        title = "¿Eliminar carpeta?",
        confirmTint = MaterialTheme.colorScheme.error,
        onDismiss = { folderToDelete = null },
        onConfirm = {
            folderToDelete?.let { viewModel.deleteFolder(it) }
            folderToDelete = null
        }
    ) {
        Text(
            text = "Al eliminar '${folderToDelete?.name ?: ""}', sus notas se moverán automáticamente a 'Notas'.",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
private fun LiquidGlassModal(
    visible: Boolean,
    backdrop: LayerBackdrop,
    title: String,
    confirmTint: Color = IOSYellow,
    confirmEnabled: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val config = LocalLiquidGlassConfig.current

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.34f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .imePadding()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(
                    initialScale = 0.86f,
                    animationSpec = spring(
                        dampingRatio = 0.65f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(),
                exit = scaleOut(targetScale = 0.90f) + fadeOut()
            ) {
                val shape = RoundedCornerShape(30.dp)
                val borderBrush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (isDark) 0.30f else 0.96f),
                        (if (isDark) Color.White else Color(0xFFC7C7CC)).copy(alpha = if (isDark) 0.08f else 0.40f)
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .shadow(20.dp, shape)
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { shape },
                            effects = {
                                if (config.enableVibrancy) vibrancy()
                                blur(18.dp.toPx())
                                if (config.enableLens) {
                                    lens(
                                        refractionHeight = 16.dp.toPx(),
                                        refractionAmount = 26.dp.toPx(),
                                        chromaticAberration = config.chromaticAberration
                                    )
                                }
                            },
                            onDrawSurface = {
                                val bg = if (isDark) Color(0xFF2C2C2E) else Color.White
                                drawRect(bg.copy(alpha = if (isDark) 0.72f else 0.78f))
                            }
                        )
                        .border(BorderStroke(1.dp, borderBrush), shape)
                        .padding(horizontal = 18.dp, vertical = 16.dp)
                ) {
                    // iOS Top Bar: Circular Cancel 'X', Bold Title, Circular Confirm 'Check'
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LiquidGlassPill(
                            backdrop = backdrop,
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            onClick = onDismiss
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancelar",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        LiquidGlassPill(
                            backdrop = backdrop,
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            tintOverride = if (confirmEnabled) confirmTint else null,
                            onClick = { if (confirmEnabled) onConfirm() }
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Guardar",
                                    tint = if (confirmEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    content()

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun IOSGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
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
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )
        if (value.isNotEmpty()) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Limpiar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onValueChange("") }
            )
        }
    }
}

@Composable
fun QuickAccessRow(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    count: Int,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .iosPressScale(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape,
                    color = iconBackground
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$count",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 60.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
fun FolderItemRow(
    name: String,
    count: Int,
    icon: ImageVector = Icons.Outlined.Folder,
    iconTint: Color = IOSYellow,
    onClick: () -> Unit,
    showDivider: Boolean = false,
    isEditMode: Boolean = false,
    onRenameClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .iosPressScale(interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (isEditMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onRenameClick != null) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Renombrar",
                            tint = IOSYellow,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable(onClick = onRenameClick)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                    }
                    if (onDeleteClick != null) {
                        Icon(
                            imageVector = Icons.Default.RemoveCircle,
                            contentDescription = "Eliminar carpeta",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(22.dp)
                                .clickable(onClick = onDeleteClick)
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$count",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 54.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
