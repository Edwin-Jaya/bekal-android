package com.edwin.bekal.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.InputStream

private const val TAG = "MultipartMapper"

fun String.toSafeUri(): Uri {
    return if (startsWith("content://") || startsWith("file://")) {
        Uri.parse(this)
    } else {
        Uri.fromFile(File(this))
    }
}

fun String.toTextRequestBody(): RequestBody =
    this.toRequestBody("text/plain".toMediaTypeOrNull())

fun Uri.toMultipartBodyPart(context: Context, paramName: String): MultipartBody.Part? {
    return try {
        val contentResolver = context.contentResolver
        val fileName = context.getFileName(this) ?: "upload_document.jpg"

        val inputStream: InputStream = if (scheme == "content") {
            contentResolver.openInputStream(this) ?: run {
                Log.e(TAG, "openInputStream() returned null for content uri=$this")
                return null
            }
        } else {
            val filePath = path
            if (filePath == null) {
                Log.e(TAG, "Uri.path is null for file uri=$this")
                return null
            }
            val file = File(filePath)
            if (!file.exists()) {
                Log.e(TAG, "File does not exist at path=$filePath")
                return null
            }
            file.inputStream()
        }

        // Baca langsung ke memory — TIDAK menyalin ulang ke cacheDir.
        // (cacheDir bisa dibersihkan OS kapan saja — ini persis bug yang sudah
        // pernah diperbaiki di flow registrasi lain, jangan diulang di sini.)
        val bytes = inputStream.use { it.readBytes() }

        // Deteksi MIME type dari ekstensi file yang sebenarnya, JANGAN hardcode
        // "image/jpeg" untuk semua non-content uri — payslip PDF akan salah label.
        val mimeType = when {
            scheme == "content" -> contentResolver.getType(this)
            else -> {
                val extension = fileName.substringAfterLast('.', "").lowercase()
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            }
        } ?: "application/octet-stream"

        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

        Log.d(TAG, "Successfully built multipart part for uri=$this, size=${bytes.size} bytes, mime=$mimeType")
        MultipartBody.Part.createFormData(paramName, fileName, requestBody)
    } catch (e: SecurityException) {
        Log.e(TAG, "SecurityException reading uri=$this — permission likely expired", e)
        null
    } catch (e: java.io.FileNotFoundException) {
        Log.e(TAG, "FileNotFoundException for uri=$this", e)
        null
    } catch (e: Exception) {
        Log.e(TAG, "Unexpected error converting uri=$this to multipart", e)
        null
    }
}

private fun Context.getFileName(uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = cursor.getString(index)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query display name for uri=$uri", e)
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}