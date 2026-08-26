import { SmcSignal } from '../types/smc';

export class TelegramNotificationService {
  /**
   * Dispatches a rich formatted SMC Signal alert to Telegram.
   */
  public async sendSignalAlert(
    botToken: string,
    chatId: string,
    signal: SmcSignal
  ): Promise<{ success: boolean; message: string }> {
    if (!botToken.trim() || !chatId.trim()) {
      return { success: false, message: 'Bot Token ou Chat ID manquant dans les réglages.' };
    }

    const token = botToken.trim();
    const chat = chatId.trim();

    const directionEmoji = signal.direction === 'BUY' ? '🟢 ACHAT (DISCOUNT ZONE)' : '🔴 VENTE (PREMIUM ZONE)';
    const levelBadge =
      signal.confluenceLevel === 'SNIPER'
        ? '🎯 SNIPER (4/4 CONFLUENCES - 95-100%)'
        : signal.confluenceLevel === 'GOOD_SETUP'
        ? '⚡ BON SETUP (3/4 CONFLUENCES - 75-90%)'
        : '👁️ WATCHLIST (2/4 CONFLUENCES - 60-70%)';

    const formatPrice = (p: number) => p.toFixed(signal.pair.decimals);

    const messageText = `
═══════════════════════
<b>${levelBadge}</b>
<b>PAIRE :</b> <code>${signal.pair.symbol}</code> (${signal.timeframe})
<b>MARCHÉ :</b> ${signal.pair.category}
<b>DIRECTION :</b> ${directionEmoji}
═══════════════════════
📍 <b>Prix d'Entrée :</b> <code>${formatPrice(signal.entryPrice)}</code>
🛑 <b>Stop Loss :</b> <code>${formatPrice(signal.stopLoss)}</code>
🎯 <b>TP1 (Liquidité Interne) :</b> <code>${formatPrice(signal.tp1Resting.price)}</code> (+${signal.tp1Resting.distancePercent.toFixed(2)}%)
🎯 <b>TP2 (Liquidité Majeure) :</b> <code>${formatPrice(signal.tp2Resting.price)}</code> (+${signal.tp2Resting.distancePercent.toFixed(2)}%)
⚖️ <b>Ratio R:R :</b> 1:${signal.riskRewardRatio}
───────────────────────
💧 <b>ANALYSE LIQUIDITÉ & MITIGATION :</b>
• <b>Sweep :</b> ${signal.checklist.sweepDetail.sweepType} @ <code>${formatPrice(signal.checklist.sweepDetail.sweptPrice)}</code> (Rejet validé 💧)
• <b>FVG Récent :</b> ${signal.checklist.fvgObDetail.recentFvgAgeHours.toFixed(1)}h ⚡ Non mitigé [${formatPrice(signal.checklist.fvgObDetail.recentFvgLow)} - ${formatPrice(signal.checklist.fvgObDetail.recentFvgHigh)}]
• <b>FVG Ancien :</b> ${signal.checklist.fvgObDetail.ancientFvgAgeHours.toFixed(1)}h (Comblé 100% déjà mitigé)
• <b>Order Block :</b> ${signal.checklist.fvgObDetail.orderBlockName}
• <b>Fibonacci :</b> ${signal.checklist.fiboDetail.zoneName} (${signal.checklist.fiboDetail.fiboLevelPercent}% OTE)
• <b>Tendance HTF :</b> ${signal.checklist.htfDescription}
═══════════════════════
<i>⚡ SMC Liquidity Signals Engine • Pas de bruit, 100% Confluences</i>
`.trim();

    try {
      const url = `https://api.telegram.org/bot${token}/sendMessage`;
      const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          chat_id: chat,
          text: messageText,
          parse_mode: 'HTML'
        })
      });

      const data = await response.json();
      if (data.ok) {
        return { success: true, message: `Alerte envoyée avec succès pour ${signal.pair.symbol} !` };
      } else {
        return { success: false, message: `Erreur Telegram : ${data.description || 'Code HTTP ' + response.status}` };
      }
    } catch (err: any) {
      return { success: false, message: `Erreur réseau : ${err?.message || 'Vérifiez la connexion'}` };
    }
  }

  /**
   * Tests bot connection with token and chat ID.
   */
  public async testConnection(
    botToken: string,
    chatId: string
  ): Promise<{ success: boolean; message: string }> {
    if (!botToken.trim() || !chatId.trim()) {
      return { success: false, message: 'Veuillez renseigner le Bot Token et le Chat ID.' };
    }

    const testText = `
🔔 <b>TEST DE CONNEXION RÉUSSI !</b>
Le bot Telegram <b>SMC Liquidity Signals</b> est correctement configuré.
Vous recevrez automatiquement les alertes de confluences 4/4 et balayages de liquidités 💧.
`.trim();

    try {
      const url = `https://api.telegram.org/bot${botToken.trim()}/sendMessage`;
      const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          chat_id: chatId.trim(),
          text: testText,
          parse_mode: 'HTML'
        })
      });

      const data = await response.json();
      if (data.ok) {
        return { success: true, message: 'Connexion Telegram validée ! Message reçu.' };
      } else {
        return { success: false, message: `Échec Telegram : ${data.description || 'Erreur inconnue'}` };
      }
    } catch (err: any) {
      return { success: false, message: `Impossible de contacter Telegram : ${err?.message}` };
    }
  }
}
