package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SecurityViewModel
import com.example.ui.viewmodel.VpnServer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnView(viewModel: SecurityViewModel) {
    val isConnected by viewModel.isVpnConnected.collectAsState()
    val isConnecting by viewModel.isVpnConnecting.collectAsState()
    val connectingProgress by viewModel.vpnConnectingProgress.collectAsState()
    val selectedServer by viewModel.selectedVpnServer.collectAsState()
    val protocol by viewModel.vpnProtocol.collectAsState()
    val bytesIn by viewModel.vpnBytesIn.collectAsState()
    val bytesOut by viewModel.vpnBytesOut.collectAsState()
    val duration by viewModel.vpnConnectionDuration.collectAsState()
    val killSwitch by viewModel.vpnKillSwitchEnabled.collectAsState()
    val adBlocker by viewModel.vpnAdBlockerEnabled.collectAsState()

    var showServerSelectorSheet by remember { mutableStateOf(false) }

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

            // Premium VPN Main Connection Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isConnected) AccentEmerald.copy(alpha = 0.5f) else if (isConnecting) ElectricCyan.copy(alpha = 0.5f) else BorderSlate
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SECURE VPN GATEWAY",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Pulse/Glow Circle Connect Button
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = if (isConnected) 1.15f else if (isConnecting) 1.08f else 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseScale"
                    )

                    val connectBtnColor by animateColorAsState(
                        targetValue = if (isConnected) AccentEmerald else if (isConnecting) ElectricCyan.copy(alpha = 0.3f) else SurfaceCard,
                        animationSpec = tween(500),
                        label = "btnColor"
                    )

                    val connectIconColor by animateColorAsState(
                        targetValue = if (isConnected) Color(0xFF1D192B) else if (isConnecting) ElectricCyan else TextWhite,
                        animationSpec = tween(500),
                        label = "iconColor"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(160.dp)
                    ) {
                        // Pulsing outer glow ring
                        if (isConnected || isConnecting) {
                            Box(
                                modifier = Modifier
                                    .size((130f * pulseScale).dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isConnected) AccentEmerald.copy(alpha = 0.12f) else ElectricCyan.copy(alpha = 0.12f)
                                    )
                            )
                        }

                        // Connect Button Base
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(connectBtnColor)
                                .clickable { viewModel.toggleVpnConnection() }
                                .shadow(2.dp, CircleShape)
                                .testTag("vpn_connect_toggle"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isConnecting) {
                                CircularProgressIndicator(
                                    progress = { connectingProgress },
                                    color = ElectricCyan,
                                    strokeWidth = 4.dp,
                                    modifier = Modifier.size(90.dp)
                                )
                            }
                            Icon(
                                imageVector = if (isConnected) Icons.Filled.VerifiedUser else Icons.Filled.PowerSettingsNew,
                                contentDescription = "Toggle Connection Status",
                                tint = connectIconColor,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Connection State Messaging
                    Text(
                        text = if (isConnected) "FORTRESS SHIELD ACTIVE" else if (isConnecting) "ESTABLISHING TUNNEL..." else "DISCONNECTED",
                        color = if (isConnected) AccentEmerald else if (isConnecting) ElectricCyan else WarningAmber,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isConnected) "IP: ${selectedServer.ipAddress} • AES-256" else if (isConnecting) "Handshaking with cryptonodes..." else "Tap the power lock to establish secure tunnel",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )

                    if (isConnected) {
                        Spacer(modifier = Modifier.height(16.dp))
                        // Real-time Timer Counter HUD
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCard)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(AccentEmerald)
                                )
                                Text(
                                    text = formatDuration(duration),
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real-Time Telemetry Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderSlate)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDownward,
                                contentDescription = "Download Traffic Speed",
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DOWNLOADED", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatBytes(bytesIn),
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp)
                            .background(BorderSlate)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.ArrowUpward,
                                contentDescription = "Upload Traffic Speed",
                                tint = CyberBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("UPLOADED", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatBytes(bytesOut),
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Server Node Selection Card
            Text(
                text = "Secure Nodes Routing",
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showServerSelectorSheet = true }
                    .testTag("vpn_server_selector_card"),
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                shape = RoundedCornerShape(16.dp),
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
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = selectedServer.flagInitials,
                            color = ElectricCyan,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedServer.city,
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${selectedServer.country} Node • Load ${selectedServer.loadPercentage}%",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NetworkCheck,
                            contentDescription = "Ping Indicator",
                            tint = if (selectedServer.pingMs < 50) AccentEmerald else WarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${selectedServer.pingMs}ms",
                            color = if (selectedServer.pingMs < 50) AccentEmerald else WarningAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = "Expand Location Drawer",
                        tint = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Advanced Connection & Shield Options
            Text(
                text = "Advanced Shield Protection",
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
                    // Protocol selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Routing Protocol", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Current: $protocol", color = TextMuted, fontSize = 12.sp)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("WireGuard", "OpenVPN").forEach { proto ->
                                val isSelected = protocol == proto
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ElectricCyan else SurfaceCard)
                                        .clickable { viewModel.setVpnProtocol(proto) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("proto_$proto"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = proto,
                                        color = if (isSelected) Color(0xFF381E72) else TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderSlate)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Kill Switch Toggler
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("VPN Kill Switch", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Blocks traffic automatically if connections drop", color = TextMuted, fontSize = 12.sp)
                        }

                        Switch(
                            checked = killSwitch,
                            onCheckedChange = { viewModel.toggleVpnKillSwitch() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF381E72),
                                checkedTrackColor = ElectricCyan,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = SurfaceCard
                            ),
                            modifier = Modifier.testTag("vpn_kill_switch_toggle")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderSlate)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Threat/AdBlocker Toggler
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Malware & Ad Blocker", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Protects browsing by blocking ads/trackers", color = TextMuted, fontSize = 12.sp)
                        }

                        Switch(
                            checked = adBlocker,
                            onCheckedChange = { viewModel.toggleVpnAdBlocker() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF381E72),
                                checkedTrackColor = ElectricCyan,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = SurfaceCard
                            ),
                            modifier = Modifier.testTag("vpn_adblock_toggle")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Animated Server Selection Sheet / Overlay
        if (showServerSelectorSheet) {
            ModalBottomSheet(
                onDismissRequest = { showServerSelectorSheet = false },
                containerColor = DarkNavy,
                contentColor = TextWhite,
                dragHandle = { BottomSheetDefaults.DragHandle(color = BorderSlate) }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Select Secure Node Location",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 40.dp)
                    ) {
                        items(viewModel.availableVpnServers) { server ->
                            val isCurrent = server.id == selectedServer.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectVpnServer(server)
                                        showServerSelectorSheet = false
                                    }
                                    .testTag("vpn_server_item_${server.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrent) SurfaceCard else DarkNavy
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isCurrent) ElectricCyan else BorderSlate
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isCurrent) ElectricCyan else SurfaceCard),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = server.flagInitials,
                                            color = if (isCurrent) Color(0xFF381E72) else TextMuted,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = server.city,
                                            color = TextWhite,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = server.country,
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.NetworkCheck,
                                                contentDescription = null,
                                                tint = if (server.pingMs < 50) AccentEmerald else WarningAmber,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${server.pingMs}ms",
                                                color = if (server.pingMs < 50) AccentEmerald else WarningAmber,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Text(
                                            text = "Load ${server.loadPercentage}%",
                                            color = if (server.loadPercentage < 50) AccentEmerald else WarningAmber,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Locale-safe pure Kotlin duration formatting
private fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    val hhStr = h.toString().padStart(2, '0')
    val mmStr = m.toString().padStart(2, '0')
    val ssStr = s.toString().padStart(2, '0')
    return "$hhStr:$mmStr:$ssStr"
}

// Locale-safe pure Kotlin byte formatting
private fun formatBytes(bytes: Long): String {
    val mb = bytes.toDouble() / (1024.0 * 1024.0)
    val kb = bytes.toDouble() / 1024.0
    return when {
        bytes >= 1024 * 1024 -> {
            val roundedMb = (mb * 100.0).toLong() / 100.0
            "$roundedMb MB"
        }
        bytes >= 1024 -> {
            val roundedKb = (kb * 10.0).toLong() / 10.0
            "$roundedKb KB"
        }
        else -> "$bytes B"
    }
}
