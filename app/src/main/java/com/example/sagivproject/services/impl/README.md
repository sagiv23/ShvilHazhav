# Service Implementations

> Concrete implementations of the service interfaces, handling the actual data logic.

---

| Implementation               | Interface                 | Responsibility                        |
|------------------------------|---------------------------|---------------------------------------|
| `AuthServiceImpl`            | `IAuthService`            | Firebase Authentication logic         |
| `GameServiceImpl`            | `IGameService`            | Memory game state and logic           |
| `UserServiceImpl`            | `IUserService`            | User profile updates in Firebase      |
| `ForumServiceImpl`           | `IForumService`           | Forum message CRUD operations         |
| `ImageServiceImpl`           | `IImageService`           | Firebase Storage image uploads        |
| `StatsServiceImpl`           | `IStatsService`           | Calculating scores and leaderboard    |
| `MedicationServiceImpl`      | `IMedicationService`      | Medication data management            |
| `TipOfTheDayServiceImpl`     | `ITipOfTheDayService`     | Random tip selection logic            |
| `ForumCategoriesServiceImpl` | `IForumCategoriesService` | Management of forum topics            |
| `BaseDatabaseService`        | --                        | Shared base logic for database access |
