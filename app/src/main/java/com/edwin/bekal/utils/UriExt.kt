package com.edwin.bekal.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream

private const val TAG = "UriExt"

/**
 * Menyalin konten dari Uri ke file lokal PERMANEN di internal storage app
 * (filesDir), BUKAN cacheDir.
 *
 * PENTING: cacheDir bisa dibersihkan otomatis oleh sistem Android kapan saja
 * (terutama saat storage device penuh), tanpa pemberitahuan ke aplikasi.
 * Untuk file yang harus bertahan selama proses multi-step (seperti form
 * registrasi ini, sebelum akhirnya diupload), WAJIB pakai filesDir yang
 * sepenuhnya dikontrol oleh aplikasi dan tidak dihapus otomatis oleh OS.
 */
fun Uri.copyToAppCache(
    context: Context,
    prefix: String = "upload_",
    extension: String = "jpg"
): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(this)
        if (inputStream == null) {
            Log.e(TAG, "openInputStream returned null for uri=$this")
            return null
        }

        // Simpan di subfolder khusus dalam filesDir, biar rapi dan mudah di-cleanup nanti
        val uploadsDir = File(context.filesDir, "pending_uploads").apply { mkdirs() }
        val destFile = File(uploadsDir, "$prefix${System.currentTimeMillis()}.$extension")

        inputStream.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }

        Log.d(TAG, "Copied uri=$this to local file=${destFile.absolutePath}")
        destFile.toUri().toString()
    } catch (e: SecurityException) {
        Log.e(TAG, "SecurityException copying uri=$this — permission expired?", e)
        null
    } catch (e: Exception) {
        Log.e(TAG, "Failed to copy uri=$this to app cache", e)
        null
    }
}

/**
 * Bersihkan folder pending_uploads setelah registrasi selesai (sukses/gagal final),
 * biar file temporary gak numpuk terus di storage.
 */
fun clearPendingUploads(context: Context) {
    val uploadsDir = File(context.filesDir, "pending_uploads")
    if (uploadsDir.exists()) {
        uploadsDir.listFiles()?.forEach { it.delete() }
    }
}