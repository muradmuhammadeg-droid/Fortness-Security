package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DashboardView
import com.example.ui.screens.NotepadView
import com.example.ui.screens.VaultView
import com.example.ui.screens.AppLockView
import com.example.ui.screens.VpnView
import com.example.ui.screens.SettingsView
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.outlined.AppRegistration
import androidx.compose.material.icons.filled.VpnLock
import androidx.compose.material.icons.outlined.VpnLock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TextMuted
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SecurityViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContainer()
            }
        }
    }
}

@Composable
fun MainAppContainer() {
    val viewModel: SecurityViewModel = viewModel()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("app_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Shield else Icons.Outlined.Shield,
                            contentDescription = "Dashboard"
                        )
                    },
                    label = { Text("Scanner") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D192B),
                        selectedTextColor = TextWhite,
                        indicatorColor = CyberBlue,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_item_scanner")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.Lock else Icons.Outlined.Lock,
                            contentDescription = "Passwords"
                        )
                    },
                    label = { Text("Vault") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D192B),
                        selectedTextColor = TextWhite,
                        indicatorColor = CyberBlue,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_item_vault")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.NoteAlt else Icons.Outlined.NoteAlt,
                            contentDescription = "Safe Notes"
                        )
                    },
                    label = { Text("Notepad") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D192B),
                        selectedTextColor = TextWhite,
                        indicatorColor = CyberBlue,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_item_notepad")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.AppRegistration else Icons.Outlined.AppRegistration,
                            contentDescription = "App Lock"
                        )
                    },
                    label = { Text("App Lock") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D192B),
                        selectedTextColor = TextWhite,
                        indicatorColor = CyberBlue,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_item_applock")
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 4) Icons.Filled.VpnLock else Icons.Outlined.VpnLock,
                            contentDescription = "Secure VPN"
                        )
                    },
                    label = { Text("VPN") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D192B),
                        selectedTextColor = TextWhite,
                        indicatorColor = CyberBlue,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_item_vpn")
                )

                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 5) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1D192B),
                        selectedTextColor = TextWhite,
                        indicatorColor = CyberBlue,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepObsidian,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepObsidian)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedTab) {
                0 -> DashboardView(viewModel = viewModel)
                1 -> VaultView(viewModel = viewModel)
                2 -> NotepadView(viewModel = viewModel)
                3 -> AppLockView(viewModel = viewModel)
                4 -> VpnView(viewModel = viewModel)
                5 -> SettingsView(viewModel = viewModel)
            }
        }
    }
}
