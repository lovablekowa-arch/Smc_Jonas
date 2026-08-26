import React from 'react';
import { Target, Zap, Eye, VolumeX } from 'lucide-react';

interface KpiMetricsProps {
  sniperCount: number;
  goodSetupCount: number;
  watchlistCount: number;
  mutedCount: number;
  onSelectFilter: (filter: string) => void;
}

export const KpiMetrics: React.FC<KpiMetricsProps> = ({
  sniperCount,
  goodSetupCount,
  watchlistCount,
  mutedCount,
  onSelectFilter
}) => {
  return (
    <div className="max-w-7xl mx-auto px-4 md:px-6 py-3">
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
        {/* Sniper 4/4 */}
        <button
          onClick={() => onSelectFilter('SNIPER')}
          className="flex items-center justify-between p-3 rounded-xl bg-surface border border-border hover:border-sniper/60 transition-all text-left group"
        >
          <div>
            <div className="flex items-center space-x-1.5">
              <Target className="w-3.5 h-3.5 text-sniper" />
              <span className="text-[11px] font-bold text-sniper uppercase tracking-wider">
                Sniper 4/4
              </span>
            </div>
            <p className="text-[10px] text-[#64748B] mt-0.5">95% - 100% Confluences</p>
          </div>
          <span className="text-xl font-mono font-black text-[#F1F5F9] group-hover:text-sniper transition-colors">
            {sniperCount}
          </span>
        </button>

        {/* Moyennes 3/4 */}
        <button
          onClick={() => onSelectFilter('GOOD_SETUP')}
          className="flex items-center justify-between p-3 rounded-xl bg-surface border border-border hover:border-setup/60 transition-all text-left group"
        >
          <div>
            <div className="flex items-center space-x-1.5">
              <Zap className="w-3.5 h-3.5 text-setup" />
              <span className="text-[11px] font-bold text-setup uppercase tracking-wider">
                Moyennes 3/4
              </span>
            </div>
            <p className="text-[10px] text-[#64748B] mt-0.5">75% - 90% Bons Setups</p>
          </div>
          <span className="text-xl font-mono font-black text-[#F1F5F9] group-hover:text-setup transition-colors">
            {goodSetupCount}
          </span>
        </button>

        {/* Watchlist 2/4 */}
        <button
          onClick={() => onSelectFilter('WATCHLIST')}
          className="flex items-center justify-between p-3 rounded-xl bg-surface border border-border hover:border-watchlist/60 transition-all text-left group"
        >
          <div>
            <div className="flex items-center space-x-1.5">
              <Eye className="w-3.5 h-3.5 text-watchlist" />
              <span className="text-[11px] font-bold text-watchlist uppercase tracking-wider">
                Watchlist 2/4
              </span>
            </div>
            <p className="text-[10px] text-[#64748B] mt-0.5">60% - 70% En formation</p>
          </div>
          <span className="text-xl font-mono font-black text-[#F1F5F9] group-hover:text-watchlist transition-colors">
            {watchlistCount}
          </span>
        </button>

        {/* Sourdines 6h */}
        <button
          onClick={() => onSelectFilter('ALL')}
          className="flex items-center justify-between p-3 rounded-xl bg-surface border border-border hover:border-liquidity/60 transition-all text-left group"
        >
          <div>
            <div className="flex items-center space-x-1.5">
              <VolumeX className="w-3.5 h-3.5 text-liquidity" />
              <span className="text-[11px] font-bold text-liquidity uppercase tracking-wider">
                Sourdine 6h
              </span>
            </div>
            <p className="text-[10px] text-[#64748B] mt-0.5">Trades Pris Actifs</p>
          </div>
          <span className="text-xl font-mono font-black text-[#F1F5F9] group-hover:text-liquidity transition-colors">
            {mutedCount}
          </span>
        </button>
      </div>
    </div>
  );
};
