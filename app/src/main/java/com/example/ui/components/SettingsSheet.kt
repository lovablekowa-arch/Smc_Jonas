package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
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
import com.example.data.model.DefaultPairs
import com.example.data.model.MarketCategory
import com.example.data.model.UserPreferences
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    preferences: UserPreferences,
    isTestingTelegram: Boolean,
    telegramTestResult: String?,
    onDismiss: () -> Unit,
    onSavePreferences: (UserPreferences) -> Unit,
    onTestTelegram: (botToken: String, chatId: String) -> Unit
) {
    var botToken by remember(preferences) { mutableStateOf(preferences.telegramBotToken) }
    var chatId by remember(preferences) { mutableStateOf(preferences.telegramChatId) }
    var tgEnabled by remember(preferences) { mutableStateOf(preferences.telegramAlertsEnabled) }

    var alertSniper by remember(preferences) { mutableStateOf(preferences.alertOnSniper) }
    var alertGood by remember(preferences) { mutableStateOf(preferences.alertOnGoodSetup) }
    var alertWatchlist by remember(preferences) { mutableStateOf(preferences.alertOnWatchlist) }

    var antiDoublonHours by remember(preferences) { mutableStateOf(preferences.antiDoublonMuteHours) }
    var autoMuteOnTrade by remember(preferences) { mutableStateOf(preferences.autoMuteOnTradeTaken) }
    var soundEnabled by remember(preferences) { mutableStateOf(preferences.soundAlertsEnabled) }
    var vibrationEnabled by remember(preferences) { mutableStateOf(preferences.vibrationEnabled) }

    var selectedSymbols by remember(preferences) { mutableStateOf(preferences.activeSymbols.toMutableSet()) }
    var selectedTfs by remember(preferences) { mutableStateOf(preferences.activeTimeframes.toMutableSet()) }

    val scrollState = rememberScrollState()

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
            // Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RÉGLAGES & ALERTES",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Configuration Telegram, Confluences et Filtres",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fermer", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Telegram Bot Integration Section
            SettingsSectionHeader(title = "1. CONFIGURATION BOT TELEGRAM")
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Activer les Alertes Telegram",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Switch(
                            checked = tgEnabled,
                            onCheckedChange = { tgEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GoodSetupBlue,
                                checkedTrackColor = GoodSetupBlueDark
                            ),
                            modifier = Modifier.testTag("switch_telegram_enabled")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = botToken,
                        onValueChange = { botToken = it },
                        label = { Text("Telegram Bot Token (ex: 712345678:AAH...)") },
                        placeholder = { Text("Collez votre token @BotFather") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            focusedBorderColor = GoodSetupBlue,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_bot_token")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = chatId,
                        onValueChange = { chatId = it },
                        label = { Text("Chat ID / ID Canal (ex: 123456789 ou -100...)") },
                        placeholder = { Text("Votre Chat ID @userinfobot") },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated,
                            focusedBorderColor = GoodSetupBlue,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_chat_id")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Test Telegram Button
                    Button(
                        onClick = { onTestTelegram(botToken, chatId) },
                        enabled = !isTestingTelegram && botToken.isNotBlank() && chatId.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoodSetupBlue,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("btn_test_telegram")
                    ) {
                        if (isTestingTelegram) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Envoi du message test...", fontSize = 12.sp)
                        } else {
                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tester la Connexion Telegram", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (telegramTestResult != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = telegramTestResult,
                            fontSize = 11.sp,
                            color = if (telegramTestResult.startsWith("✅")) BuyGreen else SellRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Confluence Alert Triggers
            SettingsSectionHeader(title = "2. NIVEAUX DE CONFLUENCES À RECEVOIR")
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SettingToggleRow(
                        title = "🎯 Conditions Hautes / Sniper (4/4 - 95-100%)",
                        subtitle = "Tendance HTF + FVG/OB + Fibo + Sweep 💧",
                        checked = alertSniper,
                        onCheckedChange = { alertSniper = it },
                        color = SniperGold
                    )
                    Divider(color = DarkBorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                    SettingToggleRow(
                        title = "⚡ Conditions Moyennes / Bon Setup (3/4 - 75-90%)",
                        subtitle = "3 confluences SMC majeures validées",
                        checked = alertGood,
                        onCheckedChange = { alertGood = it },
                        color = GoodSetupBlue
                    )
                    Divider(color = DarkBorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                    SettingToggleRow(
                        title = "👁️ À Surveiller / Watchlist (2/4 - 60-70%)",
                        subtitle = "Setups en cours de formation structurelle",
                        checked = alertWatchlist,
                        onCheckedChange = { alertWatchlist = it },
                        color = WatchlistPurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Anti-Doublons (6h Mute on Trade Taken)
            SettingsSectionHeader(title = "3. OPTION ANTI-DOUBLONS (SOURDINE 6H)")
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SettingToggleRow(
                        title = "Sourdine 6h après « Trade Pris »",
                        subtitle = "Bloque les alertes répétitives sur la paire pendant 6 heures",
                        checked = autoMuteOnTrade,
                        onCheckedChange = { autoMuteOnTrade = it },
                        color = LiquidityCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Durée de la sourdine : $antiDoublonHours heures par position prise.",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Audio & Notifications
            SettingsSectionHeader(title = "4. SONS & VIBRATIONS")
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SettingToggleRow(
                        title = "Alertes Sonores Distinctes",
                        subtitle = "Chime Sniper & Signal de Liquidité Sweep",
                        checked = soundEnabled,
                        onCheckedChange = { soundEnabled = it },
                        color = TextPrimary
                    )
                    Divider(color = DarkBorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                    SettingToggleRow(
                        title = "Retour Haptique / Vibration",
                        subtitle = "Vibration instantanée sur signal",
                        checked = vibrationEnabled,
                        onCheckedChange = { vibrationEnabled = it },
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Active Pairs Management (Unitary and by category)
            SettingsSectionHeader(title = "5. PAIRES ACTIVES SOUHAITÉES")
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Fast action buttons: Select All / Deselect All
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                selectedSymbols = DefaultPairs.ALL_PAIRS.map { it.symbol }.toMutableSet()
                            }
                        ) {
                            Text("Tout Sélectionner", fontSize = 11.sp, color = PrimaryDark)
                        }
                        TextButton(
                            onClick = { selectedSymbols = mutableSetOf() }
                        ) {
                            Text("Tout Désélectionner", fontSize = 11.sp, color = SellRed)
                        }
                    }

                    MarketCategory.values().forEach { cat ->
                        Text(
                            text = cat.displayName.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(cat.badgeColorHex),
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )

                        DefaultPairs.ALL_PAIRS.filter { it.category == cat }.forEach { pair ->
                            val isChecked = selectedSymbols.contains(pair.symbol)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) selectedSymbols.remove(pair.symbol)
                                        else selectedSymbols.add(pair.symbol)
                                    }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = {
                                        if (it) selectedSymbols.add(pair.symbol)
                                        else selectedSymbols.remove(pair.symbol)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = PrimaryDark,
                                        uncheckedColor = DarkBorder
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = pair.symbol,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${pair.name})",
                                    fontSize = 11.sp,
                                    color = TextTertiary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    val updated = preferences.copy(
                        telegramBotToken = botToken,
                        telegramChatId = chatId,
                        telegramAlertsEnabled = tgEnabled,
                        alertOnSniper = alertSniper,
                        alertOnGoodSetup = alertGood,
                        alertOnWatchlist = alertWatchlist,
                        antiDoublonMuteHours = antiDoublonHours,
                        autoMuteOnTradeTaken = autoMuteOnTrade,
                        soundAlertsEnabled = soundEnabled,
                        vibrationEnabled = vibrationEnabled,
                        activeSymbols = selectedSymbols,
                        activeTimeframes = selectedTfs
                    )
                    onSavePreferences(updated)
                    onDismiss()
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BuyGreen,
                    contentColor = DarkBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_save_settings")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sauvegarder les Réglages",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
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
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = TextTertiary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = color,
                checkedTrackColor = color.copy(alpha = 0.2f)
            )
        )
    }
}
