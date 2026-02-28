# Shvil Hazhav (The Golden Path)

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-ffca28.svg)](https://firebase.google.com)
[![Hilt](https://img.shields.io/badge/DI-Hilt-blue.svg)](https://developer.android.com/training/dependency-injection/hilt-android)

**Shvil Hazhav** is a comprehensive, accessibility-focused Android application designed to empower elderly individuals (60+) and people with disabilities. It combines essential health management tools with cognitive stimulation and social connectivity to improve daily quality of life and safety.

---

## 🚀 Key Features

### 💊 Medication Management & Safety
- **Smart Scheduling:** Easily add and manage medication regimens with specific dosages and timings.
- **Automated Alerts:** High-priority notifications and alarms to ensure no dose is missed.
- **Inventory Tracking:** Real-time monitoring of medication supplies with expiration alerts.

### 🧠 Cognitive Training Hub
- **Online Memory Game:** Engage in 1-on-1 matches or solo practice to strengthen short-term memory and focus.
- **Math Problems Activity:** Daily arithmetic challenges designed to maintain mental agility and logical thinking.
- **Progress Tracking:** Monitor game scores and cognitive activity over time.

### 🤖 AI Health Companion
- **Intelligent Q&A:** An AI assistant powered by Google Gemini (Firebase AI) to answer general health and wellness questions in simple, accessible language.
- **Accessibility First:** Designed to provide clear, jargon-free information.
- *Note: Not a substitute for professional medical advice.*

### 👥 Social Community & Support
- **Moderated Forums:** Discussion boards categorized by topics for sharing experiences and advice.
- **Peer Connection:** Foster a sense of community to combat social isolation.

### 💡 Daily Inspiration
- **Tip of the Day:** Receive daily motivational quotes and practical health tips to start the day positively.

---

## 🛠 Tech Stack & Architecture

The project is built using modern Android development best practices, ensuring scalability and maintainability.

- **Architecture:** MVVM (Model-View-ViewModel) with Repository Pattern for clean separation of concerns.
- **Dependency Injection:** **Hilt** for robust and testable component management.
- **Backend Services:**
    - **Firebase Authentication:** Secure user sign-in and profile management.
    - **Firebase Realtime Database:** Low-latency data synchronization for games and forums.
    - **Firebase Storage:** Secure hosting for user profile images and medical documents.
    - **Firebase Cloud Messaging:** Reliable delivery of medication reminders.
- **AI Integration:** **Firebase Vertex AI (Gemini)** for the medical assistant module.
- **UI/UX:** Material Design 3, custom adapters for high-contrast accessibility, and responsive layouts.

---

## 📁 Project Structure

This project follows a modular package structure for organized development.

| Package                                                           | Purpose                                        |
|:------------------------------------------------------------------|:-----------------------------------------------|
| [`screens`](app/src/main/java/com/example/sagivproject/screens)   | Activities, Fragments, and UI logic.           |
| [`services`](app/src/main/java/com/example/sagivproject/services) | Business logic interfaces and implementations. |
| [`models`](app/src/main/java/com/example/sagivproject/models)     | Data entities and Enums.                       |
| [`adapters`](app/src/main/java/com/example/sagivproject/adapters) | RecyclerView adapters for lists and grids.     |
| [`di`](app/src/main/java/com/example/sagivproject/di)             | Dependency Injection modules (Hilt).           |
| [`utils`](app/src/main/java/com/example/sagivproject/utils)       | Static helpers for validation, UI, and dates.  |

---

## ♿ Accessibility Standards
- **High Contrast:** UI elements optimized for visual clarity.
- **Large Typography:** Default font sizes optimized for elderly users.
- **Intuitive Navigation:** Minimalist screen layouts to reduce cognitive load.

---

## ⚙️ Getting Started

### Prerequisites
- Android Studio Hedgehog (or newer)
- JDK 17
- Android SDK 34 (API Level 34)

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
