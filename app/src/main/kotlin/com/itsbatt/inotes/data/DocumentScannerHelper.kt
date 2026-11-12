package com.itsbatt.inotes.data

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream

object DocumentScannerHelper {

    fun getScannerClient(): GmsDocumentScanner {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(8)
            .setResultFormats(
                GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                GmsDocumentScannerOptions.RESULT_FORMAT_PDF
            )
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
        return GmsDocumentScanning.getClient(options)
    }

    fun copyScannedUriToImages(context: Context, sourceUri: Uri): String? {
        return try {
            val dir = File(context.filesDir, "images")
            if (!dir.exists()) dir.mkdirs()
            val fileName = "scan_${System.currentTimeMillis()}_${(100..999).random()}.jpg"
            val destFile = File(dir, fileName)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (destFile.exists() && destFile.length() > 0L) fileName else null
        } catch (_: Exception) {
            null
        }
    }

    fun copyScannedPdfToFiles(context: Context, pdfUri: Uri): File? {
        return try {
            val dir = File(context.filesDir, "scans")
            if (!dir.exists()) dir.mkdirs()
            val destFile = File(dir, "doc_${System.currentTimeMillis()}.pdf")
            context.contentResolver.openInputStream(pdfUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            if (destFile.exists() && destFile.length() > 0L) destFile else null
        } catch (_: Exception) {
            null
        }
    }

    fun recognizeTextOffline(
        context: Context,
        imageUri: Uri,
        onComplete: (String) -> Unit
    ) {
        try {
            val image = InputImage.fromFilePath(context, imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    onComplete(visionText.text.trim())
                }
                .addOnFailureListener {
                    onComplete("")
                }
        } catch (_: Exception) {
            onComplete("")
        }
    }
}
