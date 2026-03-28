# Bases

> Base classes that provide common functionality and infrastructure across the application.

---

| Class                 | Type           | Purpose                                                                           |
|-----------------------|----------------|-----------------------------------------------------------------------------------|
| `BaseActivity`        | Abstract Class | Manages common UI components like the navigation drawer, top bar, and Hilt setup. |
| `BaseAdapter`         | Abstract Class | Generic base for RecyclerView adapters using `DiffUtil` for optimized updates.    |
| `BaseDatabaseService` | Abstract Class | Provides CRUD operations and transaction handling for Firebase Realtime Database. |
