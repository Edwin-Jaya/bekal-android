package com.edwin.bekal.utils

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Formatter standar ISO 8601 (yyyy-MM-dd)
private val ISO_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

/**
 * Mengonversi Epoch Milliseconds (misal dari Compose Material3 DatePickerState)
 * ke format ISO (yyyy-MM-dd).
 *
 * Menggunakan zona waktu UTC untuk mencegah pergeseran H-1 / H+1 tanggal
 * akibat perbedaan zona waktu lokal.
 */
fun Long?.toIsoDateString(): String {
    if (this == null) return ""
    return try {
        Instant.ofEpochMilli(this)
            .atZone(ZoneId.of("UTC"))
            .toLocalDate()
            .format(ISO_DATE_FORMATTER)
    } catch (e: Exception) {
        ""
    }
}

/**
 * Mengonversi String tanggal dari input UI (misal "20/05/1998")
 * ke format ISO (yyyy-MM-dd).
 *
 * @param inputPattern Pola tanggal asal, default "dd/MM/yyyy"
 */
fun String.toIsoDateString(inputPattern: String = "dd/MM/yyyy"): String {
    if (this.isBlank()) return ""
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern(inputPattern, Locale.getDefault())
        val parsedDate = LocalDate.parse(this.trim(), inputFormatter)
        parsedDate.format(ISO_DATE_FORMATTER)
    } catch (e: Exception) {
        ""
    }
}

/**
 * Mengonversi nilai year, month (0-indexed dari DatePickerDialog legacy), dan day
 * ke format ISO (yyyy-MM-dd).
 *
 * Contoh: formatToIsoDate(1998, 4, 20) -> "1998-05-20" (Bulan 4 = Mei)
 */
fun formatToIsoDate(year: Int, monthZeroIndexed: Int, dayOfMonth: Int): String {
    return try {
        val date = LocalDate.of(year, monthZeroIndexed + 1, dayOfMonth)
        date.format(ISO_DATE_FORMATTER)
    } catch (e: Exception) {
        ""
    }
}

/**
 * Mengonversi String ISO (yyyy-MM-dd) kembali ke format tampilan UI lokal.
 * Contoh: "1998-05-20" -> "20 Mei 1998"
 */
fun String.formatIsoToDisplay(
    outputPattern: String = "dd MMMM yyyy",
    locale: Locale = Locale("id", "ID")
): String {
    if (this.isBlank()) return ""
    return try {
        val parsedDate = LocalDate.parse(this.trim(), ISO_DATE_FORMATTER)
        val outputFormatter = DateTimeFormatter.ofPattern(outputPattern, locale)
        parsedDate.format(outputFormatter)
    } catch (e: Exception) {
        this // Kembalikan string asli jika gagal parsing
    }
}

/**
 * Memeriksa apakah sebuah String sudah berformat ISO (yyyy-MM-dd) yang valid.
 */
fun String.isValidIsoDate(): Boolean {
    if (this.isBlank()) return false
    return try {
        LocalDate.parse(this.trim(), ISO_DATE_FORMATTER)
        true
    } catch (e: Exception) {
        false
    }
}