package com.redox.assistant.domain.model

sealed class DeviceAction {
    data class OpenApp(val appName: String) : DeviceAction()
    data class ClickElement(val targetTextOrId: String) : DeviceAction()
    data class TypeText(val text: String) : DeviceAction()
    data class Scroll(val direction: String) : DeviceAction() // "up", "down", "forward", "backward"
    data class MakeCall(val contactOrNumber: String, val confirmed: Boolean = false) : DeviceAction()
    data class SendSms(val recipient: String, val message: String, val confirmed: Boolean = false) : DeviceAction()
    data class SendWhatsApp(val recipient: String, val message: String, val confirmed: Boolean = false) : DeviceAction()
    data class ToggleFlashlight(val enable: Boolean) : DeviceAction()
    data class AdjustVolume(val levelPercent: Int) : DeviceAction()
    data class MediaControl(val command: String) : DeviceAction() // "play", "pause", "next", "previous"
    data class SetTimer(val durationSeconds: Int, val label: String) : DeviceAction()
    data object ReadVisibleScreen : DeviceAction()
    data object CaptureScreen : DeviceAction()
    data object CheckDeviceStatus : DeviceAction()
    data class Unknown(val rawCommand: String) : DeviceAction()
}
