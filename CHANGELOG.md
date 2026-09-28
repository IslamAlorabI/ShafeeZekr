# Changelog

## v2.3 (2026-09-28)

> This release also includes everything from v2.2, which was not published separately.

### New in v2.3
- New app logo, with a choice of Cream, Green, or Classic app icons in settings
- Splash screen now follows the in-app theme
- The big button and widget play one sound at a time and show playback progress
- More compact sound selection grid
- Ask for the "Alarms & reminders" permission so reminders arrive on time, also from the Quick Settings tile
- Fix the same sound repeating in shuffle mode
- Disable automatic cloud backup of app data

### New in v2.2
- Support Android 9 and newer (minimum SDK lowered to 28)
- Shuffle Sounds option that plays a random built-in sound each time
- Export and import statistics together with quiet-hours rules
- Celebration when the daily goal is reached
- More compact Today card on the statistics screen
- Smooth corners and haptic feedback on taps
- Playing indicator now stays in sync with the audio
- New notification icon
- About screen links show an error message when no app can open them

### Chores
- Update Android Gradle Plugin to 9.4.1
- Add unit tests for statistics, quiet hours, period rules, and backup
- Bump version code to 11 and version name to 2.3

## v2.0 (2026-05-21)

### New Features
- Add audio playback visualizer animation and circular countdown dial in HomeScreen
- Add support for custom audio import and voice recording in settings
- Add support for silent mode, DND muting options and other options to audio playback
- Add delete functionality and localized button strings to the recording dialog

### Improvements
- Replace OutlinedCard with filled Card components and update corner radii across UI screens
- Update SettingsScreen layout to use column-based arrangement for audio selection buttons
- Center-align action buttons in SettingsScreen layout
- Remove manual color overrides from AlertDialog components to use default theme colors
- Remove unnecessary top spacer from AboutScreen layout
- Remove unused OutlinedCard and OutlinedTextField imports from HomeScreen

### Chores
- Bump version code to 8 and version name to 2.0

## v1.8 (2026-02-13)

### New Features
- Add Salawat home screen widget with play sound on tap
- Add 1x1 icon widget that plays zikr sound
- Add Quick Settings tile to pause/resume Dhikr reminders
- Add GitHub Actions workflow for automated release builds

### Improvements
- Migrate widget to Jetpack Glance with Material 3 and Material You colors
- Widget follows app language with bigger content and compact layout
- Localize numbers to Arabic/Persian/Urdu and use 12-hour time format
- Localized number formatting for sound names in settings
- Optimize Gradle build with caching, parallel builds, and configuration cache
- Update Compose BOM to 2026.01.01
- Update Retrofit and Gson to 3.0.0

### Bug Fixes
- Fix icon cutting and splash screen by separating UI/Launcher icons with proper padding
- Fix FlowRow runtime crash (NoSuchMethodError)
- Fix Quiet Hours day selection chip layout
- Fix system language option and locale handling for notifications
- Fix ProGuard rules for Glance/WorkManager widget classes in release builds
- Fix deprecated edge-to-edge APIs flagged by Play Console
- Fix custom reminder interval applying before dialog confirmation

## v1.7 (2026-02-01)

### New Features
- Add sound selection feature with 8 dhikr sound options
- Add Quiet Hours with schedule types, Select All Days, and All Day options
- Add master volume control with Silent/Vibrate/DND protection
- Add independent app volume control in settings
- Add custom period times for reminder control with time picker UI
- Add timer presets (1 minute, 2 hours) with updated picker UI
- Add Orange, Pink, and Red theme colors
- Add privacy policy and Play Store documentation

### Improvements
- Rebuild period rules dialog with Material 3 pickers
- Enhanced custom interval dialog with user-friendly UI
- Replace Up/Down arrows with Minus/Plus icons
- Add localization for rule descriptions and timer numerals
- Warning note in sound selection dialog about immediate playback

### Bug Fixes
- Fix AlertDialog theme consistency
- Fix sound bypassing DND and Bedtime modes
- Fix sound selection showing all 8 sounds with translations
- Fix reliable sound playback using direct R.raw resource IDs
- Fix sound selection UI divider and spacing
- Fix volume slider minimum restricted to 10%
- Update battery optimization icon to reflect current status

## v1.6 (2026-01-27)

### Improvements
- Reduce app size by enabling release build minification and resource shrinking
- Add ProGuard rules for GSON and Retrofit
- Configure multi-language resource filtering
- Fix update dialog sizing and add dark mode support


## v1.5 (2026-01-26)

### New Features
- Display interval reset note in home screen
- Add battery optimization warning in settings screen with localized text
- Replace Facebook link with Telegram channel Shadow6by8 in About screen
- Replace GitHub profile link with app source code repository in About screen
- Configure release signing and ABI splits
- Scale down app icon foreground to prevent cropping

### Bug Fixes
- Fix language change resetting to home page
- Fix battery optimization status not updating immediately after granting permission
- Fix home screen big box appearing as image instead of button
- Resolve navigation and UI update issues

## v1.0 (2026-01-25)

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
