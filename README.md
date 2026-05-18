# Финансы / FinanceApp

## Русский

**Финансы** — локальное Android-приложение для учета личных финансов. Проект создан как дипломное приложение: данные хранятся на устройстве, банковские API не используются.

### Возможности

- Добавление и редактирование операций.
- Категоризация трат по предустановленным категориям и ключевым словам.
- Аналитика расходов за выбранный месяц.
- Круговая диаграмма с суммами и процентами по категориям.
- Импорт PDF-выписок Т-Банка и Сбера.
- Защита от повторного импорта одинаковых операций.
- Экспорт операций в CSV.
- Недельный бюджет и уведомления при превышении.
- Ежедневные напоминания о внесении трат.

### Архитектура

Проект использует Clean Architecture + MVVM + Repository:

- `ui` — Jetpack Compose экраны, ViewModel, UI state.
- `domain` — модели, интерфейсы репозиториев, use cases.
- `data` — Room, DataStore, PDF-парсинг, реализации репозиториев.
- `di` — Koin-модуль приложения.

### Технологии

- Kotlin
- Jetpack Compose + Material 3
- Room
- DataStore
- Coroutines + Flow
- Koin
- WorkManager
- Vico
- PdfBox-Android

### Сборка

Откройте проект в Android Studio и выполните:

```powershell
.\gradlew.bat :app:assembleDebug
```

Тесты:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

### Импорт выписок

Импорт работает локально: пользователь выбирает PDF-файл через системный picker, приложение извлекает текст из PDF, определяет банк, парсит операции и сохраняет их в Room. Повторный импорт той же выписки пропускает дубликаты.

Поддерживаются:

- Т-Банк PDF-выписка.
- Сбер PDF-выписка по счету дебетовой карты.

## English

**FinanceApp** is a local Android personal finance tracker. It is designed as a graduation project: all data is stored on the device, and no banking APIs are used.

### Features

- Add and edit transactions.
- Categorize expenses using predefined categories and keyword rules.
- Monthly spending analytics.
- Pie chart with category amounts and percentages.
- Import PDF statements from T-Bank and Sber.
- Duplicate protection for repeated imports.
- Export transactions to CSV.
- Weekly budget tracking with notifications.
- Daily reminders to record expenses.

### Architecture

The project follows Clean Architecture + MVVM + Repository:

- `ui` — Jetpack Compose screens, ViewModel, UI state.
- `domain` — models, repository interfaces, use cases.
- `data` — Room, DataStore, PDF parsing, repository implementations.
- `di` — Koin application module.

### Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- Room
- DataStore
- Coroutines + Flow
- Koin
- WorkManager
- Vico
- PdfBox-Android

### Build

Open the project in Android Studio and run:

```powershell
.\gradlew.bat :app:assembleDebug
```

Tests:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

### Statement Import

The import flow runs locally: the user selects a PDF file with the system picker, the app extracts text from the PDF, detects the bank, parses transactions, and stores them in Room. Re-importing the same statement skips duplicates.

Supported formats:

- T-Bank PDF statement.
- Sber debit card account PDF statement.
