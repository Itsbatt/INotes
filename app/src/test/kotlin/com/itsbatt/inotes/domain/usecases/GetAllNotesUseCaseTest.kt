package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.dao.NoteDao
import com.itsbatt.inotes.data.room.entities.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAllNotesUseCaseTest {

    @Test
    fun executeReturnsAllNotes() = runTest {
        val testNotes = listOf(
            Note(id = 1, title = "Nota 1", body = "Texto 1"),
            Note(id = 2, title = "Nota 2", body = "Texto 2")
        )

        val fakeDao = object : NoteDao {
            override suspend fun insertNote(note: Note): Long = 1L
            override suspend fun updateNote(note: Note) {}
            override suspend fun deleteNote(note: Note) {}
            override fun getAllNotes(): Flow<List<Note>> = flowOf(testNotes)
            override suspend fun getAllNotesList(): List<Note> = testNotes
            override fun getPinnedNotes(): Flow<List<Note>> = flowOf(emptyList())
            override fun getNotesByFolder(folderName: String): Flow<List<Note>> = flowOf(testNotes)
            override fun searchNotes(query: String): Flow<List<Note>> = flowOf(emptyList())
            override suspend fun searchNotesList(query: String): List<Note> = emptyList()
            override suspend fun getNoteById(noteId: Long): Note? = null
            override suspend fun deleteNoteById(noteId: Long) {}
            override suspend fun setPin(noteId: Long, pinned: Boolean) {}
            override suspend fun moveToTrash(noteId: Long, timestamp: Long) {}
            override suspend fun restoreFromTrash(noteId: Long) {}
            override fun getDeletedNotes(): Flow<List<Note>> = flowOf(emptyList())
            override suspend fun emptyTrash() {}
            override suspend fun purgeExpiredTrash(thresholdMillis: Long) {}
            override suspend fun reassignFolderNotes(oldFolder: String, defaultFolder: String) {}
        }

        val useCase = GetAllNotesUseCase(fakeDao)
        val result = useCase.execute().first()

        assertEquals(2, result.size)
        assertEquals("Nota 1", result[0].title)
        assertEquals("Nota 2", result[1].title)
    }
}
