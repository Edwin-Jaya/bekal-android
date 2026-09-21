package com.edwin.bekal.presentation.auth.register.steps

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.edwin.bekal.presentation.auth.register.RegisterUiState
import com.edwin.bekal.presentation.auth.register.RegisterViewModel
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing
import com.edwin.bekal.utils.copyToAppCache
import java.io.File

@Composable
fun StepIdentityContent(
    uiState: RegisterUiState,
    viewModel: RegisterViewModel,
    modifier: Modifier = Modifier
) {
    val extendedColors = BekalTheme.extendedColors
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    val scrollState = rememberScrollState()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = extendedColors.textMuted.copy(alpha = 0.15f),
        focusedBorderColor = extendedColors.electricViolet,
        unfocusedContainerColor = extendedColors.canvasBackground.copy(alpha = 0.5f),
        focusedContainerColor = Color.White
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val localPath = it.copyToAppCache(context, prefix = "ktp_")
            if (localPath != null) {
                viewModel.onKtpImagePathChange(localPath)
            } else {
                Log.e("StepIdentityContent", "Failed to copy gallery uri to cache: $it")
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            val localPath = tempCameraUri!!.copyToAppCache(context, prefix = "ktp_")
            if (localPath != null) {
                viewModel.onKtpImagePathChange(localPath)
            } else {
                Log.e("StepIdentityContent", "Failed to copy camera uri to cache: $tempCameraUri")
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // --- BENTO CARD 01: Input NIK ---
        Card(
            shape = RoundedCornerShape(Radius.xl),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = extendedColors.accentSoft,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = extendedColors.electricViolet,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text("Data NIK KTP", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = extendedColors.deepCharcoal)
                        Text("Terintegrasi dengan sistem validasi kependudukan", fontSize = 12.sp, color = extendedColors.textMuted)
                    }
                }

                OutlinedTextField(
                    value = uiState.nik,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 16) {
                            viewModel.onNikChange(input)
                        }
                    },
                    label = { Text("16 Digit NIK *", fontSize = 12.sp) },
                    trailingIcon = {
                        if (uiState.nik.length == 16) {
                            Surface(shape = CircleShape, color = extendedColors.accentSoft) {
                                Text(
                                    text = "Valid",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extendedColors.electricViolet,
                                    modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.nikError != null,
                    supportingText = uiState.nikError?.let { { Text(it, color = Color.Red, fontSize = 11.sp) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = textFieldColors,
                    singleLine = true
                )
            }
        }

        // --- BENTO CARD 02: Upload Foto e-KTP ---
        Card(
            shape = RoundedCornerShape(Radius.xl),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text("Foto e-KTP Asli *", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = extendedColors.deepCharcoal)

                Surface(
                    shape = RoundedCornerShape(Radius.lg),
                    color = extendedColors.canvasBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (uiState.ktpImagePath != null) {
                            AsyncImage(
                                model = uiState.ktpImagePath,
                                contentDescription = "Preview e-KTP",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(Radius.lg)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = extendedColors.textMuted.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(Spacing.xs))
                                Text("Unggah Foto Fisik e-KTP Anda", fontSize = 12.sp, color = extendedColors.textMuted)
                            }
                        }
                    }
                }

                uiState.ktpImageError?.let { errorMsg ->
                    Text(text = errorMsg, color = Color.Red, fontSize = 11.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    OutlinedButton(
                        onClick = {
                            val uri = createTempImageUri(context)
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text("Kamera", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text("Galeri", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                HorizontalDivider(color = extendedColors.textMuted.copy(alpha = 0.15f))

                Text("Panduan Kualitas Foto:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = extendedColors.deepCharcoal)
                val guides = listOf(
                    "Gunakan e-KTP fisik asli (bukan fotokopi).",
                    "Tulisan dan NIK harus dapat terbaca dengan jelas.",
                    "Pastikan seluruh sudut e-KTP berada dalam bingkai foto."
                )
                guides.forEach { text ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = extendedColors.electricViolet,
                            modifier = Modifier.size(14.dp).padding(top = 2.dp)
                        )
                        Text(text = text, fontSize = 11.sp, color = extendedColors.textMuted)
                    }
                }
            }
        }
    }
}

private fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile("ktp_image_", ".jpg", context.externalCacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
}