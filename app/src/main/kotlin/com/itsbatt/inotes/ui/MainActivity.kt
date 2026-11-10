package com.itsbatt.inotes.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.itsbatt.inotes.ui.screens.FolderScreen
import com.itsbatt.inotes.ui.screens.NoteDetailScreen
import com.itsbatt.inotes.ui.screens.NoteListScreen

sealed class AppScreen {
    object Folders : AppScreen()
    object NoteList : AppScreen()
    data class NoteDetail(val noteId: Long) : AppScreen()
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            INotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Folders) }

                    when (val screen = currentScreen) {
                        is AppScreen.Folders -> {
                            FolderScreen(
                                viewModel = viewModel,
                                onFolderSelected = { folderName ->
                                    viewModel.selectFolder(folderName)
                                    currentScreen = AppScreen.NoteList
                                },
                                onTrashSelected = {
                                    viewModel.openTrash()
                                    currentScreen = AppScreen.NoteList
                                },
                                onCreateNote = {
                                    currentScreen = AppScreen.NoteDetail(0L)
                                },
                                onSearchClicked = {
                                    viewModel.selectFolder("Todas las notas")
                                    currentScreen = AppScreen.NoteList
                                }
                            )
                        }

                        is AppScreen.NoteList -> {
                            BackHandler {
                                currentScreen = AppScreen.Folders
                            }

                            NoteListScreen(
                                viewModel = viewModel,
                                onNavigateToFolders = {
                                    currentScreen = AppScreen.Folders
                                },
                                onOpenNote = { noteId ->
                                    currentScreen = AppScreen.NoteDetail(noteId)
                                },
                                onCreateNote = {
                                    currentScreen = AppScreen.NoteDetail(0L)
                                }
                            )
                        }

                        is AppScreen.NoteDetail -> {
                            BackHandler {
                                currentScreen = AppScreen.NoteList
                            }

                            NoteDetailScreen(
                                noteId = screen.noteId,
                                viewModel = viewModel,
                                onBack = {
                                    currentScreen = AppScreen.NoteList
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
