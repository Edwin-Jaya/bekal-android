package com.edwin.bekal.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
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

        val tempFile = File(context.cacheDir, fileName)
        FileOutputStream(tempFile).use { outputStream ->
            inputStream.use { it.copyTo(outputStream) }
        }

        val mimeType = if (scheme == "content") contentResolver.getType(this) else "image/jpeg"
        val safeMimeType = mimeType ?: "image/jpeg"

        val requestBody = tempFile.readBytes().toRequestBody(safeMimeType.toMediaTypeOrNull())

        Log.d(TAG, "Successfully built multipart part for uri=$this, size=${tempFile.length()} bytes")
        MultipartBody.Part.createFormData(paramName, tempFile.name, requestBody)
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
