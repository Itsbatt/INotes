package com.itsbatt.inotes.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ComposeView
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            INotesTheme {
                // Main UI composition
            }
        }
    }
}

@Composable
fun INotesTheme(content: @Composable () -> Unit) {
    MaterialTheme.content {
        content()
    }
}