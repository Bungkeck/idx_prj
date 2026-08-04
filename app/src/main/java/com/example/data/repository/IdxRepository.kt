package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.math.BigInteger
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

class IdxRepository {

    // Default Initial Stock Catalog
    private var initialStocks = listOf(
        IdxStock("BBCA", "Bank Central Asia Tbk", "Financials", 10250.0, 150.0, 1.49, 10100.0, 10300.0, 10075.0, 10100.0, 42_150_000, 432.5, 1263.2, 10225.0, 12400, 10250.0, 15800, peRatio = 24.1, pbvRatio = 4.8, dividendYield = 2.8, foreignNetFlow = 345.2),
        IdxStock("BBRI", "Bank Rakyat Indonesia Tbk", "Financials", 5200.0, -50.0, -0.95, 5250.0, 5275.0, 5175.0, 5250.0, 78_400_000, 407.6, 788.1, 5175.0, 24500, 5200.0, 18900, peRatio = 13.8, pbvRatio = 2.3, dividendYield = 5.4, foreignNetFlow = -84.5),
        IdxStock("TLKM", "Telkom Indonesia Tbk", "Telecommunication", 3850.0, 30.0, 0.79, 3820.0, 3880.0, 3810.0, 3820.0, 51_200_000, 197.1, 381.4, 3840.0, 31200, 3850.0, 28400, peRatio = 14.5, pbvRatio = 2.8, dividendYield = 4.2, foreignNetFlow = 112.8),
        IdxStock("GOTO", "GoTo Gojek Tokopedia Tbk", "Technology", 68.0, 2.0, 3.03, 66.0, 70.0, 65.0, 66.0, 850_000_000, 57.8, 81.6, 67.0, 185000, 68.0, 210000, peRatio = -8.2, pbvRatio = 0.9, dividendYield = 0.0, foreignNetFlow = 28.4),
        IdxStock("AMMN", "Amman Mineral Internasional Tbk", "Basic Materials", 11450.0, 325.0, 2.92, 11125.0, 11600.0, 11100.0, 11125.0, 32_100_000, 367.5, 830.5, 11425.0, 8400, 11450.0, 9200, peRatio = 38.5, pbvRatio = 7.2, dividendYield = 0.5, foreignNetFlow = 210.6),
        IdxStock("BREN", "Barito Renewables Energy Tbk", "Energy", 9850.0, -150.0, -1.50, 10000.0, 10150.0, 9775.0, 10000.0, 28_900_000, 284.6, 1317.5, 9825.0, 11200, 9850.0, 14300, peRatio = 142.0, pbvRatio = 28.5, dividendYield = 0.2, foreignNetFlow = -45.1),
        IdxStock("ASII", "Astra International Tbk", "Industrials", 4980.0, 80.0, 1.63, 4900.0, 5025.0, 4890.0, 4900.0, 22_400_000, 111.5, 201.6, 4970.0, 18400, 4980.0, 15200, peRatio = 6.8, pbvRatio = 1.0, dividendYield = 8.5, foreignNetFlow = 54.3),
        IdxStock("UNVR", "Unilever Indonesia Tbk", "Consumer Non-Cyclicals", 2650.0, -40.0, -1.49, 2690.0, 2710.0, 2640.0, 2690.0, 19_800_000, 52.4, 101.1, 2640.0, 22000, 2650.0, 28100, peRatio = 21.3, pbvRatio = 22.4, dividendYield = 5.1, foreignNetFlow = -18.2),
        IdxStock("ICBP", "Indofood CBP Sukses Makmur Tbk", "Consumer Non-Cyclicals", 11200.0, 200.0, 1.82, 11000.0, 11300.0, 10975.0, 11000.0, 14_200_000, 159.0, 130.6, 11175.0, 6800, 11200.0, 8900, peRatio = 14.8, pbvRatio = 2.9, dividendYield = 3.6, foreignNetFlow = 67.8),
        IdxStock("BBNI", "Bank Negara Indonesia Tbk", "Financials", 5450.0, 75.0, 1.40, 5375.0, 5500.0, 5350.0, 5375.0, 31_500_000, 171.6, 203.3, 5425.0, 14500, 5450.0, 16200, peRatio = 9.8, pbvRatio = 1.2, dividendYield = 5.2, foreignNetFlow = 89.4),
        IdxStock("BMRI", "Bank Mandiri Tbk", "Financials", 6825.0, 100.0, 1.49, 6725.0, 6875.0, 6700.0, 6725.0, 48_600_000, 331.6, 637.0, 6800.0, 21500, 6825.0, 24000, peRatio = 11.2, pbvRatio = 2.1, dividendYield = 5.0, foreignNetFlow = 195.0),
        IdxStock("ADRO", "Adaro Energy Indonesia Tbk", "Energy", 3280.0, -20.0, -0.61, 3300.0, 3330.0, 3260.0, 3300.0, 38_100_000, 124.9, 104.9, 3270.0, 31000, 3280.0, 29000, peRatio = 4.2, pbvRatio = 0.9, dividendYield = 12.8, foreignNetFlow = -12.4),
        IdxStock("KLBF", "Kalbe Farma Tbk", "Healthcare", 1620.0, 15.0, 0.93, 1605.0, 1635.0, 1600.0, 1605.0, 18_200_000, 29.4, 75.9, 1615.0, 28000, 16200.0, 31000, peRatio = 22.1, pbvRatio = 3.4, dividendYield = 2.3, foreignNetFlow = 15.6)
    )

