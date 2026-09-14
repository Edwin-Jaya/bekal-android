package com.edwin.bekal.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edwin.bekal.presentation.auth.AuthViewModel
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing

@Composable
fun HomeScreen(
    onNavigateToSimulation: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var selectedFilter by remember { mutableStateOf("Semua") }
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val displayName = authUiState.user?.name?.let { "Hi, $it 👋🏻" } ?: ""
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = Spacing.md,
            start = Spacing.xl,
            end = Spacing.xl,
            bottom = 96.dp // Memberikan ruang agar footer OJK dapat di-scroll sepenuhnya di atas floating pill bar
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // Header
        item {
            HeaderSection(displayName = displayName)
        }

        // Card Tagihan Bulan Ini
        item {
            TagihanBulanIniCard(
                onRincianClick = { onNavigateToDetail("TAGIHAN-OCT") }
            )
        }

        // Card Fasilitas Kredit
        item {
            FasilitasKreditCard()
        }

        // Aktivitas Terkini
        item {
            AktivitasTerkiniSection(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )
        }

        // Footer OJK
        item {
            OjkComplianceFooter()
        }
    }
}

@Composable
private fun HeaderSection(displayName: String) {
    val extendedColors = BekalTheme.extendedColors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Shield Logo Icon
            Surface(
                shape = RoundedCornerShape(Radius.md),
                color = Color.White,
                border = BorderStroke(1.dp, extendedColors.textMuted.copy(alpha = 0.2f)),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Logo",
                        tint = extendedColors.electricViolet,
                        modifier = Modifier.size(Spacing.xxl)
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.sm + Spacing.xxs))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "BEKAL",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = extendedColors.deepCharcoal
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs + Spacing.xxs))
                    Surface(
                        shape = CircleShape,
                        color = extendedColors.accentSoft
                    ) {
                        Text(
                            text = "PRO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.electricViolet,
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
                        )
                    }
                }
                Text(
                    text = "$displayName",
                    style = MaterialTheme.typography.labelMedium,
                    color = extendedColors.textMuted
                )
            }
        }

        // Action Icons Right: Bell & Profile
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm + Spacing.xxs)) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifikasi",
                        tint = extendedColors.deepCharcoal,
                        modifier = Modifier.size(Spacing.xl)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = extendedColors.deepCharcoal,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "RA",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TagihanBulanIniCard(onRincianClick: () -> Unit) {
    val extendedColors = BekalTheme.extendedColors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRincianClick() },
        shape = RoundedCornerShape(Radius.xl),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(Radius.md),
                    color = extendedColors.accentSoft,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = extendedColors.electricViolet,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Spacing.md))

                Column {
                    Text(
                        text = "Tagihan Bulan Ini",
                        style = MaterialTheme.typography.labelMedium,
                        color = extendedColors.textMuted
                    )
                    Text(
                        text = "Rp 1.450.000",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.deepCharcoal
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Rincian",
                    style = MaterialTheme.typography.labelLarge,
                    color = extendedColors.electricViolet,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = extendedColors.electricViolet,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun FasilitasKreditCard() {
    val extendedColors = BekalTheme.extendedColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.xxl),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xl)
        ) {
            // Top Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FASILITAS KREDIT AKTIF",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textMuted,
                    letterSpacing = 0.5.sp
                )

                Surface(
                    shape = CircleShape,
                    color = extendedColors.accentSoft
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.sm + Spacing.xxs, vertical = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(extendedColors.electricViolet, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Siap Dicairkan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = extendedColors.electricViolet
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = "Sisa Plafon",
                style = MaterialTheme.typography.labelMedium,
                color = extendedColors.textMuted
            )

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "Rp ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.deepCharcoal
                )
                Text(
                    text = "35.000.000",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = extendedColors.deepCharcoal
                )
            }

            Text(
                text = "dari Total Plafon Rp 50.000.000",
                style = MaterialTheme.typography.labelMedium,
                color = extendedColors.textMuted,
                modifier = Modifier.padding(top = Spacing.xxs)
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Progress Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "70% Tersedia",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.electricViolet
                )
                Text(
                    text = "Terpakai: Rp 15.000.000",
                    fontSize = 12.sp,
                    color = extendedColors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { 0.7f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Spacing.sm)
                    .clip(CircleShape),
                color = extendedColors.electricViolet,
                trackColor = extendedColors.accentSoft,
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tempo Pill
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Radius.lg),
                    color = extendedColors.accentSoft
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(Spacing.sm)
                                .background(extendedColors.electricViolet, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = "Tempo: 28 Nov 2023",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = extendedColors.electricViolet
                        )
                    }
                }

                // Interest Pill
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(Radius.lg),
                    color = extendedColors.canvasBackground
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(Spacing.sm)
                                .background(extendedColors.deepCharcoal, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = "Bunga Harian: 0.1%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = extendedColors.deepCharcoal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AktivitasTerkiniSection(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    val extendedColors = BekalTheme.extendedColors
    val filters = listOf("Semua", "Pencairan", "Pembayaran")

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Aktivitas Terkini",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = extendedColors.deepCharcoal
            )
            Text(
                text = "Lihat Semua",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = extendedColors.electricViolet,
                modifier = Modifier.clickable { }
            )
        }

        // Filter Pills
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            filters.forEach { filter ->
                val isSelected = filter == selectedFilter
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) extendedColors.deepCharcoal else Color.White,
                    border = if (!isSelected) BorderStroke(1.dp, extendedColors.textMuted.copy(alpha = 0.2f)) else null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onFilterSelected(filter) }
                ) {
                    Text(
                        text = filter,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else extendedColors.deepCharcoal,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = Spacing.sm)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xxs))

        // Activity Item 1: Pencairan Kilat BCA
        ActivityCardItem(
            title = "Pencairan Kilat BCA",
            subtitle = "Hari ini • 10:20 WIB",
            amount = "+Rp 5.000.000",
            status = "Disetujui",
            statusBgColor = Color(0xFFE8F5E9),
            statusTextColor = Color(0xFF2E7D32),
            icon = Icons.Default.ArrowDownward,
            iconBgColor = extendedColors.accentSoft,
            iconColor = extendedColors.electricViolet
        )

        // Activity Item 2: Pembayaran Angsuran
        ActivityCardItem(
            title = "Pembayaran Angsuran",
            subtitle = "Cicilan ke-2 • 28 Okt",
            amount = "-Rp 1.450.000",
            status = "Berhasil",
            statusBgColor = extendedColors.canvasBackground,
            statusTextColor = extendedColors.deepCharcoal,
            icon = Icons.Default.ArrowUpward,
            iconBgColor = extendedColors.canvasBackground,
            iconColor = extendedColors.deepCharcoal
        )
    }
}

@Composable
private fun ActivityCardItem(
    title: String,
    subtitle: String,
    amount: String,
    status: String,
    statusBgColor: Color,
    statusTextColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBgColor: Color,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.xl),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconBgColor,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BekalTheme.extendedColors.deepCharcoal
                    )
                    Spacer(modifier = Modifier.height(Spacing.xxs))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = BekalTheme.extendedColors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.sm))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amount,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BekalTheme.extendedColors.deepCharcoal
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Surface(
                    shape = RoundedCornerShape(Radius.sm),
                    color = statusBgColor
                ) {
                    Text(
                        text = status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusTextColor,
                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
                    )
                }
            }
        }
    }
}

@Composable
private fun OjkComplianceFooter() {
    val extendedColors = BekalTheme.extendedColors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = extendedColors.electricViolet,
                modifier = Modifier.size(Spacing.lg)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Berizin & Diawasi oleh OJK",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = extendedColors.deepCharcoal
            )
        }
        Spacer(modifier = Modifier.height(Spacing.xxs))
        Text(
            text = "Terdaftar Asosiasi Fintech Pendanaan Bersama Indonesia (AFPI)",
            fontSize = 10.sp,
            color = extendedColors.textMuted
        )
    }
}