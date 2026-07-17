package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SecurityViewModel
import com.example.ui.viewmodel.AppItem

@Composable
fun AppLockView(viewModel: SecurityViewModel) {
    val installedApps by viewModel.filteredInstalledApps.collectAsState()
    val lockedApps by viewModel.lockedApps.collectAsState()
    val searchQuery by viewModel.appLockSearchQuery.collectAsState()
    val hasMasterPin by viewModel.hasMasterPin.collectAsState()
    val simulatedLockedAppToOpen by viewModel.simulatedLockedAppToOpen.collectAsState()

    var showPinSetupDialog by remember { mutableStateOf(false) }
    var newPinCode by remember { mutableStateOf("") }
    var pinSetupError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Lock Header Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AppRegistration,
                            contentDescription = "App Lock Icon",
                            tint = ElectricCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Fortress App Lock",
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${lockedApps.size} Applications Secured",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }

                    if (!hasMasterPin) {
                        Button(
                            onClick = { showPinSetupDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarningAmber,
                                contentColor = DeepObsidian
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Setup PIN",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SET PIN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Filter Row
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateAppLockSearchQuery(it) },
                placeholder = { Text("Search installed applications...", color = TextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = TextMuted
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_lock_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedContainerColor = DarkNavy,
                    unfocusedContainerColor = DarkNavy,
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = BorderSlate,
                    cursorColor = ElectricCyan
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Protected & Installed Apps",
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (installedApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ElectricCyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Querying device package systems...", color = TextMuted, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(installedApps) { appItem ->
                        AppLockListItem(
                            appItem = appItem,
                            hasPin = hasMasterPin,
                            onToggle = { viewModel.toggleAppLock(appItem) },
                            onTestLaunch = { viewModel.triggerSimulatedAppOpen(appItem) },
                            onRequestPinSetup = { showPinSetupDialog = true }
                        )
                    }
                }
            }
        }

        // Animated PIN Verification Lock Screen Overlay
        AnimatedVisibility(
            visible = simulatedLockedAppToOpen != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            simulatedLockedAppToOpen?.let { app ->
                AppLockVerificationOverlay(
                    app = app,
                    onVerify = { pin -> viewModel.verifyAppLockPin(pin) },
                    onClose = { viewModel.closeSimulatedApp() }
                )
            }
        }

        // Setup PIN dialog if not set up yet
        if (showPinSetupDialog) {
            AlertDialog(
                onDismissRequest = {
                    showPinSetupDialog = false
                    newPinCode = ""
                    pinSetupError = null
                },
                title = { Text("Configure Master PIN", color = TextWhite) },
                text = {
                    Column {
                        Text(
                            text = "Establish a 4-digit Master Security PIN to protect locked files, notepad, credentials, and apps.",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = newPinCode,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 4) {
                                    newPinCode = input
                                }
                            },
                            label = { Text("Enter 4-Digit PIN", color = TextMuted) },
                            placeholder = { Text("e.g. 1234", color = BorderSlate) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = BorderSlate,
                                focusedLabelColor = ElectricCyan
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("app_lock_pin_setup_input")
                        )

                        if (pinSetupError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = pinSetupError ?: "",
                                color = DangerCrimson,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPinCode.length == 4) {
                                viewModel.setupMasterPin(newPinCode)
                                showPinSetupDialog = false
                                newPinCode = ""
                                pinSetupError = null
                            } else {
                                pinSetupError = "PIN must be exactly 4 digits."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = Color(0xFF381E72)
                        )
                    ) {
                        Text("Save PIN", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showPinSetupDialog = false
                        newPinCode = ""
                        pinSetupError = null
                    }) {
                        Text("Cancel", color = TextMuted)
                    }
                },
                containerColor = DarkNavy
            )
        }
    }
}

@Composable
fun AppLockListItem(
    appItem: AppItem,
    hasPin: Boolean,
    onToggle: () -> Unit,
    onTestLaunch: () -> Unit,
    onRequestPinSetup: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_lock_item_${appItem.packageName}"),
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (appItem.isLocked) ElectricCyan.copy(alpha = 0.5f) else BorderSlate)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Simulated / Elegant Monogram Logo
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appItem.appName.take(2).uppercase(),
                    color = if (appItem.isLocked) ElectricCyan else TextMuted,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appItem.appName,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = appItem.packageName,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            // Simulated Launch Button
            if (appItem.isLocked) {
                IconButton(
                    onClick = {
                        if (hasPin) {
                            onTestLaunch()
                        } else {
                            onRequestPinSetup()
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(SurfaceCard, CircleShape)
                        .testTag("test_launch_${appItem.packageName}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Test Lock Simulator",
                        tint = AccentEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Lock Switch Toggler
            IconButton(
                onClick = onToggle,
                modifier = Modifier
                    .size(36.dp)
                    .background(if (appItem.isLocked) ElectricCyan else SurfaceCard, CircleShape)
                    .testTag("lock_toggle_${appItem.packageName}")
            ) {
                Icon(
                    imageVector = if (appItem.isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    contentDescription = if (appItem.isLocked) "Locked" else "Unlocked",
                    tint = if (appItem.isLocked) Color(0xFF381E72) else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AppLockVerificationOverlay(
    app: AppItem,
    onVerify: (String) -> Boolean,
    onClose: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessAnimation by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .clickable(enabled = false) {}, // Intercept click events
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = TextWhite)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Locked Lock Visual Indicator
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (showSuccessAnimation) AccentEmerald.copy(alpha = 0.2f) else DangerCrimson.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (showSuccessAnimation) Icons.Filled.LockOpen else Icons.Filled.Lock,
                    contentDescription = "Lock",
                    tint = if (showSuccessAnimation) AccentEmerald else DangerCrimson,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ACCESS RESTRICTED",
                color = if (showSuccessAnimation) AccentEmerald else DangerCrimson,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = app.appName,
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "This application is encrypted & protected by Fortness Security",
                color = TextMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4 Dots representation
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    showSuccessAnimation -> AccentEmerald
                                    i < enteredPin.length -> ElectricCyan
                                    else -> BorderSlate
                                }
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = DangerCrimson,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Numeric Keypad
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(280.dp)
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "⌫")
                )

                keys.forEach { rowKeys ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowKeys.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.3f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DarkNavy)
                                    .clickable {
                                        if (showSuccessAnimation) return@clickable
                                        errorMessage = null

                                        when (key) {
                                            "C" -> enteredPin = ""
                                            "⌫" -> if (enteredPin.isNotEmpty()) enteredPin =
                                                enteredPin.dropLast(1)

                                            else -> {
                                                if (enteredPin.length < 4) {
                                                    enteredPin += key
                                                    if (enteredPin.length == 4) {
                                                        val result = onVerify(enteredPin)
                                                        if (result) {
                                                            showSuccessAnimation = true
                                                        } else {
                                                            errorMessage = "Wrong PIN. Access Denied."
                                                            enteredPin = ""
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .testTag("sim_key_$key"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    color = TextWhite,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
