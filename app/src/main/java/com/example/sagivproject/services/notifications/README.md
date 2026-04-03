# Notifications

> Services, workers, and receivers responsible for scheduling and delivering local reminders and
> system alerts.

---

| Class                      | Type              | Purpose                                                                           |
|----------------------------|-------------------|-----------------------------------------------------------------------------------|
| `AlarmReceiver`            | BroadcastReceiver | Processes triggered system alarms to fire notifications and reschedule events.    |
| `AlarmScheduler`           | Utility           | Manages high-precision daily scheduling of alarms via the Android `AlarmManager`. |
| `BootReceiver`             | BroadcastReceiver | Restores and reschedules all active medication reminders upon device restart.     |
| `MedicationActionReceiver` | BroadcastReceiver | Handles direct user responses from notifications (Taken/Snoozed) via Intents.     |
| `NotificationService`      | Service           | Centralizes channel management, group summary logic, and building alerts.         |

## Key Features

- **Direct Interaction**: Supports `MedicationActionReceiver` for logging intake without opening the
  app.
- **Persistence**: `BootReceiver` ensures alarms survive device reboots.
- **Reliability**: Uses exact alarms to ensure critical medical alerts arrive on time.
- **Organization**: Implements notification grouping and summary channels to avoid cluttering the
  system tray.
