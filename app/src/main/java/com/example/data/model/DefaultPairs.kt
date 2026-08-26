package com.example.data.model

object DefaultPairs {
    val ALL_PAIRS: List<TradingPair> = listOf(
        // 1. Crypto en direct (Binance WebSockets)
        TradingPair(
            symbol = "BTC/USDT",
            rawTicker = "BTCUSDT",
            name = "Bitcoin Spot",
            category = MarketCategory.CRYPTO,
            decimals = 2,
            isDirectBinanceWs = true,
            initialPrice = 64350.00
        ),
        TradingPair(
            symbol = "ETH/USDT",
            rawTicker = "ETHUSDT",
            name = "Ethereum Spot",
            category = MarketCategory.CRYPTO,
            decimals = 2,
            isDirectBinanceWs = true,
            initialPrice = 3480.50
        ),
        TradingPair(
            symbol = "SOL/USDT",
            rawTicker = "SOLUSDT",
            name = "Solana Spot",
            category = MarketCategory.CRYPTO,
            decimals = 2,
            isDirectBinanceWs = true,
            initialPrice = 148.75
        ),
        TradingPair(
            symbol = "BNB/USDT",
            rawTicker = "BNBUSDT",
            name = "BNB Spot",
            category = MarketCategory.CRYPTO,
            decimals = 2,
            isDirectBinanceWs = true,
            initialPrice = 575.20
        ),

        // 2. Forex Institutionnel
        TradingPair(
            symbol = "EUR/USD",
            rawTicker = "EURUSD",
            name = "Euro / Dollar US",
            category = MarketCategory.FOREX,
            decimals = 5,
            initialPrice = 1.08450
        ),
        TradingPair(
            symbol = "GBP/USD",
            rawTicker = "GBPUSD",
            name = "Livre Sterling / Dollar US",
            category = MarketCategory.FOREX,
            decimals = 5,
            initialPrice = 1.29820
        ),
        TradingPair(
            symbol = "USD/JPY",
            rawTicker = "USDJPY",
            name = "Dollar US / Yen Japonais",
            category = MarketCategory.FOREX,
            decimals = 3,
            initialPrice = 154.650
        ),
        TradingPair(
            symbol = "AUD/USD",
            rawTicker = "AUDUSD",
            name = "Dollar Australien / Dollar US",
            category = MarketCategory.FOREX,
            decimals = 5,
            initialPrice = 0.66540
        ),
        TradingPair(
            symbol = "USD/CAD",
            rawTicker = "USDCAD",
            name = "Dollar US / Dollar Canadien",
            category = MarketCategory.FOREX,
            decimals = 5,
            initialPrice = 1.37120
        ),
        TradingPair(
            symbol = "USD/CHF",
            rawTicker = "USDCHF",
            name = "Dollar US / Franc Suisse",
            category = MarketCategory.FOREX,
            decimals = 5,
            initialPrice = 0.88420
        ),

        // 3. Matières Premières
        TradingPair(
            symbol = "XAU/USD",
            rawTicker = "XAUUSD",
            name = "Or Spot (Gold)",
            category = MarketCategory.COMMODITIES,
            decimals = 2,
            initialPrice = 2512.40
        ),
        TradingPair(
            symbol = "XAG/USD",
            rawTicker = "XAGUSD",
            name = "Argent Spot (Silver)",
            category = MarketCategory.COMMODITIES,
            decimals = 3,
            initialPrice = 29.850
        ),
        TradingPair(
            symbol = "USOIL",
            rawTicker = "USOIL",
            name = "Pétrole WTI Brut",
            category = MarketCategory.COMMODITIES,
            decimals = 2,
            initialPrice = 77.45
        ),

        // 4. Indices Synthétiques Deriv Volatility
        TradingPair(
            symbol = "V10",
            rawTicker = "R_10",
            name = "Volatility 10 Index",
            category = MarketCategory.SYNTHETICS,
            decimals = 3,
            initialPrice = 6420.150,
            isDeriv = true
        ),
        TradingPair(
            symbol = "V25",
            rawTicker = "R_25",
            name = "Volatility 25 Index",
            category = MarketCategory.SYNTHETICS,
            decimals = 3,
            initialPrice = 1890.420,
            isDeriv = true
        ),
        TradingPair(
            symbol = "V50",
            rawTicker = "R_50",
            name = "Volatility 50 Index",
            category = MarketCategory.SYNTHETICS,
            decimals = 4,
            initialPrice = 284.1500,
            isDeriv = true
        ),
        TradingPair(
            symbol = "V75",
            rawTicker = "R_75",
            name = "Volatility 75 Index",
            category = MarketCategory.SYNTHETICS,
            decimals = 2,
            initialPrice = 478520.00,
            isDeriv = true
        ),
        TradingPair(
            symbol = "V100",
            rawTicker = "R_100",
            name = "Volatility 100 Index",
            category = MarketCategory.SYNTHETICS,
            decimals = 2,
            initialPrice = 1250.80,
            isDeriv = true
        ),
        TradingPair(
            symbol = "V10 (1s)",
            rawTicker = "1HZ10V",
            name = "Volatility 10 (1s) Index",
            category = MarketCategory.SYNTHETICS,
            decimals = 3,
            initialPrice = 8450.320,
            isDeriv = true
        ),
        TradingPair(
            symbol = "V25 (1s)",
            rawTicker = "1HZ25V",
            name = "Volatility 25 (1s) Index",
            category = MarketCategory.SYNTHETICS,
            decimals = 3,
            initialPrice = 3210.840,
            isDeriv = true
        ),
        TradingPair(
            symbol = "V75 (1s)",
            rawTicker = "1HZ75V",
            name = "Volatility 75 (1s) Index",
            category = MarketCategory.SYNTHETICS,
            decimals = 2,
            initialPrice = 365400.00,
            isDeriv = true
        ),
        TradingPair(
            symbol = "V100 (1s)",
            rawTicker = "1HZ100V",
            name = "Volatility 100 (1s) Index",
            category = MarketCategory.SYNTHETICS,
            decimals = 2,
            initialPrice = 2140.60,
            isDeriv = true
        )
    )
}
