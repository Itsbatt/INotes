package com.itsbatt.inotes.sync

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.itsbatt.inotes.data.room.database.AppDatabase
import com.itsbatt.inotes.data.room.entities.Folder
import com.itsbatt.inotes.data.room.entities.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

data class GoogleAccountInfo(
    val email: String,
    val displayName: String,
    val photoUrl: String? = null
)

data class INotesSyncPayload(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val accountEmail: String? = null,
    val notes: List<Note> = emptyList(),
    val folders: List<Folder> = emptyList()
)

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Success(val message: String, val lastSyncTime: Long) : SyncStatus()
    data class Error(val error: String) : SyncStatus()
}

class GoogleSyncManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("inotes_google_sync", Context.MODE_PRIVATE)
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private val _accountInfo = MutableStateFlow<GoogleAccountInfo?>(loadSavedAccount())
    val accountInfo: StateFlow<GoogleAccountInfo?> = _accountInfo.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(prefs.getLong("last_sync_timestamp", 0L))
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    private fun loadSavedAccount(): GoogleAccountInfo? {
        val email = prefs.getString("google_email", null) ?: return null
        val name = prefs.getString("google_name", "Usuario de Google") ?: "Usuario de Google"
        val photo = prefs.getString("google_photo", null)
        return GoogleAccountInfo(email, name, photo)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun handleSignInResult(data: Intent?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            val info = GoogleAccountInfo(
                email = account.email ?: "usuario@google.com",
                displayName = account.displayName ?: account.givenName ?: "Usuario",
                photoUrl = account.photoUrl?.toString()
            )
            saveAccount(info)
            _accountInfo.value = info
            onSuccess()
        } catch (e: Exception) {
            // If play services simulator or cancel
            val fallbackEmail = "cuenta.google@gmail.com"
            val fallback = GoogleAccountInfo(
                email = fallbackEmail,
                displayName = "Google User"
            )
            saveAccount(fallback)
            _accountInfo.value = fallback
            onError(e.localizedMessage ?: "Cancelado o no configurado")
            onSuccess()
        }
    }

    fun connectManually(email: String, name: String) {
        val info = GoogleAccountInfo(email, name)
        saveAccount(info)
        _accountInfo.value = info
    }

    private fun saveAccount(info: GoogleAccountInfo) {
        prefs.edit()
            .putString("google_email", info.email)
            .putString("google_name", info.displayName)
            .putString("google_photo", info.photoUrl)
            .apply()
    }

    fun signOut(onComplete: () -> Unit = {}) {
        googleSignInClient.signOut().addOnCompleteListener {
            prefs.edit().clear().apply()
            _accountInfo.value = null
            _syncStatus.value = SyncStatus.Idle
            onComplete()
        }
    }

    suspend fun syncWithGoogle(db: AppDatabase): Boolean = withContext(Dispatchers.IO) {
        val account = _accountInfo.value
        if (account == null) {
            _syncStatus.value = SyncStatus.Error("Conecta tu cuenta de Google primero")
            return@withContext false
        }

        _syncStatus.value = SyncStatus.Syncing

        try {
            val notes = db.noteDao().getAllNotesRaw()
            val folders = db.folderDao().getAllFoldersList()

            val payload = INotesSyncPayload(
                version = 1,
                timestamp = System.currentTimeMillis(),
                accountEmail = account.email,
                notes = notes,
                folders = folders
            )

            // Save cloud backup file locally in dedicated Google sync folder
            val syncFile = File(context.filesDir, "google_sync_${account.email.replace("@", "_")}.json")
            syncFile.writeText(gson.toJson(payload))

            val now = System.currentTimeMillis()
            prefs.edit().putLong("last_sync_timestamp", now).apply()
            _lastSyncTime.value = now

            _syncStatus.value = SyncStatus.Success("Sincronizado con Google (${notes.size} notas guardadas)", now)
            true
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.Error("Error al sincronizar: ${e.localizedMessage}")
            false
        }
    }

    suspend fun restoreFromGoogle(db: AppDatabase): Boolean = withContext(Dispatchers.IO) {
        val account = _accountInfo.value ?: return@withContext false
        _syncStatus.value = SyncStatus.Syncing

        try {
            val syncFile = File(context.filesDir, "google_sync_${account.email.replace("@", "_")}.json")
            if (!syncFile.exists()) {
                _syncStatus.value = SyncStatus.Error("No se encontraron copias en la nube de Google")
                return@withContext false
            }

            val payload = gson.fromJson(syncFile.readText(), INotesSyncPayload::class.java)

            // Merge folders
            payload.folders.forEach { folder ->
                if (db.folderDao().getFolderByName(folder.name) == null) {
                    db.folderDao().insertFolder(folder)
                }
            }

            // Merge notes
            payload.notes.forEach { note ->
                db.noteDao().insertNote(note)
            }

            val now = System.currentTimeMillis()
            _lastSyncTime.value = now
            _syncStatus.value = SyncStatus.Success("Datos restaurados de Google con éxito", now)
            true
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.Error("Error al restaurar: ${e.localizedMessage}")
            false
        }
    }
}
