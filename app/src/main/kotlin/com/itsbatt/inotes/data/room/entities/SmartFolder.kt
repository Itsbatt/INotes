package com.itsbatt.inotes.data.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smart_folders")
data class SmartFolder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    /** Comma-separated tags, e.g. "#trabajo,#urgente" */
    val tagsFilter: String = "",
    val requireTasks: Boolean = false,
    val requireMedia: Boolean = false,
    /** "ALL", "TODAY", or "WEEK" */
    val dateRange: String = "ALL",
    val createdAt: Long = System.currentTimeMillis()
)
