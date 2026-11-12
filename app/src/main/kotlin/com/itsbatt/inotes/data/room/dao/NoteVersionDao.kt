package com.itsbatt.inotes.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.itsbatt.inotes.data.room.entities.NoteVersion
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteVersionDao {
    @Query("SELECT * FROM note_versions WHERE noteId = :noteId ORDER BY createdAt DESC")
    fun getVersionsForNote(noteId: Long): Flow<List<NoteVersion>>

    @Query("SELECT * FROM note_versions WHERE noteId = :noteId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestVersionForNote(noteId: Long): NoteVersion?

    @Insert
    suspend fun insertVersion(version: NoteVersion): Long

    @Query("DELETE FROM note_versions WHERE noteId = :noteId AND id NOT IN (SELECT id FROM note_versions WHERE noteId = :noteId ORDER BY createdAt DESC LIMIT :keepCount)")
    suspend fun trimOldVersions(noteId: Long, keepCount: Int = 25)
}
