package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.TradingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SignalFilterTab(val label: String, val badge: String? = null) {
    ALL("Tous", null),
    SNIPER("🎯 Sniper 4/4", "95-100%"),
    GOOD_SETUP("⚡ Moyennes 3/4", "75-90%"),
    WATCHLIST("👁️ Watchlist 2/4", "60-70%"),
    CRYPTO("Crypto", null),
    FOREX("Forex", null),
    COMMODITIES("Matières", null),
    SYNTHETICS("Deriv", null)
}

data class UiNotificationMessage(
    val message: String,
    val isError: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

class TradingViewModel(application: Application) : AndroidViewModel(application) {
    val repository = TradingRepository(application.applicationContext)

    private val _selectedFilterTab = MutableStateFlow(SignalFilterTab.ALL)
    val selectedFilterTab: StateFlow<SignalFilterTab> = _selectedFilterTab.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow(SignalTimeframe.H1)
    val selectedTimeframe: StateFlow<SignalTimeframe> = _selectedTimeframe.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSignalForDetail = MutableStateFlow<SmcSignal?>(null)
    val selectedSignalForDetail: StateFlow<SmcSignal?> = _selectedSignalForDetail.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _uiNotification = MutableStateFlow<UiNotificationMessage?>(null)
    val uiNotification: StateFlow<UiNotificationMessage?> = _uiNotification.asStateFlow()

    private val _isTestingTelegram = MutableStateFlow(false)
    val isTestingTelegram: StateFlow<Boolean> = _isTestingTelegram.asStateFlow()

    private val _telegramTestResult = MutableStateFlow<String?>(null)
    val telegramTestResult: StateFlow<String?> = _telegramTestResult.asStateFlow()

    init {
        repository.start()
    }

    // Filtered signals computed reactively
    val filteredSignals: StateFlow<List<SmcSignal>> = combine(
        repository.signals,
        _selectedFilterTab,
        _searchQuery
    ) { signals, filterTab, query ->
        signals.filter { signal ->
            val matchesQuery = query.isBlank() ||
                    signal.pair.symbol.contains(query, ignoreCase = true) ||
                    signal.pair.name.contains(query, ignoreCase = true)

            val matchesFilter = when (filterTab) {
                SignalFilterTab.ALL -> true
                SignalFilterTab.SNIPER -> signal.confluenceLevel == ConfluenceLevel.SNIPER
                SignalFilterTab.GOOD_SETUP -> signal.confluenceLevel == ConfluenceLevel.GOOD_SETUP
                SignalFilterTab.WATCHLIST -> signal.confluenceLevel == ConfluenceLevel.WATCHLIST
                SignalFilterTab.CRYPTO -> signal.pair.category == MarketCategory.CRYPTO
                SignalFilterTab.FOREX -> signal.pair.category == MarketCategory.FOREX
                SignalFilterTab.COMMODITIES -> signal.pair.category == MarketCategory.COMMODITIES
                SignalFilterTab.SYNTHETICS -> signal.pair.category == MarketCategory.SYNTHETICS
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // KPI Counters
    val sniperCount: StateFlow<Int> = repository.signals.map { list ->
        list.count { it.confluenceLevel == ConfluenceLevel.SNIPER }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val goodSetupCount: StateFlow<Int> = repository.signals.map { list ->
        list.count { it.confluenceLevel == ConfluenceLevel.GOOD_SETUP }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val watchlistCount: StateFlow<Int> = repository.signals.map { list ->
        list.count { it.confluenceLevel == ConfluenceLevel.WATCHLIST }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val mutedCount: StateFlow<Int> = repository.signals.map { list ->
        list.count { it.isMuted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setFilterTab(tab: SignalFilterTab) {
        _selectedFilterTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectSignalForDetail(signal: SmcSignal?) {
        _selectedSignalForDetail.value = signal
    }

    fun openSettings() {
        _isSettingsOpen.value = true
        _telegramTestResult.value = null
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun toggleTradeTaken(symbol: String) {
        repository.toggleTradeTaken(symbol)
        val sig = repository.signals.value.find { it.pair.symbol == symbol }
        val isNowMuted = sig?.isMuted == true
        if (isNowMuted) {
            showNotification("Sourdine 6h activée pour $symbol (Anti-doublon)")
        } else {
            showNotification("Sourdine désactivée pour $symbol")
        }
    }

    fun sendSignalToTelegram(signal: SmcSignal) {
        viewModelScope.launch {
            val result = repository.sendTelegramAlertManual(signal)
            if (result.isSuccess) {
                showNotification("Alerte ${signal.pair.symbol} envoyée sur Telegram ! ✈️", isError = false)
            } else {
                showNotification(result.exceptionOrNull()?.message ?: "Échec d'envoi Telegram", isError = true)
            }
        }
    }

    fun testTelegramConnection(botToken: String, chatId: String) {
        viewModelScope.launch {
            _isTestingTelegram.value = true
            _telegramTestResult.value = null
            val result = repository.telegramService.testConnection(botToken, chatId)
            _isTestingTelegram.value = false
            if (result.isSuccess) {
                _telegramTestResult.value = "✅ ${result.getOrNull()}"
                showNotification("Test Telegram Réussi !", isError = false)
            } else {
                _telegramTestResult.value = "❌ ${result.exceptionOrNull()?.message}"
                showNotification("Échec du test Telegram", isError = true)
            }
        }
    }

    fun updatePreferences(newPrefs: UserPreferences) {
        repository.updatePreferences(newPrefs)
        showNotification("Réglages enregistrés avec succès")
    }

    fun showNotification(message: String, isError: Boolean = false) {
        _uiNotification.value = UiNotificationMessage(message, isError)
    }

    fun dismissNotification() {
        _uiNotification.value = null
    }

    override fun onCleared() {
        super.onCleared()
        repository.stop()
    }
}
