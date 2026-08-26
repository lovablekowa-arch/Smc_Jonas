package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConfluenceLevel
import com.example.data.model.SmcSignal
import com.example.ui.theme.*

@Composable
fun SignalCard(
    signal: SmcSignal,
    onTradeTakenClick: () -> Unit,
    onSendTelegramClick: () -> Unit,
    onDetailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val formatPrice = { p: Double -> "%.${signal.pair.decimals}f".format(p) }
    val isBuy = signal.direction.isBuy

    val directionColor = if (isBuy) BuyGreen else SellRed
    val directionBgColor = if (isBuy) BuyGreenDark else SellRedDark
    val directionBorder = if (isBuy) BuyGreenBorder else SellRedBorder

    val confluenceColor = Color(signal.confluenceLevel.accentColorHex)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (signal.confluenceLevel == ConfluenceLevel.SNIPER) SniperGold.copy(alpha = 0.5f) else DarkBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("signal_card_${signal.pair.rawTicker}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Pair Symbol, Category, Confluence Badge, Live Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Pair Symbol & Category
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = signal.pair.symbol,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(signal.pair.category.badgeColorHex).copy(alpha = 0.15f),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Text(
                            text = signal.pair.category.displayName,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(signal.pair.category.badgeColorHex),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    if (signal.isMuted) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = LiquidityCyanDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, LiquidityCyan.copy(alpha = 0.4f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeMute,
                                    contentDescription = "Sourdine active",
                                    tint = LiquidityCyan,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${"%.1f".format(signal.remainingMuteHours)}h",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LiquidityCyan
                                )
                            }
                        }
                    }
                }

                // Right: Confluence Score Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = confluenceColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, confluenceColor.copy(alpha = 0.6f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = signal.confluenceLevel.iconEmoji,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${signal.checklist.trueConfluenceCount}/4 ${signal.confluenceLevel.scoreRange}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = confluenceColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle Row: Direction Banner (BUY/SELL), Timeframe, Realtime Live Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Direction Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = directionBgColor,
                    border = androidx.compose.foundation.BorderStroke(1.dp, directionBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(directionColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = signal.direction.labelFr,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = directionColor,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = signal.timeframe.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }

                // Live Price & 24h Delta
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatPrice(signal.currentPrice),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${if (signal.priceChange24h >= 0) "+" else ""}${"%.2f".format(signal.priceChange24h)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (signal.priceChange24h >= 0) BuyGreen else SellRed,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Price Structure Grid (Entry, SL, TP1, TP2, R:R)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PriceMetricItem(label = "ENTRÉE", value = formatPrice(signal.entryPrice), color = TextPrimary)
                    PriceMetricItem(label = "STOP LOSS", value = formatPrice(signal.stopLoss), color = SellRed)
                    PriceMetricItem(
                        label = "TP1 (INTERNE)",
                        value = formatPrice(signal.tp1Resting.price),
                        color = BuyGreen
                    )
                    PriceMetricItem(
                        label = "TP2 (MAJEUR)",
                        value = formatPrice(signal.tp2Resting.price),
                        color = SniperGold
                    )
                    PriceMetricItem(
                        label = "R:R",
                        value = "1:${signal.riskRewardRatio}",
                        color = LiquidityCyan,
                        isBold = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Condition 2 Highlight: FVG Récent non mitigé vs Ancien mitigé (CRITICAL REQUIREMENT)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ FVG :",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SniperGold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = signal.checklist.fvgObDetail.displaySummary,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Liquidity Sweeps & Targets Strip (Requirement 2 & 4)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = LiquidityCyanDark.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, LiquidityCyan.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Sweep de liquidité",
                            tint = LiquidityCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${signal.checklist.sweepDetail.sweepType} @ ${formatPrice(signal.checklist.sweepDetail.sweptPrice)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = LiquidityCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "Rejet Immédiat 💧",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BuyGreen
                    )
                }
            }

            // Expandable Confluences Checklist (4 SMC Confluences)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = "CONFLUENCES INSTITUTIONNELLES (4/4 SMC) :",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = TextTertiary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Condition 1: Tendance HTF
                    ConfluenceCheckRow(
                        conditionNumber = "1",
                        title = "Tendance HTF (1D / 4H / 30M)",
                        detail = signal.checklist.htfDescription,
                        isValid = signal.checklist.htfTrendAligned
                    )

                    // Condition 2: FVG & Order Block
                    ConfluenceCheckRow(
                        conditionNumber = "2",
                        title = "FVG & Order Block",
                        detail = "Récent: ${"%.1f".format(signal.checklist.fvgObDetail.recentFvgAgeHours)}h (Non mitigé) | Ancien: ${"%.1f".format(signal.checklist.fvgObDetail.ancientFvgAgeHours)}h (Comblé 100%)",
                        isValid = signal.checklist.fvgObValid
                    )

                    // Condition 3: Fibonacci Discount / Premium
                    ConfluenceCheckRow(
                        conditionNumber = "3",
                        title = "Fibonacci OTE",
                        detail = "${signal.checklist.fiboDetail.zoneName} à ${"%.1f".format(signal.checklist.fiboDetail.fiboLevelPercent)}% OTE",
                        isValid = signal.checklist.fiboDiscountPremiumValid
                    )

                    // Condition 4: Balayage de Liquidité Sweep
                    ConfluenceCheckRow(
                        conditionNumber = "4",
                        title = "Balayage Sweep 💧 & Rejet",
                        detail = "${signal.checklist.sweepDetail.sweepType} avec mèche de rejet confirmée",
                        isValid = signal.checklist.sweepValid
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Trade Pris (Sourdine 6h)" Button
                Button(
                    onClick = onTradeTakenClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (signal.isMuted) LiquidityCyanDark else DarkSurfaceElevated,
                        contentColor = if (signal.isMuted) LiquidityCyan else TextSecondary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (signal.isMuted) LiquidityCyan.copy(alpha = 0.5f) else DarkBorder
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(36.dp)
                        .testTag("btn_trade_taken_${signal.pair.rawTicker}")
                ) {
                    Icon(
                        imageVector = if (signal.isMuted) Icons.Default.CheckCircle else Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (signal.isMuted) "Trade Pris (Sourdine)" else "Trade Pris (6h)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // Telegram Alert Button
                IconButton(
                    onClick = onSendTelegramClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoodSetupBlueDark)
                        .border(1.dp, GoodSetupBlue.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .testTag("btn_telegram_${signal.pair.rawTicker}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Alerter Telegram",
                        tint = GoodSetupBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Expand / Detail Button
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Afficher confluences",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PriceMetricItem(
    label: String,
    value: String,
    color: Color,
    isBold: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 8.sp,
            color = TextTertiary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Black else FontWeight.Bold,
            color = color,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun ConfluenceCheckRow(
    conditionNumber: String,
    title: String,
    detail: String,
    isValid: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = if (isValid) BuyGreenDark else SellRedDark,
            modifier = Modifier.size(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (isValid) "✓" else "✕",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isValid) BuyGreen else SellRed
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = "Condition $conditionNumber: $title",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isValid) TextPrimary else TextTertiary
            )
            Text(
                text = detail,
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}
