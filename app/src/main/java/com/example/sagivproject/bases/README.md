# Base Classes

> Abstract base classes and application-level components providing standardized infrastructure and
> shared functionality across the application.

---

| Class             | Type                   | Responsibility                                                                 |
|-------------------|------------------------|--------------------------------------------------------------------------------|
| `BaseActivity`    | `AppCompatActivity`    | Handles common UI setup, orientation locking, and menu navigation integration. |
| `BaseAdapter`     | `RecyclerView.Adapter` | Boilerplate reduction for adapters, supporting consistent ViewHolder patterns. |
| `BaseDialog`      | `DialogFragment`       | Standardizes dialog lifecycle, layout inflation, and window styling.           |
| `MainApplication` | `Application`          | Entry point for Hilt dependency injection and global application state.        |

### Key Features

- **Boilerplate Reduction**: Centralizes repetitive code such as `setRequestedOrientation` and
  `setContentView`.
- **Consistent Navigation**: `BaseActivity` ensures the `AppMenuFragment` is handled uniformly
  across all screens.
- **Simplified Dialogs**: `BaseDialog` provides a structured way to initialize views and handle
  dialog arguments.
- **Hilt Foundation**: `MainApplication` is annotated with `@HiltAndroidApp` to trigger code
  generation.
