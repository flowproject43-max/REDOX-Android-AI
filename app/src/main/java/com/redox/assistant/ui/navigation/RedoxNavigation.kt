package com.redox.assistant.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.redox.assistant.ui.components.GlassNavigationBar
import com.redox.assistant.ui.components.LiquidGlassCard
import com.redox.assistant.ui.screens.ActivityHistoryScreen
import com.redox.assistant.ui.screens.AiProviderSettingsScreen
import com.redox.assistant.ui.screens.AutomationScreen
import com.redox.assistant.ui.screens.ChatScreen
import com.redox.assistant.ui.screens.DeviceControlScreen
import com.redox.assistant.ui.screens.HomeScreen
import com.redox.assistant.ui.screens.LiveVoiceScreen
import com.redox.assistant.ui.screens.MemoryScreen
import com.redox.assistant.ui.screens.PermissionsScreen
import com.redox.assistant.ui.screens.SecurityCenterScreen
import com.redox.assistant.ui.screens.VisionScreen
import com.redox.assistant.ui.screens.VoiceSettingsScreen
import com.redox.assistant.ui.theme.CrimsonRed
import com.redox.assistant.ui.theme.PureBlack
import com.redox.assistant.ui.theme.TextGrayMuted
import com.redox.assistant.ui.theme.TextWhitePrimary
import com.redox.assistant.viewmodel.RedoxViewModel

data class HubMenuItem(val screen: Screen, val label: String, val icon: ImageVector)

@Composable
fun RedoxNavigation(viewModel: RedoxViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    var isHubMenuOpen by remember { mutableStateOf(false) }

    val allHubItems = listOf(
        HubMenuItem(Screen.Home, "Home / AI Orb", Icons.Default.GridView),
        HubMenuItem(Screen.LiveVoice, "Live Voice Terminal", Icons.Default.Mic),
        HubMenuItem(Screen.Chat, "AI Chat", Icons.Default.Mic),
        HubMenuItem(Screen.DeviceControl, "Device Controls", Icons.Default.PhoneAndroid),
        HubMenuItem(Screen.Automation, "Automation Center", Icons.Default.SmartToy),
        HubMenuItem(Screen.Vision, "Vision & OCR Center", Icons.Default.Visibility),
        HubMenuItem(Screen.Memory, "Neural Memory", Icons.Default.Memory),
        HubMenuItem(Screen.VoiceSettings, "Voice Settings", Icons.Default.VolumeUp),
        HubMenuItem(Screen.AiProviderSettings, "AI Providers", Icons.Default.Settings),
        HubMenuItem(Screen.Permissions, "Permissions Matrix", Icons.Default.VerifiedUser),
        HubMenuItem(Screen.ActivityHistory, "Activity Audit Logs", Icons.Default.History),
        HubMenuItem(Screen.SecurityCenter, "Security & Privacy", Icons.Default.Security)
    )

    Scaffold(
        bottomBar = {
            GlassNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        },
        containerColor = PureBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(viewModel = viewModel, onNavigate = { s -> navController.navigate(s.route) })
                }
                composable(Screen.LiveVoice.route) {
                    LiveVoiceScreen(viewModel = viewModel)
                }
                composable(Screen.Chat.route) {
                    ChatScreen(viewModel = viewModel)
                }
                composable(Screen.DeviceControl.route) {
                    DeviceControlScreen(viewModel = viewModel)
                }
                composable(Screen.Automation.route) {
                    AutomationScreen(viewModel = viewModel)
                }
                composable(Screen.Vision.route) {
                    VisionScreen(viewModel = viewModel)
                }
                composable(Screen.Memory.route) {
                    MemoryScreen(viewModel = viewModel)
                }
                composable(Screen.VoiceSettings.route) {
                    VoiceSettingsScreen(viewModel = viewModel)
                }
                composable(Screen.AiProviderSettings.route) {
                    AiProviderSettingsScreen(viewModel = viewModel)
                }
                composable(Screen.Permissions.route) {
                    PermissionsScreen(viewModel = viewModel)
                }
                composable(Screen.ActivityHistory.route) {
                    ActivityHistoryScreen(viewModel = viewModel)
                }
                composable(Screen.SecurityCenter.route) {
                    SecurityCenterScreen(viewModel = viewModel)
                }
            }

            // Top Floating Hub Menu Button
            IconButton(
                onClick = { isHubMenuOpen = !isHubMenuOpen },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33FFFFFF))
            ) {
                Icon(
                    imageVector = if (isHubMenuOpen) Icons.Default.Close else Icons.Default.GridView,
                    contentDescription = "System Hub Menu",
                    tint = CrimsonRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Futuristic Fullscreen Glass Hub Overlay
            AnimatedVisibility(
                visible = isHubMenuOpen,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xE6050505))
                        .clickable { isHubMenuOpen = false }
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "REDOX SYSTEM HUD MATRIX",
                            color = CrimsonRed,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Select any of the 12 core subsystems to navigate:",
                            color = TextGrayMuted,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        allHubItems.forEach { item ->
                            LiquidGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        isHubMenuOpen = false
                                        navController.navigate(item.screen.route) {
                                            popUpTo(Screen.Home.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                hasCrimsonGlow = currentRoute == item.screen.route,
                                contentPadding = 12.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = if (currentRoute == item.screen.route) CrimsonRed else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                                    Text(
                                        text = item.label,
                                        color = TextWhitePrimary,
                                        fontSize = 14.sp,
                                        fontWeight = if (currentRoute == item.screen.route) FontWeight.Bold else FontWeight.Normal
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
