package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SecurityViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(viewModel: SecurityViewModel) {
    val realTimeProtection by viewModel.realTimeProtectionEnabled.collectAsState()
    val autoScan by viewModel.autoScanEnabled.collectAsState()
    val deepScan by viewModel.deepScanEngineEnabled.collectAsState()
    val safeBrowsing by viewModel.safeBrowsingEnabled.collectAsState()
    val notifications by viewModel.securityNotificationsEnabled.collectAsState()
    val hasMasterPin by viewModel.hasMasterPin.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInputValue by remember { mutableStateOf("") }
    var pinErrorText by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Premium Header Badge
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(CyberBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AdminPanelSettings,
                            contentDescription = "Security Status",
                            tint = CyberBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "SECURITY ENGINE",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "v2.1.0-Fortress",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Signature databases up to date",
                            color = AccentEmerald,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real-Time Protection Banner Switch
            Text(
                text = "Primary Shield Configuration",
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(24.dp))
                    .testTag("settings_realtime_banner_card"),
                colors = CardDefaults.cardColors(
                    containerColor = if (realTimeProtection) DarkNavy else SurfaceCard
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = if (realTimeProtection) AccentEmerald.copy(alpha = 0.6f) else BorderSlate
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (realTimeProtection) AccentEmerald.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.1f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (realTimeProtection) Icons.Filled.GppGood else Icons.Filled.GppMaybe,
                                    contentDescription = "Shield Icon",
                                    tint = if (realTimeProtection) AccentEmerald else WarningAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Real-Time Protection",
                                    color = TextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (realTimeProtection) "Active • Monitoring system" else "Shield Suspended",
                                    color = if (realTimeProtection) AccentEmerald else WarningAmber,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Switch(
                            checked = realTimeProtection,
                            onCheckedChange = { viewModel.toggleRealTimeProtection() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF1D192B),
                                checkedTrackColor = AccentEmerald,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = SurfaceCard
                            ),
                            modifier = Modifier.testTag("settings_realtime_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Actively monitors system modifications, network requests, and installs background locks to safeguard local databases on-the-fly.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Additional Protection Knobs
            Text(
                text = "Advanced Engine Protection",
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    
                    // Toggle Item: Auto scan
                    SettingsToggleItem(
                        icon = Icons.Filled.Autorenew,
                        iconTint = ElectricCyan,
                        title = "Automatic Background Scan",
                        subtitle = "Scans storage and permissions dynamically",
                        checked = autoScan,
                        onCheckedChange = { viewModel.toggleAutoScan() },
                        testTag = "settings_autoscan_switch"
                    )

                    HorizontalDivider(color = BorderSlate, modifier = Modifier.padding(vertical = 14.dp))

                    // Toggle Item: Deep scan
                    SettingsToggleItem(
                        icon = Icons.Filled.SavedSearch,
                        iconTint = CyberBlue,
                        title = "Deep Heuristics Scan Engine",
                        subtitle = "Enhanced scanning logic (high resource cost)",
                        checked = deepScan,
                        onCheckedChange = { viewModel.toggleDeepScanEngine() },
                        testTag = "settings_deepscan_switch"
                    )

                    HorizontalDivider(color = BorderSlate, modifier = Modifier.padding(vertical = 14.dp))

                    // Toggle Item: Safe browsing
                    SettingsToggleItem(
                        icon = Icons.Filled.Language,
                        iconTint = ElectricCyan,
                        title = "Safe Browsing Gateway",
                        subtitle = "Checks target domain paths for known threats",
                        checked = safeBrowsing,
                        onCheckedChange = { viewModel.toggleSafeBrowsing() },
                        testTag = "settings_safebrowsing_switch"
                    )

                    HorizontalDivider(color = BorderSlate, modifier = Modifier.padding(vertical = 14.dp))

                    // Toggle Item: Notifications
                    SettingsToggleItem(
                        icon = Icons.Filled.NotificationsActive,
                        iconTint = WarningAmber,
                        title = "Security & Advisory Alerts",
                        subtitle = "Immediate push notifications for warnings",
                        checked = notifications,
                        onCheckedChange = { viewModel.toggleSecurityNotifications() },
                        testTag = "settings_notifications_switch"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Master Authentication Settings
            Text(
                text = "Device Access Control",
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPinDialog = true }
                            .padding(vertical = 8.dp)
                            .testTag("settings_master_pin_trigger"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LockPerson,
                                contentDescription = "Lock",
                                tint = CyberBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Setup / Update Master PIN",
                                color = TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (hasMasterPin) "Master authentication PIN is enabled" else "Master PIN not set up",
                                color = if (hasMasterPin) AccentEmerald else WarningAmber,
                                fontSize = 12.sp
                            )
                        }

                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Expand",
                            tint = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sandbox Simulation Settings
            Text(
                text = "Security Sandbox & Threat Testing",
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "For security simulation and verification purposes, you can reset the threat vectors inside our sandbox environment. This will restore the simulated virus files to test the 'Uninstall viruses' capability.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.resetViruses() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reset_threat_simulation_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberBlue,
                            contentColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BugReport,
                            contentDescription = "Reset Threats Icon",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SIMULATE / RESET VIRUS VECTORS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Legal & About info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "FORTRESS SECURITY PLATFORM",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Your localized fortress of on-device protection, credential vaulting, and network safeguarding. Built with military-grade, fully local architecture. No external cloud servers required.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Encryption Key: Local Keystore AES-GCM 256\nDatabase: SQLite Cipher Room\nPlatform Architecture: Android Jetpack Compose M3",
                        color = CyberBlue,
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Setup/Update PIN Dialog
        if (showPinDialog) {
            AlertDialog(
                onDismissRequest = {
                    showPinDialog = false
                    pinInputValue = ""
                    pinErrorText = null
                },
                containerColor = DarkNavy,
                titleContentColor = TextWhite,
                textContentColor = TextWhite,
                title = {
                    Text(
                        text = if (hasMasterPin) "Update Master PIN" else "Configure Master PIN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Set a 4-digit Master PIN to secure your Credentials Vault, App Lock, and Safe Notes.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )

                        OutlinedTextField(
                            value = pinInputValue,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 4) {
                                    pinInputValue = input
                                    pinErrorText = null
                                }
                            },
                            label = { Text("4-Digit PIN", color = TextMuted) },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = BorderSlate,
                                focusedLabelColor = ElectricCyan,
                                cursorColor = ElectricCyan
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_pin_input_field")
                        )

                        if (pinErrorText != null) {
                            Text(
                                text = pinErrorText!!,
                                color = Color.Red,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (pinInputValue.length == 4) {
                                viewModel.setupMasterPin(pinInputValue)
                                showPinDialog = false
                                pinInputValue = ""
                                pinErrorText = null
                            } else {
                                pinErrorText = "PIN must be exactly 4 digits long."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                        modifier = Modifier.testTag("settings_pin_confirm_button")
                    ) {
                        Text("Save PIN", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showPinDialog = false
                            pinInputValue = ""
                            pinErrorText = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = TextMuted)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF1D192B),
                checkedTrackColor = ElectricCyan,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = SurfaceCard
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
