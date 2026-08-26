package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SmcSignal
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalDetailSheet(
    signal: SmcSignal,
    onDismiss: () -> Unit,
    onSendTelegram: () -> Unit,
    onToggleTradeTaken: () -> Unit
) {
    val scrollState = rememberScrollState()
    val formatPrice = { p: Double -> "%.${signal.pair.decimals}f".format(p) }
    val isBuy = signal.direction.isBuy
    val directionColor = if (isBuy) BuyGreen else SellRed
    val directionBgColor = if (isBuy) BuyGreenDark else SellRedDark

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.7f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(TextTertiary)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState)
        ) {
            // Header with Pair & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = signal.pair.symbol,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(signal.pair.category.badgeColorHex).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = signal.pair.category.displayName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(signal.pair.category.badgeColorHex),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Text(
                        text = signal.pair.name,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Direction & Price Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = directionBgColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, directionColor.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = signal.direction.labelFr,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = directionColor
                        )
                        Text(
                            text = "Timeframe: ${signal.timeframe.label} • R:R = 1:${signal.riskRewardRatio}",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatPrice(signal.currentPrice),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${signal.checklist.trueConfluenceCount}/4 Confluences réunies",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SniperGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. HTF Trend Alignment
            SectionHeader(title = "1. TENDANCE HTF (DAILY / 4H / 30M)")
            DetailBox {
                Text(
                    text = signal.checklist.htfDescription,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Validation stricte du flux d'ordres institutionnel sur les unités majeures.",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. FVG & OB Mechanics (Recent vs Ancient)
            SectionHeader(title = "2. FAIR VALUE GAPS (FVG) & ORDER BLOCKS")
            DetailBox {
                DetailRow(
                    label = "⚡ FVG Récent (< 3h) NON MITIGÉ :",
                    value = "${"%.1f".format(signal.checklist.fvgObDetail.recentFvgAgeHours)}h (${formatPrice(signal.checklist.fvgObDetail.recentFvgLow)} - ${formatPrice(signal.checklist.fvgObDetail.recentFvgHigh)})",
                    color = BuyGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                DetailRow(
                    label = "⏳ FVG Ancien (> 8h) DÉJÀ MITIGÉ :",
                    value = "${"%.1f".format(signal.checklist.fvgObDetail.ancientFvgAgeHours)}h (Comblé à 100%)",
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                DetailRow(
                    label = "🧱 Order Block :",
                    value = signal.checklist.fvgObDetail.orderBlockName,
                    color = SniperGold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Fibonacci Discount / Premium Zone
            SectionHeader(title = "3. FIBONACCI DISCOUNT / PREMIUM")
            DetailBox {
                DetailRow(
                    label = "Zone Détectée :",
                    value = signal.checklist.fiboDetail.zoneName,
                    color = if (isBuy) BuyGreen else SellRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                DetailRow(
                    label = "Niveau Fibo OTE :",
                    value = "${"%.1f".format(signal.checklist.fiboDetail.fiboLevelPercent)}% Retracement",
                    color = LiquidityCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                DetailRow(
                    label = "Équilibre 50% :",
                    value = formatPrice(signal.checklist.fiboDetail.equilibrium50),
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Balayage de Liquidité Sweep 💧 & Resting Liquidity Targets
            SectionHeader(title = "4. GESTION DE LA LIQUIDITÉ (SWEEPS & CIBLES)")
            DetailBox {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = LiquidityCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Balayage : ${signal.checklist.sweepDetail.sweepType} @ ${formatPrice(signal.checklist.sweepDetail.sweptPrice)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidityCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                DetailRow(
                    label = "🎯 TP1 (Liquidité Interne Non Balayée) :",
                    value = "${formatPrice(signal.tp1Resting.price)} (+${"%.2f".format(signal.tp1Resting.distancePercent)}%)",
                    color = BuyGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                DetailRow(
                    label = "🎯 TP2 (Liquidité Majeure HTF Non Balayée) :",
                    value = "${formatPrice(signal.tp2Resting.price)} (+${"%.2f".format(signal.tp2Resting.distancePercent)}%)",
                    color = SniperGold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onToggleTradeTaken,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (signal.isMuted) LiquidityCyanDark else DarkSurfaceElevated,
                        contentColor = if (signal.isMuted) LiquidityCyan else TextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (signal.isMuted) LiquidityCyan else DarkBorder
                    ),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text(
                        text = if (signal.isMuted) "Démuter (6h)" else "Trade Pris (Sourdine 6h)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onSendTelegram,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoodSetupBlue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Alerter Telegram",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        color = TextTertiary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun DetailBox(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            content = content
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            fontFamily = FontFamily.Monospace
        )
    }
}
