import { ALL_PAIRS } from '../data/defaultPairs';
import { TradingPair } from '../types/smc';

export class MarketEngine {
  private currentPrices: Map<string, number> = new Map();
  private change24hMap: Map<string, number> = new Map();
  private intervalId: number | null = null;
  private onTickCallback: ((symbol: string, price: number, change24h: number) => void) | null = null;

  constructor() {
    ALL_PAIRS.forEach((pair) => {
      this.currentPrices.set(pair.symbol, pair.initialPrice);
      this.change24hMap.set(pair.symbol, (Math.random() * 5) - 2);
    });
  }

  public setOnTick(callback: (symbol: string, price: number, change24h: number) => void) {
    this.onTickCallback = callback;
  }

  public updateExternalPrice(rawTicker: string, price: number, change24h: number) {
    const pair = ALL_PAIRS.find((p) => p.rawTicker.toLowerCase() === rawTicker.toLowerCase());
    if (pair) {
      this.currentPrices.set(pair.symbol, price);
      this.change24hMap.set(pair.symbol, change24h);
      this.onTickCallback?.(pair.symbol, price, change24h);
    }
  }

  public start() {
    if (this.intervalId) clearInterval(this.intervalId);

    this.intervalId = window.setInterval(() => {
      ALL_PAIRS.filter((p) => !p.isDirectBinanceWs).forEach((pair) => {
        const basePrice = this.currentPrices.get(pair.symbol) || pair.initialPrice;
        let factor = 0.00008;

        if (pair.symbol.includes('V100 (1s)') || pair.symbol.includes('V75 (1s)')) {
          factor = 0.0007;
        } else if (pair.symbol.includes('V75') || pair.symbol.includes('V100')) {
          factor = 0.0004;
        } else if (pair.symbol.includes('V50') || pair.symbol.includes('V25') || pair.symbol.includes('V10')) {
          factor = 0.00025;
        } else if (pair.symbol === 'XAU/USD') {
          factor = 0.00018;
        } else if (pair.symbol === 'XAG/USD' || pair.symbol === 'USOIL') {
          factor = 0.00025;
        }

        const delta = (Math.random() * 2 - 1) * factor;
        const newPrice = basePrice * (1 + delta);
        const oldChange = this.change24hMap.get(pair.symbol) || 0.4;
        const newChange = oldChange + delta * 30;

        this.currentPrices.set(pair.symbol, newPrice);
        this.change24hMap.set(pair.symbol, newChange);

        this.onTickCallback?.(pair.symbol, newPrice, newChange);
      });
    }, 1200);
  }

  public getPrice(symbol: string): number {
    return this.currentPrices.get(symbol) || ALL_PAIRS.find((p) => p.symbol === symbol)?.initialPrice || 100;
  }

  public getChange24h(symbol: string): number {
    return this.change24hMap.get(symbol) || 0;
  }

  public stop() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
  }
}
