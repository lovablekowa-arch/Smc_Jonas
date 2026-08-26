import React, { useState, useEffect, useMemo, useRef } from 'react';
import { ALL_PAIRS } from './data/defaultPairs';
import { SmcSignal, UserPreferences } from './types/smc';
import { BinanceWebSocketManager } from './services/binanceWs';
import { MarketEngine } from './services/marketEngine';
import { SmcStrategyEngine } from './services/smcStrategy';
import { TelegramNotificationService } from './services/telegramService';
import { SoundService } from './services/soundService';

import { Header } from './components/Header';
import { KpiMetrics } from './components/KpiMetrics';
import { FilterBar } from './components/FilterBar';
import { SignalCard } from './components/SignalCard';
import { SignalModal } from './components/SignalModal';
import { SettingsModal } from './components/SettingsModal';
import { Info, CheckCircle, AlertTriangle } from 'lucide-react';

const DEFAULT_PREFERENCES: UserPreferences = {
  telegramBotToken: '',
  telegramChatId: '',
  telegramAlertsEnabled: false,
  alertOnSniper: true,
  alertOnGoodSetup: true,
  alertOnWatchlist: false,
  antiDoublonMuteHours: 6,
  autoMuteOnTradeTaken: true,
  soundAlertsEnabled: true,
  vibrationEnabled: true,
  activeSymbols: ALL_PAIRS.map((p) => p.symbol),
  activeTimeframes: ['15M', '30M', '1H', '4H', '1D']
};

