package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.dao.NoteDao
import com.itsbatt.inotes.data.room.entities.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PinNoteUseCaseTest {

    @Test
    fun executeTogglesPin() = runTest {
        var pinnedId: Long? = null
        var pinnedState: Boolean? = null

        val fakeDao = object : NoteDao {
            override suspend fun insertNote(note: Note): Long = 1L
            override suspend fun insertNotes(notes: List<Note>): List<Long> = emptyList()
            override suspend fun updateNote(note: Note) {}
            override suspend fun deleteNote(note: Note) {}
            override fun getAllNotes(): Flow<List<Note>> = flowOf(emptyList())
            override suspend fun getAllNotesList(): List<Note> = emptyList()
            override fun getPinnedNotes(): Flow<List<Note>> = flowOf(emptyList())
            override fun getNotesByFolder(folderName: String): Flow<List<Note>> = flowOf(emptyList())
            override fun searchNotes(query: String): Flow<List<Note>> = flowOf(emptyList())
            override suspend fun searchNotesList(query: String): List<Note> = emptyList()
            override suspend fun getNoteById(noteId: Long): Note? = null
            override suspend fun deleteNoteById(noteId: Long) {}
            override suspend fun deleteNotesByIds(noteIds: List<Long>) {}
            override suspend fun setPin(noteId: Long, pinned: Boolean) {
                pinnedId = noteId
                pinnedState = pinned
            }
            override suspend fun moveToTrash(noteId: Long, timestamp: Long) {}
            override suspend fun moveToTrashBatch(noteIds: List<Long>, timestamp: Long) {}
            override suspend fun restoreFromTrash(noteId: Long) {}
            override suspend fun moveNotesToFolder(noteIds: List<Long>, targetFolder: String) {}
            override fun getDeletedNotes(): Flow<List<Note>> = flowOf(emptyList())
            override suspend fun emptyTrash() {}
            override suspend fun getAllNotesRaw(): List<Note> = emptyList()
        }

        val useCase = PinNoteUseCase(fakeDao)
        useCase.execute(42L, true)

        assertEquals(42L, pinnedId)
        assertEquals(true, pinnedState)
    }
}
