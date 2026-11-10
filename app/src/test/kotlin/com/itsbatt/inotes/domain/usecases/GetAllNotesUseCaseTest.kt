package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.entities.Note
import com.itsbatt.inotes.data.room.database.AppDatabase
import com.itsbatt.inotes.data.room.dao.NoteDao
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.ArgumentMatchers
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class GetAllNotesUseCaseTest {

    @Test
    fun `execute returns all notes from database`() {
        // Set up Room in-memory database
        val db = Room.inMemoryDatabaseBuilder(
            /* application context */ null,
            AppDatabase::class.java
        ).build()
        
        try {
            val noteDao = db.noteDao()
            
            // Add test notes
            runBlocking {
                noteDao.insertNote(Note(title = "Test Note 1", body = "Body 1"))
                noteDao.insertNote(Note(title = "Test Note 2", body = "Body 2"))
            }
            
            // Execute use case
            val useCase = GetAllNotesUseCase(noteDao)
            val notes = useCase.execute().firstOrValue()
            
            // Verify
            assert(notes.size == 2) { "Expected 2 notes, got ${notes?.size}" }
        } finally {
            db.close()
        }
    }
}