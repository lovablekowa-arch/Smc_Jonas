package com.example.data.model

enum class SignalDirection(val labelFr: String, val shortLabel: String, val isBuy: Boolean) {
    BUY("ACHAT / LONG", "ACHAT", true),
    SELL("VENTE / SHORT", "VENTE", false)
}

enum class ConfluenceLevel(
    val titleFr: String,
    val shortBadge: String,
    val scoreRange: String,
    val requiredCount: Int,
    val iconEmoji: String,
    val accentColorHex: Long
) {
    SNIPER("Conditions Hautes / Sniper", "SNIPER 4/4", "95% - 100%", 4, "🎯", 0xFFFFB300),
    GOOD_SETUP("Conditions Moyennes / Bon Setup", "BON SETUP 3/4", "75% - 90%", 3, "⚡", 0xFF2979FF),
    WATCHLIST("À Surveiller / Watchlist", "WATCHLIST 2/4", "60% - 70%", 2, "👁️", 0xFFA855F7)
}

enum class SignalTimeframe(val label: String) {
    M15("15M"),
    M30("30M"),
    H1("1H"),
    H4("4H"),
    D1("1D")
}

data class FvgObDetail(
    val recentFvgAgeHours: Double,
    val recentFvgMitigated: Boolean, // false = non mitigé (prioritaire)
    val recentFvgLow: Double,
    val recentFvgHigh: Double,
    val ancientFvgAgeHours: Double,
    val ancientFvgMitigated: Boolean, // true = comblé à 100%
    val ancientFvgLow: Double,
    val ancientFvgHigh: Double,
    val orderBlockName: String,
    val orderBlockLow: Double,
    val orderBlockHigh: Double
) {
    val displaySummary: String
        get() = "FVG Récent ${"%.1f".format(recentFvgAgeHours)}h non mitigé + FVG Ancien ${"%.1f".format(ancientFvgAgeHours)}h déjà mitigé"
}

data class FibonacciDetail(
    val zoneName: String, // "Discount (< 50%)" or "Premium (> 50%)"
    val fiboLevelPercent: Double, // e.g. 62.0 for 62% OTE
    val swingLow: Double,
    val swingHigh: Double,
    val equilibrium50: Double
)

data class LiquiditySweepDetail(
    val sweepType: String, // "SSL (Sell-Side Liquidity)" or "BSL (Buy-Side Liquidity)"
    val sweptPrice: Double,
    val rejectionConfirmed: Boolean,
    val rejectionWickPips: Double,
    val sweepTimeAgoMinutes: Int
)

data class RestingLiquidityTarget(
    val targetName: String, // e.g. "Internal BSL Liquidity Pool" / "HTF Major High Liquidity"
    val price: Double,
    val distancePercent: Double,
    val isMajor: Boolean = false
)

data class ConfluenceChecklist(
    val htfTrendAligned: Boolean,
    val htfDescription: String,
    val fvgObValid: Boolean,
    val fvgObDetail: FvgObDetail,
    val fiboDiscountPremiumValid: Boolean,
    val fiboDetail: FibonacciDetail,
    val sweepValid: Boolean,
    val sweepDetail: LiquiditySweepDetail
) {
    val trueConfluenceCount: Int
        get() = (if (htfTrendAligned) 1 else 0) +
                (if (fvgObValid) 1 else 0) +
                (if (fiboDiscountPremiumValid) 1 else 0) +
                (if (sweepValid) 1 else 0)
}

data class SmcSignal(
    val id: String,
    val pair: TradingPair,
    val currentPrice: Double,
    val priceChange24h: Double,
    val direction: SignalDirection,
    val timeframe: SignalTimeframe,
    val entryPrice: Double,
    val stopLoss: Double,
    val tp1Resting: RestingLiquidityTarget,
    val tp2Resting: RestingLiquidityTarget,
    val riskRewardRatio: Double,
    val checklist: ConfluenceChecklist,
    val confluenceLevel: ConfluenceLevel,
    val createdAt: Long = System.currentTimeMillis(),
    val tradeTakenTimestamp: Long? = null
) {
    val isMuted: Boolean
        get() {
            if (tradeTakenTimestamp == null) return false
            val sixHoursMillis = 6 * 60 * 60 * 1000L
            return (System.currentTimeMillis() - tradeTakenTimestamp) < sixHoursMillis
        }

    val remainingMuteHours: Double
        get() {
            if (tradeTakenTimestamp == null) return 0.0
            val elapsed = System.currentTimeMillis() - tradeTakenTimestamp
            val remaining = (6 * 60 * 60 * 1000L) - elapsed
            return if (remaining > 0) remaining / (1000.0 * 60 * 60) else 0.0
        }
}
