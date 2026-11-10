package com.itsbatt.inotes.data.room

import com.itsbatt.inotes.data.room.entities.Folder
import com.itsbatt.inotes.data.room.entities.Note
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomEntityTest {

    @Test
    fun testNoteEntityDefaultValues() {
        val note = Note(title = "Test", body = "Body")
        assertEquals("Test", note.title)
        assertEquals("Body", note.body)
        assertEquals("Notas", note.folder)
        assertFalse(note.isPinned)
        assertFalse(note.isDeleted)
        assertTrue(note.createdAt > 0)
    }

    @Test
    fun testFolderEntityCreation() {
        val folder = Folder(name = "Trabajo")
        assertEquals("Trabajo", folder.name)
        assertTrue(folder.createdAt > 0)
    }
}
