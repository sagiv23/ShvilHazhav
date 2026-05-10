# Notifications

> Services and receivers responsible for scheduling and delivering local reminders and
> system alerts.

---

| Class                  | Type              | Purpose                                                                             |
|------------------------|-------------------|-------------------------------------------------------------------------------------|
| `NotificationReceiver` | BroadcastReceiver | Unified receiver for boot events, medication alarms, and user notification actions. |
| `NotificationService`  | Service           | Centralizes channel management, alarm scheduling, and building alerts.              |

## Key Features

- **Unified Handling**: `NotificationReceiver` centralizes all broadcast logic, reducing
  boilerplate.
- **Integrated Scheduling**: `NotificationService` now manages `AlarmManager` directly, simplifying
  the API.
- **Persistence**: Automatically restores alarms upon device reboot.
- **Interactive**: Supports direct logging (Taken/Snoozed) from the notification shade.
- **Organization**: Implements notification grouping and summary channels.
