# Shvil Hazhav (The Golden Path)

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.java.com)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-ffca28.svg)](https://firebase.google.com)
[![Hilt](https://img.shields.io/badge/DI-Hilt-blue.svg)](https://developer.android.com)
[![Material 3](https://img.shields.io/badge/UI-Material--3-7b4397.svg)](https://m3.material.io)

**Shvil Hazhav** is a high-impact, accessibility-driven Android application engineered specifically for the elderly (60+) and individuals with physical or cognitive challenges. The project bridges the gap between modern mobile technology and senior needs by providing a unified ecosystem for health monitoring, cognitive maintenance, and emergency response.

The core philosophy of the application is **"Safety through Autonomy"**—empowering users to manage their medical routines and stay mentally active while maintaining a robust safety net through intelligent sensors and community support.

---

## 🚀 Key Features

### 🚑 Safety & Emergency Infrastructure
- **Real-time Fall Detection:** A background service utilizing the device's high-frequency accelerometer to detect abrupt velocity changes. It implements a custom G-force threshold algorithm to minimize false positives.
- **SOS Broadcast System:** One-tap emergency triggers that broadcast SMS alerts containing high-accuracy GPS coordinates (via Google Maps integration) to a prioritized contact list.
- **Background Persistence:** Utilizes Foreground Services to ensure life-saving monitoring remains active even under system memory pressure.

### 💊 Medication Management & Compliance
- **Smart Prescriptions:** Granular control over medication regimens, allowing categorization by physical form (Pills, Drops, Injections) and complex multi-dose daily schedules.
- **Critical Alarms:** Leverages the Android `AlarmManager` with **Exact Alarms** API to bypass battery-saving restrictions (Doze mode) for medical reminders.
- **Zero-Launch Interaction:** Full integration with Notification Actions, allowing users to log intake status directly from the lock screen without opening the app.
- **Historical Analysis:** Atomic logging system that generates visual compliance reports, helping families and doctors monitor treatment adherence.

### 🧠 Cognitive Training & Analytics
- **Multiplayer Memory Game:** Real-time 1-on-1 multiplayer sessions synchronized via Firebase, featuring 3D flip animations and matchmaking to stimulate short-term recall.
- **Mental Arithmetic Engine:** A randomized problem generator covering six different mathematical operations to maintain logical processing speeds and mental agility.
- **Progress Visualization:** Custom XY Graph components with **Linear Regression** trend lines to provide users with visual proof of their cognitive improvements over time.

### 🤖 AI Health Companion
- **Intelligent Assistant:** Fully integrated with **Google Gemini 2.5 Flash Lite** via the **Firebase Vertex AI SDK**.
- **Natural Language Processing:** Optimized to handle health-related queries and provide simplified, actionable wellness advice.
- **Accessibility Layers:** All AI responses feature a typewriter animation for improved readability and full **Text-to-Speech (TTS)** support for visually impaired users.

### 👥 Community & Inspiration
- **Moderated Discussion Boards:** Categorized forums for peer-to-peer support, managed by administrative tools to ensure a safe and positive environment.
- **Daily Inspiration:** An automated system that generates unique daily health tips and inspirational quotes, synchronized globally for all users.

---

## 🛠 Tech Stack & Architecture

### Modern Android Development
- **Single-Activity Architecture:** Maximizes performance and simplifies navigation state management.
- **MVVM Pattern:** Strict separation of concerns between UI, Business Logic, and Data.
- **Dependency Injection:** **Dagger Hilt** for compile-time safe DI and modular service management across the app.

### Core Technologies
- **Backend (Firebase):**
    - **Realtime Database:** Low-latency NoSQL storage for live game state, forum data, and user profiles.
    - **Atomic Transactions:** Ensures statistics and usage logs are updated concurrently without data loss.
    - **Vertex AI:** Implementation of Google's latest LLM models for intelligent conversational features.
- **System Services:**
    - **WorkManager:** Reliable periodic background tasks for system maintenance and daily checks.
    - **GMS Location:** High-accuracy location retrieval for emergency alerting.
    - **Sensor Framework:** Real-time accelerometer processing for fall detection.
- **UI & Presentation:**
    - **Material 3 (M3):** Implementation of Google's latest design language with dynamic colors and accessible components.
    - **Custom Views:** Pixel-perfect rendering of complex statistical data and interactive graphs.
    - **ViewPager2 & TabLayout:** Fluid navigation within feature modules.

---

## 📂 Project Structure

| Package                                                                              | Responsibility                                                                  |
|:-------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------|
| [`screens`](app/src/main/java/com/example/sagivproject/screens)                      | Feature-specific Activities and high-level UI controllers.                      |
| [`dialogs`](app/src/main/java/com/example/sagivproject/screens/dialogs)              | Reusable UI popups for specialized user inputs and confirmations.               |
| [`services`](app/src/main/java/com/example/sagivproject/services)                    | The business logic layer; interfaces defining the logic façade.                 |
| [`impl`](app/src/main/java/com/example/sagivproject/services/impl)                   | Concrete implementations involving Firebase, Sensors, and AI orchestration.     |
| [`notifications`](app/src/main/java/com/example/sagivproject/services/notifications) | High-precision alarm scheduling and complex notification construction.          |
| [`models`](app/src/main/java/com/example/sagivproject/models)                        | Domain entities, structured data objects, and system Enums.                     |
| [`adapters`](app/src/main/java/com/example/sagivproject/adapters)                    | Optimized data-to-view binding logic using the **DiffUtil** algorithm.          |
| [`ui`](app/src/main/java/com/example/sagivproject/ui)                                | Reusable custom views, 3D animations, and centralized menu fragments.           |
| [`bases`](app/src/main/java/com/example/sagivproject/bases)                          | Abstract base classes providing standardized infrastructure for the entire app. |
| [`di`](app/src/main/java/com/example/sagivproject/di)                                | Hilt modules providing singleton and activity-scoped dependencies.              |
| [`utils`](app/src/main/java/com/example/sagivproject/utils)                          | Static helper utilities for validation, image processing, and date math.        |

---

## 🐍 Maintenance Scripts
The project includes Python utility scripts to automate development tasks:
- `export_to_word.py`: Compiles the entire Java source code into a formatted Word document for reviews or academic submission.
- `delete_comments.py`: Safely strips comments and excessive whitespace from source files for production builds or distribution.

---

## ⚙️ Getting Started

### Prerequisites
- **Android Studio Ladybug** (or newer)
- **JDK 17**
- **Firebase:** Add your unique `google-services.json` to the `app/` directory.

### Build Instructions
1. Clone the repository.
2. Sync Project with Gradle Files.
3. Run on a physical device (highly recommended for testing sensor-based Fall Detection).

---

## 👨‍💻 Developer
**Sagiv**  
*Computer Science Final Project - Shvil Hazhav*
