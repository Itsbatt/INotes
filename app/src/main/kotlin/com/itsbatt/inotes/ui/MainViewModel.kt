package com.itsbatt.inotes.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.itsbatt.inotes.INotesApplication
import com.itsbatt.inotes.data.room.entities.Folder
import com.itsbatt.inotes.data.room.entities.Note
import com.itsbatt.inotes.sync.GoogleAccountInfo
import com.itsbatt.inotes.sync.GoogleSyncManager
import com.itsbatt.inotes.sync.SyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val db = (application as INotesApplication).database
    private val noteDao = db.noteDao()
    private val folderDao = db.folderDao()

    val googleSyncManager = GoogleSyncManager(application)
    val googleAccount: StateFlow<GoogleAccountInfo?> = googleSyncManager.accountInfo
    val syncStatus: StateFlow<SyncStatus> = googleSyncManager.syncStatus
    val lastSyncTime: StateFlow<Long> = googleSyncManager.lastSyncTime

    val allFolders: StateFlow<List<Folder>> = folderDao.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<Note>> = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedNotes: StateFlow<List<Note>> = noteDao.getDeletedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedFolder = MutableStateFlow("Todo lo de Google")
    val selectedFolder: StateFlow<String> = _selectedFolder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isTrashMode = MutableStateFlow(false)
    val isTrashMode: StateFlow<Boolean> = _isTrashMode.asStateFlow()

    // Grid vs List view toggle (Screenshot 5)
    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    // Selection mode (Screenshot 2)
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedNoteIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedNoteIds: StateFlow<Set<Long>> = _selectedNoteIds.asStateFlow()

    // Tag filter (Screenshot 8)
    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    // Extract dynamic tags from notes
    val allTags: StateFlow<List<String>> = allNotes.combine(_selectedFolder) { notes, _ ->
        val tagsSet = mutableSetOf<String>()
        val tagRegex = Regex("#[\\p{L}0-9_]+")
        notes.forEach { note ->
            tagRegex.findAll(note.title + " " + note.body).forEach { match ->
                tagsSet.add(match.value)
            }
        }
        if (tagsSet.isEmpty()) {
            listOf("#Compras", "#Jardinería", "#Proyectos", "#Ideas")
        } else {
            tagsSet.toList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("#Compras", "#Jardinería", "#Proyectos", "#Ideas"))

    val displayedNotes: StateFlow<List<Note>> = combine(
        allNotes,
        deletedNotes,
        _selectedFolder,
        _searchQuery,
        _isTrashMode
    ) { notes, trashNotes, folder, query, inTrash ->
        val sourceList = if (inTrash) trashNotes else notes
        val filteredByFolder = if (inTrash || folder == "Todo lo de Google" || folder == "Todas las notas") {
            sourceList
        } else if (folder == "Notas rápidas") {
            sourceList.take(5)
        } else {
            sourceList.filter { it.folder.equals(folder, ignoreCase = true) }
        }

        val tag = _selectedTag.value
        val filteredByTag = if (tag != null) {
            filteredByFolder.filter { (it.title + " " + it.body).contains(tag, ignoreCase = true) }
        } else {
            filteredByFolder
        }

        if (query.isBlank()) {
            filteredByTag
        } else {
            val q = query.trim().lowercase()
            filteredByTag.filter {
                it.title.lowercase().contains(q) || it.body.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Seed default iOS/Google folders if empty
            val defaultFolders = listOf("Notas", "Ideas de regalos", "Lista de compras", "Renovación de hogar 🛠️", "Garabatos")
            defaultFolders.forEach { name ->
                if (folderDao.getFolderByName(name) == null) {
                    folderDao.insertFolder(Folder(name = name))
                }
            }

            // Seed sample notes with checklists and tags if empty
            if (noteDao.getAllNotesRaw().isEmpty()) {
                val now = System.currentTimeMillis()
                val sampleNotes = listOf(
                    Note(
                        title = "Viaje anual de senderismo con papá",
                        body = "No olvidar de empacar lo siguiente:\n[ ] ☀️ Protección solar\n[ ] 🥾 Botas\n[ ] 🧭 Brújula\n[ ] 🍪 Comida\n[ ] 🗺️ Mapa\n[ ] 🔦 Linterna\n[ ] ⛑️ Kit de primeros auxilios\n[ ] 💧 Botella de agua\n[ ] 🧼 Desinfectante de manos\n[ ] 😴 Bolsa de dormir",
                        createdAt = now,
                        updatedAt = now,
                        folder = "Notas",
                        isPinned = true,
                        isChecklist = true
                    ),
                    Note(
                        title = "Paisajismo",
                        body = "9:20 a.m. Buscar baldosas para el pórtico trasero #Jardinería #Árboles\n[ ] Esquinas\n[ ] ¿Baldosas?\n[ ] Herramientas",
                        createdAt = now - 15 * 60 * 1000,
                        updatedAt = now - 15 * 60 * 1000,
                        folder = "Notas",
                        isPinned = true
                    ),
                    Note(
                        title = "Lista de compras",
                        body = "[x] Huevos\n[x] Pan\n[ ] Leche\n[ ] Refrescos\n[ ] Tomates #Compras",
                        createdAt = now - 24 * 3600 * 1000,
                        updatedAt = now - 24 * 3600 * 1000,
                        folder = "Lista de compras",
                        isChecklist = true
                    ),
                    Note(
                        title = "Proyecto de renovación de hogar",
                        body = "Duración del proyecto: 4 meses #Proyectos #Jardinería\nInicio: Enero\nComenzar después de año nuevo.",
                        createdAt = now - 26 * 3600 * 1000,
                        updatedAt = now - 26 * 3600 * 1000,
                        folder = "Renovación de hogar 🛠️"
                    ),
                    Note(
                        title = "Ideas de remodelación para la cocina",
                        body = "El inspector vendrá la próxima semana para revisar la parte eléctrica.",
                        createdAt = now - 30 * 3600 * 1000,
                        updatedAt = now - 30 * 3600 * 1000,
                        folder = "Ideas de regalos"
                    )
                )
                sampleNotes.forEach { noteDao.insertNote(it) }
            }
        }
    }

    fun selectFolder(folderName: String) {
        _isTrashMode.value = false
        _selectedTag.value = null
        _selectedFolder.value = folderName
        clearSelection()
    }

    fun selectTag(tag: String?) {
        _selectedTag.value = tag
    }

    fun openTrash() {
        _isTrashMode.value = true
        _selectedFolder.value = "Papelera"
        clearSelection()
    }

    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Selection mode actions
    fun setSelectionMode(enabled: Boolean) {
        _isSelectionMode.value = enabled
        if (!enabled) {
            _selectedNoteIds.value = emptySet()
        }
    }

    fun toggleSelectNote(noteId: Long) {
        val current = _selectedNoteIds.value.toMutableSet()
        if (current.contains(noteId)) {
            current.remove(noteId)
        } else {
            current.add(noteId)
        }
        _selectedNoteIds.value = current
    }

    fun selectAllNotes() {
        _selectedNoteIds.value = displayedNotes.value.map { it.id }.toSet()
    }

    fun clearSelection() {
        _isSelectionMode.value = false
        _selectedNoteIds.value = emptySet()
    }

    fun deleteSelectedNotes() {
        val ids = _selectedNoteIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            if (_isTrashMode.value) {
                noteDao.deleteNotesByIds(ids)
            } else {
                noteDao.moveToTrashBatch(ids)
            }
            clearSelection()
            triggerAutoSync()
        }
    }

    fun moveSelectedNotesToFolder(targetFolder: String) {
        val ids = _selectedNoteIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            noteDao.moveNotesToFolder(ids, targetFolder)
            clearSelection()
            triggerAutoSync()
        }
    }

    fun saveNote(
        id: Long,
        title: String,
        body: String,
        folder: String = "Notas",
        isChecklist: Boolean = false,
        onSaved: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val targetFolder = if (folder == "Todo lo de Google" || folder == "Todas las notas" || folder == "Papelera") "Notas" else folder
            val now = System.currentTimeMillis()
            if (id == 0L) {
                val newNote = Note(
                    title = title,
                    body = body,
                    createdAt = now,
                    updatedAt = now,
                    folder = targetFolder,
                    isChecklist = isChecklist
                )
                val newId = noteDao.insertNote(newNote)
                onSaved(newId)
            } else {
                val existing = noteDao.getNoteById(id)
                val updatedNote = existing?.copy(
                    title = title,
                    body = body,
                    updatedAt = now,
                    folder = targetFolder,
                    isChecklist = isChecklist
                ) ?: Note(
                    id = id,
                    title = title,
                    body = body,
                    createdAt = now,
                    updatedAt = now,
                    folder = targetFolder,
                    isChecklist = isChecklist
                )
                noteDao.insertNote(updatedNote)
                onSaved(id)
            }
            triggerAutoSync()
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            noteDao.setPin(note.id, !note.isPinned)
            triggerAutoSync()
        }
    }

    fun moveToTrash(noteId: Long) {
        viewModelScope.launch {
            noteDao.moveToTrash(noteId)
            triggerAutoSync()
        }
    }

    fun restoreFromTrash(noteId: Long) {
        viewModelScope.launch {
            noteDao.restoreFromTrash(noteId)
            triggerAutoSync()
        }
    }

    fun deletePermanently(noteId: Long) {
        viewModelScope.launch {
            noteDao.deleteNoteById(noteId)
            triggerAutoSync()
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            noteDao.emptyTrash()
            triggerAutoSync()
        }
    }

    fun createFolder(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            if (folderDao.getFolderByName(trimmed) == null) {
                folderDao.insertFolder(Folder(name = trimmed))
            }
            triggerAutoSync()
        }
    }

    fun renameFolder(folderId: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            folderDao.renameFolder(folderId, trimmed)
            triggerAutoSync()
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            folderDao.deleteFolder(folder)
            if (_selectedFolder.value == folder.name) {
                _selectedFolder.value = "Todo lo de Google"
            }
            triggerAutoSync()
        }
    }

    // Google Sync calls
    fun syncWithGoogle() {
        viewModelScope.launch {
            googleSyncManager.syncWithGoogle(db)
        }
    }

    fun restoreFromGoogle() {
        viewModelScope.launch {
            googleSyncManager.restoreFromGoogle(db)
        }
    }

    private fun triggerAutoSync() {
        if (googleAccount.value != null) {
            viewModelScope.launch {
                googleSyncManager.syncWithGoogle(db)
            }
        }
    }
}
