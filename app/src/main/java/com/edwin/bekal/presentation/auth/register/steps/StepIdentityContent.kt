package com.edwin.bekal.presentation.auth.register.steps

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.edwin.bekal.presentation.auth.register.RegisterUiState
import com.edwin.bekal.presentation.auth.register.RegisterViewModel
import com.edwin.bekal.utils.copyToAppCache
import java.io.File

@Composable
fun StepIdentityContent(
    uiState: RegisterUiState,
    viewModel: RegisterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // 1. Launcher untuk Pilih dari Galeri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // PENTING: copy langsung ke cache app SAAT DIPILIH,
            // jangan simpan content:// uri mentah ke state.
            val localPath = it.copyToAppCache(context, prefix = "ktp_")
            if (localPath != null) {
                viewModel.onKtpImagePathChange(localPath)
            } else {
                Log.e("StepIdentityContent", "Failed to copy gallery uri to cache: $it")
                // opsional: tampilkan snackbar/error ke user di sini
            }
        }
    }

    // 2. Launcher untuk Ambil Foto dari Kamera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            // Foto kamera sudah berada di file milik app sendiri (lewat FileProvider),
            // tapi tetap aman untuk di-copy ulang biar konsisten formatnya (file:// murni).
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
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- CARD 01: NIK ---
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("DATA NIK RESMI", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("Terintegrasi sistem Dukcapil Pusat", fontSize = 10.sp, color = Color.Gray)

                OutlinedTextField(
                    value = uiState.nik,
                    onValueChange = viewModel::onNikChange,
                    label = { Text("Nomor Induk Kependudukan (16 Digit NIK) *", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF3E8FF)) {
                            Text("Valid NIK", color = Color(0xFF7E22CE), fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    singleLine = true
                )
            }
        }

        // --- CARD 02: Upload Foto KTP ---
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("UNGGAH FOTO FISIK E-KTP", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text("Format: JPG / PNG • Maks. 5MB", fontSize = 10.sp, color = Color.Gray)

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF9FAFB),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (uiState.ktpImagePath != null) {
                            AsyncImage(
                                model = uiState.ktpImagePath,
                                contentDescription = "Preview e-KTP",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.Gray)
                                Text("Belum Ada Foto e-KTP", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            val uri = createTempImageUri(context)
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kamera", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Galeri", fontSize = 11.sp)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                Text("PANDUAN KUALITAS DOKUMEN", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                val guides = listOf(
                    "Foto e-KTP fisik asli, bukan fotokopi atau screenshot.",
                    "Teks dan NIK terbaca jelas tanpa pantulan cahaya.",
                    "Pastikan seluruh 4 sudut e-KTP masuk ke dalam bingkai."
                )
                guides.forEach { text ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF7E22CE), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text, fontSize = 10.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

private fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile(
        "ktp_image_",
        ".jpg",
        context.externalCacheDir
    ).apply {
        createNewFile()
        deleteOnExit()
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}