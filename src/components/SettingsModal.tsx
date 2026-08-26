import React, { useState } from 'react';
import { X, Send, Check, Shield, Bell, Volume2, Sliders, CheckSquare, Square } from 'lucide-react';
import { ALL_PAIRS } from '../data/defaultPairs';
import { UserPreferences, MarketCategory } from '../types/smc';

interface SettingsModalProps {
  isOpen: boolean;
  preferences: UserPreferences;
  onClose: () => void;
  onSave: (prefs: UserPreferences) => void;
  onTestTelegram: (token: string, chatId: string) => Promise<{ success: boolean; message: string }>;
}

export const SettingsModal: React.FC<SettingsModalProps> = ({
  isOpen,
  preferences,
  onClose,
  onSave,
  onTestTelegram
}) => {
  if (!isOpen) return null;

  const [botToken, setBotToken] = useState(preferences.telegramBotToken);
  const [chatId, setChatId] = useState(preferences.telegramChatId);
  const [tgEnabled, setTgEnabled] = useState(preferences.telegramAlertsEnabled);

  const [alertSniper, setAlertSniper] = useState(preferences.alertOnSniper);
  const [alertGood, setAlertGood] = useState(preferences.alertOnGoodSetup);
  const [alertWatchlist, setAlertWatchlist] = useState(preferences.alertOnWatchlist);

  const [antiDoublonHours, setAntiDoublonHours] = useState(preferences.antiDoublonMuteHours);
  const [autoMute, setAutoMute] = useState(preferences.autoMuteOnTradeTaken);
  const [soundEnabled, setSoundEnabled] = useState(preferences.soundAlertsEnabled);
  const [vibrationEnabled, setVibrationEnabled] = useState(preferences.vibrationEnabled);

  const [activeSymbols, setActiveSymbols] = useState<string[]>(preferences.activeSymbols);
  const [activeTfs, setActiveTfs] = useState<string[]>(preferences.activeTimeframes);

  const [isTesting, setIsTesting] = useState(false);
  const [testResult, setTestResult] = useState<{ success: boolean; message: string } | null>(null);

  const handleTest = async () => {
    setIsTesting(true);
    setTestResult(null);
    const res = await onTestTelegram(botToken, chatId);
    setIsTesting(false);
    setTestResult(res);
  };

  const handleSave = () => {
    onSave({
      telegramBotToken: botToken,
      telegramChatId: chatId,
      telegramAlertsEnabled: tgEnabled,
      alertOnSniper: alertSniper,
      alertOnGoodSetup: alertGood,
      alertOnWatchlist: alertWatchlist,
      antiDoublonMuteHours: antiDoublonHours,
      autoMuteOnTradeTaken: autoMute,
      soundAlertsEnabled: soundEnabled,
      vibrationEnabled: vibrationEnabled,
      activeSymbols,
      activeTimeframes: activeTfs
    });
    onClose();
  };

  const toggleSymbol = (sym: string) => {
    if (activeSymbols.includes(sym)) {
      setActiveSymbols(activeSymbols.filter((s) => s !== sym));
    } else {
      setActiveSymbols([...activeSymbols, sym]);
    }
  };

  const categories: { key: MarketCategory; label: string }[] = [
    { key: 'CRYPTO', label: 'Crypto en Direct (Binance)' },
    { key: 'FOREX', label: 'Forex Institutionnel' },
    { key: 'COMMODITIES', label: 'Matières Premières' },
    { key: 'SYNTHETICS', label: 'Indices Deriv Synthétiques' }
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-surface border border-border rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between p-5 border-b border-border sticky top-0 bg-surface/95 backdrop-blur z-10">
          <div>
            <h2 className="font-black text-lg text-[#F1F5F9] tracking-tight">RÉGLAGES & ALERTES TELEGRAM</h2>
            <p className="text-xs text-[#94A3B8]">Paramètres de notifications, confluences et paires actives</p>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-lg bg-surface-elevated text-[#94A3B8] hover:text-[#F1F5F9] hover:bg-surface-variant transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-5 space-y-5">
          {/* Section 1: Telegram Bot */}
          <div className="p-4 rounded-xl bg-surface-variant border border-border space-y-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <Send className="w-4 h-4 text-setup" />
                <span className="text-xs font-bold uppercase tracking-wider text-[#F1F5F9]">
                  1. Alertes Bot Telegram
                </span>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={tgEnabled}
                  onChange={(e) => setTgEnabled(e.target.checked)}
                  className="sr-only peer"
                />
                <div className="w-9 h-5 bg-surface-elevated peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-setup"></div>
              </label>
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#94A3B8] mb-1">
                Telegram Bot Token (@BotFather)
              </label>
              <input
                type="text"
                value={botToken}
                onChange={(e) => setBotToken(e.target.value)}
                placeholder="ex: 712345678:AAHAbc123..."
                className="w-full bg-surface-elevated border border-border focus:border-setup rounded-lg px-3 py-2 text-xs text-[#F1F5F9] font-mono outline-none"
              />
            </div>

            <div>
              <label className="block text-[11px] font-semibold text-[#94A3B8] mb-1">
                Chat ID / Canal ID (@userinfobot)
              </label>
              <input
                type="text"
                value={chatId}
                onChange={(e) => setChatId(e.target.value)}
                placeholder="ex: 123456789 ou -100123456789"
                className="w-full bg-surface-elevated border border-border focus:border-setup rounded-lg px-3 py-2 text-xs text-[#F1F5F9] font-mono outline-none"
              />
            </div>

            <button
              onClick={handleTest}
              disabled={isTesting || !botToken.trim() || !chatId.trim()}
              className="w-full py-2.5 px-3 rounded-lg bg-setup text-white text-xs font-bold flex items-center justify-center space-x-1.5 hover:bg-setup/90 disabled:opacity-50 transition-colors"
            >
              {isTesting ? (
                <span>Test en cours...</span>
              ) : (
                <>
                  <Send className="w-3.5 h-3.5" />
                  <span>Tester la Connexion Telegram</span>
                </>
              )}
            </button>

            {testResult && (
              <div
                className={`p-2.5 rounded-lg text-xs font-semibold ${
                  testResult.success
                    ? 'bg-buy-dark text-buy border border-buy-border'
                    : 'bg-sell-dark text-sell border border-sell-border'
                }`}
              >
                {testResult.message}
              </div>
            )}
          </div>

          {/* Section 2: Confluence Trigger Toggles */}
          <div className="p-4 rounded-xl bg-surface-variant border border-border space-y-3">
            <div className="flex items-center space-x-2">
              <Shield className="w-4 h-4 text-sniper" />
              <span className="text-xs font-bold uppercase tracking-wider text-[#F1F5F9]">
                2. Niveaux de Confluences à Recevoir
              </span>
            </div>

            <div className="space-y-2">
              <label className="flex items-center justify-between p-2 rounded-lg bg-surface-elevated hover:bg-surface-elevated/80 cursor-pointer">
                <div>
                  <div className="text-xs font-bold text-sniper">🎯 Conditions Hautes / Sniper (4/4 - 95-100%)</div>
                  <div className="text-[10px] text-[#94A3B8]">Tendance HTF + FVG Récent non mitigé + Fibo OTE + Sweep 💧</div>
                </div>
                <input
                  type="checkbox"
                  checked={alertSniper}
                  onChange={(e) => setAlertSniper(e.target.checked)}
                  className="rounded border-border text-sniper focus:ring-0"
                />
              </label>

              <label className="flex items-center justify-between p-2 rounded-lg bg-surface-elevated hover:bg-surface-elevated/80 cursor-pointer">
                <div>
                  <div className="text-xs font-bold text-setup">⚡ Conditions Moyennes / Bon Setup (3/4 - 75-90%)</div>
                  <div className="text-[10px] text-[#94A3B8]">3 confluences SMC institutionnelles validées</div>
                </div>
                <input
                  type="checkbox"
                  checked={alertGood}
                  onChange={(e) => setAlertGood(e.target.checked)}
                  className="rounded border-border text-setup focus:ring-0"
                />
              </label>

              <label className="flex items-center justify-between p-2 rounded-lg bg-surface-elevated hover:bg-surface-elevated/80 cursor-pointer">
                <div>
                  <div className="text-xs font-bold text-watchlist">👁️ À Surveiller / Watchlist (2/4 - 60-70%)</div>
                  <div className="text-[10px] text-[#94A3B8]">Setups en cours de formation structurelle</div>
                </div>
                <input
                  type="checkbox"
                  checked={alertWatchlist}
                  onChange={(e) => setAlertWatchlist(e.target.checked)}
                  className="rounded border-border text-watchlist focus:ring-0"
                />
              </label>
            </div>
          </div>

          {/* Section 3: Anti-doublons 6h Mute */}
          <div className="p-4 rounded-xl bg-surface-variant border border-border space-y-2">
            <div className="flex items-center justify-between">
              <div>
                <span className="text-xs font-bold uppercase tracking-wider text-[#F1F5F9]">
                  3. Option Anti-Doublons (Sourdine 6h)
                </span>
                <p className="text-[11px] text-[#94A3B8]">
                  Sourdine automatique de 6h sur une paire après avoir cliqué sur « Trade Pris ».
                </p>
              </div>
              <input
                type="checkbox"
                checked={autoMute}
                onChange={(e) => setAutoMute(e.target.checked)}
                className="rounded border-border text-liquidity focus:ring-0"
              />
            </div>
          </div>

          {/* Section 4: Sons & Alertes */}
          <div className="p-4 rounded-xl bg-surface-variant border border-border space-y-2">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <Volume2 className="w-4 h-4 text-buy" />
                <span className="text-xs font-bold text-[#F1F5F9]">Alertes Sonores Distinctes (Sniper Chime)</span>
              </div>
              <input
                type="checkbox"
                checked={soundEnabled}
                onChange={(e) => setSoundEnabled(e.target.checked)}
                className="rounded border-border text-buy focus:ring-0"
              />
            </div>
          </div>

          {/* Section 5: Active Pairs Management */}
          <div className="p-4 rounded-xl bg-surface-variant border border-border space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase tracking-wider text-[#F1F5F9]">
                5. Paires Actives Désirées ({activeSymbols.length}/{ALL_PAIRS.length})
              </span>
              <div className="flex items-center space-x-2 text-xs">
                <button
                  onClick={() => setActiveSymbols(ALL_PAIRS.map((p) => p.symbol))}
                  className="text-setup hover:underline font-semibold"
                >
                  Tout
                </button>
                <span className="text-[#64748B]">•</span>
                <button
                  onClick={() => setActiveSymbols([])}
                  className="text-sell hover:underline font-semibold"
                >
                  Aucun
                </button>
              </div>
            </div>

            <div className="space-y-3 max-h-60 overflow-y-auto pr-1">
              {categories.map((cat) => (
                <div key={cat.key} className="space-y-1.5">
                  <div className="text-[10px] font-black uppercase text-sniper tracking-wider">
                    {cat.label}
                  </div>
                  <div className="grid grid-cols-2 sm:grid-cols-3 gap-1.5">
                    {ALL_PAIRS.filter((p) => p.category === cat.key).map((pair) => {
                      const isChecked = activeSymbols.includes(pair.symbol);
                      return (
                        <button
                          key={pair.symbol}
                          onClick={() => toggleSymbol(pair.symbol)}
                          className={`flex items-center space-x-1.5 p-2 rounded-lg text-left text-xs font-mono transition-colors ${
                            isChecked
                              ? 'bg-surface-elevated text-[#F1F5F9] border border-setup/50'
                              : 'bg-surface/50 text-[#64748B] border border-transparent'
                          }`}
                        >
                          {isChecked ? (
                            <CheckSquare className="w-3.5 h-3.5 text-setup shrink-0" />
                          ) : (
                            <Square className="w-3.5 h-3.5 text-[#64748B] shrink-0" />
                          )}
                          <span className="truncate">{pair.symbol}</span>
                        </button>
                      );
                    })}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Save Button */}
          <button
            onClick={handleSave}
            className="w-full py-3.5 px-4 rounded-xl bg-buy text-[#090C10] font-black text-sm hover:bg-buy/90 flex items-center justify-center space-x-2 transition-colors shadow-lg shadow-buy/10"
          >
            <Check className="w-4 h-4" />
            <span>Enregistrer les Réglages</span>
          </button>
        </div>
      </div>
    </div>
  );
};
