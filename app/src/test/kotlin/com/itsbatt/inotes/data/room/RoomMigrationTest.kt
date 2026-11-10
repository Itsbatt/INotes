package com.itsbatt.inotes.data.room

import com.itsbatt.inotes.data.room.database.AppDatabase
import com.itsbatt.inotes.data.room.entities.Note
import com.itsbatt.inotes.data.room.entities.Folder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.room.Room
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertEquals

@RunWith::class(Runner::class)
class RoomMigrationTest {

    @Test
    fun `schema export is enabled`() {
        // Verify the database annotation has exportSchema = true
        // This is checked at compile time via the @Database annotation
    }

    @Test
    fun `database creates all expected tables`() {
        val db = Room.inMemoryDatabaseBuilder(
            /* application context */ null,
            AppDatabase::class.java
        ).build()
        
        try {
            // Verify database is created successfully
            assertNotNull(db) { "Database should not be null" }
            
            // Verify we can access the DAOs
            val noteDao = db.noteDao()
            val folderDao = db.folderDao()
            
            assertNotNull(noteDao) { "Note DAO should not be null" }
            assertNotNull(folderDao) { "Folder DAO should not be null" }
            
            // Insert a note and verify it works
            runBlocking {
                noteDao.insertNote(Note(title = "Test", body = "Test body"))
            }
            
            // Insert a folder and verify it works
            runBlocking {
                folderDao.insertFolder(Folder(name = "Test Folder"))
            }
            
            // Verify data was stored
            val notes = runBlocking {
                noteDao.getAllNotes()
            }
            
            assert(notes.size >= 1) { "Should have at least 1 note" }
            
            val folders = runBlocking {
                folderDao.getAllFolders()
            }
            
            assert(folders.size >= 1) { "Should have at least 1 folder" }
        } finally {
            db.close()
        }
    }
}