    private var currentStocks = initialStocks.toMutableList()
    private var currentIndexSummary = IdxIndexSummary()

    /**
     * Real-time live market ticker stream emitting price updates every 2 seconds.
     */
    fun getLiveMarketStream(): Flow<List<IdxStock>> = flow {
        while (true) {
            // Fluctuate prices slightly
            currentStocks = currentStocks.map { stock ->
                val delta = (Random.nextDouble(-0.015, 0.015) * stock.lastPrice).toInt().toDouble()
                val newPrice = (stock.lastPrice + delta).coerceAtLeast(10.0)
                val newChange = newPrice - stock.prevClose
                val newPercent = (newChange / stock.prevClose) * 100.0
                val newHigh = maxOf(stock.high, newPrice)
                val newLow = minOf(stock.low, newPrice)
                val addedVol = Random.nextLong(100, 5000) * 100
                val newVol = stock.volume + addedVol
                val newTurnover = stock.turnover + (addedVol * newPrice / 1_000_000_000.0)

                stock.copy(
                    lastPrice = newPrice,
                    change = newChange,
                    changePercent = newPercent,
                    high = newHigh,
                    low = newLow,
                    volume = newVol,
                    turnover = newTurnover,
                    bid = newPrice - 25.0,
                    ask = newPrice
                )
            }.toMutableList()

            // Update IHSG
            val avgPct = currentStocks.map { it.changePercent }.average()
            val newIhsgValue = currentIndexSummary.value * (1 + avgPct / 100.0)
            currentIndexSummary = currentIndexSummary.copy(
                value = newIhsgValue,
                change = newIhsgValue - 7282.35,
                changePercent = ((newIhsgValue - 7282.35) / 7282.35) * 100.0,
                volume = currentIndexSummary.volume + Random.nextLong(10_000_000, 50_000_000)
            )

            emit(currentStocks.toList())
            delay(2000)
        }
    }

    fun getCurrentIndexSummary(): IdxIndexSummary = currentIndexSummary

    fun getSectors(): List<IdxSector> {
        return listOf(
            IdxSector("Keuangan", "FINANCIALS", 1524.80, 1.25, 102),
            IdxSector("Teknologi", "TECH", 3840.12, -0.85, 45),
            IdxSector("Energi", "ENERGY", 2480.60, -0.42, 78),
            IdxSector("Barang Konsumen Primer", "NON_CYCLICAL", 742.30, 0.65, 92),
            IdxSector("Infrastruktur", "INFRA", 1620.10, 0.98, 64),
            IdxSector("Barang Baku", "BASIC_MAT", 1310.45, 2.10, 88),
            IdxSector("Perindustrian", "INDUSTRIALS", 1090.20, 0.45, 56)
        )
    }

