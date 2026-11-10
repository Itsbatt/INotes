package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.entities.Note
import com.itsbatt.inotes.data.room.database.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class SearchNotesUseCaseTest {

    @Test
    fun `execute filters notes by search query`() {
        val db = Room.inMemoryDatabaseBuilder(
            /* application context */ null,
            AppDatabase::class.java
        ).build()
        
        try {
            val noteDao = db.noteDao()
            
            runBlocking {
                noteDao.insertNote(Note(title = "Meeting", body = "Project discussion"))
                noteDao.insertNote(Note(title = "Shopping", body = "Milk and eggs"))
                noteDao.insertNote(Note(title = "Meeting follow-up", body = "Call client"))
            }
            
            val useCase = SearchNotesUseCase(noteDao)
            
            // Search for "meeting"
            val results = runBlocking {
                useCase.execute("meeting").firstOrValue()
            }
            
            // Should find 2 notes with "meeting" in title
            assert(results?.size == 2) { "Expected 2 results, got ${results?.size}" }
            
            // Verify titles contain "meeting"
            results?.forEach { note ->
                assert(note.title.contains("meeting", ignoreCase = true)) {
                    "Note title should contain 'meeting': ${note.title}"
                }
            }
        } finally {
            db.close()
        }
    }
}