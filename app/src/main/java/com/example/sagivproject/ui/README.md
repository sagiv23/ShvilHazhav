# UI Components

> Reusable UI components, navigation menu fragments, and specialized display helpers used across the
> application.

---

| Component                | Purpose                                                                                                |
|--------------------------|--------------------------------------------------------------------------------------------------------|
| `AdminMenuFragment`      | Top bar menu providing navigation back to the administrator dashboard.                                 |
| `CustomTypefaceSpan`     | Implementation of `TypefaceSpan` used to apply custom Hebrew fonts to `Spannable` strings.             |
| `LoggedInMenuFragment`   | Sidebar menu for authenticated regular users, providing access to profiles, contacts, and settings.    |
| `LoggedOutMenuFragment`  | Sidebar menu for guests, providing links to login, registration, and general information.              |
| `MenuNavigationListener` | Core interface for facilitating navigation between fragments and activities via the host base classes. |
| `SimpleXYGraphView`      | Custom `View` for rendering scrollable, scaled XY graphs with linear regression trend line support.    |
