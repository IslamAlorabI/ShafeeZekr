# Google Play Data Safety Form - ShafeeZekr

Use this guide when filling out the Data Safety section in Google Play Console.

---

## Data Collection and Security

### Does your app collect or share any of the required user data types?
**No**

### Is all of the user data collected by your app encrypted in transit?
**Yes** (The app doesn't transmit data, but local storage uses Android's secure DataStore)

### Do you provide a way for users to request that their data be deleted?
**Not applicable** - No user data is collected. Users can clear app data or uninstall the app to remove all local preferences.

---

## Data Types Questionnaire

For each category, select "Not collected":

| Data Type | Collected? | Shared? |
|-----------|------------|---------|
| Location | ❌ No | ❌ No |
| Personal info | ❌ No | ❌ No |
| Financial info | ❌ No | ❌ No |
| Health and fitness | ❌ No | ❌ No |
| Messages | ❌ No | ❌ No |
| Photos and videos | ❌ No | ❌ No |
| Audio files | ❌ No | ❌ No |
| Files and docs | ❌ No | ❌ No |
| Calendar | ❌ No | ❌ No |
| Contacts | ❌ No | ❌ No |
| App activity | ❌ No | ❌ No |
| Web browsing | ❌ No | ❌ No |
| App info and performance | ❌ No | ❌ No |
| Device or other IDs | ❌ No | ❌ No |

---

## Permissions Justification

When asked to justify permissions:

### Alarm Permissions (SCHEDULE_EXACT_ALARM, USE_EXACT_ALARM)
> "This app is a religious reminder application that plays audio reminders at user-specified intervals. Exact alarms are essential for delivering reminders at precise times as configured by the user. This is the app's core functionality."

### Battery Optimization (REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
> "The app requires unrestricted battery access to reliably deliver scheduled reminders. Without this, reminder notifications may be delayed or not delivered at all, breaking the app's core purpose."

### Boot Completed (RECEIVE_BOOT_COMPLETED)
> "The app needs to restore user's reminder schedules after device restart. Without this permission, users would need to manually re-enable reminders every time their device restarts."

### Notifications (POST_NOTIFICATIONS)
> "Notifications are required to display reminder alerts to the user. This is the app's primary way of prompting users to perform their religious remembrance."

---

## Content Rating

When completing the content rating questionnaire:

- **Violence**: None
- **Sexual content**: None
- **Profanity**: None
- **Drugs**: None
- **Gambling**: None
- **User interaction**: None (no social features)
- **Shares location**: No
- **Digital purchases**: No

**Expected Rating**: Everyone / PEGI 3 / USK 0

---

## Target Audience

- **Target age group**: All ages (Everyone)
- **Is this app specifically designed for children?**: No (but suitable for all ages)
- **Does the app appeal to children?**: App contains religious content appropriate for all ages

---

## App Category

- **Primary Category**: Lifestyle or Books & Reference
- **Secondary Category**: (Optional) Personalization

---

## Store Listing Notes

### Short Description (80 chars max)
> Reminder app to send blessings upon Prophet Muhammad ﷺ

### Full Description Highlights
- Privacy-focused: No data collection
- Open source: MIT License
- Offline functionality: Works without internet
- Multiple languages: English, Arabic, German, Farsi, Indonesian, Russian, Turkish, Urdu
