package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.dao.NoteDao
import com.itsbatt.inotes.data.room.entities.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrashUseCaseTest {

    @Test
    fun testMoveToTrashAndRestore() = runTest {
        var trashedId: Long? = null
        var restoredId: Long? = null
        var emptied = false

        val fakeDao = object : NoteDao {
            override suspend fun insertNote(note: Note): Long = 1L
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
            override suspend fun setPin(noteId: Long, pinned: Boolean) {}
            override suspend fun moveToTrash(noteId: Long, timestamp: Long) {
                trashedId = noteId
            }
            override suspend fun restoreFromTrash(noteId: Long) {
                restoredId = noteId
            }
            override fun getDeletedNotes(): Flow<List<Note>> = flowOf(emptyList())
            override suspend fun emptyTrash() {
                emptied = true
            }
            override suspend fun purgeExpiredTrash(thresholdMillis: Long) {}
            override suspend fun reassignFolderNotes(oldFolder: String, defaultFolder: String) {}
        }

        val moveUseCase = MoveToTrashUseCase(fakeDao)
        val restoreUseCase = RestoreFromTrashUseCase(fakeDao)
        val deleteUseCase = DeletePermanentlyUseCase(fakeDao)

        moveUseCase.execute(10L)
        assertEquals(10L, trashedId)

        restoreUseCase.execute(10L)
        assertEquals(10L, restoredId)

        deleteUseCase.emptyTrash()
        assertTrue(emptied)
    }
}
