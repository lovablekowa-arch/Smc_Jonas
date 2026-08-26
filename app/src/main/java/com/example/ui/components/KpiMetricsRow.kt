package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SignalFilterTab
import com.example.ui.theme.*

@Composable
fun KpiMetricsRow(
    sniperCount: Int,
    goodSetupCount: Int,
    watchlistCount: Int,
    mutedCount: Int,
    onTabSelect: (SignalFilterTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Sniper KPI Card (4/4)
        KpiCard(
            title = "SNIPER",
            subtitle = "4/4 Confluences",
            count = sniperCount,
            accentColor = SniperGold,
            backgroundColor = SniperGoldDark,
            modifier = Modifier
                .weight(1f)
                .clickable { onTabSelect(SignalFilterTab.SNIPER) }
        )

        // Good Setup KPI Card (3/4)
        KpiCard(
            title = "MOYENNES",
            subtitle = "3/4 Setups",
            count = goodSetupCount,
            accentColor = GoodSetupBlue,
            backgroundColor = GoodSetupBlueDark,
            modifier = Modifier
                .weight(1f)
                .clickable { onTabSelect(SignalFilterTab.GOOD_SETUP) }
        )

        // Watchlist KPI Card (2/4)
        KpiCard(
            title = "WATCHLIST",
            subtitle = "2/4 En cours",
            count = watchlistCount,
            accentColor = WatchlistPurple,
            backgroundColor = WatchlistPurpleDark,
            modifier = Modifier
                .weight(1f)
                .clickable { onTabSelect(SignalFilterTab.WATCHLIST) }
        )

        // Muted / Anti-doublons Card
        KpiCard(
            title = "SOURDINE",
            subtitle = "Trades 6h",
            count = mutedCount,
            accentColor = LiquidityCyan,
            backgroundColor = LiquidityCyanDark,
            modifier = Modifier
                .weight(1f)
                .clickable { onTabSelect(SignalFilterTab.ALL) }
        )
    }
}

@Composable
private fun KpiCard(
    title: String,
    subtitle: String,
    count: Int,
    accentColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = count.toString(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = TextTertiary,
                maxLines = 1
            )
        }
    }
}
