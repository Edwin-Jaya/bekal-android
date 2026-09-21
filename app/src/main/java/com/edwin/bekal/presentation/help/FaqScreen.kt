package com.edwin.bekal.presentation.help

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing

private data class FaqItem(
    val question: String,
    val answer: String
)

private val faqItems = listOf(
    FaqItem(
        question = "Bagaimana cara mendaftar akun BEKAL?",
        answer = "Buka aplikasi, pilih \"Daftar sekarang\" di layar login, lalu lengkapi 4 tahap: data diri, identitas (NIK & foto KTP), data pekerjaan, dan data finansial (slip gaji & rekening bank)."
    ),
    FaqItem(
        question = "Bagaimana cara membayar angsuran?",
        answer = "Anda dapat membayar melalui transfer ke Virtual Account BCA yang tertera di halaman \"Cara Bayar\", menggunakan BCA mobile, ATM BCA, atau KlikBCA."
    ),
    FaqItem(
        question = "Berapa lama proses verifikasi pengajuan pinjaman?",
        answer = "Proses verifikasi umumnya memakan waktu 1x24 jam kerja setelah seluruh dokumen (KTP dan slip gaji) berhasil diunggah dengan lengkap."
    ),
    FaqItem(
        question = "Saya lupa kata sandi, bagaimana cara resetnya?",
        answer = "Pada halaman login, pilih \"Lupa Kata Sandi?\", masukkan email terdaftar, lalu masukkan kode OTP 6 digit yang dikirim ke email Anda untuk membuat kata sandi baru."
    ),
    FaqItem(
        question = "Apakah data pribadi saya aman?",
        answer = "Ya. BEKAL berizin dan diawasi oleh OJK, terdaftar di AFPI, serta menggunakan enkripsi standar perbankan ISO/IEC 27001 untuk melindungi seluruh data pribadi Anda."
    ),
    FaqItem(
        question = "Kenapa pengajuan saya ditolak?",
        answer = "Penolakan dapat terjadi karena data yang diunggah tidak lengkap/tidak jelas, profil pendapatan tidak memenuhi syarat minimum, atau terdapat riwayat kredit bermasalah."
    ),
    FaqItem(
        question = "Bank apa saja yang didukung untuk pencairan dan pembayaran?",
        answer = "Saat ini BEKAL mendukung Bank BCA untuk proses pencairan dana maupun pembayaran angsuran."
    ),
    FaqItem(
        question = "Bagaimana jika saya butuh bantuan lebih lanjut?",
        answer = "Hubungi tim dukungan pelanggan kami melalui menu \"Bantuan\" di aplikasi, atau kirimkan pesan langsung melalui email dukungan resmi."
    )
)

@Composable
fun FaqScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = BekalTheme.extendedColors
    val scrollState = rememberScrollState()
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    BackHandler {
        onBackClick()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(extendedColors.canvasBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Header Simpel & Presisi
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = extendedColors.deepCharcoal
                )
            }
            Text(
                text = "Pertanyaan Umum (FAQ)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.deepCharcoal,
                modifier = Modifier.padding(start = Spacing.xs)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.xl),
                colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
                border = BorderStroke(1.dp, extendedColors.textMuted.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = extendedColors.accentSoft,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = extendedColors.electricViolet,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Column {
                        Text(
                            text = "Pusat Bantuan",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.deepCharcoal
                        )
                        Text(
                            text = "Temukan jawaban cepat atas pertanyaan Anda",
                            fontSize = 11.sp,
                            color = extendedColors.textMuted
                        )
                    }
                }
            }

            faqItems.forEachIndexed { index, item ->
                val isExpanded = expandedIndex == index
                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    label = "faq_chevron_rotation"
                )

                Card(
                    shape = RoundedCornerShape(Radius.lg),
                    colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none),
                    border = BorderStroke(1.dp, extendedColors.textMuted.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedIndex = if (isExpanded) null else index }
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.question,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.deepCharcoal,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = extendedColors.electricViolet,
                                modifier = Modifier
                                    .size(20.dp)
                                    .rotate(rotation)
                            )
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(Spacing.xs))
                                Text(
                                    text = item.answer,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = extendedColors.textMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.lg))
        }
    }
}