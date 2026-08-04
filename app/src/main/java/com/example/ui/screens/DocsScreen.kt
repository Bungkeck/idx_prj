package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApiCategory
import com.example.data.model.IdxApiEndpoint
import com.example.ui.components.JsonViewer
import com.example.ui.theme.*
import com.example.ui.viewmodel.IdxViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocsScreen(
    viewModel: IdxViewModel,
    onNavigateToExplorer: (IdxApiEndpoint) -> Unit,
    modifier: Modifier = Modifier
) {
    val endpoints = viewModel.apiEndpoints
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ApiCategory?>(null) }
    var expandedEndpointId by remember { mutableStateOf<String?>(endpoints.first().id) }

    val filteredEndpoints = remember(searchQuery, selectedCategory) {
        endpoints.filter { ep ->
            (selectedCategory == null || ep.category == selectedCategory) &&
            (searchQuery.isBlank() || ep.name.contains(searchQuery, ignoreCase = true) || ep.path.contains(searchQuery, ignoreCase = true) || ep.description.contains(searchQuery, ignoreCase = true))
        }
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

            // Title & Search
            item {
                Text(
                    text = "Dokumentasi API BEI / IDX",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Spesifikasi teknis OpenAPI / Swagger untuk integrasi sistem perdagangan & riset",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari endpoint, nama, atau kata kunci...", fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondaryDark) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder, unfocusedContainerColor = SlateCardBg),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Category filters
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("Semua (${endpoints.size})", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = Color.Black)
                        )
                    }
                    items(ApiCategory.values()) { category ->
                        val count = endpoints.count { it.category == category }
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text("${category.displayName} ($count)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = Color.Black)
                        )
                    }
                }
            }

            // Authentication & Security Guide Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = EmeraldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Panduan Otentikasi & Tanda Tangan HMAC", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Setiap request API IDX membutuhkan Header 'Authorization: Bearer <token>' dan 'X-IDX-Signature' menggunakan hash HMAC-SHA256(path + timestamp, secret_key) untuk menjamin integritas data real-time.",
                            fontSize = 12.sp,
                            color = TextSecondaryDark,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Endpoints Spec List
            items(filteredEndpoints, key = { it.id }) { ep ->
                val isExpanded = expandedEndpointId == ep.id

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedEndpointId = if (isExpanded) null else ep.id },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                MethodBadge(method = ep.method)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = ep.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimaryDark
                                    )
                                    Text(
                                        text = ep.path,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = EmeraldPrimary
                                    )
                                }
                            }

                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand",
                                tint = TextSecondaryDark
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.padding(top = 14.dp)) {
                                Divider(color = SlateBorder)
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(text = ep.description, fontSize = 12.sp, color = TextSecondaryDark)

                                Spacer(modifier = Modifier.height(12.dp))

                                // Quota & Scope badges
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Quota: ${ep.rateLimitQuota}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AmberGold,
                                        modifier = Modifier.background(Color(0xFF78350F), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                    Text(
                                        text = "Scope: ${ep.requiredScope}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BlueAccent,
                                        modifier = Modifier.background(Color(0xFF1E3A8A), RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Parameters Table
                                val allParams = ep.pathParams + ep.queryParams
                                if (allParams.isNotEmpty()) {
                                    Text("Parameter Request:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    allParams.forEach { p ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row {
                                                Text(p.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary, fontFamily = FontFamily.Monospace)
                                                Text(" (${p.type})", fontSize = 10.sp, color = TextMutedDark)
                                            }
                                            Text(p.description, fontSize = 11.sp, color = TextSecondaryDark)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                // Default Json Response schema preview
                                JsonViewer(jsonString = ep.defaultResponseBodyJson, title = "Skema Respon 200 OK")

                                Spacer(modifier = Modifier.height(12.dp))

                                // Button to test in Sandbox
                                Button(
                                    onClick = { onNavigateToExplorer(ep) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Uji Coba di API Explorer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
