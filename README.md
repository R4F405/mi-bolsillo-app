# Mi Bolsillo App

**Mi Bolsillo App** is an Android mobile application that allows users to easily and visually track their personal finances by recording income and expenses, categorizing them, and viewing their monthly balance.

---

## Key Features (Current MVP)

- **Quick income and expense entry:** Add transactions specifying amount, date, category, and optional description.
- **Chronological transaction list:** View your recent transactions and access the full history.
- **Edit and delete transactions:** Modify or remove any recorded transaction.
- **Flexible categorization:** Use predefined categories (Food, Transport, Leisure, etc.) or create, edit, and delete your own custom categories, each with a distinctive icon and color.
- **Monthly visualization:** See a breakdown of expenses by category with a pie chart and access a summary of income, expenses, and monthly balance.
- **Modern, fluid interface:** Built with **Jetpack Compose**, following Material Design principles and a professional color palette.
- **Local data storage:** All data is securely stored on-device using **Room**.
- **Dependency injection:** Robust, scalable architecture powered by **Hilt**.
- **MVVM architecture:** Clear separation of concerns for maintainability and scalability.

---

## User Experience & Visual Style

- **Color palette:** Deep blue, black accent, light backgrounds, and distinct colors for income and expenses.
- **Typography:** Roboto in various weights and sizes for clear visual hierarchy.
- **Iconography:** Material Symbols (Rounded), with custom icons for categories.
- **Reusable components:** Consistent buttons, text fields, and visual elements throughout the app.

---

## User Flows

- **Register transaction:** From the dashboard or history, tap "+" and fill in the details. Immediate confirmation and fields ready for new entries.
- **Dashboard:** Direct access to balance, expense breakdown, recent transactions, and month navigation.
- **History:** View, filter, and edit all your transactions. Filter by type and category.
- **Category management:** Easily add, edit, or delete custom categories.

---

## Technologies & Architecture

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Persistence:** Room
- **Dependency injection:** Hilt
- **Architecture:** MVVM (Model-View-ViewModel)
- **State management:** StateFlow
- **Compatibility:** Android 6.0 (API 23) or higher

---

## Project Structure

- `data/`: Data access and management (Room, DAOs, entities, repositories)
- `ui/`: Screens, components, and ViewModels organized by feature
- `di/`: Dependency injection modules (Hilt)
- `utils/`: Utilities and helpers

---

## Planned Features (Upcoming Versions)

- **Monthly budgets by category:** Set limits and track your progress.
- **Cloud sync / Backup:** Keep your data safe and available across devices.
- **Data export:** Download your transactions as CSV or PDF.
- **Smart notifications:** Alerts when you exceed budgets or for other relevant events.
- **Bank API integration:** Automatically import transactions from your bank.
- **Advanced customization:** Themes, widgets, and visual settings to tailor the app to your style.

---

## Current Status

The app implements all features described in the MVP. Advanced features (budgets, sync, export, notifications, banking integration, advanced customization) are planned and in the design phase, but not yet present in the codebase.

---

## Installation & Contribution

1. Clone the repository and open the project in Android Studio.
2. Build and run on an Android device or emulator.
3. Contributions are welcome! See the internal documentation for details on architecture and user flows.