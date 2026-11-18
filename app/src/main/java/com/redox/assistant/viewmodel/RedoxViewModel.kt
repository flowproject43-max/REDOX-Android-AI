package com.redox.assistant.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.redox.assistant.RedoxApplication
import com.redox.assistant.automation.AutonomousAgent
import com.redox.assistant.automation.DeviceAutomationManager
import com.redox.assistant.automation.ScreenVisionManager
import com.redox.assistant.domain.model.ActivityLog
import com.redox.assistant.domain.model.ActivityStatus
import com.redox.assistant.domain.model.AiMessage
import com.redox.assistant.domain.model.AiProvider
import com.redox.assistant.domain.model.DeviceAction
import com.redox.assistant.domain.model.MemoryCategory
import com.redox.assistant.domain.model.MemoryItem
import com.redox.assistant.domain.model.MessageSender
import com.redox.assistant.domain.model.OrbState
import com.redox.assistant.domain.model.VoiceConfig
import com.redox.assistant.service.RedoxAccessibilityService
import com.redox.assistant.voice.VoiceEngine
import com.redox.assistant.voice.WakeWordDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RedoxViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as RedoxApplication
    val settingsRepo = app.settingsRepository
    val memoryRepo = app.memoryRepository
    val logRepo = app.activityLogRepository
    val aiRepo = app.aiRepository

    val automationManager = DeviceAutomationManager(application)
    val visionManager = ScreenVisionManager(application)
    val autonomousAgent = AutonomousAgent(automationManager)
    val wakeWordDetector = WakeWordDetector()

    val voiceEngine = VoiceEngine(application)

    private val _orbState = MutableStateFlow(OrbState.IDLE)
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    private val _currentTranscript = MutableStateFlow("")
    val currentTranscript: StateFlow<String> = _currentTranscript.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<AiMessage>>(emptyList())
    val chatMessages: StateFlow<List<AiMessage>> = _chatMessages.asStateFlow()

    private val _deviceDiagnostics = MutableStateFlow("")
    val deviceDiagnostics: StateFlow<String> = _deviceDiagnostics.asStateFlow()

    val activeProvider: StateFlow<AiProvider> = settingsRepo.getActiveProvider()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AiProvider.GEMINI)

    val voiceConfig: StateFlow<VoiceConfig> = settingsRepo.getVoiceConfig()
        .stateIn(viewModelScope, SharingStarted.Eagerly, VoiceConfig())

    val memories: StateFlow<List<MemoryItem>> = memoryRepo.getAllMemories()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val activityLogs: StateFlow<List<ActivityLog>> = logRepo.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val isListening: StateFlow<Boolean> = voiceEngine.isListening
    val isSpeaking: StateFlow<Boolean> = voiceEngine.isSpeaking
    val speechAmplitude: StateFlow<Float> = voiceEngine.speechAmplitude
    val isAccessibilityActive: StateFlow<Boolean> = RedoxAccessibilityService.isServiceActive

    init {
        loadChatHistory()
        refreshDiagnostics()

        // Sync VoiceConfig changes with VoiceEngine
        viewModelScope.launch {
            voiceConfig.collect { cfg ->
                voiceEngine.updateVoiceConfig(cfg)
            }
        }
    }

    private fun loadChatHistory() {
        viewModelScope.launch {
            val list = app.database.getAllChatMessages()
            if (list.isEmpty()) {
                val welcome = AiMessage(
                    sender = MessageSender.REDOX,
                    text = "REDOX core online. Systems nominal. How may I serve you today, sir?"
                )
                app.database.insertChatMessage(welcome)
                _chatMessages.value = listOf(welcome)
            } else {
                _chatMessages.value = list
            }
        }
    }

    fun refreshDiagnostics() {
        _deviceDiagnostics.value = automationManager.getDeviceDiagnostics()
    }

    fun startVoiceInteraction() {
        voiceEngine.stopSpeaking()
        _orbState.value = OrbState.LISTENING
        _currentTranscript.value = ""

        voiceEngine.startListening(
            onResult = { finalResult ->
                _currentTranscript.value = finalResult
                processUserPrompt(finalResult)
            },
            onPartial = { partial ->
                _currentTranscript.value = partial
            }
        )
    }

    fun stopVoiceInteraction() {
        voiceEngine.stopListening()
        voiceEngine.stopSpeaking()
        _orbState.value = OrbState.IDLE
    }

    fun processUserPrompt(prompt: String, screenContext: String? = null) {
        if (prompt.isBlank()) return

        viewModelScope.launch {
            // Check wake word if applicable
            val cleanPrompt = if (wakeWordDetector.isWakeWordDetected(prompt)) {
                wakeWordDetector.extractCommandAfterWakeWord(prompt)
            } else {
                prompt
            }

            val userMsg = AiMessage(sender = MessageSender.USER, text = prompt)
            app.database.insertChatMessage(userMsg)
            _chatMessages.value = _chatMessages.value + userMsg

            _orbState.value = OrbState.THINKING

            val activeProv = activeProvider.value
            val responseResult = aiRepo.sendMessage(
                conversationHistory = _chatMessages.value,
                userMessage = cleanPrompt,
                screenContext = screenContext
            )

            if (responseResult.isSuccess) {
                val aiText = responseResult.getOrNull() ?: ""
                _orbState.value = OrbState.SPEAKING

                // Parse any embedded action tags
                val actions = autonomousAgent.parseActionsFromText(aiText)
                var toolResultInfo: String? = null

                if (actions.isNotEmpty()) {
                    val task = autonomousAgent.planAndExecuteTask(cleanPrompt, actions)
                    toolResultInfo = task.steps.joinToString("\n") { "${it.title}: ${it.result}" }
                }

                val redoxMsg = AiMessage(
                    sender = MessageSender.REDOX,
                    text = aiText,
                    providerUsed = activeProv.displayName,
                    toolCallInfo = toolResultInfo
                )
                app.database.insertChatMessage(redoxMsg)
                _chatMessages.value = _chatMessages.value + redoxMsg

                // Speak response with Jarvis voice
                voiceEngine.speak(aiText) {
                    _orbState.value = OrbState.IDLE
                }

                // If voice wasn't spoken, return to idle
                if (!voiceEngine.isSpeaking.value) {
                    _orbState.value = OrbState.IDLE
                }
            } else {
                _orbState.value = OrbState.ERROR
                val err = responseResult.exceptionOrNull()?.message ?: "Communication error"
                val errMsg = AiMessage(
                    sender = MessageSender.REDOX,
                    text = "System alert: Unable to complete cognitive request. $err"
                )
                app.database.insertChatMessage(errMsg)
                _chatMessages.value = _chatMessages.value + errMsg
                voiceEngine.speak("System alert: $err")
            }
        }
    }

    fun executeDirectAction(action: DeviceAction, onComplete: ((String) -> Unit)? = null) {
        viewModelScope.launch {
            val result = automationManager.executeAction(action)
            refreshDiagnostics()
            onComplete?.invoke(result)
        }
    }

    fun saveMemory(category: MemoryCategory, key: String, value: String) {
        viewModelScope.launch {
            memoryRepo.saveMemory(
                MemoryItem(
                    category = category,
                    key = key,
                    value = value
                )
            )
        }
    }

    fun deleteMemory(id: String) {
        viewModelScope.launch {
            memoryRepo.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            memoryRepo.clearAllMemories()
        }
    }

    fun clearActivityLogs() {
        viewModelScope.launch {
            logRepo.clearLogs()
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            app.database.clearChat()
            _chatMessages.value = emptyList()
        }
    }

    fun setProvider(provider: AiProvider) {
        viewModelScope.launch {
            settingsRepo.setActiveProvider(provider)
        }
    }

    fun saveApiKey(provider: AiProvider, key: String) {
        viewModelScope.launch {
            settingsRepo.setApiKey(provider, key)
        }
    }

    fun updateVoiceConfig(config: VoiceConfig) {
        viewModelScope.launch {
            settingsRepo.setVoiceConfig(config)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.shutdown()
    }
}
