package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IdxCandle
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldPrimary
import java.text.DecimalFormat

@Composable
fun CandlestickChart(
    candles: List<IdxCandle>,
    selectedTimeframe: String,
    onTimeframeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCandle by remember { mutableStateOf<IdxCandle?>(null) }
    val formatter = remember { DecimalFormat("#,###") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Timeframe selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Grafik Candlestick OHLCV",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("1D", "1W", "1M", "1Y").forEach { period ->
                    FilterChip(
                        selected = selectedTimeframe == period,
                        onClick = { onTimeframeSelected(period) },
                        label = { Text(period, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected candle info header
        val displayCandle = selectedCandle ?: candles.lastOrNull()
        if (displayCandle != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "O: ${formatter.format(displayCandle.open)}  H: ${formatter.format(displayCandle.high)}  L: ${formatter.format(displayCandle.low)}  C: ${formatter.format(displayCandle.close)}",
                    fontSize = 11.sp,
                    color = if (displayCandle.close >= displayCandle.open) EmeraldPrimary else CrimsonRed,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Vol: ${formatter.format(displayCandle.volume / 100)} Lot",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Canvas Chart Drawing
        if (candles.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(candles) {
                            detectTapGestures { offset ->
                                val candleWidth = size.width / candles.size
                                val index = (offset.x / candleWidth).toInt().coerceIn(0, candles.size - 1)
                                selectedCandle = candles[index]
                            }
                        }
                ) {
                    val minPrice = candles.minOf { it.low }
                    val maxPrice = candles.maxOf { it.high }
                    val priceRange = (maxPrice - minPrice).coerceAtLeast(1.0)
                    val candleWidth = size.width / candles.size
                    val bodyWidth = (candleWidth * 0.65f).coerceAtLeast(2f)

                    val maxVolume = candles.maxOf { it.volume }.coerceAtLeast(1L)
                    val volumeHeightMax = size.height * 0.25f

                    // Draw grid lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = size.height * (i.toFloat() / gridLines)
                        drawLine(
                            color = Color(0xFF23314B),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }

                    // Draw candles & volume bars
                    candles.forEachIndexed { index, candle ->
                        val x = index * candleWidth + (candleWidth / 2f)
                        val isBullish = candle.close >= candle.open
                        val color = if (isBullish) EmeraldPrimary else CrimsonRed

                        // Price to Y mapping
                        val highY = size.height - ((candle.high - minPrice) / priceRange * (size.height * 0.70f) + size.height * 0.25f).toFloat()
                        val lowY = size.height - ((candle.low - minPrice) / priceRange * (size.height * 0.70f) + size.height * 0.25f).toFloat()
                        val openY = size.height - ((candle.open - minPrice) / priceRange * (size.height * 0.70f) + size.height * 0.25f).toFloat()
                        val closeY = size.height - ((candle.close - minPrice) / priceRange * (size.height * 0.70f) + size.height * 0.25f).toFloat()

                        // Draw wick (high-low line)
                        drawLine(
                            color = color,
                            start = Offset(x, highY),
                            end = Offset(x, lowY),
                            strokeWidth = 2f
                        )

                        // Draw candle body
                        val topY = minOf(openY, closeY)
                        val bodyHeight = kotlin.math.abs(closeY - openY).coerceAtLeast(3f)

                        drawRect(
                            color = color,
                            topLeft = Offset(x - (bodyWidth / 2f), topY),
                            size = Size(bodyWidth, bodyHeight)
                        )

                        // Draw volume bar at bottom
                        val volHeight = (candle.volume.toFloat() / maxVolume) * volumeHeightMax
                        drawRect(
                            color = color.copy(alpha = 0.35f),
                            topLeft = Offset(x - (bodyWidth / 2f), size.height - volHeight),
                            size = Size(bodyWidth, volHeight)
                        )
                    }
                }
            }
        }
    }
}
