# Grama-Suvidha: Rural Infrastructure Tracker

Grama-Suvidha is a transparency-focused Android application designed to track the progress of government-funded projects in rural areas. It empowers villagers to monitor local development works, provide feedback, and report issues.

## 🚀 Key Features
- **Project Tracking:** View a list of ongoing village projects with real-time progress updates.
- **Before/After Visualization:** Compare project site photos to see tangible development.
- **Citizen Feedback:** Rate projects and share feedback directly through the app.
- **Issue Reporting:** Report construction quality or delay issues to the Panchayat.
- **Bilingual Support:** Full UI support for both **English** and **Kannada**.
- **Offline Access:** View cached project data even without an internet connection.

## 🛠 Technical Stack
- **Architecture:** MVVM (Model-View-ViewModel)
- **Language:** Kotlin
- **Database:** Room (for local persistence and offline caching)
- **UI Framework:** Material Design 3 with XML Layouts
- **Image Loading:** Coil
- **Reactive Updates:** LiveData and Kotlin Flows

## 📊 Mock API Documentation
The app simulates a backend API by parsing a local JSON file (`assets/projects.json`) and migrating it to a Room database on the first launch.

### Project Data Schema
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | Integer | Unique identifier for the project. |
| `nameEn` / `nameKn` | String | Project title in English and Kannada. |
| `descriptionEn` / `descriptionKn` | String | Detailed objectives and scope. |
| `locationEn` / `locationKn` | String | Geographic area/ward of the project. |
| `statusEn` / `statusKn` | String | Current state (Pending, In Progress, Completed). |
| `progress` | Integer | Percentage completion (0-100). |
| `budget` | String | Allocated funds (formatted). |
| `expectedCompletion` | String | Target date for completion (YYYY-MM-DD). |
| `imageUrlBefore` | String | Local asset path for the site "Before" photo. |
| `imageUrlAfter` | String | Local asset path for the site "Current/After" photo. |
| `rating` | Float | Aggregated citizen rating (1-5 stars). |

## 📖 How to Run
1. Open the project in **Android Studio (Ladybug or newer)**.
2. Sync the project with Gradle files.
3. Run the app on an emulator or physical device.
4. **To Test Simulation:** Go to a project's detail page and click the **"Simulate Progress"** button to see LiveData update the UI in real-time.
