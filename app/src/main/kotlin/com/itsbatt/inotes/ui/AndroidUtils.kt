package com.itsbatt.inotes.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.ui.platform.LocalContext

object AndroidUtils {

    /** Share text via Android's share intent */
    fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(intent, "Compartir nota"))
    }

    /** Share text via Compose-friendly context */
    @Composable
    fun shareText(text: String) {
        val context = LocalContext.current
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(intent, "Compartir nota"))
    }
}