package com.itsbatt.inotes.data.room.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.itsbatt.inotes.data.room.entities.Folder

@Dao
interface FolderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder)

    @Query("SELECT * FROM folder ORDER BY name ASC")
    suspend fun getAllFolders(): List<Folder>

    @Query("SELECT * FROM folder WHERE name = :name")
    suspend fun getFolderByName(name: String): Folder?

    @Query("DELETE FROM folder WHERE id = :folderId")
    suspend fun deleteFolder(folderId: Int)

    @Query("UPDATE folder SET name = :newName WHERE id = :folderId")
    suspend fun renameFolder(folderId: Int, newName: String)
}