package com.edwin.bekal.presentation.auth.register.steps

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edwin.bekal.presentation.auth.register.RegisterUiState
import com.edwin.bekal.presentation.auth.register.RegisterViewModel
import com.edwin.bekal.utils.copyToAppCache

@Composable
fun StepFinancialContent(
    uiState: RegisterUiState,
    viewModel: RegisterViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Launcher untuk memilih dokumen Slip Gaji (PDF / Gambar)
    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val currentList = uiState.paySlipPaths.toMutableList()

            // PENTING: copy tiap Uri ke cache app SAAT DIPILIH,
            // jangan simpan content:// uri mentah ke state.
            uris.forEach { uri ->
                if (currentList.size >= 3) return@forEach

                val extension = context.contentResolver.getType(uri)
                    ?.substringAfterLast('/')
                    ?: "pdf"

                val localPath = uri.copyToAppCache(
                    context,
                    prefix = "payslip_",
                    extension = extension
                )

                if (localPath != null && !currentList.contains(localPath)) {
                    currentList.add(localPath)
                } else if (localPath == null) {
                    Log.e("StepFinancialContent", "Failed to copy payslip uri to cache: $uri")
                    // opsional: tampilkan snackbar/error ke user di sini
                }
            }

            viewModel.onPaySlipPathsChange(currentList)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // --- CARD 01: Upload Slip Gaji ---
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("DOKUMEN SLIP GAJI", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("Unggah Berkas (3 Bulan Terakhir) • PDF / Image *", fontSize = 10.sp, color = Color.Gray)
                    }
                    Text(
                        text = "${uiState.paySlipPaths.size}/3 File",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.paySlipPaths.size == 3) Color(0xFF10B981) else Color(0xFF7E22CE)
                    )
                }

                if (uiState.paySlipPaths.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF9FAFB),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text("Belum ada dokumen slip gaji yang diunggah", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                } else {
                    uiState.paySlipPaths.forEachIndexed { index, path ->
                        val fileName = getFileNameFromUri(context, Uri.parse(path)) ?: "Slip_Gaji_${index + 1}.pdf"

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF9FAFB),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = Color(0xFF7E22CE),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = fileName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val updatedList = uiState.paySlipPaths.toMutableList().apply { removeAt(index) }
                                        viewModel.onPaySlipPathsChange(updatedList)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus File",
                                        tint = Color.Red,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.paySlipPaths.size < 3) {
                    OutlinedButton(
                        onClick = { documentLauncher.launch("*/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pilih Berkas Slip Gaji", fontSize = 11.sp)
                    }
                }
            }
        }

        // --- CARD 02: Rekening Bank ---
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("REKENING BANK PENERIMA", fontWeight = FontWeight.Bold, fontSize = 11.sp)

                OutlinedTextField(
                    value = uiState.bankName,
                    onValueChange = viewModel::onBankNameChange,
                    label = { Text("Nama Bank *", fontSize = 11.sp) },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.bankAccountNumber,
                    onValueChange = viewModel::onBankAccountNumberChange,
                    label = { Text("Nomor Rekening *", fontSize = 11.sp) },
                    trailingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.fullName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Nama Pemilik Rekening (Sesuai KTP)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    singleLine = true
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = uiState.isTermsAgreed,
                onCheckedChange = viewModel::onTermsAgreedChange,
                modifier = Modifier.scale(0.85f)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                "Saya menyatakan seluruh dokumen dan data yang diberikan adalah benar dan menyetujui Syarat & Ketentuan Layanan BEKAL.",
                fontSize = 10.sp,
                color = Color.DarkGray
            )
        }
    }
}

// Helper untuk membaca nama asli berkas dari Content Resolver Uri
private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var fileName: String? = null
    if (uri.scheme == "content") {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    fileName = cursor.getString(index)
                }
            }
        }
    }
    return fileName ?: uri.path?.substringAfterLast('/')
}