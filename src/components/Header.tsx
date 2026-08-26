import React from 'react';
import { Settings, Search, X, Activity, Radio } from 'lucide-react';

interface HeaderProps {
  wsConnected: boolean;
  statusText: string;
  searchQuery: string;
  onSearchChange: (val: string) => void;
  onOpenSettings: () => void;
  telegramEnabled: boolean;
}

export const Header: React.FC<HeaderProps> = ({
  wsConnected,
  statusText,
  searchQuery,
  onSearchChange,
  onOpenSettings,
  telegramEnabled
}) => {
  return (
    <header className="bg-surface border-b border-border sticky top-0 z-30 px-4 py-3 md:px-6">
      <div className="max-w-7xl mx-auto flex flex-col md:flex-row md:items-center md:justify-between gap-3">
        {/* Logo & Tagline */}
        <div className="flex items-center justify-between">
          <div>
            <div className="flex items-center space-x-2">
              <span className="font-mono font-black text-xl text-sniper tracking-tight">SMC</span>
              <span className="font-black text-xl text-[#F1F5F9] tracking-wider">SIGNALS</span>
              <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded bg-surface-elevated text-sniper border border-border">
                Live SMC 4/4
              </span>
            </div>
            <p className="text-xs text-[#94A3B8] font-medium mt-0.5">
              Smart Money Concepts & Analyse de Liquidité • Sans Bruit
            </p>
          </div>

          {/* Mobile Right Controls */}
          <div className="flex items-center space-x-2 md:hidden">
            <button
              onClick={onOpenSettings}
              className="p-2 rounded-lg bg-surface-elevated border border-border text-[#F1F5F9] hover:border-setup transition-colors relative"
              title="Réglages"
            >
              <Settings className="w-4 h-4" />
              {telegramEnabled && (
                <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-buy animate-pulse" />
              )}
            </button>
          </div>
        </div>

        {/* Center Search & Live Feeds Badge */}
        <div className="flex items-center space-x-3 flex-1 max-w-xl">
          <div className="relative flex-1">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-[#64748B]" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => onSearchChange(e.target.value)}
              placeholder="Rechercher (ex: BTC, EUR, XAU, V75)..."
              className="w-full bg-surface-variant border border-border-subtle focus:border-setup rounded-lg pl-9 pr-8 py-1.5 text-xs text-[#F1F5F9] placeholder-[#64748B] outline-none transition-colors"
            />
            {searchQuery && (
              <button
                onClick={() => onSearchChange('')}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-[#64748B] hover:text-[#F1F5F9]"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            )}
          </div>

          {/* WS Status Badge */}
          <div
            className={`hidden sm:flex items-center space-x-1.5 px-3 py-1.5 rounded-lg border text-[11px] font-mono font-semibold ${
              wsConnected
                ? 'bg-buy-dark/80 text-buy border-buy-border'
                : 'bg-surface-elevated text-sniper border-border'
            }`}
          >
            <span
              className={`w-2 h-2 rounded-full ${
                wsConnected ? 'bg-buy animate-pulse' : 'bg-sniper'
              }`}
            />
            <span>{wsConnected ? 'BINANCE WS LIVE' : 'TICKS LIVE'}</span>
          </div>

          {/* Desktop Settings Button */}
          <button
            onClick={onOpenSettings}
            className="hidden md:flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-surface-elevated border border-border hover:border-setup text-xs font-semibold text-[#F1F5F9] transition-all relative"
          >
            <Settings className="w-3.5 h-3.5 text-[#94A3B8]" />
            <span>Réglages & Alertes</span>
            {telegramEnabled && (
              <span className="w-2 h-2 rounded-full bg-buy inline-block ml-1" />
            )}
          </button>
        </div>
      </div>
    </header>
  );
};
