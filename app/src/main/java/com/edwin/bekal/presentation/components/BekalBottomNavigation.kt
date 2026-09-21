package com.edwin.bekal.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edwin.bekal.navigation.BottomTabRoute
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: BottomTabRoute
)

@Composable
fun BekalBottomNavigation(
    currentScreen: BottomTabRoute?,
    onTabSelected: (BottomTabRoute) -> Unit,
    onApplyClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasActiveApplication: Boolean = false
) {
    val extendedColors = BekalTheme.extendedColors

    val leftItems = listOf(
        BottomNavItem("Beranda", Icons.Default.Home, BottomTabRoute.Home),
        BottomNavItem("Simulasi", Icons.Default.Calculate, BottomTabRoute.Simulation)
    )
    val rightItems = listOf(
        BottomNavItem("Pinjaman", Icons.Default.AccountBalanceWallet, BottomTabRoute.Loans),
        BottomNavItem("Akun", Icons.Default.Person, BottomTabRoute.Account)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.lg, vertical = Spacing.xs),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Kontainer Utama Navbar (Bento Surface)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp), // Ruang agar tombol "+" dapat keluar sedikit di atas
            shape = RoundedCornerShape(Radius.pill),
            color = Color.White,
            shadowElevation = Elevation.high
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xs, horizontal = Spacing.xxs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Slot Item Kiri
                leftItems.forEach { item ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        NavItemButton(
                            item = item,
                            isSelected = currentScreen != null && item.route::class == currentScreen::class,
                            onClick = { onTabSelected(item.route) }
                        )
                    }
                }

                // Slot Kosong Tengah untuk menjaga jarak simetris
                Spacer(modifier = Modifier.weight(1f))

                // Slot Item Kanan
                rightItems.forEach { item ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        NavItemButton(
                            item = item,
                            isSelected = currentScreen != null && item.route::class == currentScreen::class,
                            onClick = { onTabSelected(item.route) }
                        )
                    }
                }
            }
        }

        // Tombol "+" Timbul & Melayang Tepat di Tengah
        Surface(
            shape = CircleShape,
            color = extendedColors.electricViolet,
            shadowElevation = Elevation.high,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(52.dp)
                .clip(CircleShape)
                .clickable {
                    if (hasActiveApplication) {
                        onTabSelected(BottomTabRoute.Loans)
                    } else {
                        onApplyClick()
                    }
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajukan Pinjaman",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun NavItemButton(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors

    Surface(
        shape = RoundedCornerShape(Radius.lg),
        color = if (isSelected) extendedColors.accentSoft else Color.Transparent,
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.lg))
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(
                horizontal = Spacing.sm,
                vertical = Spacing.xs
            )
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (isSelected) extendedColors.electricViolet else extendedColors.textMuted,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = item.label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) extendedColors.electricViolet else extendedColors.textMuted
            )
        }
    }
}