    /**
     * Generate 10-level Order Book Depth for a symbol.
     */
    fun getOrderBook(symbol: String): List<OrderBookLevel> {
        val stock = currentStocks.find { it.symbol == symbol } ?: currentStocks.first()
        val tick = if (stock.lastPrice > 5000) 25.0 else if (stock.lastPrice > 2000) 10.0 else 1.0
        val basePrice = stock.lastPrice

        return (0 until 10).map { i ->
            OrderBookLevel(
                bidPrice = basePrice - ((i + 1) * tick),
                bidVolume = Random.nextLong(120, 4500) * 100,
                askPrice = basePrice + (i * tick),
                askVolume = Random.nextLong(150, 5200) * 100
            )
        }
    }

    /**
     * Generate Candlestick history for chart visualization.
     */
    fun getHistoricalCandles(symbol: String, period: String = "1M"): List<IdxCandle> {
        val stock = currentStocks.find { it.symbol == symbol } ?: currentStocks.first()
        val count = when (period) {
            "1D" -> 24
            "1W" -> 7
            "1M" -> 30
            "1Y" -> 52
            else -> 30
        }

        val basePrice = stock.lastPrice
        val candles = mutableListOf<IdxCandle>()
        var currentPrice = basePrice * 0.90
        val now = System.currentTimeMillis()
        val step = 86_400_000L

        for (i in count downTo 1) {
            val open = currentPrice + Random.nextDouble(-15.0, 15.0)
            val close = open + Random.nextDouble(-25.0, 25.0)
            val high = maxOf(open, close) + Random.nextDouble(5.0, 30.0)
            val low = minOf(open, close) - Random.nextDouble(5.0, 30.0)
            val vol = Random.nextLong(5000, 80000) * 100

            candles.add(
                IdxCandle(
                    timestamp = now - (i * step),
                    open = open.coerceAtLeast(10.0),
                    high = high.coerceAtLeast(10.0),
                    low = low.coerceAtLeast(10.0),
                    close = close.coerceAtLeast(10.0),
                    volume = vol
                )
            )
            currentPrice = close
        }
        return candles
    }

    /**
     * Broker Flow summary.
     */
    fun getBrokerFlow(symbol: String): List<IdxBrokerSummary> {
        return listOf(
            IdxBrokerSummary("ZP", "Maybank Sekuritas", 125.4, 42.1, 83.3, isForeign = true),
            IdxBrokerSummary("BK", "J.P. Morgan Sekuritas", 98.2, 35.0, 63.2, isForeign = true),
            IdxBrokerSummary("KZ", "CLSA Sekuritas", 84.5, 21.0, 63.5, isForeign = true),
            IdxBrokerSummary("CC", "Mandiri Sekuritas", 145.0, 182.5, -37.5, isForeign = false),
            IdxBrokerSummary("YP", "Mirae Asset Sekuritas", 210.2, 255.8, -45.6, isForeign = false),
            IdxBrokerSummary("PD", "Indo Premier Sekuritas", 188.0, 214.2, -26.2, isForeign = false)
        )
    }

    /**
     * Corporate actions calendar.
     */
    fun getCorporateActions(): List<IdxCorporateAction> {
        return listOf(
            IdxCorporateAction("BBCA", "Dividend", "Cash Dividend IDR 270.0 / share", "2026-08-12", "2026-08-13", "2026-08-28", "IDR 270"),
            IdxCorporateAction("BBRI", "Dividend", "Interim Dividend IDR 84.0 / share", "2026-08-18", "2026-08-19", "2026-09-02", "IDR 84"),
            IdxCorporateAction("GOTO", "Stock Buyback", "Share Buyback Program Phase II", "2026-08-01", "2026-12-31", "-", "USD 200 Million"),
            IdxCorporateAction("AMMN", "Capital Expenditure", "Expansion of Smelter Phase 2", "2026-08-05", "-", "-", "USD 1.2 Billion"),
            IdxCorporateAction("SOLAR", "IPO", "Initial Public Offering (Main Board)", "2026-08-15", "2026-08-18", "2026-08-20", "IDR 350 - 420")
        )
    }

