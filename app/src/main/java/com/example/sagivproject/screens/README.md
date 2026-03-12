# Screens

> Fragments that handle user interaction and screen navigation within the Single-Activity
> architecture.

---

| Fragment                                        | Description                                                  |
|-------------------------------------------------|--------------------------------------------------------------|
| `SplashFragment`                                | Initial launch screen with branding and authentication check |
| `LoginFragment` / `RegisterFragment`            | User authentication and account creation screens             |
| `HomeFragment` / `LandingFragment`              | Main navigation hubs for the application                     |
| `ForumFragment` / `ForumCategoriesFragment`     | Discussion forums and category browsing                      |
| `MemoryGameFragment` / `GameHomeScreenFragment` | Core game logic and memory game selection                    |
| `MedicationListFragment`                        | Medication tracking and management UI                        |
| `AdminPageFragment` / `UsersTableFragment`      | Administrative dashboards for managing content and users     |
| `SettingsFragment` / `DetailsAboutUserFragment` | User preferences and profile management                      |
| `UserStatsFragment`                             | Detailed statistics and progress tracking for a user         |
| `TipOfTheDayFragment`                           | Displays daily motivational tips                             |
| `AiFragment`                                    | AI interaction features (Gemini)                             |
| `MathProblemsFragment`                          | Mental stimulation through math challenges                   |
| `ContactFragment`                               | Support and contact information                              |
| `AdminForumFragment`                            | Admin view for forum moderation                              |
| `AdminForumCategoriesFragment`                  | Admin view for managing forum categories                     |
| `MemoryGameLogsTableFragment`                   | Admin/User view for reviewing game history                   |
| `MedicationImagesTableFragment`                 | View for managing medication-related image logs              |
| `SecretFragment`                                | An easter-egg or hidden developer activity                   |

---

### UI Components

- **Main Components**: Fragments and Navigation Components.
- **Dialogs**: See the [dialogs](./dialogs/README.md) directory for popup interactions (implemented
  as `DialogFragment`).
