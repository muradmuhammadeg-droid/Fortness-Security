package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SecurityCheckItem
import com.example.data.SecurityStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.SecurityViewModel
import com.example.ui.viewmodel.SecurityNotification
import com.example.ui.viewmodel.NotificationType
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardView(viewModel: SecurityViewModel) {
    val isScanning by viewModel.isScanning.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val scanMessage by viewModel.scanStatusMessage.collectAsState()
    val scanItems by viewModel.scanResultItems.collectAsState()
    val score by viewModel.securityScore.collectAsState()
    val lastScanTime by viewModel.lastScanTime.collectAsState()

    val notifications by viewModel.notifications.collectAsState()
    val unreadCount = notifications.count { !it.isRead }

    val virusesUninstalled by viewModel.virusesUninstalled.collectAsState()
    val isUninstallingViruses by viewModel.isUninstallingViruses.collectAsState()
    val uninstallProgress by viewModel.uninstallProgress.collectAsState()
    val uninstallMessage by viewModel.uninstallStatusMessage.collectAsState()

    var showLogsDialog by remember { mutableStateOf(false) }

    val isRepairingSystem by viewModel.isRepairingSystem.collectAsState()
    val repairProgress by viewModel.repairProgress.collectAsState()
    val repairMessage by viewModel.repairStatusMessage.collectAsState()

    val context = LocalContext.current

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val readG = perms[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        val writeG = perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false
        if (readG || writeG) {
            Toast.makeText(context, "Storage scanner permission granted!", Toast.LENGTH_SHORT).show()
            viewModel.runSecurityScan()
        } else {
            Toast.makeText(context, "Deep storage scan requires permission.", Toast.LENGTH_SHORT).show()
        }
    }
    var isSystemNotificationPermissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isSystemNotificationPermissionGranted = isGranted
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Hero Header
        item {
            HeaderSection(
                lastScanTime = lastScanTime,
                unreadCount = unreadCount,
                onNotificationClick = {
                    showLogsDialog = true
                    viewModel.markNotificationsAsRead()
                }
            )
        }

        // Circular Gauge Card
        item {
            GaugeCard(score, isScanning, scanProgress, scanMessage) {
                viewModel.runSecurityScan()
            }
        }

        // Active Viruses & Malware Warning Banner Card
        val dangerThreats = scanItems.filter { it.status == SecurityStatus.DANGER }
        if (!virusesUninstalled && dangerThreats.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(20.dp))
                        .testTag("active_viruses_warning_card"),
                    colors = CardDefaults.cardColors(containerColor = DangerCrimson.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, DangerCrimson.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(DangerCrimson.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Dangerous,
                                    contentDescription = "Malware Detected",
                                    tint = DangerCrimson,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Active Viruses & Malware",
                                    color = TextWhite,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${dangerThreats.size} security threat(s) found",
                                    color = DangerCrimson,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Background keyloggers, active Trojan payloads, and adware overlay injectors were identified. These processes violate security limits and compromise device security.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.uninstallViruses() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("uninstall_viruses_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DangerCrimson,
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteForever,
                                contentDescription = "Purge Icon",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PURGE ALL DETECTED THREATS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        // Action Quick Header
        item {
            Text(
                text = "System Security Status",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Security Checklist Items
        if (scanItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkNavy),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = "Ready to scan",
                            tint = ElectricCyan,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Awaiting Security Scan",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the scanner above to perform a real-time integrity and sandbox check.",
                            color = TextMuted,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(scanItems) { checkItem ->
                SecurityCheckCard(
                    item = checkItem,
                    onUninstallPackage = { pkg ->
                        if (pkg.startsWith("com.sandbox.")) {
                            viewModel.uninstallSelectedPackage(pkg)
                        } else {
                            try {
                                val intent = Intent(Intent.ACTION_DELETE).apply {
                                    data = Uri.parse("package:$pkg")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Uninstall trigger failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onRepairSystemFiles = {
                        viewModel.repairAndOptimizeSystemFiles()
                    },
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            try {
                                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                storagePermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    )
                                )
                            }
                        } else {
                            storagePermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                )
                            )
                        }
                    }
                )
            }
        }
    }

    if (showLogsDialog) {
        NotificationLogSheet(
            notifications = notifications,
            onDismiss = { showLogsDialog = false },
            onClearAll = { viewModel.clearAllNotifications() },
            onMarkAsRead = { viewModel.markNotificationsAsRead() },
            onGrantSystemNotificationPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            isSystemNotificationPermissionGranted = isSystemNotificationPermissionGranted
        )
    }

    if (isUninstallingViruses) {
        AlertDialog(
            onDismissRequest = {},
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            containerColor = DeepObsidian,
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { uninstallProgress },
                        color = DangerCrimson,
                        trackColor = BorderSlate,
                        strokeWidth = 6.dp,
                        modifier = Modifier.size(72.dp)
                    )

                    Text(
                        text = "DECONTAMINATING SYSTEM...",
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )

                    LinearProgressIndicator(
                        progress = { uninstallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = DangerCrimson,
                        trackColor = BorderSlate
                    )

                    Text(
                        text = uninstallMessage,
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    if (isRepairingSystem) {
        AlertDialog(
            onDismissRequest = {},
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            ),
            containerColor = DeepObsidian,
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { repairProgress },
                        color = ElectricCyan,
                        trackColor = BorderSlate,
                        strokeWidth = 6.dp,
                        modifier = Modifier.size(72.dp)
                    )

                    Text(
                        text = "REPAIRING & OPTIMIZING SYSTEM FILES...",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        textAlign = TextAlign.Center
                    )

                    LinearProgressIndicator(
                        progress = { repairProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = ElectricCyan,
                        trackColor = BorderSlate
                    )

                    Text(
                        text = repairMessage,
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }
}

@Composable
fun HeaderSection(
    lastScanTime: Long?,
    unreadCount: Int,
    onNotificationClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Fortress Security",
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
            Text(
                text = "ACTIVE SANDBOX THREAT SCANNER",
                color = ElectricCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        Box(
            modifier = Modifier.padding(end = 4.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .background(DarkNavy, CircleShape)
                    .testTag("dashboard_notification_bell")
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Security Activity Logs",
                    tint = TextWhite
                )
            }
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.Red)
                        .border(1.5.dp, DeepObsidian, CircleShape)
                )
            }
        }
    }

    lastScanTime?.let { time ->
        val dateFormat = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault())
        Text(
            text = "Last verified: ${dateFormat.format(Date(time))}",
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun NotificationLogSheet(
    notifications: List<SecurityNotification>,
    onDismiss: () -> Unit,
    onClearAll: () -> Unit,
    onMarkAsRead: () -> Unit,
    onGrantSystemNotificationPermission: () -> Unit,
    isSystemNotificationPermissionGranted: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f)
            .padding(16.dp),
        containerColor = DeepObsidian,
        title = null,
        text = {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Security Logs & Activity",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions: Mark as Read & Clear All
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onMarkAsRead,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(contentColor = ElectricCyan)
                    ) {
                        Icon(imageVector = Icons.Filled.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark all read", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = onClearAll,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.8f))
                    ) {
                        Icon(imageVector = Icons.Filled.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear Logs", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // System Notification Status Banner for Android 13+
                if (!isSystemNotificationPermissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGrantSystemNotificationPermission() }
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationImportant,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "System Alerts Disabled",
                                    color = TextWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap to authorize system push alerts.",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "Enable",
                                tint = WarningAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Notifications Feed
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsOff,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Log Feed Clean",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "No recent security logs recorded on this device.",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications) { item ->
                            val itemColor = when (item.type) {
                                NotificationType.VPN -> ElectricCyan
                                NotificationType.SECURE -> AccentEmerald
                                NotificationType.WARNING -> WarningAmber
                                NotificationType.INFO -> CyberBlue
                            }

                            val itemIcon = when (item.type) {
                                NotificationType.VPN -> Icons.Filled.VpnLock
                                NotificationType.SECURE -> Icons.Filled.GppGood
                                NotificationType.WARNING -> Icons.Filled.GppMaybe
                                NotificationType.INFO -> Icons.Filled.Info
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (item.isRead) DarkNavy else DarkNavy.copy(alpha = 0.8f)
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (item.isRead) BorderSlate else itemColor.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(itemColor.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = itemIcon,
                                            contentDescription = null,
                                            tint = itemColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item.title,
                                                color = TextWhite,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            val timeStr = formatRelativeTime(item.timestamp)
                                            Text(
                                                text = timeStr,
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = item.message,
                                            color = TextMuted,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> {
            val date = Date(timestamp)
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            sdf.format(date)
        }
    }
}

@Composable
fun GaugeCard(
    score: Int,
    isScanning: Boolean,
    scanProgress: Float,
    scanMessage: String,
    onScanClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Animated gauge representation
            val animatedScore by animateIntAsState(
                targetValue = if (isScanning) 100 else score,
                animationSpec = tween(durationMillis = 1000),
                label = "ScoreText"
            )

            val animatedSweepAngle by animateFloatAsState(
                targetValue = if (isScanning) scanProgress * 360f else (score / 100f) * 360f,
                animationSpec = tween(durationMillis = 1000),
                label = "GaugeArc"
            )

            val colorBrush = when {
                isScanning -> Brush.sweepGradient(listOf(ElectricCyan, CyberBlue, ElectricCyan))
                score >= 90 -> Brush.linearGradient(listOf(AccentEmerald, ElectricCyan))
                score >= 60 -> Brush.linearGradient(listOf(WarningAmber, AccentEmerald))
                else -> Brush.linearGradient(listOf(DangerCrimson, WarningAmber))
            }

            val statusLabel = when {
                isScanning -> "AUDITING..."
                score >= 90 -> "SAFE"
                score >= 60 -> "WARNING"
                else -> "COMPROMISED"
            }

            val statusColor = when {
                isScanning -> ElectricCyan
                score >= 90 -> AccentEmerald
                score >= 60 -> WarningAmber
                else -> DangerCrimson
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(190.dp)
            ) {
                // Background Track Arc
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = BorderSlate,
                        radius = size.minDimension / 2.2f,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Foreground Animated Progress Arc
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        brush = colorBrush,
                        startAngle = -90f,
                        sweepAngle = animatedSweepAngle,
                        useCenter = false,
                        style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner Stats Displays
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isScanning) "${(scanProgress * 100).toInt()}%" else "$animatedScore%",
                        color = TextWhite,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic scan prompt or scanning progress bar
            if (isScanning) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = { scanProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = ElectricCyan,
                        trackColor = BorderSlate,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = scanMessage,
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Button(
                    onClick = onScanClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("run_scan_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = Color(0xFF381E72)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = "Scan Icon",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TRIGGER THREAT AUDIT",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityCheckCard(
    item: SecurityCheckItem,
    onUninstallPackage: (String) -> Unit,
    onRepairSystemFiles: () -> Unit,
    onRequestPermission: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val statusIcon = when (item.status) {
        SecurityStatus.SECURE -> Icons.Filled.CheckCircle
        SecurityStatus.WARNING -> Icons.Filled.Warning
        SecurityStatus.DANGER -> Icons.Filled.Dangerous
    }

    val statusColor = when (item.status) {
        SecurityStatus.SECURE -> AccentEmerald
        SecurityStatus.WARNING -> WarningAmber
        SecurityStatus.DANGER -> DangerCrimson
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("security_check_card_${item.title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (expanded) ElectricCyan else BorderSlate)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = item.status.name,
                    tint = statusColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.category.uppercase(),
                        color = ElectricCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = TextMuted
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider(color = BorderSlate, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Diagnostics:",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        color = TextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Security Recommendation:",
                        color = ElectricCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.solution,
                        color = TextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    // Contextual Action Buttons based on findings
                    if (item.packageName != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onUninstallPackage(item.packageName) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("uninstall_app_${item.packageName}"),
                            colors = ButtonDefaults.buttonColors(containerColor = DangerCrimson),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = "Uninstall")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("FORCE UNINSTALL PACKAGE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else if (item.isFileRisk && item.filePath != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onRepairSystemFiles,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("repair_system_files"),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = Color(0xFF381E72)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Build, contentDescription = "Repair")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("REPAIR & OPTIMIZE SYSTEM SECTORS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else if (item.title.contains("Deep Storage")) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onRequestPermission,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("authorize_storage_scan"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberBlue, contentColor = TextWhite),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.AdminPanelSettings, contentDescription = "Authorize")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AUTHORIZE SCAN PERMISSIONS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
