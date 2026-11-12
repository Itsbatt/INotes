package com.itsbatt.inotes.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.itsbatt.inotes.INotesApplication
import com.itsbatt.inotes.data.room.entities.Folder
import com.itsbatt.inotes.data.room.entities.Note
import com.itsbatt.inotes.data.room.entities.NoteVersion
import com.itsbatt.inotes.data.room.entities.SmartFolder
import com.itsbatt.inotes.domain.usecases.CreateFolderUseCase
import com.itsbatt.inotes.domain.usecases.DeleteFolderUseCase
import com.itsbatt.inotes.domain.usecases.DeletePermanentlyUseCase
import com.itsbatt.inotes.domain.usecases.GetAllNotesUseCase
import com.itsbatt.inotes.domain.usecases.GetFoldersUseCase
import com.itsbatt.inotes.domain.usecases.MoveToTrashUseCase
import com.itsbatt.inotes.domain.usecases.PinNoteUseCase
import com.itsbatt.inotes.domain.usecases.RestoreFromTrashUseCase
import com.itsbatt.inotes.domain.usecases.SaveNoteUseCase
import com.itsbatt.inotes.domain.usecases.SearchNotesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NoteSortOrder(val label: String) {
    UPDATED_DESC("Fecha de edición"),
    CREATED_DESC("Fecha de creación"),
    TITLE_ASC("Título (A–Z)")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as INotesApplication).database
    private val noteDao = db.noteDao()
    private val folderDao = db.folderDao()
    private val noteVersionDao = db.noteVersionDao()
    private val smartFolderDao = db.smartFolderDao()
    private val prefs = application.getSharedPreferences("inotes_prefs", android.content.Context.MODE_PRIVATE)

    // Domain Use Cases
    private val getAllNotesUseCase = GetAllNotesUseCase(noteDao)
    private val searchNotesUseCase = SearchNotesUseCase(noteDao)
    private val pinNoteUseCase = PinNoteUseCase(noteDao)
    private val moveToTrashUseCase = MoveToTrashUseCase(noteDao)
    private val restoreFromTrashUseCase = RestoreFromTrashUseCase(noteDao)
    private val deletePermanentlyUseCase = DeletePermanentlyUseCase(noteDao)
    private val saveNoteUseCase = SaveNoteUseCase(noteDao)
    private val getFoldersUseCase = GetFoldersUseCase(folderDao)
    private val createFolderUseCase = CreateFolderUseCase(folderDao)
    private val deleteFolderUseCase = DeleteFolderUseCase(folderDao, noteDao)

    val allFolders: StateFlow<List<Folder>> = getFoldersUseCase.execute()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<Note>> = getAllNotesUseCase.execute()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedNotes: StateFlow<List<Note>> = noteDao.getDeletedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSmartFolders: StateFlow<List<SmartFolder>> = smartFolderDao.getAllSmartFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedFolder = MutableStateFlow("Todas las notas")
    val selectedFolder: StateFlow<String> = _selectedFolder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isTrashMode = MutableStateFlow(false)
    val isTrashMode: StateFlow<Boolean> = _isTrashMode.asStateFlow()

    private val _sortOrder = MutableStateFlow(NoteSortOrder.UPDATED_DESC)
    val sortOrder: StateFlow<NoteSortOrder> = _sortOrder.asStateFlow()

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags: StateFlow<Set<String>> = _selectedTags.asStateFlow()

    private val _autoLockMinutes = MutableStateFlow(prefs.getInt("auto_lock_minutes", 0))
    val autoLockMinutes: StateFlow<Int> = _autoLockMinutes.asStateFlow()

    private val _unlockedNoteIds = MutableStateFlow<Set<Long>>(emptySet())
    val unlockedNoteIds: StateFlow<Set<Long>> = _unlockedNoteIds.asStateFlow()

    val displayedNotes: StateFlow<List<Note>> = combine(
        allNotes,
        deletedNotes,
        _selectedFolder,
        _searchQuery,
        _isTrashMode,
        _sortOrder,
        _selectedTags,
        allSmartFolders
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val notes = values[0] as List<Note>
        @Suppress("UNCHECKED_CAST")
        val trashNotes = values[1] as List<Note>
        val folder = values[2] as String
        val query = values[3] as String
        val inTrash = values[4] as Boolean
        val sort = values[5] as NoteSortOrder
        @Suppress("UNCHECKED_CAST")
        val tagFilters = values[6] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val smartFolders = values[7] as List<SmartFolder>

        val sourceList = if (inTrash) trashNotes else notes
        val customSmart = if (folder.startsWith("smart:")) {
            val id = folder.removePrefix("smart:").toLongOrNull()
            smartFolders.firstOrNull { it.id == id }
        } else null

        val filteredByFolder = when {
            inTrash || folder == "Todas las notas" || folder == "Todo lo de iCloud" -> sourceList
            folder == "Notas rápidas" -> sourceList.take(5)
            folder == "Con tareas" -> sourceList.filter { it.body.contains("- [ ] ") || it.body.contains("- [x] ") }
            folder == "Con dibujos y fotos" -> sourceList.filter { it.body.contains("[drawing:") || it.body.contains("[image:") }
            folder == "De hoy" -> {
                val startOfDay = startOfDayMillis(0)
                sourceList.filter { it.updatedAt >= startOfDay }
            }
            customSmart != null -> filterBySmartFolder(sourceList, customSmart)
            else -> sourceList.filter { it.folder.equals(folder, ignoreCase = true) }
        }

        val filteredByTags = if (tagFilters.isEmpty()) {
            filteredByFolder
        } else {
            filteredByFolder.filter { note ->
                val fullText = "${note.title}\n${note.body}".lowercase()
                tagFilters.all { tag -> fullText.contains(tag.lowercase()) }
            }
        }

        val filteredByQuery = if (query.isBlank()) {
            filteredByTags
        } else {
            val q = query.trim().lowercase()
            filteredByTags.filter {
                it.title.lowercase().contains(q) || it.body.lowercase().contains(q)
            }
        }

        when (sort) {
            NoteSortOrder.UPDATED_DESC -> filteredByQuery.sortedByDescending { it.updatedAt }
            NoteSortOrder.CREATED_DESC -> filteredByQuery.sortedByDescending { it.createdAt }
            NoteSortOrder.TITLE_ASC -> filteredByQuery.sortedBy { it.title.ifBlank { "zzz" }.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Purge expired trash (> 30 days old)
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            noteDao.purgeExpiredTrash(thirtyDaysAgo)

            // Seed default "Notas" folder if empty
            val defaultFolders = listOf("Notas")
            defaultFolders.forEach { name ->
                if (folderDao.getFolderByName(name) == null) {
                    folderDao.insertFolder(Folder(name = name))
                }
            }
        }
    }

    fun selectFolder(folderName: String) {
        _isTrashMode.value = false
        _selectedFolder.value = folderName
    }

    fun openTrash() {
        _isTrashMode.value = true
        _selectedFolder.value = "Papelera"
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: NoteSortOrder) {
        _sortOrder.value = order
    }

    fun setTagFilters(tags: Set<String>) {
        _selectedTags.value = tags
    }

    fun toggleTagFilter(tag: String) {
        val current = _selectedTags.value
        _selectedTags.value = if (current.contains(tag)) current - tag else current + tag
    }

    fun clearTagFilters() {
        _selectedTags.value = emptySet()
    }

    fun setAutoLockMinutes(minutes: Int) {
        _autoLockMinutes.value = minutes
        prefs.edit().putInt("auto_lock_minutes", minutes).apply()
    }

    fun markNoteUnlocked(noteId: Long) {
        _unlockedNoteIds.value = _unlockedNoteIds.value + noteId
    }

    fun lockAllNotes() {
        _unlockedNoteIds.value = emptySet()
    }

    fun getVersionsForNote(noteId: Long): Flow<List<NoteVersion>> =
        noteVersionDao.getVersionsForNote(noteId)

    fun createSmartFolder(
        name: String,
        tagsFilter: String,
        requireTasks: Boolean,
        requireMedia: Boolean,
        dateRange: String
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        viewModelScope.launch {
            smartFolderDao.insertSmartFolder(
                SmartFolder(
                    name = cleanName,
                    tagsFilter = tagsFilter.trim(),
                    requireTasks = requireTasks,
                    requireMedia = requireMedia,
                    dateRange = dateRange
                )
            )
        }
    }

    fun deleteSmartFolder(id: Long) {
        viewModelScope.launch {
            smartFolderDao.deleteSmartFolder(id)
            if (_selectedFolder.value == "smart:$id") {
                _selectedFolder.value = "Todas las notas"
            }
        }
    }

    fun countMatchingSmartFolder(notes: List<Note>, sf: SmartFolder): Int =
        filterBySmartFolder(notes, sf).size

    private fun filterBySmartFolder(notes: List<Note>, sf: SmartFolder): List<Note> {
        val tags = sf.tagsFilter.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        val minTime = when (sf.dateRange) {
            "TODAY" -> startOfDayMillis(0)
            "WEEK" -> startOfDayMillis(-6)
            else -> 0L
        }
        return notes.filter { note ->
            val fullText = "${note.title}\n${note.body}".lowercase()
            val tagsOk = tags.isEmpty() || tags.all { fullText.contains(it.lowercase()) }
            val tasksOk = !sf.requireTasks || note.body.contains("- [ ] ") || note.body.contains("- [x] ")
            val mediaOk = !sf.requireMedia || note.body.contains("[drawing:") || note.body.contains("[image:")
            val dateOk = minTime == 0L || note.updatedAt >= minTime
            tagsOk && tasksOk && mediaOk && dateOk
        }
    }

    private fun startOfDayMillis(offsetDays: Int): Long {
        return java.util.Calendar.getInstance().apply {
            if (offsetDays != 0) add(java.util.Calendar.DAY_OF_YEAR, offsetDays)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun moveNoteToFolder(noteId: Long, targetFolder: String) {
        viewModelScope.launch {
            val existing = allNotes.value.firstOrNull { it.id == noteId } ?: return@launch
            noteDao.updateNote(existing.copy(folder = targetFolder, updatedAt = System.currentTimeMillis()))
        }
    }

    fun duplicateNote(note: Note, onDuplicated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val newTitle = if (note.title.isNotBlank()) "${note.title} (copia)" else "Nota (copia)"
            val newId = saveNoteUseCase.execute(0L, newTitle, note.body, note.folder)
            onDuplicated(newId)
        }
    }

    fun saveNote(
        id: Long,
        title: String,
        body: String,
        folder: String = "Notas",
        onSaved: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val savedId = saveNoteUseCase.execute(id, title, body, folder)
            if (savedId > 0L && (title.isNotBlank() || body.isNotBlank())) {
                val latest = noteVersionDao.getLatestVersionForNote(savedId)
                val changed = latest == null || latest.title != title || latest.body != body
                val enoughTimePassed = latest == null || (System.currentTimeMillis() - latest.createdAt) >= 4_000L
                if (changed && enoughTimePassed) {
                    noteVersionDao.insertVersion(
                        NoteVersion(
                            noteId = savedId,
                            title = title,
                            body = body
                        )
                    )
                    noteVersionDao.trimOldVersions(savedId, 25)
                }
            }
            onSaved(savedId)
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            pinNoteUseCase.execute(note.id, !note.isPinned)
        }
    }

    fun moveToTrash(noteId: Long) {
        viewModelScope.launch {
            moveToTrashUseCase.execute(noteId)
        }
    }

    fun restoreFromTrash(noteId: Long) {
        viewModelScope.launch {
            restoreFromTrashUseCase.execute(noteId)
        }
    }

    fun deletePermanently(noteId: Long) {
        viewModelScope.launch {
            deletePermanentlyUseCase.execute(noteId)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            deletePermanentlyUseCase.emptyTrash()
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            createFolderUseCase.execute(name)
        }
    }

    fun renameFolder(folderId: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            folderDao.renameFolder(folderId, trimmed)
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            deleteFolderUseCase.execute(folder)
            if (_selectedFolder.value == folder.name) {
                _selectedFolder.value = "Todas las notas"
            }
        }
    }

    fun exportBackup(
        context: android.content.Context,
        uri: android.net.Uri,
        password: String? = null,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val ok = com.itsbatt.inotes.data.ExportAndBackupManager.exportBackupZip(
                context = context,
                destUri = uri,
                notes = allNotes.value + deletedNotes.value,
                folders = allFolders.value,
                password = password
            )
            onResult(ok)
        }
    }

    fun importBackup(
        context: android.content.Context,
        uri: android.net.Uri,
        password: String? = null,
        onResult: (Int) -> Unit
    ) {
        viewModelScope.launch {
            val count = com.itsbatt.inotes.data.ExportAndBackupManager.importBackupZip(
                context = context,
                srcUri = uri,
                noteDao = noteDao,
                folderDao = folderDao,
                password = password
            )
            onResult(count)
        }
    }
}
