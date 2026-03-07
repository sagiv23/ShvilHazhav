# Services

> Interfaces and specialized classes defining the business logic, data access, and orchestration
> layers.

---

### Core Business Services

| Service                   | Purpose                                           |
|---------------------------|---------------------------------------------------|
| `IAuthService`            | User authentication and session management        |
| `IDatabaseService`        | Core Firebase Realtime Database interactions      |
| `IUserService`            | User profile data management                      |
| `IForumService`           | Forum message posting and retrieval               |
| `IForumCategoriesService` | Management of forum topic categories              |
| `IMemoryGameService`      | Memory game logic and session handling            |
| `IMedicationService`      | Medication scheduling and tracking                |
| `IImageService`           | Cloud storage and image processing logic          |
| `IStatsService`           | User performance statistics and leaderboard logic |
| `ITipOfTheDayService`     | Fetching daily motivational content               |

---

### UI & Infrastructure Services

| Service           | Purpose                                                                        |
|-------------------|--------------------------------------------------------------------------------|
| `DialogService`   | Orchestrates the creation and display of all specialized Dialogs in the app.   |
| `AdapterService`  | Centralizes Hilt-injected RecyclerView adapters for easy Activity integration. |
| `DatabaseService` | Implementation-agnostic database access helper (see `IDatabaseService`).       |
