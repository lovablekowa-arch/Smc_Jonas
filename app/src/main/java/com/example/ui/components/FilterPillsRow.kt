package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SignalFilterTab
import com.example.ui.theme.*

@Composable
fun FilterPillsRow(
    selectedTab: SignalFilterTab,
    onTabSelected: (SignalFilterTab) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SignalFilterTab.values().forEach { tab ->
            val isSelected = tab == selectedTab
            val accentColor = when (tab) {
                SignalFilterTab.SNIPER -> SniperGold
                SignalFilterTab.GOOD_SETUP -> GoodSetupBlue
                SignalFilterTab.WATCHLIST -> WatchlistPurple
                SignalFilterTab.CRYPTO -> Color(0xFFF59E0B)
                SignalFilterTab.FOREX -> Color(0xFF3B82F6)
                SignalFilterTab.COMMODITIES -> Color(0xFFEAB308)
                SignalFilterTab.SYNTHETICS -> Color(0xFFEC4899)
                SignalFilterTab.ALL -> PrimaryDark
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) accentColor.copy(alpha = 0.15f) else DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) accentColor else DarkBorder
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onTabSelected(tab) }
                    .testTag("filter_tab_${tab.name.lowercase()}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tab.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) accentColor else TextSecondary
                    )
                    if (tab.badge != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tab.badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) accentColor else TextTertiary
                        )
                    }
                }
            }
        }
    }
}
