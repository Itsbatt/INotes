package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.dao.NoteDao

class PinNoteUseCase(private val noteDao: NoteDao) {
    suspend fun execute(noteId: Long, pinned: Boolean) {
        noteDao.setPin(noteId, pinned)
    }
}
