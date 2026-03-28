# Services

> Interfaces and specialized classes defining the business logic, data access, and infrastructure
> layers.

---

### Core Business Services

| Service                   | Purpose                                                                           |
|---------------------------|-----------------------------------------------------------------------------------|
| `IAuthService`            | Handles user authentication, registration, and administrative account management. |
| `IDatabaseService`        | A central façade providing a single entry point to all domain-specific services.  |
| `IUserService`            | Manages user profile data, credentials validation, and role updates.              |
| `IForumService`           | Provides real-time forum messaging, synchronization, and moderation tools.        |
| `IForumCategoriesService` | Manages the collection of forum discussion topics and data integrity.             |
| `IMemoryGameService`      | Coordinates online multiplayer sessions, matchmaking, and game state sync.        |
| `IMedicationService`      | Manages medication schedules, prescriptions, and historical intake logging.       |
| `IImageService`           | Handles the repository of image assets used for game content.                     |
| `IStatsService`           | Tracks and updates daily performance metrics across application modules.          |
| `ITipOfTheDayService`     | Manages the persistence and AI-generation of daily health/motivational advice.    |
| `IEmergencyService`       | Manages emergency contacts and coordinates automated SMS alerting systems.        |
| `IFallDetectionService`   | Provides background monitoring of device movement to detect and report falls.     |

---

### UI & Infrastructure Services

| Service           | Purpose                                                                          |
|-------------------|----------------------------------------------------------------------------------|
| `DialogService`   | Orchestrates the lazy instantiation and display of all specialized UI Dialogs.   |
| `AdapterService`  | Provides activity-scoped access to pre-configured RecyclerView adapters.         |
| `DatabaseService` | Singleton implementation of the central service façade (see `IDatabaseService`). |
