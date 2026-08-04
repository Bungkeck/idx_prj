package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.IdxViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthSandboxScreen(
    viewModel: IdxViewModel,
    modifier: Modifier = Modifier
) {
    val authSession by viewModel.authSession.collectAsState()
    val watchlistItems by viewModel.watchlistItems.collectAsState()
    val context = LocalContext.current
    val formatter = remember { DecimalFormat("#,###") }

    var hmacMessage by remember { mutableStateOf("/v1/stocks/BBCA/quote:1785851146") }
    var calculatedHmac by remember { mutableStateOf("") }

    LaunchedEffect(hmacMessage, authSession.apiSecret) {
        calculatedHmac = viewModel.repository.calculateHmacSha256(hmacMessage, authSession.apiSecret)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Text(
                    text = "Autentikasi & Keamanan Sandbox",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Manajemen kredensial API Key, token terenkripsi JWT, dan audit batas kuota",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
            }

            // API Key & Token Credentials Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = EmeraldPrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("API Key Produksi & Token", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = authSession.role,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier
                                    .background(EmeraldPrimary, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // API Key
                        CredentialItem(
                            label = "API Key",
                            value = authSession.apiKey,
                            onCopy = { copyToClipboard(context, "API Key", authSession.apiKey) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Secret Key
                        CredentialItem(
                            label = "API Secret (HMAC)",
                            value = authSession.apiSecret,
                            onCopy = { copyToClipboard(context, "API Secret", authSession.apiSecret) }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bearer JWT
                        CredentialItem(
                            label = "JWT Bearer Token",
                            value = authSession.bearerToken,
                            onCopy = { copyToClipboard(context, "JWT Token", authSession.bearerToken) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.regenerateApiKeys()
                                Toast.makeText(context, "API Key & Token JWT baru berhasil dibuat!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateSurfaceVariant, contentColor = TextPrimaryDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generasi Ulang Key & Token JWT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Interactive HMAC SHA-256 Calculator Tool
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = AmberGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kalkulator Tanda Tangan HMAC SHA-256", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = hmacMessage,
                            onValueChange = { hmacMessage = it },
                            label = { Text("Pesan Input (Path + Timestamp)", fontSize = 11.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Hasil Hash HMAC SHA-256 (64 Char Hex):", fontSize = 11.sp, color = TextSecondaryDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF070B14), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = calculatedHmac,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = EmeraldPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Security Status Audit & Quota Meter
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Audit Status Keamanan & Kuota Rate Limit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Rate limit meter
                        val progress = authSession.rateLimitRemaining.toFloat() / authSession.rateLimitMax.toFloat()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sisa Kuota API (Per Menit)", fontSize = 12.sp, color = TextSecondaryDark)
                            Text("${authSession.rateLimitRemaining} / ${authSession.rateLimitMax} req", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = EmeraldPrimary,
                            trackColor = SlateBorder,
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SecurityBadgeItem(title = "SSL Pinning", status = "AKTIF", color = EmeraldPrimary)
                            SecurityBadgeItem(title = "Enkripsi Data", status = authSession.encryptionMode, color = BlueAccent)
                            SecurityBadgeItem(title = "IP Whitelist", status = "TERVERIFIKASI", color = AmberGold)
                        }
                    }
                }
            }

            // Saved Watchlist Persistence Section (Room DB)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Pantau Tersimpan (${watchlistItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Room Database",
                        fontSize = 11.sp,
                        color = BlueAccent
                    )
                }
            }

            if (watchlistItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateCardBg, RoundedCornerShape(14.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Belum ada saham tersimpan. Tekan ikon hati pada tab Pasar untuk menyimpan saham ke Room DB.",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                    }
                }
            } else {
                items(watchlistItems, key = { it.symbol }) { item ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateCardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.symbol, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = FontFamily.Monospace, color = TextPrimaryDark)
                                Text(item.name, fontSize = 11.sp, color = TextSecondaryDark)
                                Text("Alert Atas: IDR ${formatter.format(item.targetAlertHigh)} | Bawah: IDR ${formatter.format(item.targetAlertLow)}", fontSize = 10.sp, color = AmberGold)
                            }

                            IconButton(onClick = { viewModel.toggleWatchlist(viewModel.stocksStream.value.find { it.symbol == item.symbol } ?: return@IconButton) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = CrimsonRed)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun CredentialItem(label: String, value: String, onCopy: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 11.sp, color = TextSecondaryDark)
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF070B14), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TextPrimaryDark,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun SecurityBadgeItem(title: String, status: String, color: Color) {
    Column {
        Text(title, fontSize = 10.sp, color = TextSecondaryDark)
        Text(
            text = status,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label telah disalin!", Toast.LENGTH_SHORT).show()
}
