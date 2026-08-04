package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.SavedRequestEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.*
import com.example.data.repository.IdxRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class IdxViewModel(application: Application) : AndroidViewModel(application) {

    val repository = IdxRepository()

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "idx_wrapper_database"
    ).fallbackToDestructiveMigration().build()

    private val watchlistDao = db.watchlistDao()
    private val savedRequestDao = db.savedRequestDao()

    // 1. Live Market Ticker Stream
    val stocksStream: StateFlow<List<IdxStock>> = repository.getLiveMarketStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val indexSummaryState: StateFlow<IdxIndexSummary> = flow {
        while (true) {
            emit(repository.getCurrentIndexSummary())
            kotlinx.coroutines.delay(2000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IdxIndexSummary())

    // 2. Selected Stock & Detail state
    private val _selectedStock = MutableStateFlow<IdxStock?>(null)
    val selectedStock: StateFlow<IdxStock?> = _selectedStock.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow("1M")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

    private val _historicalCandles = MutableStateFlow<List<IdxCandle>>(emptyList())
    val historicalCandles: StateFlow<List<IdxCandle>> = _historicalCandles.asStateFlow()

    private val _orderBook = MutableStateFlow<List<OrderBookLevel>>(emptyList())
    val orderBook: StateFlow<List<OrderBookLevel>> = _orderBook.asStateFlow()

    private val _brokerFlow = MutableStateFlow<List<IdxBrokerSummary>>(emptyList())
    val brokerFlow: StateFlow<List<IdxBrokerSummary>> = _brokerFlow.asStateFlow()

    // 3. API Explorer & Sandbox State
    val apiEndpoints: List<IdxApiEndpoint> = repository.getAllApiEndpoints()

    private val _selectedEndpoint = MutableStateFlow(apiEndpoints.first())
    val selectedEndpoint: StateFlow<IdxApiEndpoint> = _selectedEndpoint.asStateFlow()

    private val _currentApiRequest = MutableStateFlow(
        IdxApiRequest(endpoint = apiEndpoints.first())
    )
    val currentApiRequest: StateFlow<IdxApiRequest> = _currentApiRequest.asStateFlow()

    private val _lastApiResponse = MutableStateFlow<IdxApiResponse?>(null)
    val lastApiResponse: StateFlow<IdxApiResponse?> = _lastApiResponse.asStateFlow()

    private val _isLoadingResponse = MutableStateFlow(false)
    val isLoadingResponse: StateFlow<Boolean> = _isLoadingResponse.asStateFlow()

    private val _showCodeSnippetDialog = MutableStateFlow(false)
    val showCodeSnippetDialog: StateFlow<Boolean> = _showCodeSnippetDialog.asStateFlow()

    // 4. Auth & Security Session
    private val _authSession = MutableStateFlow(IdxAuthSession())
    val authSession: StateFlow<IdxAuthSession> = _authSession.asStateFlow()

    // 5. Room Watchlist & Saved Requests
    val watchlistItems: StateFlow<List<WatchlistEntity>> = watchlistDao.getAllWatchlist()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedRequests: StateFlow<List<SavedRequestEntity>> = savedRequestDao.getAllSavedRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 6. Search & Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<ApiCategory?>(null)
    val selectedCategoryFilter: StateFlow<ApiCategory?> = _selectedCategoryFilter.asStateFlow()

    init {
        // Select initial stock BBCA
        selectStockBySymbol("BBCA")
    }

    fun selectStockBySymbol(symbol: String) {
        val stock = stocksStream.value.find { it.symbol == symbol }
            ?: repository.getLiveMarketStream().toString().let {
                // Fallback default stock if stream not initialized
                IdxStock("BBCA", "Bank Central Asia Tbk", "Financials", 10250.0, 150.0, 1.49, 10100.0, 10300.0, 10075.0, 10100.0, 42150000, 432.5, 1263.2, 10225.0, 12400, 10250.0, 15800)
            }
        _selectedStock.value = stock
        refreshStockDetails(symbol, _selectedTimeframe.value)
    }

    fun updateTimeframe(timeframe: String) {
        _selectedTimeframe.value = timeframe
        _selectedStock.value?.symbol?.let { refreshStockDetails(it, timeframe) }
    }

    private fun refreshStockDetails(symbol: String, timeframe: String) {
        _historicalCandles.value = repository.getHistoricalCandles(symbol, timeframe)
        _orderBook.value = repository.getOrderBook(symbol)
        _brokerFlow.value = repository.getBrokerFlow(symbol)
    }

    fun selectEndpoint(endpoint: IdxApiEndpoint) {
        _selectedEndpoint.value = endpoint
        _currentApiRequest.value = IdxApiRequest(
            endpoint = endpoint,
            pathParamsMap = endpoint.pathParams.associate { it.name to it.defaultValue },
            queryParamsMap = endpoint.queryParams.associate { it.name to it.defaultValue },
            requestBodyJson = endpoint.sampleBodyJson,
            authToken = _authSession.value.bearerToken
        )
    }

    fun updateParamValue(paramName: String, value: String, isPath: Boolean) {
        val current = _currentApiRequest.value
        val updatedMap = if (isPath) {
            current.pathParamsMap.toMutableMap().apply { put(paramName, value) }
        } else {
            current.queryParamsMap.toMutableMap().apply { put(paramName, value) }
        }

        _currentApiRequest.value = if (isPath) {
            current.copy(pathParamsMap = updatedMap)
        } else {
            current.copy(queryParamsMap = updatedMap)
        }
    }

    fun updateAuthTokenInRequest(token: String) {
        _currentApiRequest.value = _currentApiRequest.value.copy(authToken = token)
    }

    fun executeCurrentRequest() {
        viewModelScope.launch {
            _isLoadingResponse.value = true
            val response = repository.executeApiRequest(_currentApiRequest.value)
            _lastApiResponse.value = response
            _isLoadingResponse.value = false

            // Deduct rate limit remaining
            _authSession.value = _authSession.value.copy(
                rateLimitRemaining = (_authSession.value.rateLimitRemaining - 1).coerceAtLeast(0)
            )
        }
    }

    fun setShowCodeSnippetDialog(show: Boolean) {
        _showCodeSnippetDialog.value = show
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: ApiCategory?) {
        _selectedCategoryFilter.value = category
    }

    // Watchlist Room DB ops
    fun toggleWatchlist(stock: IdxStock) {
        viewModelScope.launch {
            val exists = watchlistItems.value.any { it.symbol == stock.symbol }
            if (exists) {
                watchlistDao.deleteBySymbol(stock.symbol)
            } else {
                watchlistDao.insertWatchlist(
                    WatchlistEntity(
                        symbol = stock.symbol,
                        name = stock.name,
                        sector = stock.sector,
                        targetAlertHigh = stock.lastPrice * 1.10,
                        targetAlertLow = stock.lastPrice * 0.90
                    )
                )
            }
        }
    }

    // Regenerate auth API keys
    fun regenerateApiKeys() {
        val newKey = "idx_key_prod_${(100000000..999999999).random()}"
        val newSecret = "sec_" + java.util.UUID.randomUUID().toString().replace("-", "").take(24)
        val newBearer = "Bearer eyJhbGciOiJIUzI1NiJ9.${java.util.UUID.randomUUID().toString().take(16)}.sig"

        _authSession.value = _authSession.value.copy(
            apiKey = newKey,
            apiSecret = newSecret,
            bearerToken = newBearer,
            rateLimitRemaining = 1000
        )

        // Update token in request builder as well
        updateAuthTokenInRequest(newBearer)
    }
}
