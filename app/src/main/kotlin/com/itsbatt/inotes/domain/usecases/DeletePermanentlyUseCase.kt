package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.dao.NoteDao

class DeletePermanentlyUseCase(private val noteDao: NoteDao) {
    suspend fun execute(noteId: Long) {
        noteDao.deleteNoteById(noteId)
    }

    suspend fun emptyTrash() {
        noteDao.emptyTrash()
    }
}
