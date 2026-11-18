package com.redox.assistant.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.redox.assistant.domain.model.VoiceConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceEngine(
    private val context: Context,
    private var config: VoiceConfig = VoiceConfig()
) {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private var speechRecognizer: SpeechRecognizer? = null
    private val _isListening = MutableStateFlow(false)
    val isListening = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking = _isSpeaking.asStateFlow()

    private val _speechAmplitude = MutableStateFlow(0f)
    val speechAmplitude = _speechAmplitude.asStateFlow()

    private var onSpeechResultCallback: ((String) -> Unit)? = null
    private var onPartialSpeechCallback: ((String) -> Unit)? = null

    init {
        initializeTts()
    }

    private fun initializeTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                applyVoiceConfig(config)

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    fun updateVoiceConfig(newConfig: VoiceConfig) {
        config = newConfig
        applyVoiceConfig(newConfig)
    }

    private fun applyVoiceConfig(cfg: VoiceConfig) {
        if (!isTtsReady) return
        val locale = when (cfg.languageCode) {
            "bn-BD", "bn-IN" -> Locale("bn", "BD")
            "hi-IN" -> Locale("hi", "IN")
            else -> Locale.US
        }
        tts?.language = locale
        tts?.setPitch(cfg.pitch)
        tts?.setSpeechRate(cfg.speechRate)
    }

    fun speak(text: String, onFinished: (() -> Unit)? = null) {
        if (!isTtsReady || text.isBlank()) return
        stopListening()
        _isSpeaking.value = true

        val utteranceId = "redox_utterance_${System.currentTimeMillis()}"
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, config.volume)
        }

        // Clean out technical tags from text for speech synthesis
        val cleanSpeech = text
            .replace(Regex("""ACTION:[A-Z_]+(\[.*?])?"""), "")
            .replace(Regex("""[*#_`~]"""), "")
            .trim()

        if (cleanSpeech.isBlank()) {
            _isSpeaking.value = false
            return
        }

        tts?.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun startListening(
        onResult: (String) -> Unit,
        onPartial: ((String) -> Unit)? = null
    ) {
        stopSpeaking()
        onSpeechResultCallback = onResult
        onPartialSpeechCallback = onPartial

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            return
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createRecognitionListener())
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            val lang = when (config.languageCode) {
                "bn-BD", "bn-IN" -> "bn-BD"
                "hi-IN" -> "hi-IN"
                else -> "en-US"
            }
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang)
        }

        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        _isListening.value = false
        _speechAmplitude.value = 0f
    }

    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _isListening.value = true
        }

        override fun onBeginningOfSpeech() {
            _isListening.value = true
        }

        override fun onRmsChanged(rmsdB: Float) {
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            _speechAmplitude.value = normalized
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _isListening.value = false
        }

        override fun onError(error: Int) {
            _isListening.value = false
            _speechAmplitude.value = 0f
        }

        override fun onResults(results: Bundle?) {
            _isListening.value = false
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull() ?: ""
            if (text.isNotBlank()) {
                onSpeechResultCallback?.invoke(text)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val text = matches?.firstOrNull() ?: ""
            if (text.isNotBlank()) {
                onPartialSpeechCallback?.invoke(text)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun shutdown() {
        stopSpeaking()
        stopListening()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}
