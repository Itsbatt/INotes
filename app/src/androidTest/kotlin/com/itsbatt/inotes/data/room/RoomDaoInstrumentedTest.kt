package com.itsbatt.inotes.data.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.itsbatt.inotes.data.room.database.AppDatabase
import com.itsbatt.inotes.data.room.entities.Folder
import com.itsbatt.inotes.data.room.entities.Note
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDaoInstrumentedTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetNote() = runBlocking {
        val note = Note(title = "Comprar pan", body = "Baguette crujiente", folder = "Notas")
        val id = db.noteDao().insertNote(note)
        assertTrue(id > 0)

        val retrieved = db.noteDao().getNoteById(id)
        assertNotNull(retrieved)
        assertEquals("Comprar pan", retrieved?.title)
        assertEquals("Baguette crujiente", retrieved?.body)
    }

    @Test
    fun testPinNote() = runBlocking {
        val note = Note(title = "Nota importante", body = "Contenido", isPinned = false)
        val id = db.noteDao().insertNote(note)

        db.noteDao().setPin(id, true)
        val updated = db.noteDao().getNoteById(id)
        assertTrue(updated?.isPinned == true)
    }

    @Test
    fun testTrashAndRestore() = runBlocking {
        val note = Note(title = "Para borrar", body = "Temporal")
        val id = db.noteDao().insertNote(note)

        // Move to trash
        db.noteDao().moveToTrash(id)
        val trashed = db.noteDao().getNoteById(id)
        assertTrue(trashed?.isDeleted == true)

        // Does not show in active notes
        val activeNotes = db.noteDao().getAllNotes().first()
        assertFalse(activeNotes.any { it.id == id })

        // Shows in deleted notes
        val deletedNotes = db.noteDao().getDeletedNotes().first()
        assertTrue(deletedNotes.any { it.id == id })

        // Restore
        db.noteDao().restoreFromTrash(id)
        val restored = db.noteDao().getNoteById(id)
        assertFalse(restored?.isDeleted == true)
    }

    @Test
    fun testFolderDeletionReassignsNotes() = runBlocking {
        val folder = Folder(name = "Personal")
        val folderId = db.folderDao().insertFolder(folder)

        val note = Note(title = "Nota Personal", body = "Secreto", folder = "Personal")
        val noteId = db.noteDao().insertNote(note)

        // Delete folder using reassignFolderNotes
        db.noteDao().reassignFolderNotes("Personal", "Notas")
        db.folderDao().deleteFolderById(folderId)

        val updatedNote = db.noteDao().getNoteById(noteId)
        assertEquals("Notas", updatedNote?.folder)
    }
}
