package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.entities.Note
import com.itsbatt.inotes.data.room.database.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PinNoteUseCaseTest {

    @Test
    fun `togglePin updates note pinned status`() {
        val db = Room.inMemoryDatabaseBuilder(
            /* application context */ null,
            AppDatabase::class.java
        ).build()
        
        try {
            val noteDao = db.noteDao()
            
            runBlocking {
                noteDao.insertNote(Note(title = "Test Note", body = "Body", isPinned = false))
            }
            
            val useCase = PinNoteUseCase(noteDao)
            
            // Toggle pin
            runBlocking {
                useCase.execute(1, true)
            }
            
            // Verify the note is now pinned
            val notes = runBlocking {
                noteDao.getAllNotes()
            }
            
            assert(notes.size == 1) { "Expected 1 note" }
            assert(notes[0].isPinned == true) { "Expected note to be pinned" }
        } finally {
            db.close()
        }
    }
}