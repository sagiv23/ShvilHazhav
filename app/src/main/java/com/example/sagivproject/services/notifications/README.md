# Notifications

> Services, workers, and receivers responsible for scheduling and delivering local notifications.

---

| Class                 | Type              | Purpose                                                                     |
|-----------------------|-------------------|-----------------------------------------------------------------------------|
| `NotificationService` | Service           | Helper for managing notification channels, groups, and displaying alerts.   |
| `AlarmScheduler`      | Utility           | Handles precise daily scheduling of alarms via `AlarmManager`.              |
| `AlarmReceiver`       | BroadcastReceiver | Triggers when a scheduled medication alarm goes off to fire a notification. |
| `DailyCheckWorker`    | Worker            | Periodic background task (every 24h) for daily checks like birthdays.       |

## Key Features

- **Notification Grouping**: Notifications are grouped by type (Medications, Birthdays) to avoid
  cluttering the notification shade.
- **Exact Alarms**: Uses `setExactAndAllowWhileIdle` to ensure medical reminders arrive on time even
  in Doze mode.
- **Modern Compatibility**: Fully compatible with Android 12+ (API 31+) using `IMMUTABLE` pending
  intents and notification channels.
- **Localization**: All notification content is managed via `strings.xml`.
