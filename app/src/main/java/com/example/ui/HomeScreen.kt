package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val connectionStatus by viewModel.repository.connectionStatus.collectAsStateWithLifecycle()
    val isLiveConnected by viewModel.repository.isLiveConnected.collectAsStateWithLifecycle()

    val filteredSignals by viewModel.filteredSignals.collectAsStateWithLifecycle()
    val selectedFilterTab by viewModel.selectedFilterTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val sniperCount by viewModel.sniperCount.collectAsStateWithLifecycle()
    val goodSetupCount by viewModel.goodSetupCount.collectAsStateWithLifecycle()
    val watchlistCount by viewModel.watchlistCount.collectAsStateWithLifecycle()
    val mutedCount by viewModel.mutedCount.collectAsStateWithLifecycle()

    val selectedDetailSignal by viewModel.selectedSignalForDetail.collectAsStateWithLifecycle()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val userPreferences by viewModel.repository.userPreferences.collectAsStateWithLifecycle()

    val uiNotification by viewModel.uiNotification.collectAsStateWithLifecycle()
    val isTestingTelegram by viewModel.isTestingTelegram.collectAsStateWithLifecycle()
    val telegramTestResult by viewModel.telegramTestResult.collectAsStateWithLifecycle()

    // Auto dismiss notification after 4 seconds
    LaunchedEffect(uiNotification) {
        if (uiNotification != null) {
            delay(4000)
            viewModel.dismissNotification()
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header (App Title, WS Live pill, Search input, Settings button)
                HeaderBar(
                    connectionStatus = connectionStatus,
                    isLiveConnected = isLiveConnected,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    onSettingsClick = { viewModel.openSettings() }
                )

                // KPI Metrics Bar
                KpiMetricsRow(
                    sniperCount = sniperCount,
                    goodSetupCount = goodSetupCount,
                    watchlistCount = watchlistCount,
                    mutedCount = mutedCount,
                    onTabSelect = { viewModel.setFilterTab(it) }
                )

                // Filter Tabs (Sniper 4/4, Moyennes 3/4, Watchlist 2/4, Crypto, Forex, etc.)
                FilterPillsRow(
                    selectedTab = selectedFilterTab,
                    onTabSelected = { viewModel.setFilterTab(it) }
                )

                // Signals Lazy Column
                if (filteredSignals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Aucun signal ne correspond aux filtres",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Modifiez vos filtres ou activez plus de paires dans les réglages.",
                                fontSize = 12.sp,
                                color = TextTertiary,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(
                            items = filteredSignals,
                            key = { it.id }
                        ) { signal ->
                            SignalCard(
                                signal = signal,
                                onTradeTakenClick = { viewModel.toggleTradeTaken(signal.pair.symbol) },
                                onSendTelegramClick = { viewModel.sendSignalToTelegram(signal) },
                                onDetailClick = { viewModel.selectSignalForDetail(signal) }
                            )
                        }
                    }
                }
            }

            // In-app Floating Toast / Notification Banner
            AnimatedVisibility(
                visible = uiNotification != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                uiNotification?.let { notif ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (notif.isError) SellRedDark else DarkSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (notif.isError) SellRed else PrimaryDark
                        ),
                        shadowElevation = 8.dp,
                        modifier = Modifier.testTag("floating_notification_toast")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = notif.message,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (notif.isError) SellRedGlow else TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Signal Detail
    selectedDetailSignal?.let { signal ->
        SignalDetailSheet(
            signal = signal,
            onDismiss = { viewModel.selectSignalForDetail(null) },
            onSendTelegram = {
                viewModel.sendSignalToTelegram(signal)
            },
            onToggleTradeTaken = {
                viewModel.toggleTradeTaken(signal.pair.symbol)
            }
        )
    }

    // Modal Bottom Sheet for Settings & Telegram Configuration
    if (isSettingsOpen) {
        SettingsSheet(
            preferences = userPreferences,
            isTestingTelegram = isTestingTelegram,
            telegramTestResult = telegramTestResult,
            onDismiss = { viewModel.closeSettings() },
            onSavePreferences = { newPrefs ->
                viewModel.updatePreferences(newPrefs)
            },
            onTestTelegram = { token, chat ->
                viewModel.testTelegramConnection(token, chat)
            }
        )
    }
}
