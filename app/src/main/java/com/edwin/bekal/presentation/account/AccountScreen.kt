package com.edwin.bekal.presentation.account

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.edwin.bekal.presentation.auth.AuthStatus
import com.edwin.bekal.presentation.auth.AuthViewModel
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing

@Composable
fun AccountScreen(
    onLogoutClick: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToFaq: () -> Unit = {},
    modifier: Modifier = Modifier,
    onNavigateToHome: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val extendedColors = BekalTheme.extendedColors
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val displayName = authUiState.user?.name ?: "Pengguna Bekal"
    val displayEmail = authUiState.user?.email ?: "-"

    LaunchedEffect(authUiState.status) {
        if (authUiState.status == AuthStatus.UNAUTHENTICATED) {
            onLogoutClick()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = Spacing.md,
            start = Spacing.xl,
            end = Spacing.xl,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // Header Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.xl),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = extendedColors.accentSoft,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = extendedColors.electricViolet,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Spacing.md))

                    Text(
                        text = displayName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.deepCharcoal
                    )

                    Text(
                        text = displayEmail,
                        fontSize = 12.sp,
                        color = extendedColors.textMuted
                    )

                    Spacer(modifier = Modifier.height(Spacing.md))

                    Surface(
                        shape = CircleShape,
                        color = extendedColors.accentSoft
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xxs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = extendedColors.electricViolet,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.xs))
                            Text(
                                text = "Akun Terverifikasi e-KTP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = extendedColors.electricViolet
                            )
                        }
                    }
                }
            }
        }

        // Menu Actions Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.xl),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.sm)
                ) {
                    MenuItemRow(
                        icon = Icons.Default.Settings,
                        title = "Pengaturan Akun",
                        onClick = onNavigateToEditProfile
                    )
                    HorizontalDivider(color = extendedColors.canvasBackground, thickness = 1.dp)
                    MenuItemRow(
                        icon = Icons.Default.HelpOutline,
                        title = "Pusat Bantuan & FAQ",
                        onClick = onNavigateToFaq
                    )
                    HorizontalDivider(color = extendedColors.canvasBackground, thickness = 1.dp)
                    MenuItemRow(
                        icon = Icons.Default.Logout,
                        title = "Keluar",
                        isDestructive = true,
                        onClick = {
                            authViewModel.logout()
                            onNavigateToHome()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuItemRow(
    icon: ImageVector,
    title: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val extendedColors = BekalTheme.extendedColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = Spacing.xl, vertical = Spacing.md),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = if (isDestructive) Color(0xFFFFEEEE) else extendedColors.accentSoft,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isDestructive) Color(0xFFD32F2F) else extendedColors.electricViolet,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(Spacing.md))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDestructive) Color(0xFFD32F2F) else extendedColors.deepCharcoal
            )
        }
        if (!isDestructive) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = extendedColors.textMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}