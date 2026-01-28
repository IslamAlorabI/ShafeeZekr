# Changelog

## v1.6 (2026-01-28) - Android 9-11 Rebase & Polish

### Core Updates (Android 9-11 Rebase)
- **Legacy Support**: Fixed critical crashes on Android 10 (Dark Mode theme issue).
- **Notifications**: Fixed notification icon rendering issues on Android 9.
- **Audio**: Implemented robust volume logic to prevent low audio on legacy devices.
- **Localization**: Notifications now strictly respect App Language instead of System Language.

### New Features

- **Master Volume**:
  - App volume temporarily overrides system volume during playback.
  - Respects Device DND/Silent modes (playback blocked).
  - Added independent Volume Slider in Settings (minimum 10%).
  - Added localized sound names and warning when selecting sounds.

### Enhancements & UI
- **Timer**: Added "1 minute" and "Every 2 hours" presets.
- **Timer UI**: Localized digits for Arabic/Farsi/Urdu in the home screen timer.
- **Picker UI**: Swapped positions of +/- buttons in Custom Interval picker.
- **Sound Picker**: Improved layout and added "Sound plays immediately" warning.
- **Cleanup**: Removed all App Update checking features (code & resources).