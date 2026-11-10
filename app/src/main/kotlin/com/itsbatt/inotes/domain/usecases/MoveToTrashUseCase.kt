package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.dao.NoteDao

class MoveToTrashUseCase(private val noteDao: NoteDao) {
    suspend fun execute(noteId: Long) {
        noteDao.moveToTrash(noteId)
    }
}
