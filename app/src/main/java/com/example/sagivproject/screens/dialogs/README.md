# Dialogs

> Reusable popup components implemented as `DialogFragment` for robust lifecycle management and
> consistent user interaction. All dialogs inherit from `BaseDialog` for standardized UI setup.

---

| Dialog                      | Description                                                                                 |
|-----------------------------|---------------------------------------------------------------------------------------------|
| `BaseDialog`                | Abstract base class handling common boilerplate (transparent background, layout inflation). |
| `AddEmergencyContactDialog` | Form for adding or editing emergency contacts with validation.                              |
| `UserDialog`                | Unified form for creating new user accounts or updating existing profiles.                  |
| `ConfirmDialog`             | Standardized confirmation or alert prompt with callback support.                            |
| `SingleInputDialog`         | Generic text input dialog used for actions like renaming forum categories.                  |
| `ImageActionDialog`         | Unified viewer for full-screen images and image source actions (Camera/Gallery/Delete).     |
| `MedicationDialog`          | Advanced form for managing medication details and multiple reminder chips.                  |

### Key Features

- **Consolidated Architecture**: Generic dialogs like `SingleInputDialog` and `ImageActionDialog`
  reduce code duplication.
- **Decoupled Logic**: Dialogs communicate results via listener interfaces, ensuring they remain
  independent of host activity implementation.
- **Hilt Integration**: Fully supported by dependency injection for utility classes like `Validator`
  and `CalendarUtil`.
- **Material Design**: Uses modern components such as `ChipGroup` and themed `MaterialButton`.
- **Validation**: Built-in real-time validation logic to ensure data integrity before submission.
