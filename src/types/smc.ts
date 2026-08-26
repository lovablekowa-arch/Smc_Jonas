export type MarketCategory = 'CRYPTO' | 'FOREX' | 'COMMODITIES' | 'SYNTHETICS';

export interface TradingPair {
  symbol: string;              // e.g. "BTC/USDT", "EUR/USD", "XAU/USD", "V75 (1s)"
  rawTicker: string;           // e.g. "BTCUSDT", "EURUSD", "XAUUSD", "R_75"
  name: string;                // e.g. "Bitcoin Spot", "Euro / US Dollar", "Gold Spot"
  category: MarketCategory;
  decimals: number;
  isDirectBinanceWs?: boolean;
  initialPrice: number;
  isDeriv?: boolean;
}

export type SignalDirection = 'BUY' | 'SELL';

export type ConfluenceLevel = 'SNIPER' | 'GOOD_SETUP' | 'WATCHLIST';

export type SignalTimeframe = '15M' | '30M' | '1H' | '4H' | '1D';

export interface FvgObDetail {
  recentFvgAgeHours: number;
  recentFvgMitigated: boolean; // false = non mitigé (prioritaire pour signal)
  recentFvgLow: number;
  recentFvgHigh: number;
  ancientFvgAgeHours: number;
  ancientFvgMitigated: boolean; // true = comblé à 100%
  ancientFvgLow: number;
  ancientFvgHigh: number;
  orderBlockName: string;
  orderBlockLow: number;
  orderBlockHigh: number;
}

export interface FibonacciDetail {
  zoneName: string; // "Zone Discount (< 50% Fibo - OTE)" or "Zone Premium (> 50% Fibo - OTE)"
  fiboLevelPercent: number; // e.g. 62.0 for 62% OTE
  swingLow: number;
  swingHigh: number;
  equilibrium50: number;
}

export interface LiquiditySweepDetail {
  sweepType: string; // "SSL (Sell-Side Liquidity Balayée 💧)" or "BSL (Buy-Side Liquidity Balayée 💧)"
  sweptPrice: number;
  rejectionConfirmed: boolean;
  rejectionWickPips: number;
  sweepTimeAgoMinutes: number;
}

export interface RestingLiquidityTarget {
  targetName: string; // e.g. "Liquidité Interne (BSL Pool)"
  price: number;
  distancePercent: number;
  isMajor: boolean;
}

export interface ConfluenceChecklist {
  htfTrendAligned: boolean;
  htfDescription: string;
  fvgObValid: boolean;
  fvgObDetail: FvgObDetail;
  fiboDiscountPremiumValid: boolean;
  fiboDetail: FibonacciDetail;
  sweepValid: boolean;
  sweepDetail: LiquiditySweepDetail;
}

export interface SmcSignal {
  id: string;
  pair: TradingPair;
  currentPrice: number;
  priceChange24h: number;
  direction: SignalDirection;
  timeframe: SignalTimeframe;
  entryPrice: number;
  stopLoss: number;
  tp1Resting: RestingLiquidityTarget;
  tp2Resting: RestingLiquidityTarget;
  riskRewardRatio: number;
  checklist: ConfluenceChecklist;
  confluenceLevel: ConfluenceLevel;
  confluenceScore: number; // 2, 3, 4
  createdAt: number;
  tradeTakenTimestamp?: number; // timestamp when marked "Trade Pris"
}

export interface UserPreferences {
  telegramBotToken: string;
  telegramChatId: string;
  telegramAlertsEnabled: boolean;
  alertOnSniper: boolean;     // 4/4 Hautes (95-100%)
  alertOnGoodSetup: boolean;  // 3/4 Moyennes (75-90%)
  alertOnWatchlist: boolean;  // 2/4 À Surveiller (60-70%)
  antiDoublonMuteHours: number; // default 6h
  autoMuteOnTradeTaken: boolean;
  soundAlertsEnabled: boolean;
  vibrationEnabled: boolean;
  activeSymbols: string[];
  activeTimeframes: string[];
}
