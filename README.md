# 🔴 REDOX AI — Autonomous Android AI Assistant

![REDOX AI Banner](app/src/main/res/drawable/ic_launcher.xml)

**REDOX** is an autonomous, standalone native Android AI voice assistant engineered in Kotlin and Jetpack Compose. Inspired by a cinematic Jarvis persona and designed with an ultra-premium **Black × Dark Red Liquid Glass** aesthetic, REDOX pairs multi-provider cognitive reasoning with deep Android system automation and computer vision.

---

## ⚡ Highlights & Core Capabilities

- **Liquid Glass HUD Interface**:
  - Pure Black (`#050505`) × Deep Crimson (`#FF1744`) visual hierarchy.
  - Multi-state canvas-rendered **Animated Energy Orb** (Standby, Listening, Thinking, Transmitting, Alert).
  - Real-time reactive spectral audio waveform.
  - 12 comprehensive modular screens with floating curved glass navigation.

- **Multi-Provider AI Brain**:
  - Primary: **Google Gemini** (Gemini 1.5/2.0 Flash & Pro).
  - Supported: OpenAI (GPT-4o), Anthropic Claude (3.5 Sonnet), Groq (Llama-3.3-70b), and OpenRouter.
  - Seamless automatic fallback routing when rate limits or connectivity issues occur.
  - Multilingual comprehension: Fluently parses English, Bengali (বাংলা), Hindi (हिन्दी), and mixed Banglish/Hinglish instructions.

- **Jarvis-Style Cinematic Voice Engine**:
  - Resonant, deep male vocal profile with calibrated pitch, tempo, and natural cadence.
  - Wake Word Detection: Continuous hands-free activation via **"Hey REDOX"**.
  - Interruptible speech playback with Voice Activity Detection (VAD).
  - Android Foreground Service with microphone status indicator.

- **Deep Device Automation & Accessibility Service**:
  - Autonomous UI navigation: Click visible text/buttons, type into input fields, scroll, swipe.
  - System controls: Flashlight toggle, volume sliders, media transport (play/pause/next/prev).
  - App launcher: Launch any installed application by voice or intent.
  - Safe telephony: Direct phone dialing, SMS drafting, and WhatsApp messaging with user confirmation boundaries.

- **Multimodal Screen Vision & OCR**:
  - Live accessibility UI hierarchy capture and OCR text extraction.
  - Multimodal visual reasoning via Gemini Vision API.
  - Camera-based visual inspection.

- **Neural Memory & Data Privacy**:
  - Persistent SQLite neural memory database for user preferences, routines, and contextual facts.
  - Hardware Android Keystore encryption (AES-256 GCM) for API keys and PIN lock.
  - Granular permissions matrix and verifiable activity audit trail.

---

## 📱 The 12 Core Subsystems / Screens

1. **Home / AI Orb**: Dynamic animated orb, time/date HUD, quick action buttons, live core diagnostics.
2. **Live Voice**: Full-screen Jarvis voice terminal, reactive spectral visualizer, live transcript, language switcher.
3. **AI Chat**: Liquid glass conversational bubble thread, tool execution cards, speech dictation, quick chips.
4. **Device Controls**: Flashlight, volume, media transport, system shortcuts, live hardware telemetry.
5. **Automation Center**: Accessibility status, system gesture triggers, live UI interaction tester.
6. **Vision Center**: Active screen hierarchy capture, OCR inspection, camera perception.
7. **Neural Memory**: Categorized knowledge base (Facts, Preferences, Contacts, Routines), CRUD controls.
8. **Voice Settings**: Deep Jarvis pitch/rate tuning, audio preview, wake-word sensitivity, background listener.
9. **AI Provider Settings**: Keystore API key manager, model selection, auto-fallback, ping test connection.
10. **Permissions Dashboard**: Granular matrix (Mic, Accessibility, Camera, Notifications, Overlay) with 1-tap grant handlers.
11. **Activity History**: Immutable audit log of all autonomous actions, timestamps, and execution statuses.
12. **Security Center**: Hardware keystore status, app lock PIN, sensitive action confirmation toggles, emergency data wipe.

---

## 🛠️ Architecture & Tech Stack

```
com.redox.assistant/
├── data/
│   ├── local/          # RedoxDatabase (SQLite), SecurePreferences (Android KeyStore AES-256 GCM)
│   ├── remote/         # GeminiApiClient, OpenAiApiClient, ClaudeApiClient
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # AiMessage, AiProvider, OrbState, DeviceAction, MemoryItem, ActivityLog, VoiceConfig
│   └── repository/     # Clean Architecture Repository interfaces
├── service/            # RedoxAccessibilityService, RedoxForegroundService
├── receiver/           # RedoxBootReceiver
├── automation/         # DeviceAutomationManager, ScreenVisionManager, AutonomousAgent
├── voice/              # VoiceEngine, WakeWordDetector
├── viewmodel/          # RedoxViewModel (StateFlow & Coroutines)
└── ui/
    ├── theme/          # Color, Type, Theme (Black & Dark Red Liquid Glass)
    ├── components/     # LiquidGlassCard, GlassButton, AnimatedEnergyOrb, VoiceWaveform, etc.
    ├── screens/        # 12 Core Subsystem Screens
    └── navigation/     # RedoxNavigation, Screen routes, System HUD Matrix
```

- **Target SDK**: 35 (Android 15)
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Programming Language**: Kotlin 2.0.21
- **UI Toolkit**: Jetpack Compose (BOM 2024.12.01) with Material 3
- **Dependency Injection / State**: MVVM + Clean Architecture + StateFlow
- **Persistence**: SQLite + Android Keystore
- **Build System**: Gradle 8.11.1 + Android Gradle Plugin 8.7.3

---

## 🚀 Cloud Build & GitHub Actions CI/CD

This repository includes a completely automated cloud CI/CD workflow in `.github/workflows/build-apk.yml`. No local PC or Android Studio installation is required.

### What the Workflow Does:
1. Clones the repository.
2. Sets up JDK 17 & Android SDK 35 tools.
3. Runs unit tests (`testDebugUnitTest`).
4. Compiles the Debug APK (`assembleDebug`) and Release APK (`assembleRelease`).
5. Calculates SHA-256 cryptographic checksums for both APK binaries.
6. Uploads the APKs as workflow artifacts.
7. Automatically publishes a GitHub Release with the downloadable APKs!

---

## 📥 APK Installation Guide (Android Phone)

1. Navigate to the **GitHub Actions** tab or **Releases** tab in this repository.
2. Download `REDOX-Assistant-debug.apk`.
3. Open the downloaded `.apk` file on your Android device.
4. If prompted, allow **"Install unknown apps"** for your browser or file manager.
5. Tap **Install** and launch **REDOX**.
6. On first launch:
   - Go to **Permissions Dashboard** and grant **Microphone** and **Accessibility Service**.
   - Go to **AI Provider Settings** and enter your free **Google Gemini API Key**.
   - Tap the Energy Orb or say **"Hey REDOX"** to begin!

---

## 🛡️ License

Built with pride for autonomous voice-first computing.
