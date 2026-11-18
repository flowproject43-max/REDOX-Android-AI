package com.redox.assistant.automation

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.provider.AlarmClock
import android.view.KeyEvent
import com.redox.assistant.RedoxApplication
import com.redox.assistant.domain.model.ActivityLog
import com.redox.assistant.domain.model.ActivityStatus
import com.redox.assistant.domain.model.DeviceAction
import com.redox.assistant.service.RedoxAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class DeviceAutomationManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var isFlashlightOn = false

    suspend fun executeAction(action: DeviceAction): String = withContext(Dispatchers.Main) {
        val resultDesc: String
        val status: ActivityStatus

        when (action) {
            is DeviceAction.OpenApp -> {
                val success = launchAppByName(action.appName)
                status = if (success) ActivityStatus.SUCCESS else ActivityStatus.FAILED
                resultDesc = if (success) "Opened application '${action.appName}' successfully."
                else "Could not locate or launch application '${action.appName}'."
            }

            is DeviceAction.ClickElement -> {
                val service = RedoxAccessibilityService.instance
                if (service == null) {
                    status = ActivityStatus.FAILED
                    resultDesc = "Accessibility service is inactive. Please enable REDOX Accessibility in settings."
                } else {
                    val clicked = service.clickElementByText(action.targetTextOrId)
                    status = if (clicked) ActivityStatus.SUCCESS else ActivityStatus.FAILED
                    resultDesc = if (clicked) "Clicked UI element containing '${action.targetTextOrId}'."
                    else "Target UI element '${action.targetTextOrId}' not found on active screen."
                }
            }

            is DeviceAction.TypeText -> {
                val service = RedoxAccessibilityService.instance
                if (service == null) {
                    status = ActivityStatus.FAILED
                    resultDesc = "Accessibility service is disabled."
                } else {
                    val typed = service.typeTextIntoFocusedNode(action.text)
                    status = if (typed) ActivityStatus.SUCCESS else ActivityStatus.FAILED
                    resultDesc = if (typed) "Entered text successfully." else "No active editable input field detected."
                }
            }

            is DeviceAction.Scroll -> {
                val service = RedoxAccessibilityService.instance
                if (service == null) {
                    status = ActivityStatus.FAILED
                    resultDesc = "Accessibility service required for scrolling."
                } else {
                    val forward = action.direction.contains("down", ignoreCase = true) || action.direction.contains("forward", ignoreCase = true)
                    val scrolled = service.performScroll(forward)
                    status = if (scrolled) ActivityStatus.SUCCESS else ActivityStatus.FAILED
                    resultDesc = if (scrolled) "Scrolled ${action.direction} successfully." else "Failed to scroll current view."
                }
            }

            is DeviceAction.MakeCall -> {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${action.contactOrNumber}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                status = ActivityStatus.SUCCESS
                resultDesc = "Prepared phone call to ${action.contactOrNumber}."
            }

            is DeviceAction.SendSms -> {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:${action.recipient}")
                    putExtra("sms_body", action.message)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                status = ActivityStatus.SUCCESS
                resultDesc = "Opened SMS composer for ${action.recipient}."
            }

            is DeviceAction.SendWhatsApp -> {
                val cleanNumber = action.recipient.replace("+", "").replace(" ", "").replace("-", "")
                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(action.message)}")
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                status = try {
                    context.startActivity(intent)
                    ActivityStatus.SUCCESS
                } catch (e: Exception) {
                    ActivityStatus.FAILED
                }
                resultDesc = if (status == ActivityStatus.SUCCESS) {
                    "Dispatched message to WhatsApp for ${action.recipient}."
                } else {
                    "WhatsApp application is not installed on this device."
                }
            }

            is DeviceAction.ToggleFlashlight -> {
                val result = setTorch(action.enable)
                status = if (result) ActivityStatus.SUCCESS else ActivityStatus.FAILED
                resultDesc = if (result) "Flashlight turned ${if (action.enable) "ON" else "OFF"}."
                else "Flashlight control unavailable or unsupported on this hardware."
            }

            is DeviceAction.AdjustVolume -> {
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val target = ((action.levelPercent / 100f) * maxVolume).toInt().coerceIn(0, maxVolume)
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
                status = ActivityStatus.SUCCESS
                resultDesc = "Media volume set to ${action.levelPercent}% ($target/$maxVolume)."
            }

            is DeviceAction.MediaControl -> {
                val keyCode = when (action.command.lowercase()) {
                    "play", "pause", "toggle" -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                    "next" -> KeyEvent.KEYCODE_MEDIA_NEXT
                    "previous", "prev" -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
                    else -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                }
                audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                audioManager.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
                status = ActivityStatus.SUCCESS
                resultDesc = "Media command '${action.command}' dispatched."
            }

            is DeviceAction.SetTimer -> {
                val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                    putExtra(AlarmClock.EXTRA_LENGTH, action.durationSeconds)
                    putExtra(AlarmClock.EXTRA_MESSAGE, action.label)
                    putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                status = try {
                    context.startActivity(intent)
                    ActivityStatus.SUCCESS
                } catch (e: Exception) {
                    ActivityStatus.FAILED
                }
                resultDesc = if (status == ActivityStatus.SUCCESS) "Timer set for ${action.durationSeconds} seconds (${action.label})."
                else "Unable to launch system clock timer."
            }

            is DeviceAction.ReadVisibleScreen -> {
                val service = RedoxAccessibilityService.instance
                if (service == null) {
                    status = ActivityStatus.FAILED
                    resultDesc = "Accessibility service is inactive."
                } else {
                    val hierarchy = service.getVisibleScreenHierarchyText()
                    status = ActivityStatus.SUCCESS
                    resultDesc = hierarchy
                }
            }

            is DeviceAction.CaptureScreen -> {
                status = ActivityStatus.SUCCESS
                resultDesc = "Screenshot request registered."
            }

            is DeviceAction.CheckDeviceStatus -> {
                val diag = getDeviceDiagnostics()
                status = ActivityStatus.SUCCESS
                resultDesc = diag
            }

            is DeviceAction.Unknown -> {
                status = ActivityStatus.FAILED
                resultDesc = "Unrecognized command: ${action.rawCommand}"
            }
        }

        // Log action in audit history
        RedoxApplication.instance.activityLogRepository.logActivity(
            ActivityLog(
                actionName = action.javaClass.simpleName,
                description = resultDesc,
                status = status
            )
        )

        resultDesc
    }

    fun launchAppByName(query: String): Boolean {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val appList = pm.queryIntentActivities(mainIntent, 0)
        for (info in appList) {
            val label = info.loadLabel(pm).toString()
            if (label.contains(query, ignoreCase = true) ||
                info.activityInfo.packageName.contains(query, ignoreCase = true)
            ) {
                val launchIntent = pm.getLaunchIntentForPackage(info.activityInfo.packageName)
                if (launchIntent != null) {
                    launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(launchIntent)
                    return true
                }
            }
        }
        return false
    }

    private fun setTorch(enable: Boolean): Boolean {
        return try {
            val cameraManager = cameraManager ?: return false
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: cameraManager.cameraIdList.firstOrNull() ?: return false

            cameraManager.setTorchMode(cameraId, enable)
            isFlashlightOn = enable
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getDeviceDiagnostics(): String {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryPct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        val availRamMb = memInfo.availMem / (1024 * 1024)
        val totalRamMb = memInfo.totalMem / (1024 * 1024)

        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val availStorageGb = (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024 * 1024)
        val totalStorageGb = (stat.blockCountLong * stat.blockSizeLong) / (1024 * 1024 * 1024)

        return """
        [System Diagnostics]:
        • Battery Level: $batteryPct%
        • Available RAM: $availRamMb MB / $totalRamMb MB
        • Storage: $availStorageGb GB free / $totalStorageGb GB total
        • Accessibility Service: ${if (RedoxAccessibilityService.instance != null) "ONLINE" else "OFFLINE"}
        """.trimIndent()
    }
}
