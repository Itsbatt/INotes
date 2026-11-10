package com.itsbatt.inotes.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.itsbatt.inotes.data.room.entities.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    suspend fun getAllNotesList(): List<Note>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isPinned = 1 ORDER BY updatedAt DESC")
    fun getPinnedNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND folder = :folderName ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotesByFolder(folderName: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND (title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%') ORDER BY isPinned DESC, updatedAt DESC")
    fun searchNotes(query: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND (title LIKE '%' || :query || '%' OR body LIKE '%' || :query || '%') ORDER BY isPinned DESC, updatedAt DESC")
    suspend fun searchNotesList(query: String): List<Note>

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    suspend fun getNoteById(noteId: Long): Note?

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Long)

    @Query("UPDATE notes SET isPinned = :pinned WHERE id = :noteId")
    suspend fun setPin(noteId: Long, pinned: Boolean)

    @Query("UPDATE notes SET isDeleted = 1, deletedAt = :timestamp WHERE id = :noteId")
    suspend fun moveToTrash(noteId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE notes SET isDeleted = 0, deletedAt = NULL WHERE id = :noteId")
    suspend fun restoreFromTrash(noteId: Long)

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedNotes(): Flow<List<Note>>

    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun emptyTrash()

    @Query("DELETE FROM notes WHERE isDeleted = 1 AND deletedAt <= :thresholdMillis")
    suspend fun purgeExpiredTrash(thresholdMillis: Long)

    @Query("UPDATE notes SET folder = :defaultFolder WHERE folder = :oldFolder")
    suspend fun reassignFolderNotes(oldFolder: String, defaultFolder: String = "Notas")
}
