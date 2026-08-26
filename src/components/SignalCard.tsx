import React, { useState } from 'react';
import {
  TrendingUp,
  TrendingDown,
  Droplet,
  Zap,
  CheckCircle2,
  XCircle,
  ChevronDown,
  ChevronUp,
  Send,
  VolumeX,
  Volume2,
  Clock,
  Shield,
  Layers
} from 'lucide-react';
import { SmcSignal } from '../types/smc';

interface SignalCardProps {
  signal: SmcSignal;
  onTradeTaken: (symbol: string) => void;
  onSendTelegram: (signal: SmcSignal) => void;
  onOpenDetail: (signal: SmcSignal) => void;
}

export const SignalCard: React.FC<SignalCardProps> = ({
  signal,
  onTradeTaken,
  onSendTelegram,
  onOpenDetail
}) => {
  const [isExpanded, setIsExpanded] = useState(false);

  const isBuy = signal.direction === 'BUY';
  const formatPrice = (p: number) => p.toFixed(signal.pair.decimals);

  // Remaining mute hours
  const isMuted = !!(
    signal.tradeTakenTimestamp &&
    Date.now() - signal.tradeTakenTimestamp < 6 * 60 * 60 * 1000
  );
  const remainingHours = isMuted
    ? Math.max(0, (6 * 60 * 60 * 1000 - (Date.now() - signal.tradeTakenTimestamp!)) / (1000 * 60 * 60))
    : 0;

  const confluenceBadge =
    signal.confluenceLevel === 'SNIPER'
      ? { label: '4/4 SNIPER', range: '95-100%', color: 'text-sniper border-sniper/50 bg-sniper-dark/40' }
      : signal.confluenceLevel === 'GOOD_SETUP'
      ? { label: '3/4 BON SETUP', range: '75-90%', color: 'text-setup border-setup/50 bg-setup-dark/40' }
      : { label: '2/4 WATCHLIST', range: '60-70%', color: 'text-watchlist border-watchlist/50 bg-watchlist-dark/40' };

  return (
    <div
      className={`rounded-2xl bg-surface border transition-all duration-200 overflow-hidden ${
        signal.confluenceLevel === 'SNIPER'
          ? 'border-sniper/40 shadow-lg shadow-sniper/5'
          : 'border-border hover:border-border-subtle'
      }`}
    >
      <div className="p-4 sm:p-5">
        {/* Row 1: Pair, Category, Confluence Badge, Live Price */}
        <div className="flex items-center justify-between gap-2">
          <div className="flex items-center space-x-2">
            <span className="font-mono font-black text-lg text-[#F1F5F9] tracking-tight">
              {signal.pair.symbol}
            </span>
            <span className="text-[10px] font-bold px-2 py-0.5 rounded bg-surface-variant text-[#94A3B8] border border-border-subtle">
              {signal.pair.category}
            </span>
            {isMuted && (
              <span className="flex items-center space-x-1 text-[10px] font-bold px-2 py-0.5 rounded bg-liquidity-dark text-liquidity border border-liquidity/30">
                <VolumeX className="w-3 h-3" />
                <span>Sourdine {remainingHours.toFixed(1)}h</span>
              </span>
            )}
          </div>

          <div className="flex items-center space-x-2">
            <span
              className={`text-[10px] font-mono font-bold px-2.5 py-1 rounded-lg border ${confluenceBadge.color}`}
            >
              {confluenceBadge.label} ({confluenceBadge.range})
            </span>
          </div>
        </div>

        {/* Row 2: Direction Banner (BUY/SELL) & Realtime Price */}
        <div className="flex items-center justify-between mt-3 pt-3 border-t border-border-subtle">
          <div className="flex items-center space-x-2">
            <div
              className={`flex items-center space-x-1.5 px-3 py-1 rounded-lg text-xs font-black tracking-wider ${
                isBuy
                  ? 'bg-buy-dark text-buy border border-buy-border'
                  : 'bg-sell-dark text-sell border border-sell-border'
              }`}
            >
              {isBuy ? <TrendingUp className="w-3.5 h-3.5" /> : <TrendingDown className="w-3.5 h-3.5" />}
              <span>{isBuy ? 'ACHAT / LONG' : 'VENTE / SHORT'}</span>
              <span className="text-[#94A3B8] font-normal text-[10px] ml-1">({signal.timeframe})</span>
            </div>
            <span className="text-[11px] text-[#94A3B8] font-medium hidden sm:inline">
              {signal.checklist.fiboDetail.zoneName}
            </span>
          </div>

          <div className="text-right">
            <div className="font-mono font-black text-lg text-[#F1F5F9]">
              {formatPrice(signal.currentPrice)}
            </div>
            <div
              className={`text-xs font-mono font-bold ${
                signal.priceChange24h >= 0 ? 'text-buy' : 'text-sell'
              }`}
            >
              {signal.priceChange24h >= 0 ? '+' : ''}
              {signal.priceChange24h.toFixed(2)}%
            </div>
          </div>
        </div>

        {/* Row 3: Price Matrix Grid (Entry, SL, TP1, TP2, R:R) */}
        <div className="grid grid-cols-5 gap-1.5 p-2.5 rounded-xl bg-surface-variant border border-border-subtle mt-3 text-center">
          <div>
            <div className="text-[9px] font-bold text-[#64748B] uppercase">Entrée</div>
            <div className="text-xs font-mono font-bold text-[#F1F5F9] mt-0.5">
              {formatPrice(signal.entryPrice)}
            </div>
          </div>
          <div>
            <div className="text-[9px] font-bold text-[#64748B] uppercase">Stop Loss</div>
            <div className="text-xs font-mono font-bold text-sell mt-0.5">
              {formatPrice(signal.stopLoss)}
            </div>
          </div>
          <div>
            <div className="text-[9px] font-bold text-[#64748B] uppercase">TP1 Interne</div>
            <div className="text-xs font-mono font-bold text-buy mt-0.5">
              {formatPrice(signal.tp1Resting.price)}
            </div>
          </div>
          <div>
            <div className="text-[9px] font-bold text-[#64748B] uppercase">TP2 Majeur</div>
            <div className="text-xs font-mono font-bold text-sniper mt-0.5">
              {formatPrice(signal.tp2Resting.price)}
            </div>
          </div>
          <div>
            <div className="text-[9px] font-bold text-[#64748B] uppercase">Ratio R:R</div>
            <div className="text-xs font-mono font-black text-liquidity mt-0.5">
              1:{signal.riskRewardRatio}
            </div>
          </div>
        </div>

        {/* Row 4: FVG & Order Block Key Strip (Condition 2 Requirement) */}
        <div className="flex items-center justify-between px-3 py-2 rounded-lg bg-surface-elevated border border-border mt-2.5 text-xs">
          <div className="flex items-center space-x-2 text-[#F1F5F9] truncate">
            <Zap className="w-3.5 h-3.5 text-sniper shrink-0" />
            <span className="truncate">
              <strong className="text-sniper">FVG Récent:</strong>{' '}
              {signal.checklist.fvgObDetail.recentFvgAgeHours.toFixed(1)}h (Non mitigé ⚡) +{' '}
              <strong className="text-[#94A3B8]">Ancien:</strong>{' '}
              {signal.checklist.fvgObDetail.ancientFvgAgeHours.toFixed(1)}h (100% mitigé)
            </span>
          </div>
        </div>

        {/* Row 5: Liquidity Sweep & Rejection Strip (Condition 4 & Liquidity Management) */}
        <div className="flex items-center justify-between px-3 py-2 rounded-lg bg-liquidity-dark/40 border border-liquidity/20 mt-2 text-xs">
          <div className="flex items-center space-x-2 text-liquidity truncate">
            <Droplet className="w-3.5 h-3.5 text-liquidity shrink-0" />
            <span className="font-mono truncate">
              {signal.checklist.sweepDetail.sweepType} @ {formatPrice(signal.checklist.sweepDetail.sweptPrice)}
            </span>
          </div>
          <span className="text-[11px] font-bold text-buy whitespace-nowrap ml-2">
            Rejet Immédiat Confirmé 💧
          </span>
        </div>

        {/* Expandable 4-Confluence Checklist */}
        {isExpanded && (
          <div className="mt-3 pt-3 border-t border-border-subtle space-y-2">
            <div className="text-[10px] font-bold uppercase tracking-wider text-[#64748B]">
              Vérification des 4 Confluences Institutionnelles SMC :
            </div>

            {/* Condition 1 */}
            <div className="flex items-start space-x-2 text-xs p-2 rounded bg-surface-variant">
              <CheckCircle2 className="w-4 h-4 text-buy shrink-0 mt-0.5" />
              <div>
                <strong className="text-[#F1F5F9]">Condition 1 (Tendance HTF) :</strong>
                <p className="text-[#94A3B8] text-[11px]">{signal.checklist.htfDescription}</p>
              </div>
            </div>

            {/* Condition 2 */}
            <div className="flex items-start space-x-2 text-xs p-2 rounded bg-surface-variant">
              <CheckCircle2 className="w-4 h-4 text-buy shrink-0 mt-0.5" />
              <div>
                <strong className="text-[#F1F5F9]">Condition 2 (FVG & Order Block) :</strong>
                <p className="text-[#94A3B8] text-[11px]">
                  Récent ({signal.checklist.fvgObDetail.recentFvgAgeHours.toFixed(1)}h) non mitigé [
                  {formatPrice(signal.checklist.fvgObDetail.recentFvgLow)} -{' '}
                  {formatPrice(signal.checklist.fvgObDetail.recentFvgHigh)}] | {signal.checklist.fvgObDetail.orderBlockName}
                </p>
              </div>
            </div>

            {/* Condition 3 */}
            <div className="flex items-start space-x-2 text-xs p-2 rounded bg-surface-variant">
              <CheckCircle2 className="w-4 h-4 text-buy shrink-0 mt-0.5" />
              <div>
                <strong className="text-[#F1F5F9]">Condition 3 (Fibonacci Discount / Premium) :</strong>
                <p className="text-[#94A3B8] text-[11px]">
                  {signal.checklist.fiboDetail.zoneName} à {signal.checklist.fiboDetail.fiboLevelPercent}% OTE
                </p>
              </div>
            </div>

            {/* Condition 4 */}
            <div className="flex items-start space-x-2 text-xs p-2 rounded bg-surface-variant">
              <CheckCircle2 className="w-4 h-4 text-buy shrink-0 mt-0.5" />
              <div>
                <strong className="text-[#F1F5F9]">Condition 4 (Balayage Sweep 💧 & Rejet) :</strong>
                <p className="text-[#94A3B8] text-[11px]">
                  {signal.checklist.sweepDetail.sweepType} avec mèche de rejet confirmée de{' '}
                  {signal.checklist.sweepDetail.rejectionWickPips} pips
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Action Buttons Row */}
        <div className="flex items-center space-x-2 mt-3.5 pt-3 border-t border-border-subtle">
          {/* Trade Pris (Sourdine 6h) Button */}
          <button
            onClick={() => onTradeTaken(signal.pair.symbol)}
            className={`flex-1 flex items-center justify-center space-x-1.5 py-2 px-3 rounded-xl text-xs font-bold transition-all ${
              isMuted
                ? 'bg-liquidity-dark text-liquidity border border-liquidity/40 hover:bg-liquidity-dark/80'
                : 'bg-surface-elevated text-[#F1F5F9] border border-border hover:border-setup'
            }`}
          >
            {isMuted ? <VolumeX className="w-3.5 h-3.5" /> : <CheckCircle2 className="w-3.5 h-3.5" />}
            <span>{isMuted ? `Trade Pris (${remainingHours.toFixed(1)}h)` : 'Trade Pris (Sourdine 6h)'}</span>
          </button>

          {/* Telegram Alert Button */}
          <button
            onClick={() => onSendTelegram(signal)}
            className="flex items-center space-x-1.5 py-2 px-3 rounded-xl bg-setup-dark text-setup border border-setup/40 hover:bg-setup/20 transition-colors text-xs font-bold"
            title="Envoyer l'alerte sur Telegram"
          >
            <Send className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">Alerter Telegram</span>
          </button>

          {/* Expand Details Button */}
          <button
            onClick={() => setIsExpanded(!isExpanded)}
            className="p-2 rounded-xl bg-surface-elevated border border-border text-[#94A3B8] hover:text-[#F1F5F9] transition-colors"
            title={isExpanded ? 'Masquer confluences' : 'Afficher confluences'}
          >
            {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
          </button>

          {/* Modal Open Button */}
          <button
            onClick={() => onOpenDetail(signal)}
            className="p-2 rounded-xl bg-surface-elevated border border-border text-[#94A3B8] hover:text-[#F1F5F9] transition-colors"
            title="Fiche Complète SMC"
          >
            <Layers className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
};
