package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.dao.NoteDao
import com.itsbatt.inotes.data.room.entities.Note

class SaveNoteUseCase(private val noteDao: NoteDao) {
    suspend fun execute(
        id: Long,
        title: String,
        body: String,
        folder: String = "Notas"
    ): Long {
        val now = System.currentTimeMillis()
        val targetFolder = if (folder == "Todas las notas" || folder == "Papelera" || folder == "Todo lo de iCloud") "Notas" else folder
        return if (id == 0L) {
            val newNote = Note(
                title = title,
                body = body,
                createdAt = now,
                updatedAt = now,
                folder = targetFolder
            )
            noteDao.insertNote(newNote)
        } else {
            val existing = noteDao.getNoteById(id)
            val updated = existing?.copy(
                title = title,
                body = body,
                updatedAt = now,
                folder = targetFolder
            ) ?: Note(
                id = id,
                title = title,
                body = body,
                createdAt = now,
                updatedAt = now,
                folder = targetFolder
            )
            noteDao.insertNote(updated)
            id
        }
    }
}