    /**
     * Full Registry of IDX API Endpoints for Wrapper & Explorer.
     */
    fun getAllApiEndpoints(): List<IdxApiEndpoint> {
        return listOf(
            IdxApiEndpoint(
                id = "mkt_summary",
                category = ApiCategory.MARKET_DATA,
                method = HttpMethod.GET,
                path = "/v1/market/summary",
                name = "IHSG & Ringkasan Pasar",
                description = "Mendapatkan statistik langsung Indeks Harga Saham Gabungan (IHSG), frekuensi, volume, dan pergerakan sektoral.",
                queryParams = listOf(
                    ParamSpec("include_sectors", "boolean", false, "true", "Sertakan rincian indeks sektoral"),
                    ParamSpec("format", "string", false, "json", "Format respon: json / xml")
                ),
                defaultResponseBodyJson = """{
  "status": "success",
  "code": 200,
  "data": {
    "index_code": "IHSG",
    "index_name": "Indeks Harga Saham Gabungan",
    "value": 7324.50,
    "change": 42.15,
    "change_percent": 0.58,
    "trading_volume": 18450000000,
    "trading_value_idr": 12800000000000,
    "transaction_count": 1240500,
    "market_status": "OPEN",
    "advancing_stocks": 285,
    "declining_stocks": 192,
    "unchanged_stocks": 210
  },
  "timestamp": "2026-08-04T13:45:00Z"
}"""
            ),
            IdxApiEndpoint(
                id = "stocks_list",
                category = ApiCategory.ISSUER,
                method = HttpMethod.GET,
                path = "/v1/stocks/list",
                name = "Daftar Emiten Terdaftar",
                description = "Mengambil daftar lengkap emiten saham yang terdaftar di BEI beserta sektor, kapitalisasi pasar, dan statistik kunci.",
                queryParams = listOf(
                    ParamSpec("sector", "string", false, "ALL", "Filter berdasarkan sektor (e.g., FINANCIALS, TECH)"),
                    ParamSpec("limit", "integer", false, "20", "Jumlah data per halaman"),
                    ParamSpec("search", "string", false, "", "Kata kunci pencarian kode / nama emiten")
                ),
                defaultResponseBodyJson = """{
  "status": "success",
  "code": 200,
  "total": 920,
  "page": 1,
  "data": [
    {
      "symbol": "BBCA",
      "name": "Bank Central Asia Tbk",
      "sector": "Financials",
      "last_price": 10250,
      "change_percent": 1.49,
      "market_cap_trillion": 1263.2
    },
    {
      "symbol": "BBRI",
      "name": "Bank Rakyat Indonesia Tbk",
      "sector": "Financials",
      "last_price": 5200,
      "change_percent": -0.95,
      "market_cap_trillion": 788.1
    }
  ]
}"""
            ),
            IdxApiEndpoint(
                id = "stock_quote",
                category = ApiCategory.ISSUER,
                method = HttpMethod.GET,
                path = "/v1/stocks/{symbol}/quote",
                name = "Real-time Stock Quote",
                description = "Harga real-time, Open, High, Low, Bid, Ask, dan statistik harian emiten spesifik.",
                pathParams = listOf(
                    ParamSpec("symbol", "string", true, "BBCA", "Kode ticker emiten 4 huruf (e.g. BBCA, GOTO)")
                ),
                defaultResponseBodyJson = """{
  "status": "success",
  "code": 200,
  "data": {
    "symbol": "BBCA",
    "company_name": "Bank Central Asia Tbk",
    "last_price": 10250.0,
    "change": 150.0,
    "change_percent": 1.49,
    "open": 10100.0,
    "high": 10300.0,
    "low": 10075.0,
    "previous_close": 10100.0,
    "volume_shares": 42150000,
    "turnover_idr": 432500000000,
    "best_bid": 10225.0,
    "best_bid_volume": 12400,
    "best_ask": 10250.0,
    "best_ask_volume": 15800,
    "pe_ratio": 24.1,
    "pbv_ratio": 4.8
  }
}"""
            ),
            IdxApiEndpoint(
                id = "orderbook_depth",
                category = ApiCategory.ORDERBOOK,
                method = HttpMethod.GET,
                path = "/v1/stocks/{symbol}/orderbook",
                name = "Market Depth / 10-Level Order Book",
                description = "Kedalaman pasar 10 antrean Bid (Beli) dan Ask (Jual) beserta volume lot real-time.",
                pathParams = listOf(
                    ParamSpec("symbol", "string", true, "BBCA", "Kode emiten")
                ),
                defaultResponseBodyJson = """{
  "status": "success",
  "symbol": "BBCA",
  "depth_levels": 10,
  "bids": [
    {"price": 10225, "volume_lots": 124, "count": 48},
    {"price": 10200, "volume_lots": 340, "count": 92},
    {"price": 10175, "volume_lots": 512, "count": 110}
  ],
  "asks": [
    {"price": 10250, "volume_lots": 158, "count": 65},
    {"price": 10275, "volume_lots": 280, "count": 84},
    {"price": 10300, "volume_lots": 620, "count": 142}
  ]
}"""
            ),
            IdxApiEndpoint(
                id = "stock_history",
                category = ApiCategory.HISTORICAL,
                method = HttpMethod.GET,
                path = "/v1/stocks/{symbol}/history",
                name = "Historical OHLCV Data",
                description = "Data candlestick historis Open, High, Low, Close, Volume untuk grafik analisis teknikal.",
                pathParams = listOf(
                    ParamSpec("symbol", "string", true, "BBCA", "Kode emiten")
                ),
                queryParams = listOf(
                    ParamSpec("timeframe", "string", false, "1M", "Pilihan: 1D, 1W, 1M, 1Y"),
                    ParamSpec("adjusted", "boolean", false, "true", "Sesuaikan dividen & stock split")
                ),
                defaultResponseBodyJson = """{
  "status": "success",
  "symbol": "BBCA",
  "timeframe": "1M",
  "candles": [
    {"date": "2026-08-01", "open": 10000, "high": 10150, "low": 9950, "close": 10100, "volume": 35400000},
    {"date": "2026-08-02", "open": 10100, "high": 10200, "low": 10050, "close": 10150, "volume": 38900000},
    {"date": "2026-08-03", "open": 10150, "high": 10300, "low": 10075, "close": 10250, "volume": 42150000}
  ]
}"""
            ),
            IdxApiEndpoint(
                id = "broker_summary",
                category = ApiCategory.BROKER_FLOW,
                method = HttpMethod.GET,
                path = "/v1/broker/summary",
                name = "Broker Net Flow & Arus Asing",
                description = "Rincian transaksi broker domestik vs asing (Net Foreign Buy/Sell) untuk deteksi akumulasi / distribusi.",
                queryParams = listOf(
                    ParamSpec("symbol", "string", true, "BBCA", "Kode emiten target"),
                    ParamSpec("date", "string", false, "TODAY", "Format YYYY-MM-DD atau TODAY")
                ),
                defaultResponseBodyJson = """{
  "status": "success",
  "symbol": "BBCA",
  "foreign_net_idr": 345200000000,
  "top_buyers": [
    {"code": "ZP", "name": "Maybank Sekuritas", "net_buy_idr": 83300000000, "is_foreign": true},
    {"code": "BK", "name": "J.P. Morgan", "net_buy_idr": 63200000000, "is_foreign": true}
  ],
  "top_sellers": [
    {"code": "YP", "name": "Mirae Asset", "net_sell_idr": 45600000000, "is_foreign": false},
    {"code": "CC", "name": "Mandiri Sekuritas", "net_sell_idr": 37500000000, "is_foreign": false}
  ]
}"""
            ),
            IdxApiEndpoint(
                id = "corp_actions",
                category = ApiCategory.CORPORATE,
                method = HttpMethod.GET,
                path = "/v1/corporate-actions/ipo",
                name = "Kalender Aksi Korporasi & IPO",
                description = "Jadwal dividen, stock split, rights issue, serta daftar saham yang akan IPO di BEI.",
                queryParams = listOf(
                    ParamSpec("type", "string", false, "ALL", "Filter: DIVIDEND, IPO, SPLIT, RIGHTS")
                ),
                defaultResponseBodyJson = """{
  "status": "success",
  "total": 5,
  "data": [
    {
      "symbol": "BBCA",
      "type": "DIVIDEND",
      "cum_date": "2026-08-12",
      "ex_date": "2026-08-13",
      "payment_date": "2026-08-28",
      "amount": "IDR 270 / share"
    },
    {
      "symbol": "SOLAR",
      "type": "IPO",
      "offering_period": "15 - 18 Aug 2026",
      "price_range": "IDR 350 - 420",
      "underwriter": "Mandiri Sekuritas"
    }
  ]
}"""
            ),
            IdxApiEndpoint(
                id = "market_news",
                category = ApiCategory.NEWS,
                method = HttpMethod.GET,
                path = "/v1/market/news",
                name = "Berita & Sentimen Pasar",
                description = "Kabar pasar finansial terkini, analisa emiten, dan sentimen penggerak Indeks.",
                queryParams = listOf(
                    ParamSpec("category", "string", false, "MARKET", "Kategori: MARKET, ISSUER, MACRO"),
                    ParamSpec("limit", "integer", false, "5", "Jumlah berita")
                ),
                defaultResponseBodyJson = """{
  "status": "success",
  "articles": [
    {
      "id": "news_8891",
      "title": "IHSG Menguat ke 7.324 Didorong Aksi Beli Bersih Investor Asing di Perbankan Big Cap",
      "source": "IDX News Hub",
      "published_at": "2026-08-04T11:20:00Z",
      "sentiment": "BULLISH",
      "impacted_symbols": ["BBCA", "BBRI", "BMRI"]
    }
  ]
}"""
            ),
            IdxApiEndpoint(
                id = "user_watchlist",
                category = ApiCategory.PORTFOLIO,
                method = HttpMethod.GET,
                path = "/v1/user/watchlist",
                name = "Daftar Pantau & Peringatan Harga",
                description = "Mengelola saham favorit dan target alert notifikasi batas atas / bawah harga.",
                defaultResponseBodyJson = """{
  "status": "success",
  "watchlist_count": 3,
  "items": [
    {"symbol": "BBCA", "target_alert_high": 11000, "target_alert_low": 9800},
    {"symbol": "GOTO", "target_alert_high": 100, "target_alert_low": 50}
  ]
}"""
            ),
            IdxApiEndpoint(
                id = "auth_token",
                category = ApiCategory.AUTH,
                method = HttpMethod.POST,
                path = "/v1/auth/token",
                name = "Generasi Token JWT & Security Key",
                description = "Menerbitkan JWT Bearer Token terenkripsi dengan HMAC-SHA256 signature validation.",
                sampleBodyJson = """{
  "api_key": "idx_key_prod_882940219",
  "api_secret": "sec_98f23049a1b2c3d4e5f67890",
  "grant_type": "client_credentials"
}""",
                defaultResponseBodyJson = """{
  "status": "success",
  "token_type": "Bearer",
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJkZXZlbG9wZXIiLCJyb2xlIjoiRU5URVJQUklTRSI...signature",
  "expires_in_seconds": 86400,
  "rate_limit_quota": "1000 req/min"
}"""
            ),
            IdxApiEndpoint(
                id = "security_inspect",
                category = ApiCategory.AUTH,
                method = HttpMethod.GET,
                path = "/v1/security/inspect",
                name = "Inspeksi Keamanan & Header SSL",
                description = "Mengecek status enkripsi payload AES-256-GCM, SSL Pinning, dan sisa quota Rate Limit API.",
                defaultResponseBodyJson = """{
  "status": "secure",
  "ip_whitelisted": true,
  "ssl_pinning": "ACTIVE",
  "encryption": "AES-256-GCM",
  "rate_limit": {
    "remaining": 982,
    "limit": 1000,
    "reset_in_seconds": 45
  }
}"""
            )
        )
    }

