package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.dao.NoteDao
import com.itsbatt.inotes.data.room.entities.Note
import kotlinx.coroutines.flow.Flow

class GetAllNotesUseCase(private val noteDao: NoteDao) {
    fun execute(): Flow<List<Note>> = noteDao.getAllNotes()
}
