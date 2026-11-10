package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.dao.NoteDao
import com.itsbatt.inotes.data.room.entities.Note
import kotlinx.coroutines.flow.Flow

class GetAllNotesUseCase(private val noteDao: NoteDao) {
    fun execute(folder: String = "Todas"): Flow<List<Note>> {
        return if (folder == "Todas" || folder == "Todo lo de iCloud" || folder == "Todas las notas") {
            noteDao.getAllNotes()
        } else {
            noteDao.getNotesByFolder(folder)
        }
    }
}
