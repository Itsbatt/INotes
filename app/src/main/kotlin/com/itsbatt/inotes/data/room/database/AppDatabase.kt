package com.itsbatt.inotes.data.room.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Migration
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.itsbatt.inotes.data.room.entities.Folder
import com.itsbatt.inotes.data.room.entities.Note

@Database(entities = [Note::class, Folder::class], version = 4, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): com.itsbatt.inotes.data.room.dao.NoteDao
    abstract fun folderDao(): com.itsbatt.inotes.data.room.dao.FolderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    INSTANCE ??= Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "inotes-db"
                    ).build()
                }
            }
            return INSTANCE!!
        }
    }

    // Migration 1 -> 2: Add isPinned to Note
    @Migration(from = 1, to = 2)
    fun migration1_2(database: androidx.room.RoomDatabase) {
        database.execSQL("ALTER TABLE note ADD COLUMN isPinned INTEGER NOT NULL DEFAULT 0")
    }

    // Migration 2 -> 3: Add folder to Note (with default "Notas")
    @Migration(from = 2, to = 3)
    fun migration2_3(database: androidx.room.RoomDatabase) {
        database.execSQL("ALTER TABLE note ADD COLUMN folder TEXT NOT NULL DEFAULT 'Notas'")
        // Migrate existing notes to default folder
        database.execSQL("UPDATE note SET folder = 'Notas' WHERE folder IS NULL")
    }

    // Migration 3 -> 4: Add createdAt and updatedAt columns (if not present)
    @Migration(from = 3, to = 4)
    fun migration3_4(database: androidx.room.RoomDatabase) {
        // These columns already exist in the entity, but if the schema was created before
        // adding the timestamps, we add them here
        database.execSQL("ALTER TABLE note ADD COLUMN createdAt INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))")
        database.execSQL("ALTER TABLE note ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT (strftime('%s', 'now'))")
        database.execSQL("UPDATE note SET createdAt = (strftime('%s', 'now')) WHERE createdAt IS NULL")
        database.execSQL("UPDATE note SET updatedAt = (strftime('%s', 'now')) WHERE updatedAt IS NULL")
    }

    // Migration 4 -> 5 (if needed in future)
    @Migration(from = 4, to = 5)
    fun migration4_5(database: androidx.room.RoomDatabase) {
        // Future migrations can go here
    }
}