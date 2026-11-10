package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.entities.Note
import com.itsbatt.inotes.data.room.dao.NoteDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetAllNotesUseCase(private val noteDao: NoteDao) {

    fun execute(): Flow<List<Note>> {
        return flow(noteDao.getAllNotes())
    }
}