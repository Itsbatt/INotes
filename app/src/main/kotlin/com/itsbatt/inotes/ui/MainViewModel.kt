package com.itsbatt.inotes.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.itsbatt.inotes.INotesApplication
import com.itsbatt.inotes.data.room.entities.Folder
import com.itsbatt.inotes.data.room.entities.Note
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
        val filteredByFolder = if (inTrash || folder == "Todas las notas" || folder == "Todo lo de iCloud") {
            sourceList
        } else if (folder == "Notas rápidas") {
            sourceList.take(5)
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

    fun saveNote(
        id: Long,
        title: String,
        body: String,
        folder: String = "Notas",
        onSaved: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val savedId = saveNoteUseCase.execute(id, title, body, folder)
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
}
