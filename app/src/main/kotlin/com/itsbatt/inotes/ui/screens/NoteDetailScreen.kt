package com.itsbatt.inotes.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsbatt.inotes.ui.AndroidUtils
import com.itsbatt.inotes.ui.IOSYellow
import com.itsbatt.inotes.ui.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Long,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onCreateNewNote: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val allNotes by viewModel.allNotes.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()

    val existingNote = remember(allNotes, noteId) {
        allNotes.firstOrNull { it.id == noteId }
    }

    var currentNoteId by remember { mutableStateOf(noteId) }
    var title by remember(existingNote) { mutableStateOf(existingNote?.title ?: "") }
    var body by remember(existingNote) { mutableStateOf(existingNote?.body ?: "") }
    var isPinned by remember(existingNote) { mutableStateOf(existingNote?.isPinned ?: false) }
    var isChecklistMode by remember(existingNote) { mutableStateOf(existingNote?.isChecklist ?: false) }
    val createdAt = remember(existingNote) { existingNote?.createdAt ?: System.currentTimeMillis() }

    // History for Undo/Redo
    val undoStack = remember { mutableStateListOf<String>() }
    val redoStack = remember { mutableStateListOf<String>() }

    var showOptionsMenu by remember { mutableStateOf(false) }

    fun persistChanges() {
        if (title.isNotBlank() || body.isNotBlank()) {
            val targetFolder = existingNote?.folder ?: if (selectedFolder == "Todo lo de Google" || selectedFolder == "Todas las notas" || selectedFolder == "Papelera") "Notas" else selectedFolder
            viewModel.saveNote(currentNoteId, title, body, targetFolder, isChecklist = isChecklistMode) { newId ->
                currentNoteId = newId
            }
        }
    }

    // Debounced autosave
    LaunchedEffect(title, body, isChecklistMode) {
        if (title.isNotEmpty() || body.isNotEmpty()) {
            delay(1000)
            persistChanges()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            persistChanges()
        }
    }

    fun updateBodyWithUndo(newBody: String) {
        undoStack.add(body)
        redoStack.clear()
        body = newBody
    }

    fun toggleChecklistItem(lineIndex: Int) {
        val lines = body.split("\n").toMutableList()
        if (lineIndex in lines.indices) {
            val line = lines[lineIndex]
            lines[lineIndex] = when {
                line.startsWith("[ ] ") -> line.replaceFirst("[ ] ", "[x] ")
                line.startsWith("[x] ") -> line.replaceFirst("[x] ", "[ ] ")
                line.startsWith("○ ") -> line.replaceFirst("○ ", "✓ ")
                line.startsWith("✓ ") -> line.replaceFirst("✓ ", "○ ")
                else -> "[x] $line"
            }
            updateBodyWithUndo(lines.joinToString("\n"))
        }
    }

    Scaffold(
        topBar = {
            // iOS Notes Top Bar (Screenshot 1, 3, 4)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button with folder title in yellow (Screenshot 4)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                persistChanges()
                                onBack()
                            }
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
                        val backLabel = if (selectedFolder.isNotBlank()) selectedFolder else "Todo lo de Google"
                        Text(
                            text = backLabel,
                            color = IOSYellow,
                            fontSize = 17.sp,
                            maxLines = 1
                        )
                    }

                    // Top Right Actions (Screenshot 1 & 4)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Undo
                        IconButton(
                            onClick = {
                                if (undoStack.isNotEmpty()) {
                                    val previous = undoStack.removeAt(undoStack.lastIndex)
                                    redoStack.add(body)
                                    body = previous
                                }
                            },
                            enabled = undoStack.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Deshacer",
                                tint = if (undoStack.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Share
                        IconButton(
                            onClick = {
                                val shareContent = "$title\n\n$body"
                                AndroidUtils.shareText(context, shareContent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // More options '...' (Screenshot 3)
                        Box {
                            IconButton(onClick = { showOptionsMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = "Opciones",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showOptionsMenu,
                                onDismissRequest = { showOptionsMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Copiar como Markdown") },
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                    onClick = {
                                        val md = "# $title\n\n$body"
                                        clipboardManager.setText(AnnotatedString(md))
                                        Toast.makeText(context, "Copiado como Markdown", Toast.LENGTH_SHORT).show()
                                        showOptionsMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isPinned) "Desfijar" else "Fijar nota") },
                                    leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null, tint = IOSYellow) },
                                    onClick = {
                                        isPinned = !isPinned
                                        existingNote?.let { viewModel.togglePin(it) }
                                        showOptionsMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Insertar línea divisoria") },
                                    leadingIcon = { Icon(Icons.Default.HorizontalRule, contentDescription = null) },
                                    onClick = {
                                        updateBodyWithUndo(body + "\n---\n")
                                        showOptionsMenu = false
                                    }
                                )
                                if (currentNoteId != 0L) {
                                    DropdownMenuItem(
                                        text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            viewModel.moveToTrash(currentNoteId)
                                            showOptionsMenu = false
                                            onBack()
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Yellow Circular Checkmark Done button (Screenshot 1 & 4)
                        Surface(
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    focusManager.clearFocus()
                                    persistChanges()
                                },
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
                }
            }
        },
        bottomBar = {
            // iOS Notes Bottom Accessory Bar (Screenshot 1 & 4)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checklist toggle
                    IconButton(
                        onClick = {
                            isChecklistMode = !isChecklistMode
                            if (isChecklistMode && !body.contains("[ ]") && !body.contains("[x]")) {
                                val lines = body.split("\n").map { line ->
                                    if (line.isBlank()) line else "[ ] $line"
                                }
                                updateBodyWithUndo(lines.joinToString("\n"))
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "Lista de tareas",
                            tint = if (isChecklistMode) IOSYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Format Text Aa
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.FormatColorText,
                            contentDescription = "Formato de texto",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Attachment / Camera icon (Screenshot 1 & 4)
                    IconButton(
                        onClick = {
                            updateBodyWithUndo(body + "\n[Archivo adjunto]")
                            Toast.makeText(context, "Archivo adjuntado a la nota", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Cámara / Adjunto",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Drawing Markup Pen (Screenshot 4 & 6)
                    IconButton(
                        onClick = {
                            updateBodyWithUndo(body + "\n[Dibujo a mano alzada]")
                            Toast.makeText(context, "Modo dibujo activado", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Draw,
                            contentDescription = "Dibujo",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // New note compose icon
                    IconButton(
                        onClick = {
                            persistChanges()
                            onCreateNewNote()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "Nueva nota",
                            tint = IOSYellow,
                            modifier = Modifier.size(24.dp)
                        )
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Timestamp header (e.g., "10 de noviembre, 18:20")
            val dateStr = SimpleDateFormat("d 'de' MMMM 'de' yyyy, HH:mm", Locale("es")).format(Date(createdAt))
            Text(
                text = dateStr,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 8.dp)
            )

            // Large Title input (Screenshot 4)
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                textStyle = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                cursorBrush = SolidColor(IOSYellow),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text(
                            text = "Título",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Body rendering: Interactive Checklist items or Plain Text (Screenshot 4)
            val lines = body.split("\n")
            val hasChecklistLines = lines.any { it.startsWith("[ ] ") || it.startsWith("[x] ") || it.startsWith("○ ") || it.startsWith("✓ ") }

            if (hasChecklistLines || isChecklistMode) {
                // Interactive Checklist rows (Screenshot 4)
                Column(modifier = Modifier.fillMaxWidth()) {
                    lines.forEachIndexed { index, line ->
                        val isChecked = line.startsWith("[x] ") || line.startsWith("✓ ")
                        val isItem = line.startsWith("[ ] ") || line.startsWith("[x] ") || line.startsWith("○ ") || line.startsWith("✓ ")

                        if (isItem) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Circular checkbox (Screenshot 4)
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isChecked) IOSYellow else Color.Transparent)
                                        .border(
                                            width = if (isChecked) 0.dp else 1.5.dp,
                                            color = if (isChecked) Color.Transparent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                        .clickable { toggleChecklistItem(index) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChecked) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                val itemText = line
                                    .removePrefix("[ ] ")
                                    .removePrefix("[x] ")
                                    .removePrefix("○ ")
                                    .removePrefix("✓ ")

                                BasicTextField(
                                    value = itemText,
                                    onValueChange = { newText ->
                                        val prefix = if (isChecked) "[x] " else "[ ] "
                                        val updatedLines = lines.toMutableList()
                                        updatedLines[index] = prefix + newText
                                        updateBodyWithUndo(updatedLines.joinToString("\n"))
                                    },
                                    textStyle = TextStyle(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground
                                    ),
                                    cursorBrush = SolidColor(IOSYellow),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            // Non-item line inside mixed content
                            BasicTextField(
                                value = line,
                                onValueChange = { newText ->
                                    val updatedLines = lines.toMutableList()
                                    updatedLines[index] = newText
                                    updateBodyWithUndo(updatedLines.joinToString("\n"))
                                },
                                textStyle = TextStyle(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                cursorBrush = SolidColor(IOSYellow),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }

                    // Add new checklist item helper row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                updateBodyWithUndo(body + (if (body.isEmpty() || body.endsWith("\n")) "" else "\n") + "[ ] ")
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(1.5.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Añadir elemento...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontSize = 17.sp
                        )
                    }
                }
            } else {
                // Freeform text editor
                BasicTextField(
                    value = body,
                    onValueChange = { updateBodyWithUndo(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .padding(vertical = 4.dp),
                    textStyle = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    cursorBrush = SolidColor(IOSYellow),
                    decorationBox = { innerTextField ->
                        if (body.isEmpty()) {
                            Text(
                                text = "Nota",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}
