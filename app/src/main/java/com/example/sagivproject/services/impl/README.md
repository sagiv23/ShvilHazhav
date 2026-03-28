# Service Implementations

> Concrete implementations of the service interfaces, containing the core application logic and
> database integration.

---

| Implementation               | Interface                 | Responsibility                                                                   |
|------------------------------|---------------------------|----------------------------------------------------------------------------------|
| `AuthServiceImpl`            | `IAuthService`            | Manages user authentication flow, registration checks, and session persistence.  |
| `EmergencyServiceImpl`       | `IEmergencyService`       | Coordinates emergency contact storage and automated SMS alerting logic.          |
| `FallDetectionManager`       | `IFallDetectionService`   | Acts as a bridge to start/stop the foreground fall detection service.            |
| `FallDetectionServiceImpl`   | `Service`                 | Background service monitoring sensors and coordinating emergency responses.      |
| `ForumCategoriesServiceImpl` | `IForumCategoriesService` | Handles discussion topics and ensures cascading deletes of associated messages.  |
| `ForumServiceImpl`           | `IForumService`           | Manages real-time message broadcasting and persistence using Firebase listeners. |
| `ImageServiceImpl`           | `IImageService`           | Provides atomic batch updates and CRUD for game image assets.                    |
| `MedicationServiceImpl`      | `IMedicationService`      | Handles prescription management and atomic logging of daily intake events.       |
| `MemoryGameServiceImpl`      | `IMemoryGameService`      | Coordinates real-time multiplayer state, matchmaking, and automatic forfeits.    |
| `StatsServiceImpl`           | `IStatsService`           | Performs atomic increments of performance metrics for daily tracking.            |
| `TipOfTheDayServiceImpl`     | `ITipOfTheDayService`     | Ensures daily tips are synchronized globally using date-based transactions.      |
| `UserServiceImpl`            | `IUserService`            | Direct interface for user account CRUD and credential validation.                |
