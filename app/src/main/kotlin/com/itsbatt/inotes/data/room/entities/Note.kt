package com.itsbatt.inotes.data.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val body: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val folder: String = "Notas",
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val tags: String = "",
    val isChecklist: Boolean = false
)
