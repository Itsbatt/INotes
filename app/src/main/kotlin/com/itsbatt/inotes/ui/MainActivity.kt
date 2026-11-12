package com.itsbatt.inotes.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.itsbatt.inotes.ui.screens.LiquidGlassLabScreen
import com.itsbatt.inotes.ui.screens.NoteDetailScreen
import com.itsbatt.inotes.ui.screens.NoteListScreen

sealed class AppScreen {
    object Folders : AppScreen()
    object NoteList : AppScreen()
    data class NoteDetail(val noteId: Long) : AppScreen()
    object LiquidGlassLab : AppScreen()
}

class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember(systemDark) { mutableStateOf(systemDark) }
            var glassConfig by remember { mutableStateOf(LiquidGlassConfig()) }
            var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Folders) }

            // Handle ACTION_SEND (shared text or .md file from other apps)
            androidx.compose.runtime.LaunchedEffect(Unit) {
                if (intent?.action == Intent.ACTION_SEND) {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    val sharedUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT) ?: ""
                    val importedText = when {
                        sharedUri != null -> contentResolver.openInputStream(sharedUri)?.bufferedReader()?.use { it.readText() }
                        else -> sharedText
                    }
                    if (!importedText.isNullOrBlank()) {
                        val lines = importedText.lines()
                        val firstLine = lines.firstOrNull()?.removePrefix("# ")?.trim().orEmpty()
                        val title = subject.ifBlank { firstLine.take(60) }
                        val body = if (subject.isBlank() && lines.size > 1) lines.drop(1).joinToString("\n").trimStart() else importedText
                        viewModel.saveNote(0L, title, body, "Notas") { newId ->
                            currentScreen = AppScreen.NoteDetail(newId)
                        }
                    }
                }
            }

            INotesTheme(
                darkTheme = isDarkTheme,
                glassConfig = glassConfig
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    fun screenDepth(screen: AppScreen): Int = when (screen) {
                        is AppScreen.Folders -> 0
                        is AppScreen.NoteList -> 1
                        is AppScreen.LiquidGlassLab -> 1
                        is AppScreen.NoteDetail -> 2
                    }

                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            val isForward = screenDepth(targetState) >= screenDepth(initialState)
                            if (isForward) {
                                (slideInHorizontally(
                                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                                ) { it } + fadeIn(tween(260))) togetherWith
                                    (slideOutHorizontally(
                                        animationSpec = tween(320, easing = FastOutSlowInEasing)
                                    ) { -it / 3 } + scaleOut(
                                        targetScale = 0.94f,
                                        animationSpec = tween(320, easing = FastOutSlowInEasing)
                                    ) + fadeOut(tween(240)))
                            } else {
                                (slideInHorizontally(
                                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                                ) { -it / 3 } + scaleIn(
                                    initialScale = 0.94f,
                                    animationSpec = tween(320, easing = FastOutSlowInEasing)
                                ) + fadeIn(tween(260))) togetherWith
                                    (slideOutHorizontally(
                                        animationSpec = tween(320, easing = FastOutSlowInEasing)
                                    ) { it } + fadeOut(tween(240)))
                            }
                        },
                        label = "iOSNavigationTransition"
                    ) { screen ->
                        when (screen) {
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
                                    },
                                    onOpenGlassLab = {
                                        currentScreen = AppScreen.LiquidGlassLab
                                    }
                                )
                            }

                            is AppScreen.LiquidGlassLab -> {
                                BackHandler {
                                    currentScreen = AppScreen.Folders
                                }

                                LiquidGlassLabScreen(
                                    config = glassConfig,
                                    isDarkTheme = isDarkTheme,
                                    onConfigChange = { glassConfig = it },
                                    onDarkThemeChange = { isDarkTheme = it },
                                    onExportBackup = { uri ->
                                        viewModel.exportBackup(this@MainActivity, uri) { ok ->
                                            android.widget.Toast.makeText(
                                                this@MainActivity,
                                                if (ok) "Copia exportada" else "Error al exportar",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onImportBackup = { uri ->
                                        viewModel.importBackup(this@MainActivity, uri) { count ->
                                            android.widget.Toast.makeText(
                                                this@MainActivity,
                                                "Importadas $count notas",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onBack = {
                                        currentScreen = AppScreen.Folders
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
                                    },
                                    onOpenNote = { linkedId ->
                                        currentScreen = AppScreen.NoteDetail(linkedId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
