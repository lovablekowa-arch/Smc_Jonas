package com.example.data.service

import com.example.data.model.DefaultPairs
import com.example.data.model.TradingPair
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class DerivAndForexMarketEngine {
    private val currentPrices = ConcurrentHashMap<String, Double>()
    private val change24hMap = ConcurrentHashMap<String, Double>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var job: Job? = null

    private var onPriceTickListener: ((symbol: String, price: Double, change24h: Double) -> Unit)? = null

    init {
        DefaultPairs.ALL_PAIRS.forEach { pair ->
            currentPrices[pair.symbol] = pair.initialPrice
            change24hMap[pair.symbol] = Random.nextDouble(-2.5, 3.8)
        }
    }

    fun setOnPriceTickListener(listener: (symbol: String, price: Double, change24h: Double) -> Unit) {
        this.onPriceTickListener = listener
    }

    fun updateExternalPrice(rawTicker: String, price: Double, change24h: Double) {
        val matchingPair = DefaultPairs.ALL_PAIRS.find { it.rawTicker.equals(rawTicker, ignoreCase = true) }
        if (matchingPair != null) {
            currentPrices[matchingPair.symbol] = price
            change24hMap[matchingPair.symbol] = change24h
            onPriceTickListener?.invoke(matchingPair.symbol, price, change24h)
        }
    }

    fun start() {
        job?.cancel()
        job = coroutineScope.launch {
            while (isActive) {
                // Ticks for Forex, Commodities, Synthetics
                DefaultPairs.ALL_PAIRS.filter { !it.isDirectBinanceWs }.forEach { pair ->
                    val basePrice = currentPrices[pair.symbol] ?: pair.initialPrice
                    val volatilityFactor = when {
                        pair.symbol.contains("V100 (1s)") || pair.symbol.contains("V75 (1s)") -> 0.0008
                        pair.symbol.contains("V75") || pair.symbol.contains("V100") -> 0.0005
                        pair.symbol.contains("V50") || pair.symbol.contains("V25") || pair.symbol.contains("V10") -> 0.0003
                        pair.symbol == "XAU/USD" -> 0.0002
                        pair.symbol == "XAG/USD" || pair.symbol == "USOIL" -> 0.0003
                        else -> 0.00005 // Forex pips
                    }

                    val deltaPercent = (Random.nextDouble(-1.0, 1.0) * volatilityFactor)
                    val newPrice = basePrice * (1.0 + deltaPercent)
                    val baseChange = change24hMap[pair.symbol] ?: 0.5
                    val newChange = baseChange + (deltaPercent * 50)

                    currentPrices[pair.symbol] = newPrice
                    change24hMap[pair.symbol] = newChange

                    onPriceTickListener?.invoke(pair.symbol, newPrice, newChange)
                }
                delay(1200) // Realistic tick frequency
            }
        }
    }

    fun getPrice(symbol: String): Double {
        return currentPrices[symbol] ?: DefaultPairs.ALL_PAIRS.find { it.symbol == symbol }?.initialPrice ?: 100.0
    }

    fun getChange24h(symbol: String): Double {
        return change24hMap[symbol] ?: 0.0
    }

    fun stop() {
        job?.cancel()
    }
}