    /**
     * Executes an API request in the sandbox, calculating real HMAC SHA-256 signatures,
     * evaluating token validity, simulating realistic network latency, and returning structured response.
     */
    suspend fun executeApiRequest(request: IdxApiRequest): IdxApiResponse {
        val startTime = System.currentTimeMillis()
        delay(Random.nextLong(80, 220)) // Network latency simulation
        val latencyMs = System.currentTimeMillis() - startTime

        // Validate Auth token if required
        val isAuthValid = request.authToken.contains("idx_") || request.authToken.contains("Bearer")
        if (!isAuthValid && request.endpoint.category != ApiCategory.AUTH) {
            return IdxApiResponse(
                statusCode = 401,
                latencyMs = latencyMs,
                headers = mapOf(
                    "Content-Type" to "application/json",
                    "WWW-Authenticate" to "Bearer realm=\"IDX Wrapper API\""
                ),
                bodyJson = """{
  "error": "Unauthorized",
  "code": 401,
  "message": "Token Autentikasi tidak valid atau sudah kadaluarsa. Sertakan Header 'Authorization: Bearer <token>'."
}""",
                isSuccess = false
            )
        }

        // Calculate HMAC SHA256 Signature for response header
        val hmacSignature = calculateHmacSha256(
            data = "${request.endpoint.path}:${System.currentTimeMillis()}",
            key = "sec_98f23049a1b2c3d4e5f67890"
        )

        // Substitute dynamic path parameters into response JSON if applicable
        var responseBody = request.endpoint.defaultResponseBodyJson
        request.pathParamsMap.forEach { (param, value) ->
            responseBody = responseBody.replace("\"BBCA\"", "\"${value.uppercase()}\"")
                .replace("symbol\": \"BBCA\"", "symbol\": \"${value.uppercase()}\"")
        }

        return IdxApiResponse(
            statusCode = 200,
            latencyMs = latencyMs,
            headers = mapOf(
                "Content-Type" to "application/json; charset=utf-8",
                "X-IDX-Signature" to hmacSignature,
                "X-RateLimit-Limit" to "1000",
                "X-RateLimit-Remaining" to "${Random.nextInt(900, 995)}",
                "X-Server-Latency" to "${latencyMs}ms",
                "Strict-Transport-Security" to "max-age=31536000; includeSubDomains"
            ),
            bodyJson = responseBody,
            isSuccess = true
        )
    }

