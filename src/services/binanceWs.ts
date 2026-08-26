export class BinanceWebSocketManager {
  private ws: WebSocket | null = null;
  private isConnected = false;
  private reconnectTimeout: number | null = null;
  private onPriceUpdateCallback: ((ticker: string, price: number, change24h: number) => void) | null = null;
  private onStatusChangeCallback: ((connected: boolean, statusText: string) => void) | null = null;

  public setOnPriceUpdate(callback: (ticker: string, price: number, change24h: number) => void) {
    this.onPriceUpdateCallback = callback;
  }

  public setOnStatusChange(callback: (connected: boolean, statusText: string) => void) {
    this.onStatusChangeCallback = callback;
  }

  public start() {
    this.connect();
  }

  private connect() {
    if (this.ws) {
      try {
        this.ws.close();
      } catch (e) {
        // ignore
      }
    }

    this.onStatusChangeCallback?.(false, 'Connexion Binance WS...');

    try {
      this.ws = new WebSocket('wss://stream.binance.com:9443/ws/!miniTicker@arr');

      this.ws.onopen = () => {
        this.isConnected = true;
        this.onStatusChangeCallback?.(true, 'Binance WS Connecté 🟢');
      };

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          if (Array.isArray(data)) {
            const watched = ['BTCUSDT', 'ETHUSDT', 'SOLUSDT', 'BNBUSDT'];
            for (const item of data) {
              if (watched.includes(item.s)) {
                const closePrice = parseFloat(item.c);
                const openPrice = parseFloat(item.o) || closePrice;
                const change24h = openPrice !== 0 ? ((closePrice - openPrice) / openPrice) * 100 : 0;
                if (!isNaN(closePrice)) {
                  this.onPriceUpdateCallback?.(item.s, closePrice, change24h);
                }
              }
            }
          }
        } catch (err) {
          console.error('Binance WS parse error:', err);
        }
      };

      this.ws.onerror = () => {
        this.isConnected = false;
        this.onStatusChangeCallback?.(false, 'Erreur WS Binance');
      };

      this.ws.onclose = () => {
        this.isConnected = false;
        this.onStatusChangeCallback?.(false, 'Reconnexion...');
        this.scheduleReconnect();
      };
    } catch (err) {
      console.error('Failed to create WebSocket', err);
      this.scheduleReconnect();
    }
  }

  private scheduleReconnect() {
    if (this.reconnectTimeout) clearTimeout(this.reconnectTimeout);
    this.reconnectTimeout = window.setTimeout(() => {
      this.connect();
    }, 4000);
  }

  public stop() {
    if (this.reconnectTimeout) clearTimeout(this.reconnectTimeout);
    if (this.ws) {
      try {
        this.ws.close();
      } catch (e) {
        // ignore
      }
      this.ws = null;
    }
    this.isConnected = false;
  }
}
