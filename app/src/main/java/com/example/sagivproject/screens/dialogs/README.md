# Dialogs

> Reusable popup components implemented as `DialogFragment` for robust lifecycle management and
> consistent user interaction.

---

| Dialog                    | Description                                         | Implementation   |
|---------------------------|-----------------------------------------------------|------------------|
| `AddUserDialog`           | Form to create a new user account (Admin only)      | `DialogFragment` |
| `EditUserDialog`          | Form to update existing user information            | `DialogFragment` |
| `ConfirmDialog`           | General confirmation prompt for destructive actions | `DialogFragment` |
| `MedicationDialog`        | Entry form for adding or editing medication details | `DialogFragment` |
| `FullImageDialog`         | Full-screen immersive image preview                 | `DialogFragment` |
| `ProfileImageDialog`      | UI for selecting, changing, or deleting photos      | `DialogFragment` |
| `EditForumCategoryDialog` | Management of forum categories (Admin only)         | `DialogFragment` |

### Key Features

- **Lifecycle Awareness**: Automatically handles screen rotations and configuration changes.
- **Dependency Injection**: Fully integrated with Hilt (`@AndroidEntryPoint`).
- **Data Persistence**: Uses `Arguments` (Bundle) to ensure data survives process death.
