package com.example.data.service

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class BinanceWebSocketManager {
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private var webSocket: WebSocket? = null
    private var isConnected = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _connectionStatus = MutableStateFlow("Connexion...")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private val _isLiveConnected = MutableStateFlow(false)
    val isLiveConnected: StateFlow<Boolean> = _isLiveConnected.asStateFlow()

    private var onPriceUpdateListener: ((rawTicker: String, price: Double, change24h: Double) -> Unit)? = null

    fun setOnPriceUpdateListener(listener: (rawTicker: String, price: Double, change24h: Double) -> Unit) {
        this.onPriceUpdateListener = listener
    }

    fun start() {
        connect()
    }

    private fun connect() {
        val streamUrl = "wss://stream.binance.com:9443/ws/!miniTicker@arr"
        val request = Request.Builder().url(streamUrl).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                _isLiveConnected.value = true
                _connectionStatus.value = "Binance WS Connecté 🟢"
                Log.d("BinanceWS", "WebSocket Connected Successfully")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val jsonArray = JSONArray(text)
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val symbol = item.optString("s") // e.g. BTCUSDT
                        val closePrice = item.optString("c").toDoubleOrNull() ?: continue
                        val openPrice = item.optString("o").toDoubleOrNull() ?: closePrice
                        val change24h = if (openPrice != 0.0) {
                            ((closePrice - openPrice) / openPrice) * 100.0
                        } else 0.0

                        if (symbol in listOf("BTCUSDT", "ETHUSDT", "SOLUSDT", "BNBUSDT")) {
                            onPriceUpdateListener?.invoke(symbol, closePrice, change24h)
                        }
                    }
                } catch (e: Exception) {
                    // Single ticker fallback parse
                    try {
                        val obj = JSONObject(text)
                        val symbol = obj.optString("s")
                        val closePrice = obj.optString("c").toDoubleOrNull()
                        val change = obj.optString("P").toDoubleOrNull() ?: 0.0
                        if (symbol.isNotEmpty() && closePrice != null) {
                            onPriceUpdateListener?.invoke(symbol, closePrice, change)
                        }
                    } catch (ex: Exception) {
                        Log.e("BinanceWS", "Error parsing WS message", ex)
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                _isLiveConnected.value = false
                _connectionStatus.value = "Reconnexion Binance..."
                Log.w("BinanceWS", "WebSocket failure: ${t.message}. Retrying in 5s...")
                scheduleReconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                _isLiveConnected.value = false
                _connectionStatus.value = "WS Déconnecté"
            }
        })
    }

    private fun scheduleReconnect() {
        coroutineScope.launch {
            delay(5000)
            if (!isConnected) {
                connect()
            }
        }
    }

    fun stop() {
        try {
            webSocket?.close(1000, "App closed")
            client.dispatcher.executorService.shutdown()
        } catch (e: Exception) {
            Log.e("BinanceWS", "Error stopping WS", e)
        }
    }
}
