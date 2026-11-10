package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.entities.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PinNoteUseCase(private val noteDao: NoteDao) {

    fun execute(noteId: Int, pinned: Boolean): Flow<Note> {
        noteDao.pinNote(noteId, pinned)
        return flow(noteDao.getAllNotes()) // Simplified - in real app would return specific note
    }
}