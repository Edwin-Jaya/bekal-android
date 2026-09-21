package com.edwin.bekal.presentation.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edwin.bekal.presentation.home.toRupiahFormat
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing
import java.math.BigDecimal

data class PaymentChannel(
    val id: String,
    val name: String,
    val category: String, // e.g. "Virtual Account", "E-Wallet", "Gerai Retail"
    val icon: ImageVector,
    val description: String = "Proses Otomatis (Real-time)"
)

val defaultPaymentChannels = listOf(
    PaymentChannel("BCA_VA", "BCA Virtual Account", "Virtual Account", Icons.Default.AccountBalance),
    PaymentChannel("MANDIRI_VA", "Mandiri Virtual Account", "Virtual Account", Icons.Default.AccountBalance),
    PaymentChannel("BRI_VA", "BRI Virtual Account", "Virtual Account", Icons.Default.AccountBalance),
    PaymentChannel("BNI_VA", "BNI Virtual Account", "Virtual Account", Icons.Default.AccountBalance),
    PaymentChannel("QRIS", "QRIS (GoPay, OVO, Dana, ShopeePay)", "E-Wallet / QRIS", Icons.Default.QrCode2),
    PaymentChannel("INDOMARET", "Indomaret / Ceriamart", "Gerai Retail", Icons.Default.Store),
    PaymentChannel("ALFAMART", "Alfamart / Alfamidi", "Gerai Retail", Icons.Default.Store)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodBottomSheet(
    billAmount: BigDecimal,
    onMethodSelected: (channelId: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val extendedColors = BekalTheme.extendedColors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = Radius.xl, topEnd = Radius.xl),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.xl)
        ) {
            // Header Bottom Sheet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pilih Metode Pembayaran",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.deepCharcoal
                    )
                    Text(
                        text = "Total Tagihan: ${billAmount.toRupiahFormat()}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = extendedColors.electricViolet
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = extendedColors.deepCharcoal
                    )
                }
            }

            HorizontalDivider(
                color = extendedColors.textMuted.copy(alpha = 0.15f),
                thickness = 1.dp
            )

            // Grouping Channels by Category
            val groupedChannels = defaultPaymentChannels.groupBy { it.category }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl)
            ) {
                groupedChannels.forEach { (category, channels) ->
                    item {
                        Text(
                            text = category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.textMuted,
                            modifier = Modifier.padding(top = Spacing.lg, bottom = Spacing.xs)
                        )
                    }

                    items(channels) { channel ->
                        PaymentChannelRow(
                            channel = channel,
                            onClick = { onMethodSelected(channel.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentChannelRow(
    channel: PaymentChannel,
    onClick: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxs)
            .clickable { onClick() },
        shape = RoundedCornerShape(Radius.lg),
        color = extendedColors.canvasBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(Radius.md),
                    color = Color.White,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = channel.icon,
                            contentDescription = null,
                            tint = extendedColors.electricViolet,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Spacing.md))

                Column {
                    Text(
                        text = channel.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.deepCharcoal
                    )
                    Text(
                        text = channel.description,
                        fontSize = 10.sp,
                        color = extendedColors.textMuted
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = extendedColors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}