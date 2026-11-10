package com.itsbatt.inotes.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.itsbatt.inotes.INotesApplication
import com.itsbatt.inotes.data.room.entities.Folder
import com.itsbatt.inotes.data.room.entities.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = (application as INotesApplication).database
    private val noteDao = db.noteDao()
    private val folderDao = db.folderDao()

    val allFolders: StateFlow<List<Folder>> = folderDao.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<Note>> = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedNotes: StateFlow<List<Note>> = noteDao.getDeletedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedFolder = MutableStateFlow("Todas las notas")
    val selectedFolder: StateFlow<String> = _selectedFolder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isTrashMode = MutableStateFlow(false)
    val isTrashMode: StateFlow<Boolean> = _isTrashMode.asStateFlow()

    val displayedNotes: StateFlow<List<Note>> = combine(
        allNotes,
        deletedNotes,
        _selectedFolder,
        _searchQuery,
        _isTrashMode
    ) { notes, trashNotes, folder, query, inTrash ->
        val sourceList = if (inTrash) trashNotes else notes
        val filteredByFolder = if (inTrash || folder == "Todas las notas") {
            sourceList
        } else {
            sourceList.filter { it.folder.equals(folder, ignoreCase = true) }
        }

        if (query.isBlank()) {
            filteredByFolder
        } else {
            val q = query.trim().lowercase()
            filteredByFolder.filter {
                it.title.lowercase().contains(q) || it.body.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Seed default folder if empty
            val defaultName = "Notas"
            if (folderDao.getFolderByName(defaultName) == null) {
                folderDao.insertFolder(Folder(name = defaultName))
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

    fun saveNote(id: Long, title: String, body: String, folder: String = "Notas", onSaved: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val targetFolder = if (folder == "Todas las notas" || folder == "Papelera") "Notas" else folder
            val now = System.currentTimeMillis()
            if (id == 0L) {
                val newNote = Note(
                    title = title,
                    body = body,
                    createdAt = now,
                    updatedAt = now,
                    folder = targetFolder
                )
                val newId = noteDao.insertNote(newNote)
                onSaved(newId)
            } else {
                val existing = noteDao.getNoteById(id)
                val updatedNote = existing?.copy(
                    title = title,
                    body = body,
                    updatedAt = now,
                    folder = targetFolder
                ) ?: Note(
                    id = id,
                    title = title,
                    body = body,
                    createdAt = now,
                    updatedAt = now,
                    folder = targetFolder
                )
                noteDao.insertNote(updatedNote)
                onSaved(id)
            }
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            noteDao.setPin(note.id, !note.isPinned)
        }
    }

    fun moveToTrash(noteId: Long) {
        viewModelScope.launch {
            noteDao.moveToTrash(noteId)
        }
    }

    fun restoreFromTrash(noteId: Long) {
        viewModelScope.launch {
            noteDao.restoreFromTrash(noteId)
        }
    }

    fun deletePermanently(noteId: Long) {
        viewModelScope.launch {
            noteDao.deleteNoteById(noteId)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            noteDao.emptyTrash()
        }
    }

    fun createFolder(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            if (folderDao.getFolderByName(trimmed) == null) {
                folderDao.insertFolder(Folder(name = trimmed))
            }
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
            folderDao.deleteFolder(folder)
            if (_selectedFolder.value == folder.name) {
                _selectedFolder.value = "Todas las notas"
            }
        }
    }
}
