# Dialogs

> Reusable popup components implemented as `DialogFragment` for robust lifecycle management and
> consistent user interaction.

---

| Dialog                      | Description                                                                 |
|-----------------------------|-----------------------------------------------------------------------------|
| `AddEmergencyContactDialog` | Form for adding or editing emergency contacts with validation.              |
| `AddUserDialog`             | Form for administrators to create new user accounts.                        |
| `ConfirmDialog`             | Standardized confirmation or alert prompt with callback support.            |
| `EditForumCategoryDialog`   | Interface for renaming existing forum discussion topics.                    |
| `EditUserDialog`            | Form for updating personal profile details and credentials.                 |
| `FullImageDialog`           | Immersive full-screen viewer for profile pictures and game assets.          |
| `MedicationDialog`          | Advanced form for managing medication details and multiple reminder chips.  |
| `ProfileImageDialog`        | UI for selecting image sources (Camera/Gallery) or removing profile photos. |

### Key Features

- **Decoupled Logic**: Dialogs communicate results via listener interfaces, ensuring they remain
  independent of host activity implementation.
- **Hilt Integration**: Fully supported by dependency injection for utility classes like `Validator`
  and `CalendarUtil`.
- **Material Design**: Uses modern components such as `ChipGroup` and themed `MaterialButton`.
- **Validation**: Built-in real-time validation logic to ensure data integrity before submission.
