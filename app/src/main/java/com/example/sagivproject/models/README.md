# Models

> Data objects representing the application's domain entities.

---

| Model                | Purpose                                                                        |
|----------------------|--------------------------------------------------------------------------------|
| `Card`               | State of a single card in the memory game (ID, image, status).                 |
| `DailyStats`         | Aggregated statistics for a user on a specific day (Game wins, med adherence). |
| `EmergencyContact`   | Personal details and phone number for emergency notifications.                 |
| `ForumCategory`      | Metadata for grouping forum discussions by topic.                              |
| `ForumMessage`       | A single post in the forum with sender info and timestamp.                     |
| `GameRoom`           | Shared real-time state for an online memory game session.                      |
| `GraphData`          | Configuration and points for rendering statistical XY graphs.                  |
| `Idable`             | Interface ensuring models have a unique string identifier.                     |
| `ImageData`          | Generic wrapper for Base64 image content.                                      |
| `Medication`         | Details of a user's medication schedule and reminders.                         |
| `MedicationUsage`    | Log entry tracking the intake status of a specific medication dose.            |
| `MemoryGameDayStats` | Game-specific metrics like correct/wrong matches per day.                      |
| `TipOfTheDay`        | Daily motivational or health advice (Static or AI generated).                  |
| `User`               | Central profile model containing all user-related data and sub-maps.           |
