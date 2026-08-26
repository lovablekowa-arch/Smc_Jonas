import React from 'react';
import { X, Send, TrendingUp, TrendingDown, Droplet, Zap, CheckCircle2, Shield, Layers } from 'lucide-react';
import { SmcSignal } from '../types/smc';

interface SignalModalProps {
  signal: SmcSignal | null;
  onClose: () => void;
  onSendTelegram: (signal: SmcSignal) => void;
  onToggleTradeTaken: (symbol: string) => void;
}

export const SignalModal: React.FC<SignalModalProps> = ({
  signal,
  onClose,
  onSendTelegram,
  onToggleTradeTaken
}) => {
  if (!signal) return null;

  const isBuy = signal.direction === 'BUY';
  const formatPrice = (p: number) => p.toFixed(signal.pair.decimals);

  const isMuted = !!(
    signal.tradeTakenTimestamp &&
    Date.now() - signal.tradeTakenTimestamp < 6 * 60 * 60 * 1000
  );

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-surface border border-border rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between p-5 border-b border-border sticky top-0 bg-surface/95 backdrop-blur z-10">
          <div>
            <div className="flex items-center space-x-2">
              <h2 className="font-mono font-black text-xl text-[#F1F5F9]">{signal.pair.symbol}</h2>
              <span className="text-xs font-bold px-2.5 py-0.5 rounded bg-surface-variant text-sniper border border-border">
                {signal.pair.category}
              </span>
            </div>
            <p className="text-xs text-[#94A3B8]">{signal.pair.name}</p>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-lg bg-surface-elevated text-[#94A3B8] hover:text-[#F1F5F9] hover:bg-surface-variant transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-5 space-y-4">
          {/* Direction & Price Big Card */}
          <div
            className={`p-4 rounded-xl border flex items-center justify-between ${
              isBuy
                ? 'bg-buy-dark/40 border-buy-border text-buy'
                : 'bg-sell-dark/40 border-sell-border text-sell'
            }`}
          >
            <div>
              <div className="flex items-center space-x-2">
                {isBuy ? <TrendingUp className="w-5 h-5" /> : <TrendingDown className="w-5 h-5" />}
                <span className="text-lg font-black">{isBuy ? 'ACHAT / LONG' : 'VENTE / SHORT'}</span>
              </div>
              <p className="text-xs text-[#94A3B8] mt-0.5">
                Timeframe: {signal.timeframe} • R:R = 1:{signal.riskRewardRatio}
              </p>
            </div>
            <div className="text-right">
              <div className="font-mono font-black text-2xl text-[#F1F5F9]">
                {formatPrice(signal.currentPrice)}
              </div>
              <div className="text-xs font-bold text-sniper">
                {signal.confluenceScore}/4 Confluences Réunies
              </div>
            </div>
          </div>

          {/* Price Matrix Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 p-3 rounded-xl bg-surface-variant border border-border-subtle text-center">
            <div>
              <div className="text-[10px] font-bold text-[#64748B] uppercase">Prix d'Entrée</div>
              <div className="text-sm font-mono font-bold text-[#F1F5F9] mt-0.5">
                {formatPrice(signal.entryPrice)}
              </div>
            </div>
            <div>
              <div className="text-[10px] font-bold text-[#64748B] uppercase">Stop Loss</div>
              <div className="text-sm font-mono font-bold text-sell mt-0.5">
                {formatPrice(signal.stopLoss)}
              </div>
            </div>
            <div>
              <div className="text-[10px] font-bold text-[#64748B] uppercase">TP1 (Interne)</div>
              <div className="text-sm font-mono font-bold text-buy mt-0.5">
                {formatPrice(signal.tp1Resting.price)} (+{signal.tp1Resting.distancePercent.toFixed(2)}%)
              </div>
            </div>
            <div>
              <div className="text-[10px] font-bold text-[#64748B] uppercase">TP2 (Majeur)</div>
              <div className="text-sm font-mono font-bold text-sniper mt-0.5">
                {formatPrice(signal.tp2Resting.price)} (+{signal.tp2Resting.distancePercent.toFixed(2)}%)
              </div>
            </div>
          </div>

          {/* Section 1: HTF Alignment */}
          <div className="p-3.5 rounded-xl bg-surface-variant border border-border space-y-1.5">
            <div className="text-xs font-bold uppercase tracking-wider text-sniper">
              1. Tendance HTF (Daily 1D / 4H / 30M)
            </div>
            <p className="text-sm text-[#F1F5F9] font-medium">{signal.checklist.htfDescription}</p>
            <p className="text-xs text-[#64748B]">
              Flux d'ordres institutionnel strictement aligné sur les unités de temps supérieures.
            </p>
          </div>

          {/* Section 2: FVG & Order Block */}
          <div className="p-3.5 rounded-xl bg-surface-variant border border-border space-y-2">
            <div className="text-xs font-bold uppercase tracking-wider text-sniper">
              2. Fair Value Gaps (FVG) & Order Blocks
            </div>
            <div className="space-y-1.5 text-xs">
              <div className="flex justify-between items-center text-[#F1F5F9]">
                <span className="text-buy font-bold">⚡ FVG Récent (&lt; 3h) NON MITIGÉ :</span>
                <span className="font-mono">
                  {signal.checklist.fvgObDetail.recentFvgAgeHours.toFixed(1)}h [
                  {formatPrice(signal.checklist.fvgObDetail.recentFvgLow)} -{' '}
                  {formatPrice(signal.checklist.fvgObDetail.recentFvgHigh)}]
                </span>
              </div>
              <div className="flex justify-between items-center text-[#94A3B8]">
                <span>⏳ FVG Ancien (&gt; 8h) DÉJÀ MITIGÉ :</span>
                <span className="font-mono">
                  {signal.checklist.fvgObDetail.ancientFvgAgeHours.toFixed(1)}h (Comblé à 100%)
                </span>
              </div>
              <div className="flex justify-between items-center text-[#F1F5F9]">
                <span className="text-sniper font-bold">🧱 Order Block :</span>
                <span className="font-medium">{signal.checklist.fvgObDetail.orderBlockName}</span>
              </div>
            </div>
          </div>

          {/* Section 3: Fibonacci Discount / Premium */}
          <div className="p-3.5 rounded-xl bg-surface-variant border border-border space-y-2">
            <div className="text-xs font-bold uppercase tracking-wider text-sniper">
              3. Fibonacci Retracement & Equilibrium
            </div>
            <div className="space-y-1.5 text-xs">
              <div className="flex justify-between items-center text-[#F1F5F9]">
                <span>Zone d'Entrée :</span>
                <span className={`font-bold ${isBuy ? 'text-buy' : 'text-sell'}`}>
                  {signal.checklist.fiboDetail.zoneName}
                </span>
              </div>
              <div className="flex justify-between items-center text-[#F1F5F9]">
                <span>Niveau OTE Fibo :</span>
                <span className="font-mono text-liquidity">
                  {signal.checklist.fiboDetail.fiboLevelPercent}% Retracement
                </span>
              </div>
              <div className="flex justify-between items-center text-[#94A3B8]">
                <span>Équilibre 50% :</span>
                <span className="font-mono">{formatPrice(signal.checklist.fiboDetail.equilibrium50)}</span>
              </div>
            </div>
          </div>

          {/* Section 4: Liquidity Sweeps & Targets */}
          <div className="p-3.5 rounded-xl bg-surface-variant border border-border space-y-2">
            <div className="text-xs font-bold uppercase tracking-wider text-sniper">
              4. Gestion des Liquidités (Sweeps 💧 & Cibles)
            </div>
            <div className="space-y-1.5 text-xs">
              <div className="flex items-center space-x-2 text-liquidity font-bold">
                <Droplet className="w-4 h-4 text-liquidity" />
                <span>
                  Sweep Récent : {signal.checklist.sweepDetail.sweepType} @{' '}
                  {formatPrice(signal.checklist.sweepDetail.sweptPrice)}
                </span>
              </div>
              <div className="flex justify-between items-center text-buy">
                <span>🎯 TP1 (Liquidité Interne Non Balayée) :</span>
                <span className="font-mono font-bold">
                  {formatPrice(signal.tp1Resting.price)} (+{signal.tp1Resting.distancePercent.toFixed(2)}%)
                </span>
              </div>
              <div className="flex justify-between items-center text-sniper">
                <span>🎯 TP2 (Liquidité Majeure HTF Non Balayée) :</span>
                <span className="font-mono font-bold">
                  {formatPrice(signal.tp2Resting.price)} (+{signal.tp2Resting.distancePercent.toFixed(2)}%)
                </span>
              </div>
            </div>
          </div>

          {/* Action Row */}
          <div className="flex items-center space-x-3 pt-2">
            <button
              onClick={() => onToggleTradeTaken(signal.pair.symbol)}
              className={`flex-1 py-3 px-4 rounded-xl text-xs font-bold transition-all ${
                isMuted
                  ? 'bg-liquidity-dark text-liquidity border border-liquidity/40'
                  : 'bg-surface-elevated text-[#F1F5F9] border border-border hover:border-setup'
              }`}
            >
              {isMuted ? 'Démuter la paire (6h)' : 'Marquer Trade Pris (Sourdine 6h)'}
            </button>

            <button
              onClick={() => {
                onSendTelegram(signal);
              }}
              className="flex-1 flex items-center justify-center space-x-2 py-3 px-4 rounded-xl bg-setup text-white font-bold text-xs hover:bg-setup/90 transition-colors"
            >
              <Send className="w-4 h-4" />
              <span>Envoyer sur Telegram</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
