package com.itsbatt.inotes.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itsbatt.inotes.data.ExportAndBackupManager
import com.itsbatt.inotes.domain.ChecklistUtils
import com.itsbatt.inotes.domain.MathEvaluator
import com.itsbatt.inotes.domain.NoteLine
import java.io.File
import java.io.FileOutputStream
import com.itsbatt.inotes.ui.AndroidUtils
import com.itsbatt.inotes.ui.DetectedLink
import com.itsbatt.inotes.ui.IOSYellow
import com.itsbatt.inotes.ui.LinkDetector
import com.itsbatt.inotes.ui.LiquidGlassPill
import com.itsbatt.inotes.ui.LocalLiquidGlassConfig
import com.itsbatt.inotes.ui.MainViewModel
import com.itsbatt.inotes.ui.RichTextTransformation
import com.itsbatt.inotes.ui.iosPressScale
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
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
    onOpenNote: (Long) -> Unit = {}
) {
    val context = LocalContext.current
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
    val createdAt = remember(existingNote) { existingNote?.createdAt ?: System.currentTimeMillis() }

    // History for Undo/Redo
    val undoStack = remember { mutableStateListOf<String>() }
    val redoStack = remember { mutableStateListOf<String>() }

    var showOptionsMenu by remember { mutableStateOf(false) }
    var showFormatBar by remember { mutableStateOf(false) }
    var showFolderPicker by remember { mutableStateOf(false) }
    var showDrawingPad by remember { mutableStateOf(false) }
    var showInNoteSearch by remember { mutableStateOf(false) }
    var inNoteQuery by remember { mutableStateOf("") }
    var collapsedSections by remember { mutableStateOf(setOf<Int>()) }
    var showNoteLinkPicker by remember { mutableStateOf(false) }
    val richTextTransformation = remember { RichTextTransformation() }

    val lockPrefs = remember { context.getSharedPreferences("inotes_locks", Context.MODE_PRIVATE) }
    var isNoteLocked by remember(currentNoteId) {
        mutableStateOf(currentNoteId != 0L && lockPrefs.getBoolean("lock_$currentNoteId", false))
    }
    val unlockedNoteIds by viewModel.unlockedNoteIds.collectAsState()
    val isUnlockedSession = !isNoteLocked || unlockedNoteIds.contains(currentNoteId)
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var showVersionHistory by remember { mutableStateOf(false) }
    val noteVersions by viewModel.getVersionsForNote(currentNoteId).collectAsState(initial = emptyList())

    val allFolders by viewModel.allFolders.collectAsState()
    val backdrop = rememberLayerBackdrop()

    fun persistChanges() {
        if (title.isNotBlank() || body.isNotBlank()) {
            val targetFolder = existingNote?.folder ?: if (selectedFolder == "Todas las notas" || selectedFolder == "Papelera" || selectedFolder == "Todo lo de iCloud") "Notas" else selectedFolder
            viewModel.saveNote(currentNoteId, title, body, targetFolder) { newId ->
                currentNoteId = newId
            }
        }
    }

    // Auto-save with 1.5s short debounce (Requirement 3)
    LaunchedEffect(title, body) {
        if (title.isNotEmpty() || body.isNotEmpty()) {
            delay(1500)
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

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedImgPath = copyUriToInternalImage(context, uri)
            if (savedImgPath != null) {
                val lead = if (body.isEmpty() || body.endsWith("\n")) "" else "\n"
                updateBodyWithUndo("$body$lead[image:$savedImgPath]\n")
            }
        }
    }

    val documentScannerLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && result.data != null) {
            val scanResult = com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val pages = scanResult?.pages.orEmpty()
            if (pages.isNotEmpty()) {
                pages.forEach { page ->
                    val savedPath = com.itsbatt.inotes.data.DocumentScannerHelper.copyScannedUriToImages(context, page.imageUri)
                    if (savedPath != null) {
                        val lead = if (body.isEmpty() || body.endsWith("\n")) "" else "\n"
                        updateBodyWithUndo("$body$lead[image:$savedPath]\n")
                    }
                    com.itsbatt.inotes.data.DocumentScannerHelper.recognizeTextOffline(context, page.imageUri) { ocrText ->
                        if (ocrText.isNotBlank()) {
                            val lead = if (body.isEmpty() || body.endsWith("\n")) "" else "\n"
                            updateBodyWithUndo("$body${lead}Texto escaneado (OCR):\n$ocrText\n")
                        }
                    }
                }
                scanResult?.pdf?.uri?.let { pdfUri ->
                    com.itsbatt.inotes.data.DocumentScannerHelper.copyScannedPdfToFiles(context, pdfUri)
                }
                Toast.makeText(context, "Documento escaneado (${pages.size} pág.)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchDocumentScanner() {
        val activity = context as? android.app.Activity ?: return
        com.itsbatt.inotes.data.DocumentScannerHelper.getScannerClient()
            .getStartScanIntent(activity)
            .addOnSuccessListener { intentSender ->
                documentScannerLauncher.launch(
                    androidx.activity.result.IntentSenderRequest.Builder(intentSender).build()
                )
            }
            .addOnFailureListener {
                Toast.makeText(context, "Escáner no disponible en este dispositivo", Toast.LENGTH_SHORT).show()
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrollable Note Content (hidden completely when locked until authenticated!)
        if (isNoteLocked && !isUnlockedSession) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .layerBackdrop(backdrop),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = IOSYellow.copy(alpha = 0.25f),
                        modifier = Modifier.size(96.dp)
                    )
                }
            }
        } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .layerBackdrop(backdrop)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(top = 64.dp, bottom = 110.dp, start = 20.dp, end = 20.dp)
        ) {
            // Formatted date and time
            val dateStr = SimpleDateFormat("d 'de' MMMM 'de' yyyy, HH:mm", Locale("es")).format(Date(createdAt))
            Text(
                text = dateStr,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 8.dp)
            )

            // Large Title input
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

            // Interactive Note Body: Checklists, Dividers, Headings (collapsible), Tables, Drawings, Photos, Math, Plain text
            val parsedLines = remember(body) { ChecklistUtils.parseLines(body) }
            val hasRichBlocks = parsedLines.any {
                it !is NoteLine.Plain || it.text.startsWith("# ") || it.text.startsWith("## ") || it.text.trim().endsWith("=")
            }

            fun handleBodyEdit(newBody: String) {
                updateBodyWithUndo(newBody)
                if (newBody.endsWith(">>")) {
                    showNoteLinkPicker = true
                }
            }

            if (!hasRichBlocks) {
                // Fast continuous multi-line editor when plain text (with RichTextTransformation)
                BasicTextField(
                    value = body,
                    onValueChange = { handleBodyEdit(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .padding(vertical = 4.dp),
                    visualTransformation = richTextTransformation,
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
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    var activeHiddenHeaderLevel: Int? = null
                    parsedLines.forEachIndexed { index, line ->
                        val headingLevel = when {
                            line is NoteLine.Plain && line.text.startsWith("# ") -> 1
                            line is NoteLine.Plain && line.text.startsWith("## ") -> 2
                            else -> null
                        }
                        if (headingLevel != null) {
                            activeHiddenHeaderLevel = if (collapsedSections.contains(index)) headingLevel else null
                        } else if (activeHiddenHeaderLevel != null) {
                            return@forEachIndexed
                        }

                        val matchesSearch = inNoteQuery.isNotBlank() && when (line) {
                            is NoteLine.Plain -> line.text.contains(inNoteQuery, ignoreCase = true)
                            is NoteLine.Checklist -> line.text.contains(inNoteQuery, ignoreCase = true)
                            is NoteLine.TableRow -> line.cells.any { it.contains(inNoteQuery, ignoreCase = true) }
                            else -> false
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (matchesSearch) Modifier.background(IOSYellow.copy(alpha = 0.20f), RoundedCornerShape(8.dp))
                                    else Modifier
                                )
                        ) {
                            when (line) {
                                is NoteLine.Checklist -> {
                                    ChecklistItemRow(
                                        text = line.text,
                                        checked = line.checked,
                                        onToggle = {
                                            val updated = ChecklistUtils.toggleChecklistAt(parsedLines, index)
                                            updateBodyWithUndo(ChecklistUtils.serializeLines(updated))
                                        },
                                        onTextChange = { newText ->
                                            if (newText.contains("\n")) {
                                                val parts = newText.split("\n", limit = 2)
                                                val updated = parsedLines.toMutableList()
                                                if (parts[0].isBlank() && line.text.isBlank()) {
                                                    updated[index] = NoteLine.Plain(parts[1])
                                                } else {
                                                    updated[index] = line.copy(text = parts[0])
                                                    updated.add(index + 1, NoteLine.Checklist(parts[1], checked = false))
                                                }
                                                updateBodyWithUndo(ChecklistUtils.serializeLines(updated))
                                            } else {
                                                val updated = parsedLines.toMutableList()
                                                updated[index] = line.copy(text = newText)
                                                updateBodyWithUndo(ChecklistUtils.serializeLines(updated))
                                            }
                                        },
                                        onRemove = {
                                            val updated = parsedLines.toMutableList()
                                            updated.removeAt(index)
                                            updateBodyWithUndo(ChecklistUtils.serializeLines(updated))
                                        }
                                    )
                                }

                                is NoteLine.Divider -> {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                                    )
                                }

                                is NoteLine.Drawing -> {
                                    InlineImageBlock(
                                        path = line.path,
                                        onRemove = {
                                            val updated = parsedLines.toMutableList().apply { removeAt(index) }
                                            updateBodyWithUndo(ChecklistUtils.serializeLines(updated))
                                        }
                                    )
                                }

                                is NoteLine.ImageAttachment -> {
                                    InlineImageBlock(
                                        path = line.uri,
                                        onRemove = {
                                            val updated = parsedLines.toMutableList().apply { removeAt(index) }
                                            updateBodyWithUndo(ChecklistUtils.serializeLines(updated))
                                        }
                                    )
                                }

                                is NoteLine.TableRow -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(0.8.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        line.cells.forEachIndexed { colIndex, cellValue ->
                                            BasicTextField(
                                                value = cellValue,
                                                onValueChange = { newCell ->
                                                    val newCells = line.cells.toMutableList().apply { this[colIndex] = newCell }
                                                    val updated = parsedLines.toMutableList().apply { this[index] = NoteLine.TableRow(newCells) }
                                                    updateBodyWithUndo(ChecklistUtils.serializeLines(updated))
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                                textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground),
                                                cursorBrush = SolidColor(IOSYellow)
                                            )
                                            if (colIndex < line.cells.lastIndex) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(0.8.dp)
                                                        .height(34.dp)
                                                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f))
                                                )
                                            }
                                        }
                                    }
                                }

                                is NoteLine.Plain -> {
                                    val isH1 = line.text.startsWith("# ")
                                    val isH2 = line.text.startsWith("## ")
                                    val mathResult = remember(line.text) { MathEvaluator.tryEvaluateTrailingEquals(line.text) }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isH1 || isH2) {
                                            val isCollapsed = collapsedSections.contains(index)
                                            Icon(
                                                imageVector = if (isCollapsed) Icons.Default.KeyboardArrowRight else Icons.Default.KeyboardArrowDown,
                                                contentDescription = "Plegar sección",
                                                tint = IOSYellow,
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clickable {
                                                        collapsedSections = if (isCollapsed) collapsedSections - index else collapsedSections + index
                                                    }
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        BasicTextField(
                                            value = line.text,
                                            onValueChange = { newText ->
                                                if (newText.contains("\n")) {
                                                    val parts = newText.split("\n", limit = 2)
                                                    val updated = parsedLines.toMutableList()
                                                    updated[index] = NoteLine.Plain(parts[0])
                                                    val nextLine = if (parts[0].startsWith("• ") && parts[0].length > 2) {
                                                        NoteLine.Plain("• ${parts[1]}")
                                                    } else {
                                                        NoteLine.Plain(parts[1])
                                                    }
                                                    updated.add(index + 1, nextLine)
                                                    updateBodyWithUndo(ChecklistUtils.serializeLines(updated))
                                                } else {
                                                    val updated = parsedLines.toMutableList()
                                                    updated[index] = NoteLine.Plain(newText)
                                                    updateBodyWithUndo(ChecklistUtils.serializeLines(updated))
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(vertical = 2.dp),
                                            visualTransformation = richTextTransformation,
                                            textStyle = TextStyle(
                                                fontSize = when {
                                                    isH1 -> 22.sp
                                                    isH2 -> 19.sp
                                                    else -> 17.sp
                                                },
                                                fontWeight = if (isH1 || isH2) FontWeight.Bold else FontWeight.Normal,
                                                lineHeight = 24.sp,
                                                color = MaterialTheme.colorScheme.onBackground
                                            ),
                                            cursorBrush = SolidColor(IOSYellow),
                                            decorationBox = { innerTextField ->
                                                if (line.text.isEmpty() && index == parsedLines.lastIndex) {
                                                    Text(
                                                        text = "Seguir escribiendo...",
                                                        fontSize = 17.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        )
                                        if (mathResult != null) {
                                            Text(
                                                text = " $mathResult",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = IOSYellow,
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(IOSYellow.copy(alpha = 0.16f))
                                                    .clickable {
                                                        val updated = parsedLines.toMutableList()
                                                        updated[index] = NoteLine.Plain("${line.text.trim()} $mathResult")
                                                        updateBodyWithUndo(ChecklistUtils.serializeLines(updated))
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Tap below blocks to add a new plain line if last line isn't empty
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                val last = parsedLines.lastOrNull()
                                if (last !is NoteLine.Plain || last.text.isNotEmpty()) {
                                    updateBodyWithUndo(if (body.isEmpty()) "" else "$body\n")
                                }
                            }
                    )
                }
            }

            // Detected Links & Internal Note Links Chips (URLs, Emails, Phones, [[id:title]])
            val detectedLinks = remember(body) { LinkDetector.detect(body) }
            if (detectedLinks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    detectedLinks.forEach { link ->
                        val (label, onLinkClick) = when (link) {
                            is DetectedLink.InternalNote -> "↗ Nota: ${link.title}" to {
                                persistChanges()
                                onOpenNote(link.noteId)
                            }
                            is DetectedLink.WebUrl -> "🌐 ${link.url}" to {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
                                }
                                Unit
                            }
                            is DetectedLink.Email -> "✉️ ${link.address}" to {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${link.address}")))
                                }
                                Unit
                            }
                            is DetectedLink.Phone -> "📞 ${link.number}" to {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${link.number}")))
                                }
                                Unit
                            }
                        }
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(IOSYellow.copy(alpha = 0.14f))
                                .clickable(onClick = onLinkClick)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, tint = IOSYellow, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = IOSYellow,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
        }

        // iOS 26 Liquid Glass Top Bar with proper statusBarsPadding
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back glass pill with folder title in yellow
            LiquidGlassPill(
                backdrop = backdrop,
                modifier = Modifier.height(40.dp),
                shape = CircleShape,
                onClick = {
                    persistChanges()
                    onBack()
                }
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
                    val backLabel = if (selectedFolder.isNotBlank()) selectedFolder else "Notas"
                    Text(
                        text = backLabel,
                        color = IOSYellow,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }

            // Top Right Actions: Grouped Liquid Glass pill + Yellow Done circle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiquidGlassPill(
                    backdrop = backdrop,
                    modifier = Modifier.height(38.dp),
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Undo
                        IconButton(
                            modifier = Modifier.size(34.dp),
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
                                tint = if (undoStack.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Redo
                        IconButton(
                            modifier = Modifier.size(34.dp),
                            onClick = {
                                if (redoStack.isNotEmpty()) {
                                    val next = redoStack.removeAt(redoStack.lastIndex)
                                    undoStack.add(body)
                                    body = next
                                }
                            },
                            enabled = redoStack.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Rehacer",
                                tint = if (redoStack.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Share
                        IconButton(
                            modifier = Modifier.size(34.dp),
                            onClick = {
                                val shareContent = "$title\n\n$body"
                                AndroidUtils.shareText(context, shareContent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // More options '...'
                        IconButton(
                            modifier = Modifier.size(34.dp),
                            onClick = { showOptionsMenu = !showOptionsMenu }
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "Opciones",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Yellow Liquid Glass Checkmark Done button (becomes translucent on press)
                LiquidGlassPill(
                    backdrop = backdrop,
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    tintOverride = IOSYellow,
                    onClick = {
                        focusManager.clearFocus()
                        persistChanges()
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Listo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // iOS 26 Floating Liquid Glass Formatting Bar (Aa) + Main Toolbar Pill
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = showFormatBar,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                LiquidGlassPill(
                    backdrop = backdrop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .height(48.dp),
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val formatActions = listOf(
                            "B" to "**texto**",
                            "I" to "*texto*",
                            "U" to "__texto__",
                            "Resaltar" to "==texto==",
                            "H1" to "# ",
                            "•" to "• ",
                            ">>" to "LINK"
                        )
                        formatActions.forEach { (label, token) ->
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (label == "Resaltar" || label == ">>") IOSYellow else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        if (token == "LINK") {
                                            showNoteLinkPicker = true
                                        } else {
                                            val lead = if (body.isEmpty() || body.endsWith("\n") || body.endsWith(" ")) "" else " "
                                            updateBodyWithUndo("$body$lead$token")
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            LiquidGlassPill(
                backdrop = backdrop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showFormatBar = !showFormatBar }) {
                        Icon(
                            imageVector = Icons.Default.TextFormat,
                            contentDescription = "Formato",
                            tint = if (showFormatBar) IOSYellow else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            val prefix = if (body.isEmpty() || body.endsWith("\n")) "" else "\n"
                            updateBodyWithUndo("$body$prefix- [ ] ")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "Lista de tareas",
                            tint = IOSYellow,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            val lead = if (body.isEmpty() || body.endsWith("\n")) "" else "\n"
                            updateBodyWithUndo("$body$lead| Columna 1 | Columna 2 |\n| Dato 1 | Dato 2 |\n")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "Insertar tabla",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Adjuntar foto",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    IconButton(onClick = { showDrawingPad = true }) {
                        Icon(
                            imageVector = Icons.Default.Draw,
                            contentDescription = "Garabato / Dibujar",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    IconButton(onClick = { showFolderPicker = true }) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Mover a carpeta",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // In-note Search Floating Liquid Glass Bar
        AnimatedVisibility(
            visible = showInNoteSearch,
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 56.dp, start = 16.dp, end = 16.dp)
        ) {
            LiquidGlassPill(
                backdrop = backdrop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = IOSYellow, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = inNoteQuery,
                        onValueChange = { inNoteQuery = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface),
                        cursorBrush = SolidColor(IOSYellow),
                        decorationBox = { inner ->
                            if (inNoteQuery.isEmpty()) {
                                Text("Buscar en la nota...", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            inner()
                        }
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar búsqueda",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                inNoteQuery = ""
                                showInNoteSearch = false
                            }
                    )
                }
            }
        }

        // iOS 26 Liquid Glass Floating Options Popover (matches Apple Notes iOS 26 style)
        val (wordCount, charCount) = remember(title, body) {
            ChecklistUtils.countWordsAndChars(title, body)
        }
        IOS26OptionsPopover(
            visible = showOptionsMenu,
            backdrop = backdrop,
            isPinned = isPinned,
            isLocked = isNoteLocked,
            canDelete = currentNoteId != 0L,
            wordCount = wordCount,
            charCount = charCount,
            onDismiss = { showOptionsMenu = false },
            onTogglePin = {
                isPinned = !isPinned
                existingNote?.let { viewModel.togglePin(it) }
                showOptionsMenu = false
            },
            onShare = {
                AndroidUtils.shareText(context, "$title\n\n$body")
                showOptionsMenu = false
            },
            onExportMarkdown = {
                val md = "# ${title.ifBlank { "Nota" }}\n\n$body"
                AndroidUtils.shareText(context, md)
                showOptionsMenu = false
            },
            onExportPdf = {
                ExportAndBackupManager.exportNoteToPdfAndShare(context, title, body, createdAt)
                showOptionsMenu = false
            },
            onPrint = {
                ExportAndBackupManager.printNote(context, title, body)
                showOptionsMenu = false
            },
            onToggleLock = {
                if (isNoteLocked) {
                    isNoteLocked = false
                    lockPrefs.edit().remove("lock_$currentNoteId").apply()
                    Toast.makeText(context, "Bloqueo eliminado", Toast.LENGTH_SHORT).show()
                } else {
                    if (lockPrefs.getString("pin_code", null) == null) {
                        pinInput = ""
                        showPinSetupDialog = true
                    } else if (currentNoteId != 0L) {
                        isNoteLocked = true
                        lockPrefs.edit().putBoolean("lock_$currentNoteId", true).apply()
                        Toast.makeText(context, "Nota bloqueada", Toast.LENGTH_SHORT).show()
                    }
                }
                showOptionsMenu = false
            },
            onSearchInNote = {
                showInNoteSearch = true
                showOptionsMenu = false
            },
            onDelete = {
                if (currentNoteId != 0L) {
                    viewModel.moveToTrash(currentNoteId)
                    showOptionsMenu = false
                    onBack()
                }
            },
            onCopyAll = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("INotes", "$title\n\n$body".trim()))
                Toast.makeText(context, "Nota copiada", Toast.LENGTH_SHORT).show()
                showOptionsMenu = false
            },
            onDuplicate = {
                existingNote?.let { note ->
                    viewModel.duplicateNote(note)
                    Toast.makeText(context, "Nota duplicada", Toast.LENGTH_SHORT).show()
                }
                showOptionsMenu = false
            },
            onInsertChecklist = {
                val prefix = if (body.isEmpty() || body.endsWith("\n")) "" else "\n"
                updateBodyWithUndo("$body$prefix- [ ] ")
                showOptionsMenu = false
            },
            onInsertDate = {
                val nowStr = SimpleDateFormat("d MMM yyyy, HH:mm", Locale("es")).format(Date())
                val prefix = if (body.isEmpty() || body.endsWith("\n")) "" else "\n"
                updateBodyWithUndo("$body$prefix$nowStr ")
                showOptionsMenu = false
            },
            onInsertDivider = {
                updateBodyWithUndo(body + "\n---\n")
                showOptionsMenu = false
            },
            onScanDocument = {
                showOptionsMenu = false
                launchDocumentScanner()
            },
            onShowHistory = {
                showOptionsMenu = false
                showVersionHistory = true
            }
        )

        // Fullscreen Apple Notes Garabatos / Markup Canvas
        if (showDrawingPad) {
            DrawingOverlay(
                onCancel = { showDrawingPad = false },
                onSaveDrawing = { savedPath ->
                    val lead = if (body.isEmpty() || body.endsWith("\n")) "" else "\n"
                    updateBodyWithUndo("$body$lead[drawing:$savedPath]\n")
                    showDrawingPad = false
                }
            )
        }

        // Lock Note Overlay with BiometricPrompt + PIN fallback
        if (isNoteLocked && !isUnlockedSession) {
            val activity = context as? FragmentActivity
            val canUseBiometrics = remember(context) {
                BiometricManager.from(context).canAuthenticate(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
                ) == BiometricManager.BIOMETRIC_SUCCESS
            }

            fun launchBiometricPrompt() {
                if (activity == null) return
                val executor = ContextCompat.getMainExecutor(context)
                val prompt = BiometricPrompt(
                    activity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            viewModel.markNoteUnlocked(currentNoteId)
                            pinInput = ""
                        }
                    }
                )
                val info = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Desbloquear nota")
                    .setSubtitle("Usa tu huella o biometría")
                    .setNegativeButtonText("Usar PIN")
                    .build()
                prompt.authenticate(info)
            }

            LaunchedEffect(currentNoteId, canUseBiometrics) {
                if (canUseBiometrics) {
                    launchBiometricPrompt()
                }
            }

            NotePinModal(
                backdrop = backdrop,
                title = "Nota bloqueada",
                subtitle = if (canUseBiometrics) "Usa biometría o introduce tu PIN" else "Introduce tu PIN para ver esta nota",
                pin = pinInput,
                showBiometricButton = canUseBiometrics,
                onBiometricClick = { launchBiometricPrompt() },
                onPinChange = { pinInput = it },
                onCancel = onBack,
                onConfirm = {
                    val expected = lockPrefs.getString("pin_code", "")
                    if (pinInput == expected) {
                        viewModel.markNoteUnlocked(currentNoteId)
                        pinInput = ""
                    } else {
                        Toast.makeText(context, "PIN incorrecto", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Set PIN for the first time
        if (showPinSetupDialog) {
            NotePinModal(
                backdrop = backdrop,
                title = "Crear PIN de Notas",
                subtitle = "Elige un PIN numérico para bloquear notas",
                pin = pinInput,
                onPinChange = { pinInput = it },
                onCancel = {
                    showPinSetupDialog = false
                    pinInput = ""
                },
                onConfirm = {
                    if (pinInput.length >= 4 && currentNoteId != 0L) {
                        lockPrefs.edit()
                            .putString("pin_code", pinInput)
                            .putBoolean("lock_$currentNoteId", true)
                            .apply()
                        isNoteLocked = true
                        viewModel.markNoteUnlocked(currentNoteId)
                        showPinSetupDialog = false
                        pinInput = ""
                        Toast.makeText(context, "Nota bloqueada con PIN", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Mínimo 4 dígitos", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Version History Modal ("Historial de versiones")
        if (showVersionHistory) {
            AlertDialog(
                onDismissRequest = { showVersionHistory = false },
                title = { Text("Historial de versiones") },
                text = {
                    if (noteVersions.isEmpty()) {
                        Text(
                            "Aún no hay instantáneas guardadas para esta nota.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 360.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val fmt = remember { SimpleDateFormat("d MMM HH:mm:ss", Locale("es")) }
                            noteVersions.forEach { ver ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            undoStack.add(body)
                                            redoStack.clear()
                                            title = ver.title
                                            body = ver.body
                                            showVersionHistory = false
                                            Toast.makeText(context, "Versión restaurada", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = fmt.format(Date(ver.createdAt)),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = IOSYellow
                                        )
                                        if (ver.title.isNotBlank()) {
                                            Text(
                                                text = ver.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1
                                            )
                                        }
                                        Text(
                                            text = ver.body.take(100).ifBlank { "(Sin contenido)" },
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showVersionHistory = false }) {
                        Text("Cerrar", color = IOSYellow)
                    }
                }
            )
        }

        // Internal Note Link Picker (triggered by ">>" or ">>" in Aa bar)
        MoveToFolderModal(
            visible = showNoteLinkPicker,
            backdrop = backdrop,
            folders = allNotes.filter { it.id != currentNoteId }.map { it.title.ifBlank { "Nota #${it.id}" } },
            currentFolder = "",
            modalTitle = "Enlazar otra nota",
            onDismiss = { showNoteLinkPicker = false },
            onSelectFolder = { chosenTitle ->
                val target = allNotes.firstOrNull { it.id != currentNoteId && it.title.ifBlank { "Nota #${it.id}" } == chosenTitle }
                if (target != null) {
                    val cleaned = if (body.endsWith(">>")) body.dropLast(2) else body
                    val lead = if (cleaned.isEmpty() || cleaned.endsWith("\n") || cleaned.endsWith(" ")) "" else " "
                    updateBodyWithUndo("$cleaned$lead[[${target.id}:$chosenTitle]] ")
                }
                showNoteLinkPicker = false
            }
        )

        // Move to Folder Liquid Glass Modal
        MoveToFolderModal(
            visible = showFolderPicker,
            backdrop = backdrop,
            folders = allFolders.map { it.name }.ifEmpty { listOf("Notas") },
            currentFolder = existingNote?.folder ?: selectedFolder,
            onDismiss = { showFolderPicker = false },
            onSelectFolder = { targetFolder ->
                persistChanges()
                if (currentNoteId != 0L) {
                    viewModel.moveNoteToFolder(currentNoteId, targetFolder)
                }
                viewModel.selectFolder(targetFolder)
                Toast.makeText(context, "Movida a $targetFolder", Toast.LENGTH_SHORT).show()
                showFolderPicker = false
            }
        )
    }
}

@Composable
private fun InlineImageBlock(
    path: String,
    onRemove: () -> Unit
) {
    val bitmap = remember(path) { BitmapFactory.decodeFile(path) }
    if (bitmap != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(18.dp))
                .border(0.8.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Adjunto",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = "Eliminar", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun NotePinModal(
    backdrop: LayerBackdrop,
    title: String,
    subtitle: String,
    pin: String,
    showBiometricButton: Boolean = false,
    onBiometricClick: () -> Unit = {},
    onPinChange: (String) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val config = LocalLiquidGlassConfig.current
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
                        drawRect(bg.copy(alpha = if (isDark) 0.88f else 0.90f))
                    }
                )
                .border(1.dp, Color.White.copy(alpha = if (isDark) 0.28f else 0.92f), shape)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = IOSYellow, modifier = Modifier.size(30.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(14.dp))
            BasicTextField(
                value = pin,
                onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) onPinChange(it) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(IOSYellow),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 18.dp),
                decorationBox = { inner ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (pin.isEmpty()) Text("PIN", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        inner()
                    }
                }
            )
            if (showBiometricButton) {
                Spacer(modifier = Modifier.height(12.dp))
                LiquidGlassPill(
                    backdrop = backdrop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    onClick = onBiometricClick
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = null,
                            tint = IOSYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Desbloquear con biometría",
                            color = IOSYellow,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LiquidGlassPill(backdrop = backdrop, modifier = Modifier.weight(1f).height(42.dp), onClick = onCancel) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                    }
                }
                LiquidGlassPill(backdrop = backdrop, modifier = Modifier.weight(1f).height(42.dp), tintOverride = IOSYellow, onClick = onConfirm) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aceptar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun copyUriToInternalImage(context: Context, uri: Uri): String? {
    return try {
        val dir = File(context.filesDir, "images").apply { mkdirs() }
        val outFile = File(dir, "img_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output -> input.copyTo(output) }
        }
        outFile.absolutePath
    } catch (_: Exception) {
        null
    }
}

@Composable
private fun ChecklistItemRow(
    text: String,
    checked: Boolean,
    onToggle: () -> Unit,
    onTextChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    val circleScale by animateFloatAsState(
        targetValue = if (checked) 1.08f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium),
        label = "checkScale"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) IOSYellow else Color.Transparent)
                .border(
                    width = 1.8.dp,
                    color = if (checked) IOSYellow else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    shape = CircleShape
                )
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completada",
                    tint = Color.White,
                    modifier = Modifier.size((14 * circleScale).dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(
                fontSize = 17.sp,
                lineHeight = 23.sp,
                color = if (checked) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
            ),
            cursorBrush = SolidColor(IOSYellow),
            decorationBox = { innerTextField ->
                if (text.isEmpty()) {
                    Text(
                        text = "Elemento de lista",
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
                innerTextField()
            }
        )

        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Quitar elemento",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onRemove)
        )
    }
}

@Composable
private fun MoveToFolderModal(
    visible: Boolean,
    backdrop: LayerBackdrop,
    folders: List<String>,
    currentFolder: String,
    modalTitle: String = "Mover a carpeta",
    onDismiss: () -> Unit,
    onSelectFolder: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val config = LocalLiquidGlassConfig.current

    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.34f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(initialScale = 0.86f, animationSpec = spring(0.65f, Spring.StiffnessMediumLow)) + fadeIn(),
                exit = scaleOut(targetScale = 0.90f) + fadeOut()
            ) {
                val shape = RoundedCornerShape(28.dp)
                val borderBrush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (isDark) 0.30f else 0.96f),
                        (if (isDark) Color.White else Color(0xFFC7C7CC)).copy(alpha = if (isDark) 0.08f else 0.40f)
                    )
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                        .shadow(20.dp, shape)
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
                                drawRect(bg.copy(alpha = if (isDark) 0.78f else 0.84f))
                            }
                        )
                        .border(BorderStroke(1.dp, borderBrush), shape)
                        .padding(18.dp)
                ) {
                    Text(
                        text = modalTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    folders.forEach { folderName ->
                        val isCurrent = folderName.equals(currentFolder, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CircleShape)
                                .clickable { onSelectFolder(folderName) }
                                .padding(horizontal = 12.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = IOSYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = folderName,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (isCurrent) {
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
private fun IOS26OptionsPopover(
    visible: Boolean,
    backdrop: LayerBackdrop,
    isPinned: Boolean,
    isLocked: Boolean,
    canDelete: Boolean,
    wordCount: Int,
    charCount: Int,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onShare: () -> Unit,
    onExportMarkdown: () -> Unit,
    onExportPdf: () -> Unit,
    onPrint: () -> Unit,
    onToggleLock: () -> Unit,
    onSearchInNote: () -> Unit,
    onDelete: () -> Unit,
    onCopyAll: () -> Unit,
    onDuplicate: () -> Unit,
    onInsertChecklist: () -> Unit,
    onInsertDate: () -> Unit,
    onInsertDivider: () -> Unit,
    onScanDocument: () -> Unit = {},
    onShowHistory: () -> Unit = {}
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
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .statusBarsPadding()
                .padding(top = 56.dp, end = 14.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(
                    initialScale = 0.82f,
                    transformOrigin = TransformOrigin(0.92f, 0f),
                    animationSpec = spring(
                        dampingRatio = 0.65f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(),
                exit = scaleOut(
                    targetScale = 0.88f,
                    transformOrigin = TransformOrigin(0.92f, 0f)
                ) + fadeOut()
            ) {
                val shape = RoundedCornerShape(28.dp)
                val borderBrush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (isDark) 0.30f else 0.96f),
                        (if (isDark) Color.White else Color(0xFFC7C7CC)).copy(alpha = if (isDark) 0.08f else 0.40f)
                    )
                )
                val dividerColor = if (isDark) {
                    Color.White.copy(alpha = 0.12f)
                } else {
                    Color.Black.copy(alpha = 0.08f)
                }

                Column(
                    modifier = Modifier
                        .width(264.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .shadow(22.dp, shape)
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { shape },
                            effects = {
                                if (config.enableVibrancy) vibrancy()
                                blur(20.dp.toPx())
                                if (config.enableLens) {
                                    lens(
                                        refractionHeight = 16.dp.toPx(),
                                        refractionAmount = 26.dp.toPx(),
                                        chromaticAberration = config.chromaticAberration
                                    )
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
                    // Top Quick Actions Bar (like Paste / Delete / Select in iOS 26)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PopoverQuickAction(
                            icon = Icons.Default.PushPin,
                            label = if (isPinned) "Desfijar" else "Fijar",
                            tint = if (isPinned) IOSYellow else MaterialTheme.colorScheme.onSurface,
                            onClick = onTogglePin,
                            modifier = Modifier.weight(1f)
                        )
                        PopoverQuickAction(
                            icon = Icons.Default.Share,
                            label = "Compartir",
                            tint = MaterialTheme.colorScheme.onSurface,
                            onClick = onShare,
                            modifier = Modifier.weight(1f)
                        )
                        PopoverQuickAction(
                            icon = Icons.Default.Delete,
                            label = "Eliminar",
                            tint = if (canDelete) Color(0xFFFF3B30) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            onClick = { if (canDelete) onDelete() },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Vertical List Actions with iOS 26 pill highlight
                    PopoverMenuItem(
                        icon = Icons.Default.Search,
                        label = "Buscar en la nota",
                        isDark = isDark,
                        onClick = onSearchInNote
                    )
                    PopoverMenuItem(
                        icon = if (isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        label = if (isLocked) "Quitar bloqueo" else "Bloquear nota",
                        isDark = isDark,
                        onClick = onToggleLock
                    )
                    PopoverMenuItem(
                        icon = Icons.Default.ContentCopy,
                        label = "Copiar todo",
                        isDark = isDark,
                        onClick = onCopyAll
                    )
                    PopoverMenuItem(
                        icon = Icons.Default.Share,
                        label = "Exportar Markdown (.md)",
                        isDark = isDark,
                        onClick = onExportMarkdown
                    )
                    PopoverMenuItem(
                        icon = Icons.Default.PictureAsPdf,
                        label = "Exportar PDF",
                        isDark = isDark,
                        onClick = onExportPdf
                    )
                    PopoverMenuItem(
                        icon = Icons.Default.Print,
                        label = "Imprimir",
                        isDark = isDark,
                        onClick = onPrint
                    )
                    if (canDelete) {
                        PopoverMenuItem(
                            icon = Icons.Default.FileCopy,
                            label = "Duplicar nota",
                            isDark = isDark,
                            onClick = onDuplicate
                        )
                    }
                    PopoverMenuItem(
                        icon = Icons.Default.Checklist,
                        label = "Añadir lista",
                        isDark = isDark,
                        onClick = onInsertChecklist
                    )
                    PopoverMenuItem(
                        icon = Icons.Default.Schedule,
                        label = "Insertar fecha",
                        isDark = isDark,
                        onClick = onInsertDate
                    )
                    PopoverMenuItem(
                        icon = Icons.Default.HorizontalRule,
                        label = "Insertar línea divisoria",
                        isDark = isDark,
                        onClick = onInsertDivider
                    )
                    PopoverMenuItem(
                        icon = Icons.Default.DocumentScanner,
                        label = "Escanear documento (OCR)",
                        isDark = isDark,
                        onClick = onScanDocument
                    )
                    PopoverMenuItem(
                        icon = Icons.Default.History,
                        label = "Historial de versiones",
                        isDark = isDark,
                        onClick = onShowHistory
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = dividerColor)
                    Text(
                        text = "$wordCount palabras · $charCount caracteres",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp, bottom = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PopoverQuickAction(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .iosPressScale(interactionSource, pressedScale = 0.92f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = tint,
            maxLines = 1
        )
    }
}

@Composable
private fun PopoverMenuItem(
    icon: ImageVector,
    label: String,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pillBg = if (isPressed) {
        if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f)
    } else {
        Color.Transparent
    }

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
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
