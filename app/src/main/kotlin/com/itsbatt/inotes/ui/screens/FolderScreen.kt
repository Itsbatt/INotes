package com.itsbatt.inotes.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsbatt.inotes.data.room.entities.Folder
import com.itsbatt.inotes.sync.SyncStatus
import com.itsbatt.inotes.ui.IOSYellow
import com.itsbatt.inotes.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderScreen(
    viewModel: MainViewModel,
    onFolderSelected: (String) -> Unit,
    onTrashSelected: () -> Unit,
    onTagSelected: (String) -> Unit,
    onCreateNote: () -> Unit,
    onSearchClicked: () -> Unit
) {
    val folders by viewModel.allFolders.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val deletedNotes by viewModel.deletedNotes.collectAsState()
    val googleAccount by viewModel.googleAccount.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val allTags by viewModel.allTags.collectAsState()

    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    var folderToRename by remember { mutableStateOf<Folder?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var folderToDelete by remember { mutableStateOf<Folder?>(null) }

    var showGoogleSyncDialog by remember { mutableStateOf(false) }
    var manualGoogleEmail by remember { mutableStateOf("") }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.googleSyncManager.handleSignInResult(
                data = result.data,
                onSuccess = { viewModel.syncWithGoogle() },
                onError = {}
            )
        }
    }

    Scaffold(
        bottomBar = {
            // iOS Style Floating Bottom Bar with Search pill and Compose button (Screenshot 7)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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
                            .height(44.dp)
                            .clickable(onClick = onSearchClicked),
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Top action buttons row: New Folder & Editar (Screenshot 7)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // New Folder pill button
                Surface(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .clickable { showNewFolderDialog = true },
                    shape = RoundedCornerShape(17.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "Nueva carpeta",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Editar pill button
                Surface(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .clickable { showNewFolderDialog = true },
                    shape = RoundedCornerShape(17.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Editar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

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
                // 1. Quick Access Card (Screenshot 7: Notas rápidas & Google Sync)
                item {
                    GroupedCard {
                        // "Notas rápidas" with yellow icon
                        QuickAccessRow(
                            icon = Icons.Default.NoteAlt,
                            iconBackground = IOSYellow,
                            title = "Notas rápidas",
                            count = allNotes.take(4).size,
                            onClick = { onFolderSelected("Notas rápidas") },
                            showDivider = true
                        )

                        // Google Sync item with Google blue icon (replaces Compartidas / iCloud)
                        QuickAccessRow(
                            icon = Icons.Default.CloudSync,
                            iconBackground = Color(0xFF1A73E8),
                            title = if (googleAccount != null) "Google: ${googleAccount?.displayName}" else "Sincronización con Google",
                            count = if (googleAccount != null) allNotes.size else 0,
                            onClick = { showGoogleSyncDialog = true },
                            showDivider = false
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 2. Section Header: "Google" (replaces iCloud!) with dropdown chevron (Screenshot 7)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showGoogleSyncDialog = true }
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Google",
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
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Google Folders Card (Screenshot 7)
                item {
                    GroupedCard {
                        // "Todo lo de Google" (shows total note count)
                        FolderItemRow(
                            name = "Todo lo de Google",
                            count = allNotes.size,
                            icon = Icons.Outlined.Folder,
                            iconTint = IOSYellow,
                            onClick = { onFolderSelected("Todo lo de Google") },
                            showDivider = folders.isNotEmpty()
                        )

                        // Folders list
                        folders.forEachIndexed { index, folder ->
                            val count = allNotes.count { it.folder.equals(folder.name, ignoreCase = true) }
                            Box(
                                modifier = Modifier.combinedClickable(
                                    onClick = { onFolderSelected(folder.name) },
                                    onLongClick = {
                                        folderToRename = folder
                                        renameInput = folder.name
                                    }
                                )
                            ) {
                                FolderItemRow(
                                    name = folder.name,
                                    count = count,
                                    icon = Icons.Outlined.Folder,
                                    iconTint = IOSYellow,
                                    onClick = { onFolderSelected(folder.name) },
                                    showDivider = index < folders.size - 1
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(22.dp))
                }

                // 3. Section: "Etiquetas" (Tags) with chips (Screenshot 7 & 8)
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
                        items(allTags) { tag ->
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onTagSelected(tag) },
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(22.dp))
                }

                // 4. Section: "Eliminado recientemente" (Papelera)
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
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }

    // Google Sync / Account Dialog
    if (showGoogleSyncDialog) {
        AlertDialog(
            onDismissRequest = { showGoogleSyncDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = Color(0xFF1A73E8),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sincronización con Google",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                }
            },
            text = {
                Column {
                    if (googleAccount != null) {
                        Text(
                            text = "Conectado como:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = googleAccount?.displayName ?: "Usuario",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = googleAccount?.email ?: "",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val lastSyncStr = if (lastSyncTime > 0) {
                            SimpleDateFormat("d MMMM, HH:mm", Locale("es")).format(Date(lastSyncTime))
                        } else {
                            "Nunca"
                        }
                        Text(
                            text = "Última sincronización: $lastSyncStr",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        when (val status = syncStatus) {
                            is SyncStatus.Syncing -> {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sincronizando...", fontSize = 13.sp)
                                }
                            }
                            is SyncStatus.Success -> {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(status.message, fontSize = 13.sp, color = Color(0xFF34A853))
                            }
                            is SyncStatus.Error -> {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(status.error, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                            }
                            else -> {}
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.syncWithGoogle() },
                            colors = ButtonDefaults.buttonColors(containerColor = IOSYellow),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sincronizar ahora", color = Color.Black, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { viewModel.restoreFromGoogle() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Restaurar copia de Google")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                viewModel.googleSyncManager.signOut {
                                    showGoogleSyncDialog = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Desconectar cuenta", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        Text(
                            text = "Conecta tu cuenta de Google para guardar tus notas, carpetas y etiquetas en la nube automáticamente.",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                googleSignInLauncher.launch(viewModel.googleSyncManager.getSignInIntent())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Iniciar sesión con Google", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "O introduce tu correo para simular sync:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = manualGoogleEmail,
                            onValueChange = { manualGoogleEmail = it },
                            placeholder = { Text("usuario@gmail.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                if (manualGoogleEmail.isNotBlank()) {
                                    viewModel.googleSyncManager.connectManually(manualGoogleEmail, "Usuario Google")
                                    viewModel.syncWithGoogle()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Conectar correo de Google")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGoogleSyncDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Dialog Create Folder
    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = {
                showNewFolderDialog = false
                newFolderName = ""
            },
            title = { Text("Nueva carpeta", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    placeholder = { Text("Nombre de la carpeta") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            viewModel.createFolder(newFolderName)
                        }
                        showNewFolderDialog = false
                        newFolderName = ""
                    }
                ) {
                    Text("Guardar", color = IOSYellow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNewFolderDialog = false
                        newFolderName = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog Rename / Delete Folder
    folderToRename?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderToRename = null },
            title = { Text("Opciones de carpeta", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        label = { Text("Nuevo nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                folderToDelete = folder
                                folderToRename = null
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar carpeta", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            viewModel.renameFolder(folder.id, renameInput)
                        }
                        folderToRename = null
                    }
                ) {
                    Text("Renombrar", color = IOSYellow, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToRename = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog Delete Confirmation
    folderToDelete?.let { folder ->
        AlertDialog(
            onDismissRequest = { folderToDelete = null },
            title = { Text("¿Eliminar carpeta?") },
            text = { Text("¿Estás seguro de que quieres eliminar la carpeta '${folder.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFolder(folder)
                        folderToDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { folderToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                    modifier = Modifier.size(30.dp),
                    shape = RoundedCornerShape(8.dp),
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
    showDivider: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                modifier = Modifier.padding(start = 54.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
