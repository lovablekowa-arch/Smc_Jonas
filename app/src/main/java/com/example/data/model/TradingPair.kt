package com.example.data.model

enum class MarketCategory(val displayName: String, val badgeColorHex: Long) {
    CRYPTO("Crypto", 0xFFF59E0B),
    FOREX("Forex", 0xFF3B82F6),
    COMMODITIES("Matières 1ères", 0xFFEAB308),
    SYNTHETICS("Deriv Synthétiques", 0xFFEC4899)
}

data class TradingPair(
    val symbol: String,              // e.g. "BTC/USDT", "EUR/USD", "XAU/USD", "V75 (1s)"
    val rawTicker: String,           // e.g. "BTCUSDT", "EURUSD", "XAUUSD", "R_75"
    val name: String,                // e.g. "Bitcoin", "Euro / US Dollar", "Gold Spot", "Volatility 75 (1s)"
    val category: MarketCategory,
    val decimals: Int = 2,
    val isDirectBinanceWs: Boolean = false,
    val initialPrice: Double,
    val isDeriv: Boolean = false
) {
    val formattedSymbol: String get() = symbol
}
