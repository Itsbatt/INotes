package com.itsbatt.inotes.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.itsbatt.inotes.data.room.entities.SmartFolder
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartFolderDao {
    @Query("SELECT * FROM smart_folders ORDER BY createdAt ASC")
    fun getAllSmartFolders(): Flow<List<SmartFolder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmartFolder(smartFolder: SmartFolder): Long

    @Query("DELETE FROM smart_folders WHERE id = :id")
    suspend fun deleteSmartFolder(id: Long)
}
