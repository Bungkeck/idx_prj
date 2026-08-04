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
import com.example.data.model.HttpMethod
import com.example.data.model.IdxApiEndpoint
import com.example.ui.components.CodeSnippetDialog
import com.example.ui.components.JsonViewer
import com.example.ui.theme.*
import com.example.ui.viewmodel.IdxViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiExplorerScreen(
    viewModel: IdxViewModel,
    modifier: Modifier = Modifier
) {
    val endpoints = viewModel.apiEndpoints
    val selectedEndpoint by viewModel.selectedEndpoint.collectAsState()
    val currentRequest by viewModel.currentApiRequest.collectAsState()
    val lastResponse by viewModel.lastApiResponse.collectAsState()
    val isLoading by viewModel.isLoadingResponse.collectAsState()
    val showSnippetModal by viewModel.showCodeSnippetDialog.collectAsState()
    val authSession by viewModel.authSession.collectAsState()

    var selectedCategory by remember { mutableStateOf<ApiCategory?>(null) }
    var showHeadersDrawer by remember { mutableStateOf(false) }

    val filteredEndpoints = remember(selectedCategory) {
        if (selectedCategory == null) endpoints else endpoints.filter { it.category == selectedCategory }
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

            // Category Filter Row
            item {
                Text(
                    text = "IDX API Wrapper Sandbox",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Uji coba langsung endpoint BEI dengan otentikasi terenkripsi & pembuatan kode",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("Semua Endpoint", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = Color.Black)
                        )
                    }
                    items(ApiCategory.values()) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.displayName, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = Color.Black)
                        )
                    }
                }
            }

            // Endpoint Selector Dropdown or Horizontal List
            item {
                Text("Pilih Endpoint Target:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondaryDark)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredEndpoints) { ep ->
                        val isSelected = ep.id == selectedEndpoint.id
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) SlateSurfaceVariant else SlateCardBg),
                            modifier = Modifier.clickable { viewModel.selectEndpoint(ep) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MethodBadge(method = ep.method)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = ep.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) EmeraldPrimary else TextPrimaryDark
                                )
                            }
                        }
                    }
                }
            }

            // Request Builder Panel
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateCardBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MethodBadge(method = selectedEndpoint.method)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "https://api.idx.co.id${selectedEndpoint.path}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = selectedEndpoint.description, fontSize = 12.sp, color = TextSecondaryDark)

                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = SlateBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Path Parameters inputs
                        if (selectedEndpoint.pathParams.isNotEmpty()) {
                            Text("Path Parameters:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                            Spacer(modifier = Modifier.height(6.dp))
                            selectedEndpoint.pathParams.forEach { param ->
                                val currentVal = currentRequest.pathParamsMap[param.name] ?: param.defaultValue
                                OutlinedTextField(
                                    value = currentVal,
                                    onValueChange = { viewModel.updateParamValue(param.name, it, isPath = true) },
                                    label = { Text("${param.name} (${param.description})", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Query Parameters inputs
                        if (selectedEndpoint.queryParams.isNotEmpty()) {
                            Text("Query Parameters:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
                            Spacer(modifier = Modifier.height(6.dp))
                            selectedEndpoint.queryParams.forEach { param ->
                                val currentVal = currentRequest.queryParamsMap[param.name] ?: param.defaultValue
                                OutlinedTextField(
                                    value = currentVal,
                                    onValueChange = { viewModel.updateParamValue(param.name, it, isPath = false) },
                                    label = { Text("${param.name} (${param.description})", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Auth Header Input
                        Text("Header Autentikasi (Bearer Token / HMAC):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = currentRequest.authToken,
                            onValueChange = { viewModel.updateAuthTokenInRequest(it) },
                            singleLine = true,
                            trailingIcon = {
                                TextButton(onClick = { viewModel.updateAuthTokenInRequest(authSession.bearerToken) }) {
                                    Text("Isi Token", fontSize = 10.sp, color = EmeraldPrimary)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldPrimary, unfocusedBorderColor = SlateBorder),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons: Execute & Generate Code
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.executeCurrentRequest() },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Black, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Mengirim...")
                                } else {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kirim Request", fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = { viewModel.setShowCodeSnippetDialog(true) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldPrimary)
                            ) {
                                Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Kode API")
                            }
                        }
                    }
                }
            }

            // Live Response Viewer Panel
            item {
                if (lastResponse != null) {
                    val resp = lastResponse!!
                    val statusColor = if (resp.isSuccess) EmeraldPrimary else CrimsonRed

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateCardBg),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(statusColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "HTTP ${resp.statusCode} ${if (resp.isSuccess) "OK" else "ERROR"}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = statusColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${resp.latencyMs} ms",
                                        fontSize = 12.sp,
                                        color = TextSecondaryDark,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                TextButton(onClick = { showHeadersDrawer = !showHeadersDrawer }) {
                                    Text("Header (${resp.headers.size})", fontSize = 11.sp, color = BlueAccent)
                                }
                            }

                            // Expandable Headers Drawer
                            AnimatedVisibility(visible = showHeadersDrawer) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0D1424), RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    resp.headers.forEach { (key, value) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(key, fontSize = 10.sp, color = AmberGold, fontFamily = FontFamily.Monospace)
                                            Text(value, fontSize = 10.sp, color = TextSecondaryDark, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Json Response Viewer
                            JsonViewer(jsonString = resp.bodyJson)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Code Snippet Generator Modal
        if (showSnippetModal) {
            CodeSnippetDialog(
                request = currentRequest,
                repository = viewModel.repository,
                onDismiss = { viewModel.setShowCodeSnippetDialog(false) }
            )
        }
    }
}

@Composable
fun MethodBadge(method: HttpMethod) {
    Text(
        text = method.name,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        color = Color.White,
        modifier = Modifier
            .background(method.color, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
