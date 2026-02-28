# Notifications

> Services and receivers responsible for scheduling and delivering local notifications.

---

| Class                 | Type              | Purpose                                                                  |
|-----------------------|-------------------|--------------------------------------------------------------------------|
| `NotificationService` | Service           | Helper for creating and showing notification channels and alerts         |
| `AlarmScheduler`      | Utility           | Handles the precise scheduling of alarms via `AlarmManager`              |
| `AlarmReceiver`       | BroadcastReceiver | Triggers when a scheduled alarm goes off to fire a notification          |
| `BirthdayWorker`      | Worker            | Periodic background task for checking and notifying about user birthdays |
