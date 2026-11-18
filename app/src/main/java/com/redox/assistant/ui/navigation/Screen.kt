package com.redox.assistant.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object LiveVoice : Screen("live_voice")
    data object Chat : Screen("chat")
    data object DeviceControl : Screen("device_control")
    data object Automation : Screen("automation")
    data object Vision : Screen("vision")
    data object Memory : Screen("memory")
    data object VoiceSettings : Screen("voice_settings")
    data object AiProviderSettings : Screen("ai_provider_settings")
    data object Permissions : Screen("permissions")
    data object ActivityHistory : Screen("activity_history")
    data object SecurityCenter : Screen("security_center")
}
