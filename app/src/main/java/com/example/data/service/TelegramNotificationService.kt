package com.example.data.service

import android.util.Log
import com.example.data.model.SmcSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TelegramNotificationService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Formats and dispatches a detailed SMC Signal alert to Telegram.
     */
    suspend fun sendSignalAlert(
        botToken: String,
        chatId: String,
        signal: SmcSignal
    ): Result<String> = withContext(Dispatchers.IO) {
        if (botToken.isBlank() || chatId.isBlank()) {
            return@withContext Result.failure(Exception("Bot Token ou Chat ID manquant dans les réglages"))
        }

        val cleanToken = botToken.trim()
        val cleanChatId = chatId.trim()

        val directionEmoji = if (signal.direction.isBuy) "🟢 ACHAT / BUY" else "🔴 VENTE / SELL"
        val confluenceBadge = "${signal.confluenceLevel.iconEmoji} ${signal.confluenceLevel.shortBadge} (${signal.confluenceLevel.scoreRange})"
        val formatPrice = { p: Double -> "%.${signal.pair.decimals}f".format(p) }

        val messageText = buildString {
            appendLine("═══════════════════════")
            appendLine("$confluenceBadge")
            appendLine("<b>PAIRE :</b> <code>${signal.pair.symbol}</code> (${signal.timeframe.label})")
            appendLine("<b>MARCHÉ :</b> ${signal.pair.category.displayName}")
            appendLine("<b>DIRECTION :</b> $directionEmoji")
            appendLine("═══════════════════════")
            appendLine("📍 <b>Prix d\'Entrée :</b> <code>${formatPrice(signal.entryPrice)}</code>")
            appendLine("🛑 <b>Stop Loss :</b> <code>${formatPrice(signal.stopLoss)}</code>")
            appendLine("🎯 <b>TP1 (Liquidité Interne) :</b> <code>${formatPrice(signal.tp1Resting.price)}</code> (+${"%.2f".format(signal.tp1Resting.distancePercent)}%)")
            appendLine("🎯 <b>TP2 (Liquidité Majeure) :</b> <code>${formatPrice(signal.tp2Resting.price)}</code> (+${"%.2f".format(signal.tp2Resting.distancePercent)}%)")
            appendLine("⚖️ <b>Ratio R:R :</b> 1:${signal.riskRewardRatio}")
            appendLine("───────────────────────")
            appendLine("💧 <b>ANALYSE LIQUIDITÉ & MITIGATION :</b>")
            appendLine("• <b>Sweep :</b> ${signal.checklist.sweepDetail.sweepType} @ ${formatPrice(signal.checklist.sweepDetail.sweptPrice)} (Rejet validé 💧)")
            appendLine("• <b>FVG Récent :</b> ${"%.1f".format(signal.checklist.fvgObDetail.recentFvgAgeHours)}h non mitigé ⚡ [${formatPrice(signal.checklist.fvgObDetail.recentFvgLow)} - ${formatPrice(signal.checklist.fvgObDetail.recentFvgHigh)}]")
            appendLine("• <b>FVG Ancien :</b> ${"%.1f".format(signal.checklist.fvgObDetail.ancientFvgAgeHours)}h déjà mitigé (100% comblé)")
            appendLine("• <b>Order Block :</b> ${signal.checklist.fvgObDetail.orderBlockName}")
            appendLine("• <b>Fibonacci :</b> ${signal.checklist.fiboDetail.zoneName} (${"%.1f".format(signal.checklist.fiboDetail.fiboLevelPercent)}% OTE)")
            appendLine("• <b>Tendance HTF :</b> ${signal.checklist.htfDescription}")
            appendLine("═══════════════════════")
            appendLine("<i>⚡ SMC Liquidity Signals Engine • Pas de bruit, 100% Confluences</i>")
        }

        try {
            val url = "https://api.telegram.org/bot$cleanToken/sendMessage"
            val jsonBody = JSONObject().apply {
                put("chat_id", cleanChatId)
                put("text", messageText)
                put("parse_mode", "HTML")
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Log.d("TelegramAlert", "Alert sent successfully to Telegram")
                Result.success("Alerte envoyée avec succès sur Telegram !")
            } else {
                Log.e("TelegramAlert", "Telegram API error: $responseString")
                val errorMsg = try {
                    JSONObject(responseString).optString("description", "Erreur HTTP ${response.code}")
                } catch (e: Exception) {
                    "Erreur HTTP ${response.code}"
                }
                Result.failure(Exception("Échec Telegram: $errorMsg"))
            }
        } catch (e: Exception) {
            Log.e("TelegramAlert", "Network failure sending alert", e)
            Result.failure(Exception("Erreur réseau: ${e.localizedMessage ?: "Vérifiez la connexion"}"))
        }
    }

    /**
     * Tests connection with user's Telegram Bot Token & Chat ID.
     */
    suspend fun testConnection(botToken: String, chatId: String): Result<String> = withContext(Dispatchers.IO) {
        if (botToken.isBlank() || chatId.isBlank()) {
            return@withContext Result.failure(Exception("Veuillez renseigner le Bot Token et le Chat ID"))
        }

        val testMessage = buildString {
            appendLine("🔔 <b>TEST DE CONNEXION RÉUSSI !</b>")
            appendLine("Le bot Telegram SMC Liquidity Signals est correctement configuré.")
            appendLine("Vous recevrez automatiquement les alertes de confluences 4/4 et liquidités.")
        }

        try {
            val url = "https://api.telegram.org/bot${botToken.trim()}/sendMessage"
            val jsonBody = JSONObject().apply {
                put("chat_id", chatId.trim())
                put("text", testMessage)
                put("parse_mode", "HTML")
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success("Connexion Telegram établie avec succès !")
            } else {
                val errorDesc = try {
                    JSONObject(responseString).optString("description", "Code ${response.code}")
                } catch (e: Exception) {
                    "Erreur HTTP ${response.code}"
                }
                Result.failure(Exception(errorDesc))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Impossible de joindre l'API Telegram : ${e.message}"))
        }
    }
}
