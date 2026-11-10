package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.dao.NoteDao
import com.itsbatt.inotes.data.room.entities.Note
import kotlinx.coroutines.flow.Flow

class SearchNotesUseCase(private val noteDao: NoteDao) {
    fun execute(query: String): Flow<List<Note>> = noteDao.searchNotes(query)
}
