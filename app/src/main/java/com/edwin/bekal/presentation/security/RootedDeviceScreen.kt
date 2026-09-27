package com.edwin.bekal.presentation.security

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GppBad
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Spacing
import com.edwin.bekal.ui.theme.Typography

@Composable
fun RootedDeviceScreen(
    onExitApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept back button to immediately exit the app, preventing bypass
    BackHandler {
        onExitApp()
    }

    val colors = BekalTheme.extendedColors
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvasBackground)
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .clip(RoundedCornerShape(24.dp))
                .background(colors.surfaceCard)
                .border(1.5.dp, colors.dangerSoft, RoundedCornerShape(24.dp))
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Security Alert Icon badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(colors.dangerSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.GppBad,
                    contentDescription = "Perangkat Di-Root",
                    tint = colors.dangerMain,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Tag/Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.dangerSoft)
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
            ) {
                Text(
                    text = "PERINGATAN KEAMANAN",
                    style = Typography.labelMedium,
                    color = colors.dangerMain,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // Title
            Text(
                text = "Perangkat Tidak Didukung",
                style = Typography.headlineSmall,
                color = colors.deepCharcoal,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Subtitle / Description
            Text(
                text = "Aplikasi BEKAL mendeteksi bahwa perangkat ini memiliki akses Root atau modifikasi sistem tak resmi (Jailbreak).",
                style = Typography.bodyMedium,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Explanation Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.canvasBackground)
                    .padding(Spacing.md)
            ) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = colors.electricViolet,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "Demi melindungi kerahasiaan data pribadi, transaksi, dan mematuhi regulasi keamanan finansial, aplikasi tidak dapat dijalankan pada perangkat dengan akses root.",
                        style = Typography.bodySmall,
                        color = colors.deepCharcoal,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Recommendation Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.canvasBackground)
                    .padding(Spacing.md)
            ) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "Silakan gunakan perangkat resmi dengan firmware pabrikan (unrooted) untuk terus menggunakan layanan BEKAL.",
                        style = Typography.bodySmall,
                        color = colors.textMuted,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Action Button to Exit
            Button(
                onClick = onExitApp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.deepCharcoal,
                    contentColor = colors.surfaceCard
                )
            ) {
                Text(
                    text = "Tutup Aplikasi",
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
