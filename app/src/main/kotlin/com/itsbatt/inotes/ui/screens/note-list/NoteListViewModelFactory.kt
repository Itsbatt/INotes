package com.itsbatt.inotes.ui.screens.note-list

import androidx.lifecycle.ViewModelProvider
import com.itsbatt.inotes.ui.MainViewModel

class NoteListViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            MainViewModel(
                getAllNotesUseCase = com.itsbatt.inotes.domain.usecases.GetAllNotesUseCase(
                    com.itsbatt.inotes.data.room.database.AppDatabase
                        .getInstance(??: throw IllegalStateException())
                        .noteDao()
                ),
                pinNoteUseCase = com.itsbatt.inotes.domain.usecases.PinNoteUseCase(
                    com.itsbatt.inotes.data.room.dao.NoteDao
                        .getInstance(??: throw IllegalStateException())
                ),
                searchNotesUseCase = com.itsbatt.inotes.domain.usecases.SearchNotesUseCase(
                    com.itsbatt.inotes.data.room.dao.NoteDao
                        .getInstance(??: throw IllegalStateException())
                ),
                folderDao = com.itsbatt.inotes.data.room.dao.FolderDao
                    .getInstance(??: throw IllegalStateException())
                    .folderDao()
            ) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}