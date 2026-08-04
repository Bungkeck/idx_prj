package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IdxStock
import com.example.ui.components.CandlestickChart
import com.example.ui.components.OrderBookView
import com.example.ui.theme.*
import com.example.ui.viewmodel.IdxViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    viewModel: IdxViewModel,
    modifier: Modifier = Modifier
) {
    val stocks by viewModel.stocksStream.collectAsState()
    val indexSummary by viewModel.indexSummaryState.collectAsState()
    val selectedStock by viewModel.selectedStock.collectAsState()
    val timeframe by viewModel.selectedTimeframe.collectAsState()
    val candles by viewModel.historicalCandles.collectAsState()
    val orderBook by viewModel.orderBook.collectAsState()
    val brokerFlow by viewModel.brokerFlow.collectAsState()
    val watchlistItems by viewModel.watchlistItems.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSector by remember { mutableStateOf("ALL") }
    var showDetailBottomSheet by remember { mutableStateOf(false) }

    val formatter = remember { DecimalFormat("#,###") }

    val sectorsList = remember {
        listOf("ALL", "Financials", "Technology", "Energy", "Telecommunication", "Consumer Non-Cyclicals", "Industrials", "Basic Materials")
    }

    val filteredStocks = remember(stocks, searchQuery, selectedSector) {
        stocks.filter { stock ->
            (selectedSector == "ALL" || stock.sector.equals(selectedSector, ignoreCase = true)) &&
            (searchQuery.isBlank() || stock.symbol.contains(searchQuery, ignoreCase = true) || stock.name.contains(searchQuery, ignoreCase = true))
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

            // IHSG Real-time Market Header Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
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
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(EmeraldPrimary, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "IHSG • LIVE BEI FEED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "PASAR BUKA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .background(Color(0xFF065F46), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = String.format("%.2f", indexSummary.value),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Monospace
                            )

                            val isPositive = indexSummary.change >= 0
                            val changeColor = if (isPositive) EmeraldPrimary else CrimsonRed
                            val sign = if (isPositive) "+" else ""

                            Text(
                                text = "$sign${String.format("%.2f", indexSummary.change)} ($sign${String.format("%.2f", indexSummary.changePercent)}%)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = changeColor,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Divider(color = SlateBorder)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Market Stats grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Volume (Saham)", fontSize = 10.sp, color = TextSecondaryDark)
                                Text("${formatter.format(indexSummary.volume / 100)} Lot", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                            }
                            Column {
                                Text("Turnover (IDR)", fontSize = 10.sp, color = TextSecondaryDark)
                                Text("${indexSummary.turnover} Triliun", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                            }
                            Column {
                                Text("Naik / Turun", fontSize = 10.sp, color = TextSecondaryDark)
                                Text("${indexSummary.advancing} ▲ / ${indexSummary.declining} ▼", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                            }
                        }
                    }
                }
            }

            // Search and Sector Chips
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari kode emiten (e.g. BBCA, GOTO)...", fontSize = 13.sp) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = TextSecondaryDark) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondaryDark)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = SlateCardBg,
                        focusedContainerColor = SlateCardBg,
                        unfocusedBorderColor = SlateBorder,
                        focusedBorderColor = EmeraldPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sectorsList) { sector ->
                        FilterChip(
                            selected = selectedSector == sector,
                            onClick = { selectedSector = sector },
                            label = { Text(sector, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPrimary,
                                selectedLabelColor = Color.Black,
                                containerColor = SlateCardBg,
                                labelColor = TextSecondaryDark
                            )
                        )
                    }
                }
            }

            // Stock Tickers Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Saham BEI (${filteredStocks.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Perbarui Tiap 2s",
                        fontSize = 11.sp,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Stock Items
            items(filteredStocks, key = { it.symbol }) { stock ->
                val isWatchlisted = watchlistItems.any { it.symbol == stock.symbol }

                StockRowCard(
                    stock = stock,
                    isWatchlisted = isWatchlisted,
                    onStockClick = {
                        viewModel.selectStockBySymbol(stock.symbol)
                        showDetailBottomSheet = true
                    },
                    onWatchlistToggle = { viewModel.toggleWatchlist(stock) }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Stock Detail Bottom Sheet
        if (showDetailBottomSheet && selectedStock != null) {
            val stock = selectedStock!!
            ModalBottomSheet(
                onDismissRequest = { showDetailBottomSheet = false },
                containerColor = SlateDarkBg,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stock.symbol,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimaryDark,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stock.sector,
                                        fontSize = 11.sp,
                                        color = BlueAccent,
                                        modifier = Modifier
                                            .background(Color(0xFF1E3A8A), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = stock.name,
                                    fontSize = 12.sp,
                                    color = TextSecondaryDark
                                )
                            }

                            val isPos = stock.change >= 0
                            val priceColor = if (isPos) EmeraldPrimary else CrimsonRed

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "IDR ${formatter.format(stock.lastPrice)}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = priceColor,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${if (isPos) "+" else ""}${String.format("%.2f", stock.change)} (${if (isPos) "+" else ""}${String.format("%.2f", stock.changePercent)}%)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = priceColor
                                )
                            }
                        }
                    }

                    // Interactive Candlestick Chart
                    item {
                        CandlestickChart(
                            candles = candles,
                            selectedTimeframe = timeframe,
                            onTimeframeSelected = { viewModel.updateTimeframe(it) }
                        )
                    }

                    // Order Book Depth Matrix
                    item {
                        OrderBookView(orderBook = orderBook)
                    }

                    // Key Stats Grid Card
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SlateCardBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Rasio Keuangan & Net Foreign Flow", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("PER (x)", fontSize = 11.sp, color = TextSecondaryDark)
                                        Text("${stock.peRatio}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("PBV (x)", fontSize = 11.sp, color = TextSecondaryDark)
                                        Text("${stock.pbvRatio}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Div Yield", fontSize = 11.sp, color = TextSecondaryDark)
                                        Text("${stock.dividendYield}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                                    }
                                    Column {
                                        Text("Asing (Net Flow)", fontSize = 11.sp, color = TextSecondaryDark)
                                        val flowColor = if (stock.foreignNetFlow >= 0) EmeraldPrimary else CrimsonRed
                                        Text("${if (stock.foreignNetFlow >= 0) "+" else ""}${stock.foreignNetFlow} M", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = flowColor)
                                    }
                                }
                            }
                        }
                    }

                    // Broker Net Flow Table
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SlateCardBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Aktivitas Top Broker (Net Flow)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                brokerFlow.take(4).forEach { broker ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = broker.brokerCode,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (broker.isForeign) BlueAccent else AmberGold,
                                                modifier = Modifier
                                                    .background(SlateSurfaceVariant, RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(broker.brokerName, fontSize = 12.sp, color = TextPrimaryDark)
                                        }
                                        val netColor = if (broker.netValBillion >= 0) EmeraldPrimary else CrimsonRed
                                        Text(
                                            text = "${if (broker.netValBillion >= 0) "+" else ""}${broker.netValBillion} M",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = netColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StockRowCard(
    stock: IdxStock,
    isWatchlisted: Boolean,
    onStockClick: () -> Unit,
    onWatchlistToggle: () -> Unit
) {
    val formatter = remember { DecimalFormat("#,###") }
    val isPositive = stock.change >= 0
    val badgeColor = if (isPositive) EmeraldPrimary else CrimsonRed

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SlateCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onStockClick)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                IconButton(onClick = onWatchlistToggle) {
                    Icon(
                        imageVector = if (isWatchlisted) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Watchlist",
                        tint = if (isWatchlisted) CrimsonRed else TextSecondaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stock.symbol,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = TextPrimaryDark,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stock.sector,
                            fontSize = 10.sp,
                            color = TextMutedDark
                        )
                    }
                    Text(
                        text = stock.name,
                        fontSize = 11.sp,
                        color = TextSecondaryDark,
                        maxLines = 1
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "IDR ${formatter.format(stock.lastPrice)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimaryDark,
                    fontFamily = FontFamily.Monospace
                )
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${if (isPositive) "+" else ""}${String.format("%.2f", stock.changePercent)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }
        }
    }
}
