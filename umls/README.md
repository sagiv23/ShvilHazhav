# Documentation (UML Diagrams)

> Visual architectural documentation of the **Shvil Hazhav** project, generated using PlantUML.

---

## 🏗 System Overview

The diagrams in this directory provide a comprehensive view of the application's structure,
following the MVVM and Service-Oriented architecture.

### 🧩 Core Modules

| Diagram Category    | Files                                    | Description                                                 |
|:--------------------|:-----------------------------------------|:------------------------------------------------------------|
| **Services**        | `services1-5.puml`                       | Business logic interfaces and method signatures.            |
| **Implementations** | `impl1-6.puml`                           | Concrete logic, Firebase integration, and AI orchestration. |
| **Screens**         | `screens1-6.puml`                        | Activity structures and UI controller logic.                |
| **Models**          | `models1-3.puml`                         | Domain entities and data relationships.                     |
| **Adapters**        | `adapters1-4.puml`                       | Data-to-view binding logic for complex lists.               |
| **Dialogs**         | `dialogs1-2.puml`                        | Specialized UI popup structures.                            |
| **Infrastructure**  | `bases1-2.puml`, `di.puml`, `utils.puml` | Base classes, DI modules, and helper utilities.             |
| **System**          | `notifications.puml`, `ui.puml`          | System services and custom UI components.                   |

---

## 🛠 Usage

To view these diagrams:

1. Install the **PlantUML** plugin in Android Studio or VS Code.
2. Open any `.puml` file.
3. The plugin will render the class diagrams and relationships automatically.

*Note: These diagrams are kept in sync with the source code to reflect the current state of the
application.*
