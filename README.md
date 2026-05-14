# 🏘️ Grama-Suvidha: Digital Notice Board for Rural Development

**Grama-Suvidha** is a transparency-focused Android application that serves as a "Digital Notice Board" for tracking government-funded infrastructure projects in rural areas. It empowers citizens to monitor development, provide feedback, and hold local governance accountable.

---

## 🚀 Key Features


### 🏗️ Project Tracking
- View all ongoing village infrastructure projects
- Real-time progress updates with animated progress bars
- Before/After photo comparison for visual progress tracking
- Budget meter showing funds released vs total allocation

### 👤 User Management
- User registration and login with secure password hashing
- Session persistence across app launches
- User profile with activity statistics
- Edit profile functionality

### 💬 Citizen Engagement
- Star rating system for each project
- Community feedback wall with timestamped entries
- Issue reporting with category, priority, and file attachments
- Track previously reported issues

### 📤 Sharing & Export
- WhatsApp sharing (supports both WhatsApp & WhatsApp Business)
- Generic share chooser fallback
- PDF report generation saved to Downloads

### 🌍 Accessibility
- **Bilingual support**: English and Kannada (ಕನ್ನಡ)
- **Dark/Light mode** toggle
- Track projects by ID without requiring login

### 📋 Panchayat Directory
- Contact details for key Panchayat officials
- Quick-access directory for citizen communication

---

## 🛠️ Technical Stack

| Component | Technology |
|---|---|
| **Language** | Kotlin |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Database** | Room (SQLite with type-safe queries) |
| **Image Loading** | Coil |
| **Reactive Data** | LiveData + Kotlin Flows |
| **UI Framework** | Material Design 3 with XML Layouts |
| **PDF Generation** | Android PdfDocument API |
| **Build System** | Gradle with Version Catalog (TOML) |
| **Annotation Processing** | KSP (Kotlin Symbol Processing) |

---

## 📂 Project Structure

```
app/src/main/
├── assets/
│   ├── images/          # Before/After project photos (local)
│   └── projects.json    # Mock project data (seeded to Room DB)
├── java/.../grama_suvidha/
│   ├── Project.kt           # Data model (Room Entity)
│   ├── User.kt              # User model
│   ├── Issue.kt             # Issue/Grievance model
│   ├── FeedbackEntry.kt     # Feedback model
│   ├── AppDatabase.kt       # Room database singleton
│   ├── ProjectRepository.kt # Data layer with asset seeding
│   ├── ProjectViewModel.kt  # MVVM ViewModel
│   ├── WelcomeActivity.kt   # Entry point (auth + tracking)
│   ├── MainActivity.kt      # Project dashboard
│   ├── ProjectDetailsActivity.kt  # Full project view
│   ├── ReportIssueActivity.kt     # Grievance form
│   ├── PdfGenerator.kt      # PDF report utility
│   └── LocaleHelper.kt      # Language switching utility
└── res/
    ├── layout/          # 14 XML layouts
    ├── drawable/        # Custom vector icons & backgrounds
    ├── values/          # Strings, colors, themes (English)
    ├── values-kn/       # Kannada translations
    └── values-night/    # Dark mode theme
```

---

## 📊 Data Architecture

The app uses a **local-first architecture**:
1. On first launch, `projects.json` is parsed and seeded into a Room database.
2. All subsequent reads are from Room, enabling offline access.
3. User data (accounts, feedback, issues) is stored exclusively in Room.
4. Progress and rating updates are persisted to Room and reflected in real-time via LiveData.

---

## 📖 How to Run

1. Open the project in **Android Studio (Ladybug or newer)**.
2. Sync the project with Gradle files.
3. Run on an emulator (API 24+) or physical device.
4. **Register** a new account to access the full dashboard.
5. **Track without login**: Enter a project ID (1–4) on the Welcome screen.

---

## 🧪 Testing Features

| Feature | How to Test |
|---|---|
| **Live Progress** | Open any project → Click "Simulate Progress" |
| **PDF Export** | Open any project → Click download button |
| **WhatsApp Share** | Open any project → Click share button |
| **Language Switch** | Tap the language toggle (ಕನ್ನಡ/English) |
| **Dark Mode** | Tap the sun/moon icon in the toolbar |
| **Issue Reporting** | Open any project → Click "Report Issue" |

---

## 📄 License

This project is developed for educational and demonstration purposes.

---

*Built with ❤️ for rural India*
