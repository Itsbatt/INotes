package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.entities.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchNotesUseCase(private val noteDao: NoteDao) {

    fun execute(query: String): Flow<List<Note>> {
        return flow(noteDao.searchNotes(query))
    }
}