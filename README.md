# Shvil Hazhav (The Golden Path)

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-ffca28.svg)](https://firebase.google.com)
[![Hilt](https://img.shields.io/badge/DI-Hilt-blue.svg)](https://developer.android.com)
[![Material 3](https://img.shields.io/badge/UI-Material--3-7b4397.svg)](https://m3.material.io)

**Shvil Hazhav** is a comprehensive, accessibility-focused Android application designed to empower elderly individuals (60+) and people with disabilities. It combines essential health management tools with cognitive stimulation and social connectivity to improve daily quality of life and safety.

---

## 🚀 Key Features

### 💊 Medication Management & Safety
- **Smart Scheduling:** Easily add and manage medication regimens with specific dosages and timings.
- **Automated Alerts:** High-priority notifications and alarms using **AlarmManager** to ensure no dose is missed.
- **Compliance Tracking:** Log medication intake and monitor adherence over time.
- **Inventory Tracking:** Real-time monitoring of medication supplies with expiration alerts.

### 🧠 Cognitive Training Hub
- **Online Memory Game:** Engage in 1-on-1 matches or solo practice to strengthen short-term memory and focus.
- **Math Problems Activity:** Daily arithmetic challenges designed to maintain mental agility and logical thinking.
- **Data Visualization:** Track daily progress across all activities with visual graphs (Custom Views) and history logs.

### 🤖 AI Health Companion
- **Intelligent Q&A:** An AI assistant powered by **Google Gemini (Firebase Vertex AI)** to answer general health and wellness questions in simple, accessible language.
- **Accessibility First:** Designed to provide clear, jargon-free information.
- *Note: Not a substitute for professional medical advice.*

### 👥 Social Community & Support
- **Moderated Forums:** Discussion boards categorized by topics for sharing experiences and advice.
- **Peer Connection:** Foster a sense of community to combat social isolation.

### 💡 Daily Inspiration
- **Tip of the Day:** Receive daily motivational quotes and practical health tips to start the day positively.

---

## 🛠 Tech Stack & Architecture

The project is built using modern Android development best practices.

- **Architecture:** **Single-Activity Architecture** with **MVVM (Model-View-ViewModel)** and Repository Pattern.
- **Navigation:** **Jetpack Navigation Component** for managing fragment transitions.
- **Dependency Injection:** **Hilt** for robust and testable component management.
- **Background Tasks:** **WorkManager** for reliable periodic checks (e.g., birthday reminders).
- **Local Persistence:** **SharedPreferences** for user settings and preferences.
- **Backend Services (Firebase):**
    - **Firebase Realtime Database:** Low-latency data synchronization for games and forums.
- **AI Integration:** **Firebase Vertex AI (Gemini SDK)** for the medical assistant module.
- **UI Framework:** **Material Design 3**, **ViewBinding**, and Custom Views for specialized data rendering.
- **Serialization:** **GSON** for efficient data handling.
- **Documentation:** Extensively documented using **Javadoc** for improved maintainability and developer onboarding.

---

## 📁 Project Structure

| Package                                                                | Purpose                                                       |
|:-----------------------------------------------------------------------|:--------------------------------------------------------------|
| [`screens`](app/src/main/java/com/example/sagivproject/screens)        | Fragments and UI logic for various screens.                   |
| [`services`](app/src/main/java/com/example/sagivproject/services)      | Business logic interfaces and implementations.                |
| [`models`](app/src/main/java/com/example/sagivproject/models)          | Data entities and Enums.                                      |
| [`adapters`](app/src/main/java/com/example/sagivproject/adapters)      | RecyclerView adapters for lists and grids.                    |
| [`ui`](app/src/main/java/com/example/sagivproject/ui)                  | Custom views, menu fragments, and shared UI components.       |
| [`bases`](app/src/main/java/com/example/sagivproject/bases)            | Abstract base classes for standardized component behavior.    |
| [`di`](app/src/main/java/com/example/sagivproject/di)                  | Dependency Injection modules (Hilt).                          |
| [`utils`](app/src/main/java/com/example/sagivproject/utils)            | Static helpers for validation, UI, dates, and storage.        |

---

## ♿ Accessibility Standards
- **High Contrast:** UI elements optimized for visual clarity.
- **Large Typography:** Default font sizes optimized for elderly users.
- **Intuitive Navigation:** Minimalist screen layouts and consistent UI patterns to reduce cognitive load.

---

## ⚙️ Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 17+
- Android SDK 35+ (API Level 35)

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/sagiv23/ShvilHazhav.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle and run the application on an emulator or physical device.

---

## 👨‍💻 Developer
**Sagiv**  
*Computer Science Final Project - Shvil Hazhav (The Golden Path)*
