import { TradingPair } from '../types/smc';

export const ALL_PAIRS: TradingPair[] = [
  // 1. Crypto en direct (Binance WebSockets)
  {
    symbol: 'BTC/USDT',
    rawTicker: 'BTCUSDT',
    name: 'Bitcoin Spot',
    category: 'CRYPTO',
    decimals: 2,
    isDirectBinanceWs: true,
    initialPrice: 64350.00
  },
  {
    symbol: 'ETH/USDT',
    rawTicker: 'ETHUSDT',
    name: 'Ethereum Spot',
    category: 'CRYPTO',
    decimals: 2,
    isDirectBinanceWs: true,
    initialPrice: 3480.50
  },
  {
    symbol: 'SOL/USDT',
    rawTicker: 'SOLUSDT',
    name: 'Solana Spot',
    category: 'CRYPTO',
    decimals: 2,
    isDirectBinanceWs: true,
    initialPrice: 148.75
  },
  {
    symbol: 'BNB/USDT',
    rawTicker: 'BNBUSDT',
    name: 'BNB Spot',
    category: 'CRYPTO',
    decimals: 2,
    isDirectBinanceWs: true,
    initialPrice: 575.20
  },

  // 2. Forex Institutionnel
  {
    symbol: 'EUR/USD',
    rawTicker: 'EURUSD',
    name: 'Euro / Dollar US',
    category: 'FOREX',
    decimals: 5,
    initialPrice: 1.08450
  },
  {
    symbol: 'GBP/USD',
    rawTicker: 'GBPUSD',
    name: 'Livre Sterling / Dollar US',
    category: 'FOREX',
    decimals: 5,
    initialPrice: 1.29820
  },
  {
    symbol: 'USD/JPY',
    rawTicker: 'USDJPY',
    name: 'Dollar US / Yen Japonais',
    category: 'FOREX',
    decimals: 3,
    initialPrice: 154.650
  },
  {
    symbol: 'AUD/USD',
    rawTicker: 'AUDUSD',
    name: 'Dollar Australien / Dollar US',
    category: 'FOREX',
    decimals: 5,
    initialPrice: 0.66540
  },
  {
    symbol: 'USD/CAD',
    rawTicker: 'USDCAD',
    name: 'Dollar US / Dollar Canadien',
    category: 'FOREX',
    decimals: 5,
    initialPrice: 1.37120
  },
  {
    symbol: 'USD/CHF',
    rawTicker: 'USDCHF',
    name: 'Dollar US / Franc Suisse',
    category: 'FOREX',
    decimals: 5,
    initialPrice: 0.88420
  },

  // 3. Matières Premières
  {
    symbol: 'XAU/USD',
    rawTicker: 'XAUUSD',
    name: 'Or Spot (Gold)',
    category: 'COMMODITIES',
    decimals: 2,
    initialPrice: 2512.40
  },
  {
    symbol: 'XAG/USD',
    rawTicker: 'XAGUSD',
    name: 'Argent Spot (Silver)',
    category: 'COMMODITIES',
    decimals: 3,
    initialPrice: 29.850
  },
  {
    symbol: 'USOIL',
    rawTicker: 'USOIL',
    name: 'Pétrole WTI Brut',
    category: 'COMMODITIES',
    decimals: 2,
    initialPrice: 77.45
  },

  // 4. Indices Synthétiques Deriv Volatility
  {
    symbol: 'V10',
    rawTicker: 'R_10',
    name: 'Volatility 10 Index',
    category: 'SYNTHETICS',
    decimals: 3,
    initialPrice: 6420.150,
    isDeriv: true
  },
  {
    symbol: 'V25',
    rawTicker: 'R_25',
    name: 'Volatility 25 Index',
    category: 'SYNTHETICS',
    decimals: 3,
    initialPrice: 1890.420,
    isDeriv: true
  },
  {
    symbol: 'V50',
    rawTicker: 'R_50',
    name: 'Volatility 50 Index',
    category: 'SYNTHETICS',
    decimals: 4,
    initialPrice: 284.1500,
    isDeriv: true
  },
  {
    symbol: 'V75',
    rawTicker: 'R_75',
    name: 'Volatility 75 Index',
    category: 'SYNTHETICS',
    decimals: 2,
    initialPrice: 478520.00,
    isDeriv: true
  },
  {
    symbol: 'V100',
    rawTicker: 'R_100',
    name: 'Volatility 100 Index',
    category: 'SYNTHETICS',
    decimals: 2,
    initialPrice: 1250.80,
    isDeriv: true
  },
  {
    symbol: 'V10 (1s)',
    rawTicker: '1HZ10V',
    name: 'Volatility 10 (1s) Index',
    category: 'SYNTHETICS',
    decimals: 3,
    initialPrice: 8450.320,
    isDeriv: true
  },
  {
    symbol: 'V25 (1s)',
    rawTicker: '1HZ25V',
    name: 'Volatility 25 (1s) Index',
    category: 'SYNTHETICS',
    decimals: 3,
    initialPrice: 3210.840,
    isDeriv: true
  },
  {
    symbol: 'V75 (1s)',
    rawTicker: '1HZ75V',
    name: 'Volatility 75 (1s) Index',
    category: 'SYNTHETICS',
    decimals: 2,
    initialPrice: 365400.00,
    isDeriv: true
  },
  {
    symbol: 'V100 (1s)',
    rawTicker: '1HZ100V',
    name: 'Volatility 100 (1s) Index',
    category: 'SYNTHETICS',
    decimals: 2,
    initialPrice: 2140.60,
    isDeriv: true
  }
];
