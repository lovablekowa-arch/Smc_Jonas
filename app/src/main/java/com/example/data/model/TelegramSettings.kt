package com.example.data.model

data class UserPreferences(
    // Telegram Configuration
    val telegramBotToken: String = "",
    val telegramChatId: String = "",
    val telegramAlertsEnabled: Boolean = false,

    // Confluence Alert Toggles
    val alertOnSniper: Boolean = true,      // 4/4 Hautes (95-100%)
    val alertOnGoodSetup: Boolean = true,   // 3/4 Moyennes (75-90%)
    val alertOnWatchlist: Boolean = false,  // 2/4 À Surveiller (60-70%)

    // Anti-doublons
    val antiDoublonMuteHours: Int = 6,
    val autoMuteOnTradeTaken: Boolean = true,

    // Sound & Vibration
    val soundAlertsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,

    // Market & Pair filters
    val activeSymbols: Set<String> = DefaultPairs.ALL_PAIRS.map { it.symbol }.toSet(),

    // Timeframes
    val activeTimeframes: Set<String> = setOf("15M", "30M", "1H", "4H", "1D")
)
