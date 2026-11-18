package com.redox.assistant.automation

import com.redox.assistant.domain.model.AgentTask
import com.redox.assistant.domain.model.DeviceAction
import com.redox.assistant.domain.model.StepStatus
import com.redox.assistant.domain.model.TaskStep

class AutonomousAgent(
    private val automationManager: DeviceAutomationManager
) {

    fun parseActionsFromText(aiResponse: String): List<DeviceAction> {
        val actions = mutableListOf<DeviceAction>()

        val regex = Regex("""ACTION:([A-Z_]+)(?:\[(.*?)])?""")
        val matches = regex.findAll(aiResponse)

        for (match in matches) {
            val command = match.groupValues[1]
            val param = if (match.groupValues.size > 2) match.groupValues[2] else ""

            when (command) {
                "OPEN_APP" -> actions.add(DeviceAction.OpenApp(param.trim()))
                "CLICK" -> actions.add(DeviceAction.ClickElement(param.trim()))
                "TYPE" -> actions.add(DeviceAction.TypeText(param.trim()))
                "SCROLL" -> actions.add(DeviceAction.Scroll(param.trim()))
                "CALL" -> actions.add(DeviceAction.MakeCall(param.trim()))
                "SMS" -> {
                    val parts = param.split(",", limit = 2)
                    val to = parts.getOrNull(0)?.trim() ?: ""
                    val msg = parts.getOrNull(1)?.trim() ?: ""
                    actions.add(DeviceAction.SendSms(to, msg))
                }
                "WHATSAPP" -> {
                    val parts = param.split(",", limit = 2)
                    val to = parts.getOrNull(0)?.trim() ?: ""
                    val msg = parts.getOrNull(1)?.trim() ?: ""
                    actions.add(DeviceAction.SendWhatsApp(to, msg))
                }
                "FLASHLIGHT" -> {
                    val on = param.contains("on", ignoreCase = true) || param.contains("true", ignoreCase = true)
                    actions.add(DeviceAction.ToggleFlashlight(on))
                }
                "VOLUME" -> {
                    val pct = param.toIntOrNull() ?: 50
                    actions.add(DeviceAction.AdjustVolume(pct))
                }
                "MEDIA" -> actions.add(DeviceAction.MediaControl(param.trim()))
                "TIMER" -> {
                    val parts = param.split(",", limit = 2)
                    val sec = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 60
                    val lbl = parts.getOrNull(1)?.trim() ?: "REDOX Timer"
                    actions.add(DeviceAction.SetTimer(sec, lbl))
                }
                "READ_SCREEN" -> actions.add(DeviceAction.ReadVisibleScreen)
                "STATUS" -> actions.add(DeviceAction.CheckDeviceStatus)
                else -> actions.add(DeviceAction.Unknown(command))
            }
        }

        return actions
    }

    suspend fun planAndExecuteTask(goal: String, actions: List<DeviceAction>): AgentTask {
        val steps = actions.mapIndexed { index, action ->
            TaskStep(
                title = "Step ${index + 1}: ${action.javaClass.simpleName}",
                description = "Execute automation $action",
                action = action,
                status = StepStatus.PENDING
            )
        }.toMutableList()

        for (i in steps.indices) {
            val step = steps[i]
            steps[i] = step.copy(status = StepStatus.IN_PROGRESS)
            val action = step.action
            if (action != null) {
                val output = automationManager.executeAction(action)
                steps[i] = step.copy(status = StepStatus.COMPLETED, result = output)
            } else {
                steps[i] = step.copy(status = StepStatus.FAILED, result = "No action defined")
            }
        }

        return AgentTask(
            goal = goal,
            steps = steps,
            isCompleted = true
        )
    }
}
