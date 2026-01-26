# Changelog

## v1.5.0 (2026-01-26)

### 🚀 New Features & Enhancements
- **Refined User Interface**: Redesigned the Home Screen with a modern card-style layout to improve visual hierarchy and accessibility.
- **Battery Optimization Intelligence**: Implemented a dynamic warning system in Settings that detects active battery optimizations and guides users to exempt the app, ensuring reliable reminder delivery.
- **Enhanced Localization Support**: Added comprehensive English and Arabic translations for all new system warnings and instructional notes.
- **Transparent User Guidance**: Introduced persistent UI notes clarifying reminder interval behaviors during device restarts and updates.
- **Community Integration**: Updated the About section to feature direct access to the official Telegram channel and source code repository.

### 🛠 Fixes & Improvements
- **Navigation Stability**: Resolved a critical issue where changing the application language triggered an unintended navigation reset.
- **UI Consistency**: Fixed layout clipping issues on smaller screens and standardized button styling.
- **System Compliance**: Updated status bar color handling to align with modern Android API standards, suppressing legacy deprecation warnings.


## v1.0.0 (2026-01-26)

### Features
- Configure release signing and ABI splits
- Scale down app icon foreground to prevent cropping
- Update Arabic app name to 'ذكرُ الشفيع'
- Localize notification content for all supported languages
- Enhance About page and Home screen UI
- Implement in-app update checking with UI feedback
- Replace volume icon with a new PBUH image in the home screen
- Add system language auto-detection and localize updates section
- Add update checking functionality to the settings screen
- Update app launcher icons and other image assets
- Configure dynamic splash screen background with dark/light mode support
- Update notification small icon and add new image assets
- App settings screen with Material 3 design and header customization
- Implement complete ShafeeZekr reminder app functionality

### Bug Fixes
- Update reminder icon to use Material primary color tint for better visibility
- Fix status bar visibility in light mode
- Localize home screen title and update About app name
- Fix language selection not applying properly
- Resolve lint errors (missing translations, non-translatables)
- Standardize settings icons tint and section header colors
- Fix GitHub username capitalization in About screen
- Fix Foreground service crash by calling startForeground immediately
- Multiple critical fixes for audio, reminders, and UI stability
