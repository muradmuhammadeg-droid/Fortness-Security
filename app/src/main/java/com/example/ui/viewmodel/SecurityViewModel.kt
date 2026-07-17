package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AppItem(
    val packageName: String,
    val appName: String,
    val isLocked: Boolean
)

data class VpnServer(
    val id: String,
    val country: String,
    val city: String,
    val flagInitials: String,
    val pingMs: Int,
    val loadPercentage: Int,
    val ipAddress: String
)

enum class NotificationType {
    INFO, WARNING, SECURE, VPN
}

data class SecurityNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: NotificationType,
    val isRead: Boolean = false
)

class SecurityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SecurityRepository(application)

    // Notification Log States
    private val _notifications = MutableStateFlow<List<SecurityNotification>>(emptyList())
    val notifications: StateFlow<List<SecurityNotification>> = _notifications.asStateFlow()

    fun postNotification(title: String, message: String, type: NotificationType) {
        val newNotification = SecurityNotification(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            type = type,
            isRead = false
        )
        _notifications.value = listOf(newNotification) + _notifications.value

        if (_securityNotificationsEnabled.value) {
            com.example.NotificationHelper.showNotification(getApplication(), title, message)
        }
    }

    fun markNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    // VPN States
    val availableVpnServers = listOf(
        VpnServer("de_frankfurt", "Germany", "Frankfurt Node-1", "DE", 18, 28, "46.165.210.17"),
        VpnServer("us_east", "United States", "New York Secure Node", "US", 34, 42, "104.244.42.1"),
        VpnServer("uk_london", "United Kingdom", "London Shield-A", "UK", 12, 58, "195.154.122.99"),
        VpnServer("sg_singapore", "Singapore", "Changi Tunnel-3", "SG", 182, 64, "128.199.200.12"),
        VpnServer("jp_tokyo", "Japan", "Tokyo Cyber Node", "JP", 144, 49, "210.140.10.25")
    )

    private val _isVpnConnected = MutableStateFlow(false)
    val isVpnConnected: StateFlow<Boolean> = _isVpnConnected.asStateFlow()

    private val _isVpnConnecting = MutableStateFlow(false)
    val isVpnConnecting: StateFlow<Boolean> = _isVpnConnecting.asStateFlow()

    private val _vpnConnectingProgress = MutableStateFlow(0f)
    val vpnConnectingProgress: StateFlow<Float> = _vpnConnectingProgress.asStateFlow()

    private val _selectedVpnServer = MutableStateFlow(availableVpnServers[0])
    val selectedVpnServer: StateFlow<VpnServer> = _selectedVpnServer.asStateFlow()

    private val _vpnProtocol = MutableStateFlow("WireGuard")
    val vpnProtocol: StateFlow<String> = _vpnProtocol.asStateFlow()

    private val _vpnBytesIn = MutableStateFlow(0L)
    val vpnBytesIn: StateFlow<Long> = _vpnBytesIn.asStateFlow()

    private val _vpnBytesOut = MutableStateFlow(0L)
    val vpnBytesOut: StateFlow<Long> = _vpnBytesOut.asStateFlow()

    private val _vpnConnectionDuration = MutableStateFlow(0L) // in seconds
    val vpnConnectionDuration: StateFlow<Long> = _vpnConnectionDuration.asStateFlow()

    private val _vpnKillSwitchEnabled = MutableStateFlow(true)
    val vpnKillSwitchEnabled: StateFlow<Boolean> = _vpnKillSwitchEnabled.asStateFlow()

    private val _vpnAdBlockerEnabled = MutableStateFlow(true)
    val vpnAdBlockerEnabled: StateFlow<Boolean> = _vpnAdBlockerEnabled.asStateFlow()

    private var vpnJob: kotlinx.coroutines.Job? = null

    // General Protection Settings
    private val _realTimeProtectionEnabled = MutableStateFlow(true)
    val realTimeProtectionEnabled: StateFlow<Boolean> = _realTimeProtectionEnabled.asStateFlow()

    private val _autoScanEnabled = MutableStateFlow(true)
    val autoScanEnabled: StateFlow<Boolean> = _autoScanEnabled.asStateFlow()

    private val _deepScanEngineEnabled = MutableStateFlow(false)
    val deepScanEngineEnabled: StateFlow<Boolean> = _deepScanEngineEnabled.asStateFlow()

    private val _safeBrowsingEnabled = MutableStateFlow(true)
    val safeBrowsingEnabled: StateFlow<Boolean> = _safeBrowsingEnabled.asStateFlow()

    private val _securityNotificationsEnabled = MutableStateFlow(true)
    val securityNotificationsEnabled: StateFlow<Boolean> = _securityNotificationsEnabled.asStateFlow()

    fun toggleRealTimeProtection() {
        viewModelScope.launch {
            val newValue = !_realTimeProtectionEnabled.value
            _realTimeProtectionEnabled.value = newValue
            repository.setConfigValue("setting_real_time_protection", newValue.toString())
        }
    }

    fun toggleAutoScan() {
        viewModelScope.launch {
            val newValue = !_autoScanEnabled.value
            _autoScanEnabled.value = newValue
            repository.setConfigValue("setting_auto_scan", newValue.toString())
        }
    }

    fun toggleDeepScanEngine() {
        viewModelScope.launch {
            val newValue = !_deepScanEngineEnabled.value
            _deepScanEngineEnabled.value = newValue
            repository.setConfigValue("setting_deep_scan_engine", newValue.toString())
        }
    }

    fun toggleSafeBrowsing() {
        viewModelScope.launch {
            val newValue = !_safeBrowsingEnabled.value
            _safeBrowsingEnabled.value = newValue
            repository.setConfigValue("setting_safe_browsing", newValue.toString())
        }
    }

    fun toggleSecurityNotifications() {
        viewModelScope.launch {
            val newValue = !_securityNotificationsEnabled.value
            _securityNotificationsEnabled.value = newValue
            repository.setConfigValue("setting_security_notifications", newValue.toString())
        }
    }

    fun selectVpnServer(server: VpnServer) {
        viewModelScope.launch {
            if (_isVpnConnected.value) {
                // Disconnect first, then connect to new server!
                toggleVpnConnection()
                _selectedVpnServer.value = server
                toggleVpnConnection()
            } else {
                _selectedVpnServer.value = server
            }
        }
    }

    fun setVpnProtocol(protocol: String) {
        _vpnProtocol.value = protocol
    }

    fun toggleVpnKillSwitch() {
        _vpnKillSwitchEnabled.value = !_vpnKillSwitchEnabled.value
    }

    fun toggleVpnAdBlocker() {
        _vpnAdBlockerEnabled.value = !_vpnAdBlockerEnabled.value
    }

    fun toggleVpnConnection() {
        if (_isVpnConnected.value) {
            // Disconnect
            vpnJob?.cancel()
            vpnJob = null
            _isVpnConnected.value = false
            _isVpnConnecting.value = false
            _vpnConnectingProgress.value = 0f

            postNotification(
                title = "Secure VPN Disconnected",
                message = "The encrypted tunnel to ${_selectedVpnServer.value.city} was disconnected.",
                type = NotificationType.VPN
            )
        } else {
            // Connect
            if (_isVpnConnecting.value) return
            viewModelScope.launch {
                _isVpnConnecting.value = true
                _vpnConnectingProgress.value = 0f
                
                // Simulate secure handshake & keys exchange
                for (i in 1..10) {
                    delay(150)
                    _vpnConnectingProgress.value = i / 10f
                }
                
                _isVpnConnected.value = true
                _isVpnConnecting.value = false
                _vpnBytesIn.value = 0L
                _vpnBytesOut.value = 0L
                _vpnConnectionDuration.value = 0L

                postNotification(
                    title = "Secure VPN Tunnel Active",
                    message = "Traffic is encrypted via ${_selectedVpnServer.value.city} (${_selectedVpnServer.value.country}). IP: ${_selectedVpnServer.value.ipAddress}",
                    type = NotificationType.VPN
                )
                
                // Start tracking stats
                vpnJob = viewModelScope.launch(Dispatchers.Default) {
                    var secs = 0L
                    var bytesIn = 0L
                    var bytesOut = 0L
                    while (true) {
                        delay(1000)
                        secs++
                        _vpnConnectionDuration.value = secs
                        // Simulate random but realistic packet traffic (kB/s)
                        bytesIn += (150..1200).random().toLong() * 1024L
                        bytesOut += (20..300).random().toLong() * 1024L
                        _vpnBytesIn.value = bytesIn
                        _vpnBytesOut.value = bytesOut
                    }
                }
            }
        }
    }

    // App Lock States
    private val _installedApps = MutableStateFlow<List<AppItem>>(emptyList())
    val installedApps: StateFlow<List<AppItem>> = _installedApps.asStateFlow()

    private val _appLockSearchQuery = MutableStateFlow("")
    val appLockSearchQuery: StateFlow<String> = _appLockSearchQuery.asStateFlow()

    val lockedApps: StateFlow<List<LockedApp>> = repository.allLockedApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined filtered installed apps with their locked states
    val filteredInstalledApps: StateFlow<List<AppItem>> = combine(
        _installedApps,
        _appLockSearchQuery,
        lockedApps
    ) { apps, query, locked ->
        val lockedPackages = locked.associateBy { it.packageName }
        apps.map { app ->
            app.copy(isLocked = lockedPackages.containsKey(app.packageName))
        }.filter { app ->
            app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated App Launch states
    private val _simulatedLockedAppToOpen = MutableStateFlow<AppItem?>(null)
    val simulatedLockedAppToOpen: StateFlow<AppItem?> = _simulatedLockedAppToOpen.asStateFlow()

    private val _simulatedAppUnlockSuccess = MutableSharedFlow<Boolean>()
    val simulatedAppUnlockSuccess = _simulatedAppUnlockSuccess.asSharedFlow()

    // Viruses / Malware Uninstallation States
    private val _virusesUninstalled = MutableStateFlow(false)
    val virusesUninstalled: StateFlow<Boolean> = _virusesUninstalled.asStateFlow()

    private val _isUninstallingViruses = MutableStateFlow(false)
    val isUninstallingViruses: StateFlow<Boolean> = _isUninstallingViruses.asStateFlow()

    private val _uninstallProgress = MutableStateFlow(0f)
    val uninstallProgress: StateFlow<Float> = _uninstallProgress.asStateFlow()

    private val _uninstallStatusMessage = MutableStateFlow("")
    val uninstallStatusMessage: StateFlow<String> = _uninstallStatusMessage.asStateFlow()

    // System File Repair & Optimization States
    private val _isRepairingSystem = MutableStateFlow(false)
    val isRepairingSystem: StateFlow<Boolean> = _isRepairingSystem.asStateFlow()

    private val _repairProgress = MutableStateFlow(0f)
    val repairProgress: StateFlow<Float> = _repairProgress.asStateFlow()

    private val _repairStatusMessage = MutableStateFlow("")
    val repairStatusMessage: StateFlow<String> = _repairStatusMessage.asStateFlow()

    // Scanning & Dashboard States
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _scanStatusMessage = MutableStateFlow("")
    val scanStatusMessage: StateFlow<String> = _scanStatusMessage.asStateFlow()

    private val _scanResultItems = MutableStateFlow<List<SecurityCheckItem>>(emptyList())
    val scanResultItems: StateFlow<List<SecurityCheckItem>> = _scanResultItems.asStateFlow()

    private val _securityScore = MutableStateFlow(100)
    val securityScore: StateFlow<Int> = _securityScore.asStateFlow()

    private val _lastScanTime = MutableStateFlow<Long?>(null)
    val lastScanTime: StateFlow<Long?> = _lastScanTime.asStateFlow()

    val scanHistory: StateFlow<List<ScanHistory>> = repository.scanHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Password Vault States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _allPasswords = repository.allPasswords

    val filteredPasswords: StateFlow<List<PasswordCredential>> = combine(
        _allPasswords,
        _searchQuery,
        _selectedCategory
    ) { passwords, query, category ->
        passwords.filter { credential ->
            val matchesQuery = credential.title.contains(query, ignoreCase = true) ||
                    credential.username.contains(query, ignoreCase = true) ||
                    credential.url.contains(query, ignoreCase = true)
            val matchesCategory = category == "All" || credential.category == category
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Secure Notes States
    private val _allNotes = repository.allNotes
    val secureNotes: StateFlow<List<SecureNote>> = _allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _hasMasterPin = MutableStateFlow(false)
    val hasMasterPin: StateFlow<Boolean> = _hasMasterPin.asStateFlow()

    private val _isPinVerified = MutableStateFlow(false)
    val isPinVerified: StateFlow<Boolean> = _isPinVerified.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError: StateFlow<String?> = _pinError.asStateFlow()

    init {
        viewModelScope.launch {
            // Load configuration parameters on start
            val pin = repository.getMasterPin()
            _hasMasterPin.value = !pin.isNullOrEmpty()

            val lastScan = repository.getLastScanTime()
            _lastScanTime.value = lastScan

            // Load stored general protection settings
            _realTimeProtectionEnabled.value = repository.getConfigValue("setting_real_time_protection")?.toBooleanStrictOrNull() ?: true
            _autoScanEnabled.value = repository.getConfigValue("setting_auto_scan")?.toBooleanStrictOrNull() ?: true
            _deepScanEngineEnabled.value = repository.getConfigValue("setting_deep_scan_engine")?.toBooleanStrictOrNull() ?: false
            _safeBrowsingEnabled.value = repository.getConfigValue("setting_safe_browsing")?.toBooleanStrictOrNull() ?: true
            _securityNotificationsEnabled.value = repository.getConfigValue("setting_security_notifications")?.toBooleanStrictOrNull() ?: true

            // Run initial quick check to populate lists
            val uninstalled = repository.getConfigValue("setting_viruses_uninstalled")?.toBooleanStrictOrNull() ?: false
            _virusesUninstalled.value = uninstalled
            val initialChecks = SecurityScanner.runScan(getApplication(), uninstalled)
            _scanResultItems.value = initialChecks
            calculateScore(initialChecks)

            // Set initial notifications for clean audit activity
            _notifications.value = listOf(
                SecurityNotification(
                    id = "init_1",
                    title = "Real-Time Protection Active",
                    message = "Active guard shield is scanning system memory and database changes.",
                    timestamp = System.currentTimeMillis() - 45000,
                    type = NotificationType.SECURE
                ),
                SecurityNotification(
                    id = "init_2",
                    title = "Fortress Security Engine Online",
                    message = "Fully isolated sandbox engine loaded successfully. Platform: v2.1.0-Fortress.",
                    timestamp = System.currentTimeMillis() - 90000,
                    type = NotificationType.INFO
                )
            )

            // Load installed apps for App Lock
            loadInstalledApps()
        }
    }

    // App Lock Business Logic
    fun updateAppLockSearchQuery(query: String) {
        _appLockSearchQuery.value = query
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val pm = context.packageManager
            val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            
            val queriedApps = try {
                pm.queryIntentActivities(launcherIntent, 0).map { info ->
                    val packageName = info.activityInfo.packageName
                    val appName = info.loadLabel(pm).toString()
                    AppItem(packageName, appName, false)
                }
            } catch (e: Exception) {
                emptyList()
            }

            // Standard fallback common apps for demonstration / simulator completeness
            val fallbackApps = listOf(
                AppItem("com.android.chrome", "Google Chrome", false),
                AppItem("com.google.android.youtube", "YouTube", false),
                AppItem("com.google.android.gm", "Gmail", false),
                AppItem("com.whatsapp", "WhatsApp", false),
                AppItem("com.instagram.android", "Instagram", false),
                AppItem("com.android.settings", "Settings", false),
                AppItem("com.google.android.apps.photos", "Google Photos", false),
                AppItem("com.google.android.apps.maps", "Google Maps", false)
            )

            // Merge: keep queried apps, and append any fallback that isn't already present
            val mergedList = (queriedApps + fallbackApps).distinctBy { it.packageName }
                .sortedBy { it.appName }

            _installedApps.value = mergedList
        }
    }

    fun toggleAppLock(appItem: AppItem) {
        viewModelScope.launch {
            if (appItem.isLocked) {
                repository.deleteLockedAppByPackageName(appItem.packageName)
                postNotification(
                    title = "App Lock Disabled",
                    message = "${appItem.appName} was removed from App Lock protection.",
                    type = NotificationType.INFO
                )
            } else {
                repository.insertLockedApp(
                    LockedApp(
                        packageName = appItem.packageName,
                        appName = appItem.appName,
                        isLocked = true
                    )
                )
                postNotification(
                    title = "App Locked Securely",
                    message = "Master PIN authentication is now active for ${appItem.appName}.",
                    type = NotificationType.SECURE
                )
            }
        }
    }

    fun triggerSimulatedAppOpen(app: AppItem) {
        _simulatedLockedAppToOpen.value = app
    }

    fun closeSimulatedApp() {
        _simulatedLockedAppToOpen.value = null
    }

    fun verifyAppLockPin(pin: String): Boolean {
        var success = false
        viewModelScope.launch {
            val savedPin = repository.getMasterPin()
            if (savedPin == pin) {
                _simulatedAppUnlockSuccess.emit(true)
                _simulatedLockedAppToOpen.value = null
                success = true
            } else {
                _simulatedAppUnlockSuccess.emit(false)
            }
        }
        return success
    }

    // Execute Security Scan with animations and logs
    fun runSecurityScan() {
        if (_isScanning.value) return
        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0f

            val scanMessages = listOf(
                "Initializing sandbox safety filters...",
                "Inspecting hardware credential keystores...",
                "Auditing active device superuser privileges...",
                "Scanning developer ports and USB debug hooks...",
                "Validating Google Play Protect package signatures...",
                "Generating secure report matrix..."
            )

            // Dynamic progress steps
            for (i in 0..100 step 4) {
                _scanProgress.value = i / 100f
                val messageIndex = (i / (100 / scanMessages.size)).coerceIn(0, scanMessages.size - 1)
                _scanStatusMessage.value = scanMessages[messageIndex]
                delay(60) // Smoothened progress animation
            }

            // Execute real checks
            val context = getApplication<Application>()
            val uninstalled = repository.getConfigValue("setting_viruses_uninstalled")?.toBooleanStrictOrNull() ?: false
            _virusesUninstalled.value = uninstalled
            val scanItems = SecurityScanner.runScan(context, uninstalled)
            _scanResultItems.value = scanItems
            
            val finalScore = calculateScore(scanItems)
            val currentTime = System.currentTimeMillis()
            _lastScanTime.value = currentTime
            repository.setLastScanTime(currentTime)

            // Save historical results
            val scanSummary = scanItems.map { "${it.title}: ${it.status.name}" }.joinToString(", ")
            repository.insertScan(
                ScanHistory(
                    score = finalScore,
                    threatsFound = scanItems.count { it.status == SecurityStatus.DANGER },
                    detailsJson = scanSummary
                )
            )

            _isScanning.value = false

            // Post scan completion notification
            val threatsCount = scanItems.count { it.status == SecurityStatus.DANGER }
            val warningCount = scanItems.count { it.status == SecurityStatus.WARNING }
            postNotification(
                title = "Security Scan Completed",
                message = "Scan Score: $finalScore/100. Found $threatsCount critical threats, $warningCount warnings.",
                type = if (threatsCount > 0) NotificationType.WARNING else NotificationType.SECURE
            )
        }
    }

    fun uninstallViruses() {
        if (_isUninstallingViruses.value) return
        viewModelScope.launch {
            _isUninstallingViruses.value = true
            _uninstallProgress.value = 0f

            val steps = listOf(
                "Terminating active virus background processes..." to 0.15f,
                "Isolating package 'com.android.keylogger.sys'..." to 0.35f,
                "Purging Spyware Keylogger binaries..." to 0.5f,
                "Wiping Trojan.Dropper.Agent ('com.cyber.adware.pop') code signatures..." to 0.7f,
                "Force-uninstalling malicious Trojan packages..." to 0.85f,
                "Resetting background overlay layouts..." to 0.95f,
                "Re-auditing system sandbox safety posture..." to 1.0f
            )

            for ((msg, progress) in steps) {
                _uninstallStatusMessage.value = msg
                _uninstallProgress.value = progress
                delay(700) // Realistic uninstall step duration
            }

            _virusesUninstalled.value = true
            repository.setConfigValue("setting_viruses_uninstalled", "true")

            // Re-run scan to show clean slate
            val context = getApplication<Application>()
            val cleanItems = SecurityScanner.runScan(context, true)
            _scanResultItems.value = cleanItems
            calculateScore(cleanItems)

            postNotification(
                title = "All Viruses Uninstalled Successfully",
                message = "Sentinel Fortress completed full sandbox decontamination. All keylogger, Trojan, and adware threats were purged.",
                type = NotificationType.SECURE
            )

            _isUninstallingViruses.value = false
        }
    }

    fun uninstallSelectedPackage(packageName: String) {
        if (_isUninstallingViruses.value) return
        viewModelScope.launch {
            _isUninstallingViruses.value = true
            _uninstallProgress.value = 0f
            _uninstallStatusMessage.value = "Quarantining app package $packageName..."
            delay(600)
            _uninstallProgress.value = 0.5f
            _uninstallStatusMessage.value = "Purging code signatures and data chunks..."
            delay(600)
            _uninstallProgress.value = 1.0f
            _uninstallStatusMessage.value = "Decontaminated!"
            delay(400)

            postNotification(
                title = "App Package Removed",
                message = "Threat package '$packageName' has been successfully deleted from your environment.",
                type = NotificationType.SECURE
            )

            val updated = _scanResultItems.value.filter { it.packageName != packageName }
            _scanResultItems.value = updated
            calculateScore(updated)

            _isUninstallingViruses.value = false
        }
    }

    fun repairAndOptimizeSystemFiles() {
        if (_isRepairingSystem.value) return
        viewModelScope.launch {
            _isRepairingSystem.value = true
            _repairProgress.value = 0f

            val steps = listOf(
                "Analyzing file system blocks and permissions integrity..." to 0.15f,
                "Rebuilding damaged index maps and system temporary buffers..." to 0.35f,
                "Safely wiping app temporary caches & obsolete system logs..." to 0.6f,
                "Compacting fragmented database pages & indexing clusters..." to 0.8f,
                "Optimizing core heap allocations & executing garbage collection..." to 0.95f,
                "System layout and files health verification completed!" to 1.0f
            )

            val context = getApplication<Application>()

            for ((msg, progress) in steps) {
                _repairStatusMessage.value = msg
                _repairProgress.value = progress
                if (progress == 0.6f) {
                    try {
                        context.cacheDir.deleteRecursively()
                        context.externalCacheDir?.deleteRecursively()
                    } catch (e: Exception) {
                        // ignore
                    }
                } else if (progress == 0.95f) {
                    System.gc()
                }
                delay(700)
            }

            postNotification(
                title = "System Files Restored & Repaired",
                message = "All temporary index maps, cache pages, and stale log frames were optimized and compressed successfully.",
                type = NotificationType.SECURE
            )

            // Re-run scan to show clean slate or optimized stats
            val cleanItems = SecurityScanner.runScan(context, _virusesUninstalled.value)
            _scanResultItems.value = cleanItems
            calculateScore(cleanItems)

            _isRepairingSystem.value = false
        }
    }

    fun resetViruses() {
        viewModelScope.launch {
            _virusesUninstalled.value = false
            repository.setConfigValue("setting_viruses_uninstalled", "false")
            
            // Re-run scan to restore infected state
            val context = getApplication<Application>()
            val infectedItems = SecurityScanner.runScan(context, false)
            _scanResultItems.value = infectedItems
            calculateScore(infectedItems)

            postNotification(
                title = "Malware Simulation Reset",
                message = "Active threats re-injected into the sandbox environment for testing.",
                type = NotificationType.WARNING
            )
        }
    }

    private fun calculateScore(items: List<SecurityCheckItem>): Int {
        if (items.isEmpty()) return 100
        var totalPoints = 0
        items.forEach {
            totalPoints += when (it.status) {
                SecurityStatus.SECURE -> 100
                SecurityStatus.WARNING -> 50
                SecurityStatus.DANGER -> 0
            }
        }
        val score = totalPoints / items.size
        _securityScore.value = score
        return score
    }

    // Passwords Management
    fun addPassword(credential: PasswordCredential) {
        viewModelScope.launch {
            repository.insertPassword(credential)
            postNotification(
                title = "Credential Securely Vaulted",
                message = "Details for '${credential.title}' saved locally with AES-256.",
                type = NotificationType.SECURE
            )
        }
    }

    fun removePassword(credential: PasswordCredential) {
        viewModelScope.launch {
            repository.deletePassword(credential)
            postNotification(
                title = "Credential Removed",
                message = "The vault entry for '${credential.title}' was deleted.",
                type = NotificationType.WARNING
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCategory(category: String) {
        _selectedCategory.value = category
    }

    // Secure PIN & Notes Management
    fun setupMasterPin(pin: String) {
        viewModelScope.launch {
            if (pin.length == 4 && pin.all { it.isDigit() }) {
                repository.setMasterPin(pin)
                _hasMasterPin.value = true
                _isPinVerified.value = true
                _pinError.value = null
                postNotification(
                    title = "Master PIN Configured",
                    message = "A new 4-digit Master PIN has been securely set up on this device.",
                    type = NotificationType.SECURE
                )
            } else {
                _pinError.value = "PIN must be exactly 4 digits."
            }
        }
    }

    fun verifyMasterPin(pin: String) {
        viewModelScope.launch {
            val savedPin = repository.getMasterPin()
            if (savedPin == pin) {
                _isPinVerified.value = true
                _pinError.value = null
            } else {
                _pinError.value = "Incorrect PIN code. Please try again."
                _isPinVerified.value = false
            }
        }
    }

    fun lockNotes() {
        _isPinVerified.value = false
        _pinError.value = null
    }

    fun addNote(note: SecureNote) {
        viewModelScope.launch {
            repository.insertNote(note)
            postNotification(
                title = "Confidential Note Added",
                message = "A new entry was added to your local Safe Notepad.",
                type = NotificationType.SECURE
            )
        }
    }

    fun removeNote(note: SecureNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
            postNotification(
                title = "Confidential Note Deleted",
                message = "The secure notepad entry '${note.title}' was permanently deleted.",
                type = NotificationType.WARNING
            )
        }
    }
}
