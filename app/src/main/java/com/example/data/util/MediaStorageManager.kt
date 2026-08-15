package com.example.data.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class ImportedFileResult(
    val originalName: String,
    val localFilePath: String,
    val fileSizeBytes: Long,
    val durationSeconds: Float = 0f,
    val mimeType: String? = null
)

object MediaStorageManager {
    private const val TAG = "MediaStorageManager"

    fun copyUriToAppStorage(
        context: Context,
        uri: Uri,
        subFolder: String,
        prefix: String = "asset"
    ): ImportedFileResult? {
        return try {
            val contentResolver = context.contentResolver
            var displayName = "$prefix-${System.currentTimeMillis()}"
            var fileSize = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        val name = cursor.getString(nameIndex)
                        if (!name.isNullOrBlank()) {
                            displayName = name
                        }
                    }
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        fileSize = cursor.getLong(sizeIndex)
                    }
                }
            }

            val mimeType = contentResolver.getType(uri)
            val extension = displayName.substringAfterLast('.', "")
            val safeExtension = if (extension.isNotEmpty()) ".$extension" else ""

            val targetDir = File(context.filesDir, "imported_media/$subFolder").apply {
                if (!exists()) mkdirs()
            }

            val safeFileName = "${System.currentTimeMillis()}_${displayName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")}"
            val targetFile = File(targetDir, safeFileName)

            contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
                FileOutputStream(targetFile).use { outputStream: FileOutputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            if (fileSize == 0L) {
                fileSize = targetFile.length()
            }

            var durationSeconds = 0f
            // Try extracting audio duration if applicable
            if (mimeType?.startsWith("audio/") == true || displayName.endsWith(".mp3", true) || displayName.endsWith(".wav", true) || displayName.endsWith(".m4a", true) || displayName.endsWith(".aac", true)) {
                durationSeconds = getAudioDurationSeconds(targetFile)
            }

            ImportedFileResult(
                originalName = displayName,
                localFilePath = targetFile.absolutePath,
                fileSizeBytes = fileSize,
                durationSeconds = durationSeconds,
                mimeType = mimeType
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error importing file from uri: $uri", e)
            null
        }
    }

    private fun getAudioDurationSeconds(file: File): Float {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            val durationMs = durationStr?.toLongOrNull() ?: 0L
            if (durationMs > 0) durationMs / 1000f else 0f
        } catch (e: Exception) {
            Log.w(TAG, "Could not extract audio duration for ${file.name}", e)
            0f
        }
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format("%.1f MB", bytes / (1024f * 1024f))
            bytes >= 1024 -> String.format("%.0f KB", bytes / 1024f)
            else -> "$bytes B"
        }
    }
}
