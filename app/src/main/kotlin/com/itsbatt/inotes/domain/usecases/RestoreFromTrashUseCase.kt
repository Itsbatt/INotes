package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.dao.NoteDao

class RestoreFromTrashUseCase(private val noteDao: NoteDao) {
    suspend fun execute(noteId: Long) {
        noteDao.restoreFromTrash(noteId)
    }
}
