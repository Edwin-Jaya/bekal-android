package com.edwin.bekal.presentation.auth.register.steps

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edwin.bekal.presentation.auth.register.RegisterUiState
import com.edwin.bekal.presentation.auth.register.RegisterViewModel
import com.edwin.bekal.ui.theme.BekalTheme
import com.edwin.bekal.ui.theme.Elevation
import com.edwin.bekal.ui.theme.Radius
import com.edwin.bekal.ui.theme.Spacing
import com.edwin.bekal.utils.copyToAppCache

@Composable
fun StepFinancialContent(
    uiState: RegisterUiState,
    viewModel: RegisterViewModel,
    modifier: Modifier = Modifier
) {
    val extendedColors = BekalTheme.extendedColors
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = extendedColors.textMuted.copy(alpha = 0.15f),
        focusedBorderColor = extendedColors.electricViolet,
        unfocusedContainerColor = extendedColors.canvasBackground.copy(alpha = 0.5f),
        focusedContainerColor = Color.White
    )

    LaunchedEffect(Unit) {
        if (uiState.bankName != "BCA") {
            viewModel.onBankNameChange("BCA")
        }
    }

    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val currentList = uiState.paySlipPaths.toMutableList()

            uris.forEach { uri ->
                if (currentList.size >= 3) return@forEach

                val extension = context.contentResolver.getType(uri)
                    ?.substringAfterLast('/')
                    ?: "pdf"

                val localPath = uri.copyToAppCache(context, prefix = "payslip_", extension = extension)

                if (localPath != null && !currentList.contains(localPath)) {
                    currentList.add(localPath)
                } else if (localPath == null) {
                    Log.e("StepFinancialContent", "Failed to copy payslip uri to cache: $uri")
                }
            }

            viewModel.onPaySlipPathsChange(currentList)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // --- BENTO CARD 01: Upload Slip Gaji ---
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dokumen Slip Gaji *", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = extendedColors.deepCharcoal)
                        Text("Unggah berkas 3 bulan terakhir (PDF / Image)", fontSize = 12.sp, color = extendedColors.textMuted)
                    }
                    Surface(shape = CircleShape, color = extendedColors.accentSoft) {
                        Text(
                            text = "${uiState.paySlipPaths.size}/3 File",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.electricViolet,
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs)
                        )
                    }
                }

                if (uiState.paySlipPaths.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(Radius.lg),
                        color = extendedColors.canvasBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(Spacing.lg)
                        ) {
                            Text("Belum ada dokumen slip gaji yang diunggah", fontSize = 12.sp, color = extendedColors.textMuted)
                        }
                    }
                } else {
                    uiState.paySlipPaths.forEachIndexed { index, path ->
                        val fileName = getFileNameFromUri(context, Uri.parse(path)) ?: "Slip_Gaji_${index + 1}.pdf"

                        Surface(
                            shape = RoundedCornerShape(Radius.md),
                            color = extendedColors.canvasBackground,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
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
                                        tint = extendedColors.electricViolet,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(Spacing.xs))
                                    Text(
                                        text = fileName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        color = extendedColors.deepCharcoal
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
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                uiState.paySlipError?.let { Text(it, color = Color.Red, fontSize = 11.sp) }

                if (uiState.paySlipPaths.size < 3) {
                    OutlinedButton(
                        onClick = { documentLauncher.launch("*/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text("Pilih Berkas Slip Gaji", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- BENTO CARD 02: Rekening Bank ---
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
                Text("Rekening Bank Penerima", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = extendedColors.deepCharcoal)

                OutlinedTextField(
                    value = "BCA",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Bank Tujuan", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = extendedColors.textMuted) },
                    trailingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = extendedColors.textMuted) },
                    supportingText = { Text("Saat ini hanya mendukung Bank BCA", fontSize = 11.sp, color = extendedColors.textMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = textFieldColors,
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.bankAccountNumber,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) viewModel.onBankAccountNumberChange(input)
                    },
                    label = { Text("Nomor Rekening *", fontSize = 12.sp) },
                    trailingIcon = {
                        if (uiState.bankAccountNumber.isNotBlank()) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = extendedColors.electricViolet)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.bankAccountNumberError != null,
                    supportingText = uiState.bankAccountNumberError?.let { { Text(it, color = Color.Red, fontSize = 11.sp) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = textFieldColors,
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.fullName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Nama Pemilik Rekening (Sesuai KTP)", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = textFieldColors,
                    singleLine = true
                )
            }
        }

        // --- BENTO CARD 03: Syarat & Ketentuan ---
        Card(
            shape = RoundedCornerShape(Radius.xl),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = uiState.isTermsAgreed,
                        onCheckedChange = viewModel::onTermsAgreedChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = extendedColors.electricViolet,
                            uncheckedColor = extendedColors.textMuted.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = "Saya menyatakan bahwa seluruh dokumen dan data yang diisi adalah benar serta menyetujui Syarat & Ketentuan Layanan BEKAL.",
                        fontSize = 12.sp,
                        color = extendedColors.deepCharcoal
                    )
                }

                uiState.termsAgreedError?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        color = Color.Red,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = Spacing.lg)
                    )
                }
            }
        }
    }
}

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