    /**
     * Calculates authentic HMAC SHA-256 for secure request validation.
     */
    fun calculateHmacSha256(data: String, key: String): String {
        return try {
            val secretKeySpec = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(secretKeySpec)
            val hmacBytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
            val bigInt = BigInteger(1, hmacBytes)
            String.format("%064x", bigInt)
        } catch (e: Exception) {
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
        }
    }

    /**
     * Code Snippet Generator for Kotlin, Python, cURL, and JS.
     */
    fun generateCodeSnippet(request: IdxApiRequest, language: String): String {
        val fullUrl = "https://api.idx.co.id${request.endpoint.path}"
        val token = request.authToken.ifBlank { "Bearer YOUR_ACCESS_TOKEN" }

        return when (language.lowercase()) {
            "kotlin" -> """// Kotlin (Ktor Client Integration for IDX Wrapper)
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

suspend fun fetchIdxData(): String {
    val client = HttpClient()
    val response: HttpResponse = client.get("$fullUrl") {
        header(HttpHeaders.Authorization, "$token")
        header("X-IDX-Signature", "${calculateHmacSha256(request.endpoint.path, "SECRET_KEY")}")
        contentType(ContentType.Application.Json)
    }
    return response.bodyAsText()
}"""

            "python" -> """# Python (requests library for IDX API Wrapper)
import requests
import hmac
import hashlib
import time

url = "$fullUrl"
secret_key = b"YOUR_SECRET_KEY"
timestamp = str(int(time.time()))
signature = hmac.new(secret_key, f"${request.endpoint.path}:{timestamp}".encode(), hashlib.sha256).hexdigest()

headers = {
    "Authorization": "$token",
    "X-IDX-Signature": signature,
    "Content-Type": "application/json"
}

response = requests.${request.endpoint.method.name.lowercase()}(url, headers=headers)
print(response.json())"""

            "curl" -> """# cURL CLI Command
curl -X ${request.endpoint.method.name} "$fullUrl" \
  -H "Authorization: $token" \
  -H "X-IDX-Signature: ${calculateHmacSha256(request.endpoint.path, "SECRET")}" \
  -H "Content-Type: application/json""""

            "javascript" -> """// JavaScript (Node.js / Browser Fetch)
async function callIdxApi() {
  const response = await fetch('$fullUrl', {
    method: '${request.endpoint.method.name}',
    headers: {
      'Authorization': '$token',
      'Content-Type': 'application/json',
      'X-IDX-Signature': '${calculateHmacSha256(request.endpoint.path, "SECRET")}'
    }
  });
  const data = await response.json();
  console.log(data);
}"""

            else -> "// Language not supported"
        }
    }
}
