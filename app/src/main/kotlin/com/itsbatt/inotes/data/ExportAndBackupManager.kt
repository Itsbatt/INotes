package com.itsbatt.inotes.data

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.TextPaint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.FileProvider
import com.itsbatt.inotes.data.room.dao.FolderDao
import com.itsbatt.inotes.data.room.dao.NoteDao
import com.itsbatt.inotes.data.room.entities.Folder
import com.itsbatt.inotes.data.room.entities.Note
import com.itsbatt.inotes.domain.ChecklistUtils
import com.itsbatt.inotes.domain.NoteLine
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ExportAndBackupManager {

    fun exportNoteToPdfAndShare(context: Context, title: String, body: String, createdAt: Long) {
        val doc = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 44f
        val contentWidth = (pageWidth - margin * 2).toInt()

        var pageNum = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
        var canvas = page.canvas
        var y = margin + 12f

        fun newPageIfNeeded(neededHeight: Float) {
            if (y + neededHeight > pageHeight - margin) {
                doc.finishPage(page)
                pageNum++
                page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
                canvas = page.canvas
                y = margin
            }
        }

        val datePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            textSize = 11f
        }
        val dateStr = SimpleDateFormat("d 'de' MMMM 'de' yyyy, HH:mm", Locale("es")).format(Date(createdAt))
        canvas.drawText(dateStr, margin, y, datePaint)
        y += 24f

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val displayTitle = title.ifBlank { "Nota" }
        canvas.drawText(displayTitle, margin, y, titlePaint)
        y += 28f

        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 13f
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        val lines = ChecklistUtils.parseLines(body)
        lines.forEach { line ->
            when (line) {
                is NoteLine.Divider -> {
                    newPageIfNeeded(18f)
                    canvas.drawLine(margin, y + 6f, pageWidth - margin, y + 6f, linePaint)
                    y += 16f
                }
                is NoteLine.Drawing, is NoteLine.ImageAttachment -> {
                    val path = if (line is NoteLine.Drawing) line.path else (line as NoteLine.ImageAttachment).uri
                    val bmp = BitmapFactory.decodeFile(path)
                    if (bmp != null) {
                        val ratio = contentWidth.toFloat() / bmp.width.coerceAtLeast(1)
                        val targetH = (bmp.height * ratio).toInt().coerceAtMost(320)
                        newPageIfNeeded(targetH + 16f)
                        canvas.drawBitmap(bmp, null, Rect(margin.toInt(), y.toInt(), (margin + contentWidth).toInt(), (y + targetH).toInt()), null)
                        bmp.recycle()
                        y += targetH + 14f
                    }
                }
                else -> {
                    val text = when (line) {
                        is NoteLine.Checklist -> (if (line.checked) "☑ " else "☐ ") + line.text
                        is NoteLine.TableRow -> line.cells.joinToString("  |  ")
                        is NoteLine.Plain -> line.text
                        else -> ""
                    }
                    val words = text.split(" ")
                    var currentLine = ""
                    for (word in words) {
                        val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
                        if (bodyPaint.measureText(candidate) > contentWidth) {
                            newPageIfNeeded(18f)
                            canvas.drawText(currentLine, margin, y, bodyPaint)
                            y += 18f
                            currentLine = word
                        } else {
                            currentLine = candidate
                        }
                    }
                    newPageIfNeeded(18f)
                    canvas.drawText(currentLine, margin, y, bodyPaint)
                    y += 18f
                }
            }
        }

        doc.finishPage(page)

        val safeName = displayTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30).ifBlank { "nota" }
        val outFile = File(context.cacheDir, "${safeName}.pdf")
        FileOutputStream(outFile).use { doc.writeTo(it) }
        doc.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir PDF"))
    }

    fun printNote(context: Context, title: String, body: String) {
        val webView = WebView(context)
        val escapedTitle = android.text.TextUtils.htmlEncode(title.ifBlank { "Nota" })
        val htmlBody = ChecklistUtils.parseLines(body).joinToString("<br/>") { line ->
            when (line) {
                is NoteLine.Checklist -> (if (line.checked) "&#9745; " else "&#9744; ") + android.text.TextUtils.htmlEncode(line.text)
                is NoteLine.Divider -> "<hr/>"
                is NoteLine.TableRow -> android.text.TextUtils.htmlEncode(line.cells.joinToString(" | "))
                is NoteLine.Plain -> android.text.TextUtils.htmlEncode(line.text)
                else -> ""
            }
        }
        val html = "<html><body style='font-family:sans-serif;padding:24px;'><h1>$escapedTitle</h1><p style='font-size:14px;line-height:1.5;'>$htmlBody</p></body></html>"
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val adapter = webView.createPrintDocumentAdapter(title.ifBlank { "INotes" })
                printManager.print("INotes_${title.take(15)}", adapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    fun exportBackupZip(context: Context, destUri: Uri, notes: List<Note>, folders: List<Folder>): Boolean {
        return try {
            val rootJson = JSONObject()
            rootJson.put("version", 1)
            rootJson.put("exportedAt", System.currentTimeMillis())

            val foldersArr = JSONArray()
            folders.forEach { f ->
                foldersArr.put(JSONObject().apply {
                    put("name", f.name)
                    put("createdAt", f.createdAt)
                })
            }
            rootJson.put("folders", foldersArr)

            val notesArr = JSONArray()
            notes.forEach { n ->
                notesArr.put(JSONObject().apply {
                    put("title", n.title)
                    put("body", n.body)
                    put("createdAt", n.createdAt)
                    put("updatedAt", n.updatedAt)
                    put("isPinned", n.isPinned)
                    put("folder", n.folder)
                    put("isDeleted", n.isDeleted)
                    put("deletedAt", n.deletedAt ?: JSONObject.NULL)
                })
            }
            rootJson.put("notes", notesArr)

            context.contentResolver.openOutputStream(destUri)?.use { out ->
                ZipOutputStream(out).use { zip ->
                    zip.putNextEntry(ZipEntry("backup.json"))
                    zip.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
                    zip.closeEntry()

                    listOf("drawings", "images").forEach { subDirName ->
                        val dir = File(context.filesDir, subDirName)
                        dir.listFiles()?.forEach { file ->
                            zip.putNextEntry(ZipEntry("$subDirName/${file.name}"))
                            file.inputStream().use { it.copyTo(zip) }
                            zip.closeEntry()
                        }
                    }
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun importBackupZip(context: Context, srcUri: Uri, noteDao: NoteDao, folderDao: FolderDao): Int {
        var importedCount = 0
        var jsonText: String? = null
        context.contentResolver.openInputStream(srcUri)?.use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name == "backup.json") {
                        jsonText = zip.readBytes().toString(Charsets.UTF_8)
                    } else if (entry.name.startsWith("drawings/") || entry.name.startsWith("images/")) {
                        val outFile = File(context.filesDir, entry.name)
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { zip.copyTo(it) }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }

        val rawJson = jsonText ?: return 0
        val root = JSONObject(rawJson)
        val foldersArr = root.optJSONArray("folders") ?: JSONArray()
        for (i in 0 until foldersArr.length()) {
            val fObj = foldersArr.getJSONObject(i)
            val name = fObj.optString("name").trim()
            if (name.isNotEmpty() && folderDao.getFolderByName(name) == null) {
                folderDao.insertFolder(Folder(name = name, createdAt = fObj.optLong("createdAt", System.currentTimeMillis())))
            }
        }

        val notesArr = root.optJSONArray("notes") ?: JSONArray()
        for (i in 0 until notesArr.length()) {
            val nObj = notesArr.getJSONObject(i)
            noteDao.insertNote(
                Note(
                    title = nObj.optString("title"),
                    body = nObj.optString("body"),
                    createdAt = nObj.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = nObj.optLong("updatedAt", System.currentTimeMillis()),
                    isPinned = nObj.optBoolean("isPinned", false),
                    folder = nObj.optString("folder", "Notas"),
                    isDeleted = nObj.optBoolean("isDeleted", false),
                    deletedAt = if (nObj.isNull("deletedAt")) null else nObj.optLong("deletedAt")
                )
            )
            importedCount++
        }
        return importedCount
    }
}
