package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.*
import com.example.data.service.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class TradingRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("smc_trading_prefs", Context.MODE_PRIVATE)

    private val binanceWsManager = BinanceWebSocketManager()
    private val marketEngine = DerivAndForexMarketEngine()
    private val smcStrategyEngine = SmcStrategyEngine()
    val telegramService = TelegramNotificationService()
    val soundAlertManager = SoundAlertManager(context)

    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Live State Flows
    private val _userPreferences = MutableStateFlow(loadPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private val _signals = MutableStateFlow<List<SmcSignal>>(emptyList())
    val signals: StateFlow<List<SmcSignal>> = _signals.asStateFlow()

    private val _connectionStatus = MutableStateFlow("Initialisation...")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private val _isLiveConnected = MutableStateFlow(false)
    val isLiveConnected: StateFlow<Boolean> = _isLiveConnected.asStateFlow()

    private val _lastAlertSentPair = MutableStateFlow<String?>(null)
    val lastAlertSentPair: StateFlow<String?> = _lastAlertSentPair.asStateFlow()

    // Cache of trade taken timestamps by symbol for 6h anti-doublon
    private val tradeTakenMap = ConcurrentHashMap<String, Long>()

    // Track recently alerted signal IDs to avoid spamming Telegram
    private val alertedSignalIds = ConcurrentHashMap<String, Long>()

    init {
        loadMutedTrades()
        setupListeners()
    }

    private fun setupListeners() {
        // Binance WS listener
        binanceWsManager.setOnPriceUpdateListener { rawTicker, price, change24h ->
            marketEngine.updateExternalPrice(rawTicker, price, change24h)
        }

        // Market Tick listener
        marketEngine.setOnPriceTickListener { symbol, price, change24h ->
            coroutineScope.launch {
                recalculateSignalForPair(symbol, price, change24h)
            }
        }

        // Collect WS connection status
        coroutineScope.launch {
            binanceWsManager.connectionStatus.collect { status ->
                _connectionStatus.value = status
            }
        }
        coroutineScope.launch {
            binanceWsManager.isLiveConnected.collect { isConn ->
                _isLiveConnected.value = isConn
            }
        }
    }

    fun start() {
        binanceWsManager.start()
        marketEngine.start()
        generateInitialSignals()
    }

    private fun generateInitialSignals() {
        val initialList = mutableListOf<SmcSignal>()
        val activeSymbols = _userPreferences.value.activeSymbols

        DefaultPairs.ALL_PAIRS.forEachIndexed { index, pair ->
            if (activeSymbols.contains(pair.symbol)) {
                val currentPrice = marketEngine.getPrice(pair.symbol)
                val change24h = marketEngine.getChange24h(pair.symbol)
                val tradeTakenTime = tradeTakenMap[pair.symbol]

                // Pre-configure realistic high confluences (Sniper 4/4 on key pairs, Good Setup 3/4 on others)
                val forceCount = when (index % 3) {
                    0 -> 4 // 4/4 Sniper
                    1 -> 3 // 3/4 Bon Setup
                    else -> 2 // 2/4 Watchlist
                }

                val signal = smcStrategyEngine.evaluateSignal(
                    pair = pair,
                    currentPrice = currentPrice,
                    priceChange24h = change24h,
                    timeframe = SignalTimeframe.H1,
                    existingTradeTakenTimestamp = tradeTakenTime,
                    forceConfluenceCount = forceCount
                )
                initialList.add(signal)
            }
        }

        // Sort by confluence score descending (Sniper 4/4 first)
        _signals.value = initialList.sortedByDescending { it.checklist.trueConfluenceCount }
    }

    private fun recalculateSignalForPair(symbol: String, price: Double, change24h: Double) {
        val pair = DefaultPairs.ALL_PAIRS.find { it.symbol == symbol } ?: return
        if (!_userPreferences.value.activeSymbols.contains(symbol)) return

        val currentList = _signals.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.pair.symbol == symbol }
        val existing = if (existingIndex >= 0) currentList[existingIndex] else null
        val tradeTakenTime = tradeTakenMap[symbol]

        val updatedSignal = smcStrategyEngine.evaluateSignal(
            pair = pair,
            currentPrice = price,
            priceChange24h = change24h,
            timeframe = existing?.timeframe ?: SignalTimeframe.H1,
            existingTradeTakenTimestamp = tradeTakenTime,
            forceConfluenceCount = existing?.checklist?.trueConfluenceCount
        )

        if (existingIndex >= 0) {
            currentList[existingIndex] = updatedSignal
        } else {
            currentList.add(updatedSignal)
        }

        _signals.value = currentList.sortedByDescending { it.checklist.trueConfluenceCount }

        // Check for automatic Telegram notification trigger
        checkAutoTelegramAlert(updatedSignal)
    }

    private fun checkAutoTelegramAlert(signal: SmcSignal) {
        val prefs = _userPreferences.value
        if (!prefs.telegramAlertsEnabled || prefs.telegramBotToken.isBlank() || prefs.telegramChatId.isBlank()) {
            return
        }

        // Check 6h anti-doublon mute
        if (signal.isMuted) return

        // Check alert level threshold
        val isLevelEnabled = when (signal.confluenceLevel) {
            ConfluenceLevel.SNIPER -> prefs.alertOnSniper
            ConfluenceLevel.GOOD_SETUP -> prefs.alertOnGoodSetup
            ConfluenceLevel.WATCHLIST -> prefs.alertOnWatchlist
        }
        if (!isLevelEnabled) return

        // Check if recently alerted to avoid repeated spam (1 hour debounce per pair)
        val lastAlert = alertedSignalIds[signal.pair.symbol] ?: 0L
        val oneHourMillis = 60 * 60 * 1000L
        if (System.currentTimeMillis() - lastAlert < oneHourMillis) return

        // Dispatch alert
        alertedSignalIds[signal.pair.symbol] = System.currentTimeMillis()
        coroutineScope.launch {
            if (prefs.soundAlertsEnabled) {
                soundAlertManager.playSniperAlertTone()
            }
            telegramService.sendSignalAlert(
                botToken = prefs.telegramBotToken,
                chatId = prefs.telegramChatId,
                signal = signal
            )
            _lastAlertSentPair.value = signal.pair.symbol
        }
    }

    /**
     * Mark a trade as taken: sets 6h anti-doublon mute.
     */
    fun toggleTradeTaken(symbol: String) {
        val currentMute = tradeTakenMap[symbol]
        val sixHoursMillis = 6 * 60 * 60 * 1000L

        if (currentMute != null && (System.currentTimeMillis() - currentMute) < sixHoursMillis) {
            // Already muted -> Unmute
            tradeTakenMap.remove(symbol)
        } else {
            // Set 6h mute
            tradeTakenMap[symbol] = System.currentTimeMillis()
            if (_userPreferences.value.soundAlertsEnabled) {
                soundAlertManager.playSweepTone()
            }
        }
        saveMutedTrades()

        // Update signals list to reflect muted state
        val updated = _signals.value.map { sig ->
            if (sig.pair.symbol == symbol) {
                sig.copy(tradeTakenTimestamp = tradeTakenMap[symbol])
            } else sig
        }
        _signals.value = updated
    }

    /**
     * Manual dispatch of a signal to Telegram on user click.
     */
    suspend fun sendTelegramAlertManual(signal: SmcSignal): Result<String> {
        val prefs = _userPreferences.value
        val result = telegramService.sendSignalAlert(
            botToken = prefs.telegramBotToken,
            chatId = prefs.telegramChatId,
            signal = signal
        )
        if (result.isSuccess) {
            _lastAlertSentPair.value = signal.pair.symbol
            if (prefs.soundAlertsEnabled) {
                soundAlertManager.playSweepTone()
            }
        }
        return result
    }

    fun updatePreferences(newPrefs: UserPreferences) {
        _userPreferences.value = newPrefs
        savePreferences(newPrefs)
        generateInitialSignals()
    }

    private fun loadPreferences(): UserPreferences {
        val token = prefs.getString("tg_token", "") ?: ""
        val chatId = prefs.getString("tg_chat_id", "") ?: ""
        val alertsEnabled = prefs.getBoolean("tg_enabled", false)
        val alertSniper = prefs.getBoolean("alert_sniper", true)
        val alertGood = prefs.getBoolean("alert_good", true)
        val alertWatch = prefs.getBoolean("alert_watch", false)
        val antiDoublonHours = prefs.getInt("anti_doublon_hours", 6)
        val autoMute = prefs.getBoolean("auto_mute", true)
        val sound = prefs.getBoolean("sound_enabled", true)
        val vibration = prefs.getBoolean("vibration_enabled", true)

        val symbolsSet = prefs.getStringSet("active_symbols", null) ?: DefaultPairs.ALL_PAIRS.map { it.symbol }.toSet()
        val timeframesSet = prefs.getStringSet("active_tfs", null) ?: setOf("15M", "30M", "1H", "4H", "1D")

        return UserPreferences(
            telegramBotToken = token,
            telegramChatId = chatId,
            telegramAlertsEnabled = alertsEnabled,
            alertOnSniper = alertSniper,
            alertOnGoodSetup = alertGood,
            alertOnWatchlist = alertWatch,
            antiDoublonMuteHours = antiDoublonHours,
            autoMuteOnTradeTaken = autoMute,
            soundAlertsEnabled = sound,
            vibrationEnabled = vibration,
            activeSymbols = symbolsSet,
            activeTimeframes = timeframesSet
        )
    }

    private fun savePreferences(userPrefs: UserPreferences) {
        prefs.edit().apply {
            putString("tg_token", userPrefs.telegramBotToken)
            putString("tg_chat_id", userPrefs.telegramChatId)
            putBoolean("tg_enabled", userPrefs.telegramAlertsEnabled)
            putBoolean("alert_sniper", userPrefs.alertOnSniper)
            putBoolean("alert_good", userPrefs.alertOnGoodSetup)
            putBoolean("alert_watch", userPrefs.alertOnWatchlist)
            putInt("anti_doublon_hours", userPrefs.antiDoublonMuteHours)
            putBoolean("auto_mute", userPrefs.autoMuteOnTradeTaken)
            putBoolean("sound_enabled", userPrefs.soundAlertsEnabled)
            putBoolean("vibration_enabled", userPrefs.vibrationEnabled)
            putStringSet("active_symbols", userPrefs.activeSymbols)
            putStringSet("active_tfs", userPrefs.activeTimeframes)
            apply()
        }
    }

    private fun loadMutedTrades() {
        val jsonStr = prefs.getString("muted_trades_json", "{}") ?: "{}"
        try {
            val json = JSONObject(jsonStr)
            json.keys().forEach { key ->
                tradeTakenMap[key] = json.getLong(key)
            }
        } catch (e: Exception) {
            Log.e("TradingRepo", "Failed to load muted trades", e)
        }
    }

    private fun saveMutedTrades() {
        val json = JSONObject()
        tradeTakenMap.forEach { (key, value) ->
            json.put(key, value)
        }
        prefs.edit().putString("muted_trades_json", json.toString()).apply()
    }

    fun stop() {
        binanceWsManager.stop()
        marketEngine.stop()
        soundAlertManager.release()
    }
}
