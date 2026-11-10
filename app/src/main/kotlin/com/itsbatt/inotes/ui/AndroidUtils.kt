package com.itsbatt.inotes.ui

import android.content.Context
import android.content.Intent

object AndroidUtils {
    fun shareText(context: Context, text: String, title: String = "Compartir nota") {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
