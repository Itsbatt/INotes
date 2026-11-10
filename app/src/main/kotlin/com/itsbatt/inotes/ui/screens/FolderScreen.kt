package com.itsbatt.inotes.ui.screens

import androidx.compose.foundation.TextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Getter
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itsbatt.inotes.data.room.entities.Folder
import com.itsbatt.inotes.ui.MainViewModel

@Composable
fun FolderScreen(viewModel: MainViewModel) {
    var newFolderName by remember { mutableStateOf("") }
    var renameFolderName by remember { mutableStateOf("") }
    var folderToRename by remember { mutableStateOf<String?>(null) }
    var folderToDelete by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Header
        Text(
            text = "Carpetas",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        )

        // Folders list
        val folders = viewModel.folders.value
        if (folders.isEmpty()) {
            Text(
                text = "No hay carpetas",
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            items(folders) { folderName ->
                FolderRow(
                    folderName = folderName,
                    onRenameClick = {
                        folderToRename = folderName
                        renameFolderName = folderName
                    },
                    onDeleteClick = {
                        folderToDelete = folderName
                    },
                    onTap = {
                        viewModel.setSelectedFolder(folderName)
                    }
                )
            }
        }

        // FAB for creating new folder
        FloatingActionButton(
            onClick = {
                viewModel.addFolder(newFolderName)
                newFolderName = ""
            }
        ) {
            Icon(
                imageVector = androidx.compose.ui.vectors.family.FamilyVector.Companion.dpAdd,
                contentDescription = "Crear carpeta"
            )
        }
    }

    // Rename dialog
    if (folderToRename != null) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center, horizontalAlignment = Alignment.Center) {
                Text(
                    text = "Renombrar carpeta",
                    style = MaterialTheme.typography.h6
                )
                TextField(
                    value = renameFolderName,
                    onValueChange = { renameFolderName = it },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    label = { Text("Nombre de la carpeta") }
                )
                Row(
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                    horizontalArrangement = androidx.compose.layout.Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.renameFolder(
                                folderToRename!!,
                                renameFolderName
                            )
                            folderToRename = null
                        }
                    ) {
                        Text("Renombrar")
                    }
                    TextButton(
                        onClick = {
                            folderToRename = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }

    // Delete confirmation
    if (folderToDelete != null) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center, horizontalAlignment = Alignment.Center) {
                Text(
                    text = "Eliminar carpeta ${folderToDelete}",
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = "Las notas se moverán a 'Notas'",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                    horizontalArrangement = androidx.compose.layout.Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.removeFolder(folderToDelete!!)
                            folderToDelete = null
                        }
                    ) {
                        Text("Mover a Notas")
                    }
                    TextButton(
                        onClick = {
                            folderToDelete = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

@Composable
fun FolderRow(
    folderName: String,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onTap: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.7f),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = folderName,
                style = MaterialTheme.typography.body1,
                overflow = androidx.compose.ui.text.TextOverflow.Ellipsis,
                maxLines = 1
            )
        }

        Row(
            modifier = Modifier
                .wrapContentSize(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
        ) {
            OutlinedButton(
                onClick = onRenameClick,
                style = MaterialTheme.button.textButton
            ) {
                Text("Renombrar")
            }
            OutlinedButton(
                onClick = onDeleteClick,
                style = MaterialTheme.button.textButton
            ) {
                Text("Eliminar")
            }
        }
    }
}