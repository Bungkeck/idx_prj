package com.example.data.model

import androidx.compose.ui.graphics.Color

/**
 * Data model for a stock issuer listed on the Indonesian Stock Exchange (IDX / BEI).
 */
data class IdxStock(
    val symbol: String, // e.g., BBCA, BBRI, TLKM, GOTO, AMMN, BREN
    val name: String,
    val sector: String,
    val lastPrice: Double,
    val change: Double,
    val changePercent: Double,
    val open: Double,
    val high: Double,
    val low: Double,
    val prevClose: Double,
    val volume: Long, // in shares
    val turnover: Double, // in IDR
    val marketCap: Double, // in IDR Trillion
    val bid: Double,
    val bidVolume: Long,
    val ask: Double,
    val askVolume: Long,
    val peRatio: Double = 15.4,
    val pbvRatio: Double = 2.1,
    val dividendYield: Double = 3.2,
    val foreignNetFlow: Double = 125.4 // Billion IDR (+ for net buy, - for net sell)
)

/**
 * Historical OHLCV candle for charts.
 */
data class IdxCandle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

/**
 * 10-level Market Depth / Order Book entry.
 */
data class OrderBookLevel(
    val bidPrice: Double,
    val bidVolume: Long,
    val askPrice: Double,
    val askVolume: Long
)

/**
 * Market Index Summary (e.g. IHSG, LQ45, IDX30, Sectoral).
 */
data class IdxIndexSummary(
    val code: String = "IHSG",
    val name: String = "Indeks Harga Saham Gabungan",
    val value: Double = 7324.50,
    val change: Double = 42.15,
    val changePercent: Double = 0.58,
    val volume: Long = 18_450_000_000L,
    val turnover: Double = 12.8, // Trillion IDR
    val frequency: Long = 1_240_500,
    val advancing: Int = 285,
    val declining: Int = 192,
    val unchanged: Int = 210
)

/**
 * Sector Index summary.
 */
data class IdxSector(
    val name: String,
    val code: String,
    val indexValue: Double,
    val changePercent: Double,
    val stockCount: Int
)

/**
 * Endpoint category in the IDX API Wrapper library.
 */
enum class ApiCategory(val displayName: String) {
    MARKET_DATA("Pasar & Indeks"),
    ISSUER("Saham & Emiten"),
    ORDERBOOK("Order Book Depth"),
    HISTORICAL("Data Historis"),
    BROKER_FLOW("Broker & Foreign Flow"),
    CORPORATE("Aksi Korporasi & IPO"),
    NEWS("Berita Pasar"),
    PORTFOLIO("Portofolio & Watchlist"),
    AUTH("Autentikasi & Security")
}

enum class HttpMethod(val color: Color) {
    GET(Color(0xFF10B981)),      // Emerald Green
    POST(Color(0xFF3B82F6)),     // Blue
    PUT(Color(0xFFF59E0B)),      // Amber/Gold
    DELETE(Color(0xFFEF4444))    // Crimson Red
}

/**
 * Specification for an IDX API Endpoint.
 */
data class IdxApiEndpoint(
    val id: String,
    val category: ApiCategory,
    val method: HttpMethod,
    val path: String,
    val name: String,
    val description: String,
    val queryParams: List<ParamSpec> = emptyList(),
    val pathParams: List<ParamSpec> = emptyList(),
    val headerParams: List<ParamSpec> = emptyList(),
    val sampleBodyJson: String? = null,
    val defaultResponseBodyJson: String,
    val rateLimitQuota: String = "60 req/min",
    val requiredScope: String = "read:market"
)

data class ParamSpec(
    val name: String,
    val type: String,
    val required: Boolean,
    val defaultValue: String,
    val description: String
)

/**
 * User API Request configuration in testing sandbox.
 */
data class IdxApiRequest(
    val endpoint: IdxApiEndpoint,
    val pathParamsMap: Map<String, String> = emptyMap(),
    val queryParamsMap: Map<String, String> = emptyMap(),
    val customHeadersMap: Map<String, String> = emptyMap(),
    val requestBodyJson: String? = null,
    val authToken: String = "Bearer idx_sec_token_demo998234",
    val useHmacSignature: Boolean = true
)

/**
 * API Response execution result.
 */
data class IdxApiResponse(
    val statusCode: Int,
    val latencyMs: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val headers: Map<String, String>,
    val bodyJson: String,
    val isSuccess: Boolean = statusCode in 200..299
)

/**
 * Authentication session / API Key details.
 */
data class IdxAuthSession(
    val apiKey: String = "idx_key_prod_882940219",
    val apiSecret: String = "sec_98f23049a1b2c3d4e5f67890",
    val bearerToken: String = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJkZXZlbG9wZXIiLCJyb2xlIjoiRU5URVJQUklTRSIsImV4cCI6MTc5OTEzNTAwMH0.signature",
    val role: String = "ENTERPRISE",
    val rateLimitRemaining: Int = 982,
    val rateLimitMax: Int = 1000,
    val sslPinningActive: Boolean = true,
    val encryptionMode: String = "AES-256-GCM"
)

/**
 * Corporate action / IPO event.
 */
data class IdxCorporateAction(
    val symbol: String,
    val type: String, // Dividend, Rights Issue, Stock Split, IPO
    val detail: String,
    val cumDate: String,
    val exDate: String,
    val paymentDate: String,
    val amountOrRatio: String
)

/**
 * Broker Net Flow summary item.
 */
data class IdxBrokerSummary(
    val brokerCode: String,
    val brokerName: String,
    val buyValBillion: Double,
    val sellValBillion: Double,
    val netValBillion: Double, // positive = net buy, negative = net sell
    val isForeign: Boolean
)
