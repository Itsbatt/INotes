package com.itsbatt.inotes.data.room.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.itsbatt.inotes.data.room.entities.Folder
import com.itsbatt.inotes.data.room.entities.Note

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Query("SELECT * FROM note ORDER BY updatedAt DESC")
    suspend fun getAllNotes(): List<Note>

    @Query("SELECT * FROM note WHERE isPinned = 1 ORDER BY updatedAt DESC")
    suspend fun getPinnedNotes(): List<Note>

    @Query("SELECT * FROM note WHERE folder = :folderName ORDER BY updatedAt DESC")
    suspend fun getNotesByFolder(folderName: String): List<Note>

    @Query("SELECT * FROM note WHERE title LIKE :query OR body LIKE :query ORDER BY updatedAt DESC")
    suspend fun searchNotes(query: String): List<Note>

    @Query("DELETE FROM note")
    suspend fun deleteAllNotes()

    @Query("DELETE FROM note WHERE id = :noteId")
    suspend fun deleteNote(noteId: Int)

    @Query("UPDATE note SET isPinned = :pinned WHERE id = :noteId")
    suspend fun pinNote(noteId: Int, pinned: Boolean)

    @Query("SELECT * FROM note WHERE updatedAt >= :start AND updatedAt <= :end ORDER BY updatedAt DESC")
    suspend fun getNotesInDateRange(start: Long, end: Long): List<Note>

    @Query("SELECT COUNT(*) FROM note WHERE DATE(updatedAt) = CURRENT_DATE")
    suspend fun getTodayCount(): Int

    @Query("SELECT COUNT(*) FROM note WHERE DATE(updatedAt) = DATE(CURRENT_DATE, '-1 day')")
    suspend fun getYesterdayCount(): Int

    @Query("SELECT COUNT(*) FROM note WHERE DATE(updatedAt) >= DATE(CURRENT_DATE, '-7 day')")
    suspend fun getThisWeekCount(): Int

    @Query("SELECT COUNT(*) FROM note WHERE DATE(updatedAt) >= DATE(CURRENT_DATE, '-30 day')")
    suspend fun getThisMonthCount(): Int
}