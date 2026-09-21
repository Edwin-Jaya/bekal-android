package com.edwin.bekal.presentation.payment

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing

private data class PaymentMethod(
    val title: String,
    val steps: List<String>
)

private val paymentMethods = listOf(
    PaymentMethod(
        title = "BCA Mobile (m-BCA)",
        steps = listOf(
            "Buka aplikasi BCA mobile, login dengan kode akses Anda",
            "Pilih menu \"m-Transfer\"",
            "Pilih \"Transfer ke Rekening BCA\"",
            "Masukkan nomor Virtual Account di atas sebagai nomor rekening tujuan",
            "Masukkan nominal sesuai tagihan angsuran Anda",
            "Periksa detail transaksi, lalu masukkan PIN m-BCA",
            "Simpan bukti transfer sebagai referensi"
        )
    ),
    PaymentMethod(
        title = "ATM BCA",
        steps = listOf(
            "Masukkan kartu ATM BCA dan PIN Anda",
            "Pilih menu \"Transaksi Lainnya\" → \"Transfer\"",
            "Pilih \"Ke Rekening BCA\"",
            "Masukkan nomor Virtual Account di atas",
            "Masukkan nominal sesuai tagihan angsuran Anda",
            "Konfirmasi transaksi dan ambil struk sebagai bukti pembayaran"
        )
    ),
    PaymentMethod(
        title = "KlikBCA (Internet Banking)",
        steps = listOf(
            "Login ke www.klikbca.com dengan User ID dan PIN Anda",
            "Pilih menu \"Transfer Dana\" → \"Transfer ke Rekening BCA\"",
            "Masukkan nomor Virtual Account di atas",
            "Masukkan nominal sesuai tagihan angsuran Anda",
            "Konfirmasi dengan KeyBCA Response Appli 1",
            "Simpan bukti transfer dari halaman konfirmasi"
        )
    )
)

@Composable
fun CaraBayarScreen(
    virtualAccountNumber: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extendedColors = BekalTheme.extendedColors
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

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
                text = "Cara Pembayaran",
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
                shape = RoundedCornerShape(Radius.xl),
                colors = CardDefaults.cardColors(containerColor = extendedColors.deepCharcoal),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spacing.xl)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = extendedColors.accentSoft,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(Spacing.sm))
                        Text(
                            text = "Virtual Account BCA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(Modifier.height(Spacing.md))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = virtualAccountNumber,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(virtualAccountNumber))
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Salin Nomor VA",
                                tint = extendedColors.electricViolet
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.xs))

                    Text(
                        text = "Gunakan nomor Virtual Account di atas untuk melakukan pembayaran melalui salah satu metode di bawah ini.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            paymentMethods.forEach { method ->
                Card(
                    shape = RoundedCornerShape(Radius.xl),
                    colors = CardDefaults.cardColors(containerColor = extendedColors.surfaceCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none),
                    border = BorderStroke(1.dp, extendedColors.textMuted.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        Text(
                            text = method.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.deepCharcoal
                        )

                        Spacer(Modifier.height(Spacing.md))

                        method.steps.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier.padding(bottom = Spacing.xs),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = extendedColors.accentSoft,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${index + 1}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = extendedColors.electricViolet
                                        )
                                    }
                                }
                                Spacer(Modifier.width(Spacing.sm))
                                Text(
                                    text = step,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = extendedColors.deepCharcoal,
                                    modifier = Modifier.weight(1f)
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