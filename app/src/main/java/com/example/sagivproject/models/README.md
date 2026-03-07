# Models

> Data objects representing the core business entities.

---

| Model                | Description                                          |
|----------------------|------------------------------------------------------|
| `User`               | Application user profile and metadata                |
| `Card`               | Game card used in the memory match game              |
| `ForumMessage`       | Message content for the forum                        |
| `ForumCategory`      | Category definition for forum posts                  |
| `Medication`         | User medication entry with schedule and details      |
| `MedicationUsage`    | Tracking record for when a medication was taken      |
| `ImageData`          | Reference to image storage and metadata              |
| `GameRoom`           | Session data for a multiplayer or logged game        |
| `TipOfTheDay`        | Daily tip content and its identifier                 |
| `DailyStats`         | Combined daily statistics for various activities     |
| `MemoryGameDayStats` | Specific statistics for memory game sessions per day |
| `Idable`             | Interface for objects that have a unique ID          |
