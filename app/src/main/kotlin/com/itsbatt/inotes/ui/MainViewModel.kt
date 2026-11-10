package com.itsbatt.inotes.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itsbatt.inotes.data.room.entities.Note
import com.itsbatt.inotes.data.room.database.AppDatabase
import com.itsbatt.inotes.domain.usecases.GetAllNotesUseCase
import com.itsbatt.inotes.domain.usecases.PinNoteUseCase
import com.itsbatt.inotes.domain.usecases.SearchNotesUseCase
import com.itsbatt.inotes.data.room.dao.FolderDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.takeUntil
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : ViewModel() {

    private val appDatabase = AppDatabase.getInstance(application)

    // Use cases with internal database reference
    private val noteDao = appDatabase.noteDao()
    private val folderDao = appDatabase.folderDao()

    private val _getAllNotesUseCase = GetAllNotesUseCase(noteDao)
    private val _pinNoteUseCase = PinNoteUseCase(noteDao)
    private val _searchNotesUseCase = SearchNotesUseCase(noteDao)
    private val _folderDao = FolderDao(folderDao)

    // Notes state - grouped by date
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Folders state
    private val _folders = MutableStateFlow<List<String>>(listOf("Notas"))
    val folders: StateFlow<List<String>> = _folders.asStateFlow()

    // Selected folder
    private val _selectedFolder = MutableStateFlow<String>("Notas")
    val selectedFolder: StateFlow<String> = _selectedFolder.asStateFlow()

    // Pinned notes
    private val _pinnedNotes = MutableStateFlow<List<Note>>(emptyList())
    val pinnedNotes: StateFlow<List<Note>> = _pinnedNotes.asStateFlow()

    // UI actions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedFolder(folder: String) {
        _selectedFolder.value = folder
        loadNotesForFolder(folder)
    }

    fun togglePin(noteId: Int) {
        _pinNoteUseCase.execute(noteId, !isNotePinned(noteId))
        loadNotes()
    }

    fun createNote(title: String, body: String) {
        val note = Note(title = title, body = body)
        noteDao.insertNote(note)
        loadNotes()
    }

    fun deleteNote(noteId: Int) {
        // Move to trash - set folder to "Trash" or mark as deleted
        val note = _notes.value?.firstOrDefault { it.id == noteId }
        note?.let { n ->
            n.folder = "Notas"  // Move to Notes (not trash, but reset)
            noteDao.insertNote(n)  // This replaces due to REPLACE strategy
        }
        loadNotes()
    }

    fun restoreFromTrash(noteId: Int) {
        // Restore from "trash" - basically just reload
        loadNotes()
    }

    private fun loadNotes() {
        val allNotes = _getAllNotesUseCase.execute()
        _notes.value = allNotes
        loadPinnedNotes()
        updateFolderLabels(allNotes)
    }

    private fun loadPinnedNotes() {
        val pinned = _notes.value?.filter { it.isPinned } ?: emptyList()
        _pinnedNotes.value = pinned
    }

    private fun updateFolderLabels(notes: List<Note>) {
        // Update folder labels based on notes
        val folderNames = notes.map { it.folder }.toSet()
        var updatedFolders = folderNames.toList()
        if (!updatedFolders.contains("Notas")) {
            updatedFolders = "Notas" :: updatedFolders
        }
        _folders.value = updatedFolders
    }

    fun addFolder(name: String) {
        // Add folder to state (database fold Dao would also need insert)
        _folders.value = _folders.value + name
    }

    fun renameFolder(oldName: String, newName: String) {
        _folders.value = _folders.value.map {
            if (it == oldName) newName else it
        }
    }

    fun removeFolder(name: String) {
        // Move notes from this folder to "Notas"
        _folders.value = _folders.value.filter { it != name }
        // Update notes folder label
        val currentNotes = _notes.value ?: emptyList()
        val updatedNotes = currentNotes.map { note ->
            if (note.folder == name) note.copy(folder = "Notas") else note
        }
        _notes.value = updatedNotes
    }

    private fun isNotePinned(noteId: Int): Boolean {
        return _notes.value?.any { it.id == noteId && it.isPinned } == true
    }
}