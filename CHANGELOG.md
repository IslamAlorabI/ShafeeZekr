# Changelog

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
