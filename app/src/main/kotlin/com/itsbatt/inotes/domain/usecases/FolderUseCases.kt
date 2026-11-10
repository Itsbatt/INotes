package com.itsbatt.inotes.domain.usecases

import com.itsbatt.inotes.data.room.dao.FolderDao
import com.itsbatt.inotes.data.room.dao.NoteDao
import com.itsbatt.inotes.data.room.entities.Folder
import kotlinx.coroutines.flow.Flow

class GetFoldersUseCase(private val folderDao: FolderDao) {
    fun execute(): Flow<List<Folder>> = folderDao.getAllFolders()
}

class CreateFolderUseCase(private val folderDao: FolderDao) {
    suspend fun execute(name: String): Long {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return -1L
        val existing = folderDao.getFolderByName(trimmed)
        return if (existing == null) {
            folderDao.insertFolder(Folder(name = trimmed))
        } else {
            existing.id
        }
    }
}

class DeleteFolderUseCase(
    private val folderDao: FolderDao,
    private val noteDao: NoteDao
) {
    suspend fun execute(folder: Folder) {
        // Al borrar una carpeta, sus notas pasan a "Notas", no se eliminan.
        noteDao.reassignFolderNotes(oldFolder = folder.name, defaultFolder = "Notas")
        folderDao.deleteFolder(folder)
    }
}
