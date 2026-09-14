package com.edwin.bekal.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
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
    modifier: Modifier = Modifier
) {
    val extendedColors = BekalTheme.extendedColors

    val navItems = listOf(
        BottomNavItem("Beranda", Icons.Default.Home, BottomTabRoute.Home),
        BottomNavItem("Simulasi", Icons.Default.Calculate, BottomTabRoute.Simulation),
        BottomNavItem("Pinjaman", Icons.Default.AccountBalanceWallet, BottomTabRoute.Loans),
        BottomNavItem("Akun", Icons.Default.Person, BottomTabRoute.Account)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.xl, vertical = Spacing.sm)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Radius.pill),
            color = Color.White,
            shadowElevation = Elevation.high
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.xs, horizontal = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isSelected = currentScreen != null && item.route::class == currentScreen::class

                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) extendedColors.accentSoft else Color.Transparent,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onTabSelected(item.route) }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(
                                horizontal = Spacing.md,
                                vertical = Spacing.xs + Spacing.xxs
                            )
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) extendedColors.electricViolet else extendedColors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) extendedColors.electricViolet else extendedColors.textMuted
                            )
                        }
                    }
                }
            }
        }
    }
}