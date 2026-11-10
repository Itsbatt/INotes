package com.itsbatt.inotes

import android.app.Application
import android.arch.persistence.room.Room
import com.itsbatt.inotes.data.room.database.AppDatabase

class INotesApplication : Application() {
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "inotes-db"
        ).build()
    }
}