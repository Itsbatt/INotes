package com.itsbatt.inotes

import android.app.Application
import com.itsbatt.inotes.data.room.database.AppDatabase

class INotesApplication : Application() {
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }
}
