package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderBookLevel
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.EmeraldPrimary
import java.text.DecimalFormat

@Composable
fun OrderBookView(
    orderBook: List<OrderBookLevel>,
    modifier: Modifier = Modifier
) {
    val formatter = remember { DecimalFormat("#,###") }
    val maxBidVol = remember(orderBook) { orderBook.maxOfOrNull { it.bidVolume } ?: 1L }
    val maxAskVol = remember(orderBook) { orderBook.maxOfOrNull { it.askVolume } ?: 1L }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Kedalaman Pasar (10-Level Order Book)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                .padding(vertical = 6.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Vol Beli", modifier = Modifier.weight(1f), fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
            Text("Bid (Beli)", modifier = Modifier.weight(1f), fontSize = 11.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Ask (Jual)", modifier = Modifier.weight(1f), fontSize = 11.sp, color = CrimsonRed, fontWeight = FontWeight.Bold)
            Text("Vol Jual", modifier = Modifier.weight(1f), fontSize = 11.sp, color = CrimsonRed, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Rows
        orderBook.forEach { level ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bid Volume bar & text
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(22.dp)
                ) {
                    val widthPct = (level.bidVolume.toFloat() / maxBidVol).coerceIn(0.05f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(widthPct)
                            .background(EmeraldPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    )
                    Text(
                        text = formatter.format(level.bidVolume / 100),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)
                    )
                }

                // Bid Price
                Text(
                    text = formatter.format(level.bidPrice),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f).padding(end = 6.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Ask Price
                Text(
                    text = formatter.format(level.askPrice),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonRed,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(1f).padding(start = 6.dp)
                )

                // Ask Volume bar & text
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(22.dp)
                ) {
                    val widthPct = (level.askVolume.toFloat() / maxAskVol).coerceIn(0.05f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(widthPct)
                            .align(Alignment.CenterEnd)
                            .background(CrimsonRed.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    )
                    Text(
                        text = formatter.format(level.askVolume / 100),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
                    )
                }
            }
        }
    }
}