export const App: React.FC = () => {
  // Services
  const binanceWsRef = useRef<BinanceWebSocketManager | null>(null);
  const marketEngineRef = useRef<MarketEngine | null>(null);
  const smcEngineRef = useRef<SmcStrategyEngine | null>(null);
  const telegramServiceRef = useRef<TelegramNotificationService | null>(null);
  const soundServiceRef = useRef<SoundService | null>(null);

  // States
  const [preferences, setPreferences] = useState<UserPreferences>(() => {
    try {
      const saved = localStorage.getItem('smc_user_prefs');
      if (saved) return JSON.parse(saved);
    } catch (e) {
      console.warn('Failed to load prefs', e);
    }
    return DEFAULT_PREFERENCES;
  });

  const [mutedTrades, setMutedTrades] = useState<Record<string, number>>(() => {
    try {
      const saved = localStorage.getItem('smc_muted_trades');
      if (saved) return JSON.parse(saved);
    } catch (e) {
      console.warn('Failed to load muted trades', e);
    }
    return {};
  });

  const [wsConnected, setWsConnected] = useState(false);
  const [statusText, setStatusText] = useState('Connexion...');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedFilter, setSelectedFilter] = useState('ALL');

  const [signalsMap, setSignalsMap] = useState<Map<string, SmcSignal>>(new Map());
  const [selectedSignalForDetail, setSelectedSignalForDetail] = useState<SmcSignal | null>(null);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);

  const [toastMessage, setToastMessage] = useState<{ text: string; isError?: boolean } | null>(null);
  const alertedTimestampsRef = useRef<Map<string, number>>(new Map());

  // Show floating toast
  const showToast = (text: string, isError = false) => {
    setToastMessage({ text, isError });
    setTimeout(() => {
      setToastMessage(null);
    }, 4000);
  };

  // Initialize Engines & WebSockets
  useEffect(() => {
    const binanceWs = new BinanceWebSocketManager();
    const marketEngine = new MarketEngine();
    const smcEngine = new SmcStrategyEngine();
    const telegramService = new TelegramNotificationService();
    const soundService = new SoundService();

    binanceWsRef.current = binanceWs;
    marketEngineRef.current = marketEngine;
    smcEngineRef.current = smcEngine;
    telegramServiceRef.current = telegramService;
    soundServiceRef.current = soundService;

    binanceWs.setOnStatusChange((connected, text) => {
      setWsConnected(connected);
      setStatusText(text);
    });

    binanceWs.setOnPriceUpdate((ticker, price, change24h) => {
      marketEngine.updateExternalPrice(ticker, price, change24h);
    });

    // Market tick update
    marketEngine.setOnTick((symbol, price, change24h) => {
      const pair = ALL_PAIRS.find((p) => p.symbol === symbol);
      if (!pair) return;

      const tradeTakenTime = mutedTrades[symbol];
      const updatedSignal = smcEngine.evaluateSignal(
        pair,
        price,
        change24h,
        '1H',
        tradeTakenTime
      );

      setSignalsMap((prev) => {
        const next = new Map(prev);
        next.set(symbol, updatedSignal);
        return next;
      });

      // Check auto Telegram alert
      checkAutoTelegram(updatedSignal);
    });

    // Initial signal population
    const initialMap = new Map<string, SmcSignal>();
    ALL_PAIRS.forEach((pair, idx) => {
      const price = marketEngine.getPrice(pair.symbol);
      const change = marketEngine.getChange24h(pair.symbol);
      const tradeTakenTime = mutedTrades[pair.symbol];

      // Pre-seed realistic top confluences
      const forceCount = idx % 3 === 0 ? 4 : idx % 3 === 1 ? 3 : 2;
      const sig = smcEngine.evaluateSignal(
        pair,
        price,
        change,
        '1H',
        tradeTakenTime,
        forceCount
      );
      initialMap.set(pair.symbol, sig);
    });
    setSignalsMap(initialMap);

    binanceWs.start();
    marketEngine.start();

    return () => {
      binanceWs.stop();
      marketEngine.stop();
    };
  }, []);

  // Check automated Telegram alert
  const checkAutoTelegram = (signal: SmcSignal) => {
    if (!preferences.telegramAlertsEnabled || !preferences.telegramBotToken || !preferences.telegramChatId) {
      return;
    }

    // Check 6h mute
    if (signal.tradeTakenTimestamp && Date.now() - signal.tradeTakenTimestamp < 6 * 60 * 60 * 1000) {
      return;
    }

    // Check level
    const isLevelEnabled =
      (signal.confluenceLevel === 'SNIPER' && preferences.alertOnSniper) ||
      (signal.confluenceLevel === 'GOOD_SETUP' && preferences.alertOnGoodSetup) ||
      (signal.confluenceLevel === 'WATCHLIST' && preferences.alertOnWatchlist);

    if (!isLevelEnabled) return;

    // Debounce 1 hour per pair
    const lastAlertTime = alertedTimestampsRef.current.get(signal.pair.symbol) || 0;
    if (Date.now() - lastAlertTime < 60 * 60 * 1000) return;

    alertedTimestampsRef.current.set(signal.pair.symbol, Date.now());

    if (preferences.soundAlertsEnabled) {
      soundServiceRef.current?.playSniperChime();
    }

    telegramServiceRef.current?.sendSignalAlert(
      preferences.telegramBotToken,
      preferences.telegramChatId,
      signal
    );
  };

  // Mark trade taken (Toggle 6h mute)
  const handleToggleTradeTaken = (symbol: string) => {
    const currentMute = mutedTrades[symbol];
    const sixHours = 6 * 60 * 60 * 1000;
    const isCurrentlyMuted = currentMute && Date.now() - currentMute < sixHours;

    let updated: Record<string, number>;
    if (isCurrentlyMuted) {
      updated = { ...mutedTrades };
      delete updated[symbol];
      showToast(`Sourdine désactivée pour ${symbol}`);
    } else {
      updated = { ...mutedTrades, [symbol]: Date.now() };
      if (preferences.soundAlertsEnabled) {
        soundServiceRef.current?.playSweepTone();
      }
      showToast(`Sourdine 6h activée pour ${symbol} (Trade Pris)`);
    }

    setMutedTrades(updated);
    localStorage.setItem('smc_muted_trades', JSON.stringify(updated));

    // Update signal in map
    setSignalsMap((prev) => {
      const next = new Map(prev);
      const sig = next.get(symbol);
      if (sig) {
        next.set(symbol, {
          ...sig,
          tradeTakenTimestamp: updated[symbol]
        });
      }
      return next;
    });
  };

  // Manual send telegram
  const handleSendTelegram = async (signal: SmcSignal) => {
    if (!preferences.telegramBotToken || !preferences.telegramChatId) {
      showToast('Configurez votre Token et Chat ID dans les réglages', true);
      setIsSettingsOpen(true);
      return;
    }

    const res = await telegramServiceRef.current?.sendSignalAlert(
      preferences.telegramBotToken,
      preferences.telegramChatId,
      signal
    );

    if (res?.success) {
      if (preferences.soundAlertsEnabled) {
        soundServiceRef.current?.playSweepTone();
      }
      showToast(res.message, false);
    } else {
      showToast(res?.message || "Échec d'envoi Telegram", true);
    }
  };

  // Save Preferences
  const handleSavePreferences = (newPrefs: UserPreferences) => {
    setPreferences(newPrefs);
    localStorage.setItem('smc_user_prefs', JSON.stringify(newPrefs));
    showToast('Réglages sauvegardés avec succès !');
  };

  // Test Telegram in settings
  const handleTestTelegram = async (token: string, chat: string) => {
    if (!telegramServiceRef.current) {
      return { success: false, message: 'Service non initialisé' };
    }
    return await telegramServiceRef.current.testConnection(token, chat);
  };

  // Filtered and sorted signals
  const allSignalsList = useMemo(() => {
    return Array.from(signalsMap.values()).filter((sig) =>
      preferences.activeSymbols.includes(sig.pair.symbol)
    );
  }, [signalsMap, preferences.activeSymbols]);

  const filteredSignals = useMemo(() => {
    return allSignalsList
      .filter((sig) => {
        const matchesSearch =
          !searchQuery.trim() ||
          sig.pair.symbol.toLowerCase().includes(searchQuery.toLowerCase()) ||
          sig.pair.name.toLowerCase().includes(searchQuery.toLowerCase());

        const matchesFilter =
          selectedFilter === 'ALL' ||
          (selectedFilter === 'SNIPER' && sig.confluenceLevel === 'SNIPER') ||
          (selectedFilter === 'GOOD_SETUP' && sig.confluenceLevel === 'GOOD_SETUP') ||
          (selectedFilter === 'WATCHLIST' && sig.confluenceLevel === 'WATCHLIST') ||
          (selectedFilter === 'CRYPTO' && sig.pair.category === 'CRYPTO') ||
          (selectedFilter === 'FOREX' && sig.pair.category === 'FOREX') ||
          (selectedFilter === 'COMMODITIES' && sig.pair.category === 'COMMODITIES') ||
          (selectedFilter === 'SYNTHETICS' && sig.pair.category === 'SYNTHETICS');

        return matchesSearch && matchesFilter;
      })
      .sort((a, b) => b.confluenceScore - a.confluenceScore);
  }, [allSignalsList, searchQuery, selectedFilter]);

  // KPI Counters
  const sniperCount = useMemo(
    () => allSignalsList.filter((s) => s.confluenceLevel === 'SNIPER').length,
    [allSignalsList]
  );
  const goodSetupCount = useMemo(
    () => allSignalsList.filter((s) => s.confluenceLevel === 'GOOD_SETUP').length,
    [allSignalsList]
  );
  const watchlistCount = useMemo(
    () => allSignalsList.filter((s) => s.confluenceLevel === 'WATCHLIST').length,
    [allSignalsList]
  );
  const mutedCount = useMemo(
    () =>
      allSignalsList.filter(
        (s) => s.tradeTakenTimestamp && Date.now() - s.tradeTakenTimestamp < 6 * 60 * 60 * 1000
      ).length,
    [allSignalsList]
  );

  return (
    <div className="min-h-screen bg-background text-[#F1F5F9] pb-12">
      {/* Top Navigation Header */}
      <Header
        wsConnected={wsConnected}
        statusText={statusText}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        onOpenSettings={() => setIsSettingsOpen(true)}
        telegramEnabled={preferences.telegramAlertsEnabled}
      />

      {/* KPI Metrics Summary Bar */}
      <KpiMetrics
        sniperCount={sniperCount}
        goodSetupCount={goodSetupCount}
        watchlistCount={watchlistCount}
        mutedCount={mutedCount}
        onSelectFilter={setSelectedFilter}
      />

      {/* Filter Tabs Row */}
      <FilterBar selectedTab={selectedFilter} onSelectTab={setSelectedFilter} />

      {/* Main Signals Feed */}
      <main className="max-w-7xl mx-auto px-4 md:px-6 mt-3">
        {filteredSignals.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 px-4 text-center rounded-2xl bg-surface border border-border mt-4">
            <Info className="w-10 h-10 text-[#64748B] mb-3" />
            <h3 className="text-sm font-bold text-[#F1F5F9]">Aucun signal ne correspond aux filtres</h3>
            <p className="text-xs text-[#94A3B8] max-w-sm mt-1">
              Modifiez votre recherche ou réactivez les paires masquées dans le menu des réglages.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredSignals.map((signal) => (
              <SignalCard
                key={signal.id}
                signal={signal}
                onTradeTaken={handleToggleTradeTaken}
                onSendTelegram={handleSendTelegram}
                onOpenDetail={setSelectedSignalForDetail}
              />
            ))}
          </div>
        )}
      </main>

      {/* Detail Modal */}
      <SignalModal
        signal={selectedSignalForDetail}
        onClose={() => setSelectedSignalForDetail(null)}
        onSendTelegram={handleSendTelegram}
        onToggleTradeTaken={handleToggleTradeTaken}
      />

      {/* Settings Modal */}
      <SettingsModal
        isOpen={isSettingsOpen}
        preferences={preferences}
        onClose={() => setIsSettingsOpen(false)}
        onSave={handleSavePreferences}
        onTestTelegram={handleTestTelegram}
      />

      {/* Floating Toast Notification */}
      {toastMessage && (
        <div className="fixed bottom-5 right-5 z-50 animate-in slide-in-from-bottom duration-200">
          <div
            className={`flex items-center space-x-2 px-4 py-3 rounded-xl shadow-2xl border text-xs font-bold ${
              toastMessage.isError
                ? 'bg-sell-dark text-sell border-sell-border'
                : 'bg-surface-elevated text-buy border-buy-border'
            }`}
          >
            {toastMessage.isError ? (
              <AlertTriangle className="w-4 h-4 text-sell shrink-0" />
            ) : (
              <CheckCircle className="w-4 h-4 text-buy shrink-0" />
            )}
            <span>{toastMessage.text}</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default App;
