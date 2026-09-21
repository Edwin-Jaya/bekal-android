package com.edwin.bekal.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class RupiahVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text
        if (digits.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val formatted = digits.reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()

        // Hitung berapa banyak titik yang muncul SEBELUM setiap posisi digit asli
        val dotPositions = formatted.indices.filter { formatted[it] == '.' }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val dotsBefore = dotPositions.count { it < offset + dotPositions.count { d -> d < it } }
                // Pendekatan lebih aman: hitung dot yang ada di formatted sebelum digit ke-`offset`
                var digitCount = 0
                for (i in formatted.indices) {
                    if (formatted[i] != '.') digitCount++
                    if (digitCount == offset) return i + 1
                }
                return formatted.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                val clamped = offset.coerceIn(0, formatted.length)
                return formatted.substring(0, clamped).count { it != '.' }
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}