# Google Play Store Publishing Guide for ShafeeZekr

This comprehensive guide walks you through publishing ShafeeZekr on the Google Play Store.

---

## Table of Contents

1. [Pre-Publication Checklist](#pre-publication-checklist)
2. [Building the AAB (Android App Bundle)](#building-the-aab)
3. [Fixing R8/ProGuard Issues](#fixing-r8proguard-issues)
4. [Setting Up Google Play Console](#setting-up-google-play-console)
5. [Creating Your Store Listing](#creating-your-store-listing)
6. [Uploading Your App](#uploading-your-app)
7. [Content Rating and Data Safety](#content-rating-and-data-safety)
8. [Pricing and Distribution](#pricing-and-distribution)
9. [Review and Launch](#review-and-launch)

---

## Pre-Publication Checklist

Before publishing, ensure the following are ready:

| Item | Status | Notes |
|------|--------|-------|
| App Icon | ✅ | 512x512 PNG required for Play Store |
| Screenshots | ✅ | At least 2 screenshots (recommended: phone + tablet) |
| Privacy Policy | ✅ | Available at [PRIVACY_POLICY.md](PRIVACY_POLICY.md) |
| Signing Key | ✅ | Configured in `local.properties` |
| Version Info | ✅ | v1.7 (versionCode: 4) |
| Tested on Device | ⬜ | Verify release build works correctly |

> [!IMPORTANT]
> Keep your signing key (`release-key.jks`) safe! You cannot update your app without this key. Back it up in multiple secure locations.

---

## Building the AAB

Google Play requires Android App Bundle (AAB) format for new apps since August 2021.

### Method 1: Using Android Studio (Recommended)

1. Open your project in Android Studio
2. Go to **Build → Generate Signed Bundle / APK**
3. Select **Android App Bundle**
4. Click **Next**
5. Choose your keystore:
   - **Key store path**: Select your `release-key.jks`
   - **Key store password**: Enter your keystore password
   - **Key alias**: Enter your key alias
   - **Key password**: Enter your key password
6. Click **Next**
7. Select **release** build variant
8. Click **Create**

The AAB will be generated at:
```
app/release/app-release.aab
```

### Method 2: Using Command Line

```bash
# Navigate to project root
cd /Users/islamalorabi/Documents/ASProjects/ShafeeZekr

# Build release AAB
./gradlew bundleRelease
```

The AAB will be at: `app/build/outputs/bundle/release/app-release.aab`

---

## Fixing R8/ProGuard Issues

If you encounter R8 shrinking errors when building the AAB, try these solutions:

### ABI Splits Conflict (Most Common)

> [!IMPORTANT]
> If you see an error about "Multiple shrunk-resources files found" or the build fails at `buildReleasePreBundle`, you have ABI splits enabled which conflicts with AAB format.

**Solution:** Disable ABI splits in `app/build.gradle.kts`:

```kotlin
splits {
    abi {
        isEnable = false  // Required for AAB builds
    }
}
```

The AAB format automatically handles device-specific optimization, making ABI splits unnecessary and causing conflicts.

### Common Error: Missing Classes

If R8 removes classes needed at runtime (like Gson models), ensure proguard-rules.pro includes:

```proguard
# Keep all data/model classes
-keep class islamalorabi.shafeezekr.pbuh.data.** { *; }
-keep class islamalorabi.shafeezekr.pbuh.model.** { *; }

# Keep serialization
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
```

### Common Error: Kotlin Reflection

If using Kotlin reflection:

```proguard
# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
```

### Common Error: Compose Issues

For Jetpack Compose apps:

```proguard
# Keep Compose stability
-keep class * extends androidx.compose.runtime.Stable { *; }
```

### Disable R8 for Testing (Not Recommended for Production)

If you need to temporarily disable R8 to identify issues:

```kotlin
// In app/build.gradle.kts
buildTypes {
    release {
        isMinifyEnabled = false  // Temporarily disable
        isShrinkResources = false
        // ...
    }
}
```

> [!WARNING]
> Only use this for debugging. Re-enable minification for production builds to reduce APK size and improve security.

### Debug R8 Issues

To get detailed R8 output:

```bash
./gradlew bundleRelease --info 2>&1 | grep -i "R8\|proguard\|warning\|error"
```

---

## Setting Up Google Play Console

### Step 1: Create Developer Account

1. Go to [Google Play Console](https://play.google.com/console)
2. Pay the one-time registration fee ($25 USD)
3. Complete identity verification

### Step 2: Create New App

1. Click **Create app**
2. Fill in the details:
   - **App name**: ShafeeZekr
   - **Default language**: English (US)
   - **App or game**: App
   - **Free or paid**: Free
3. Accept declarations
4. Click **Create app**

---

## Creating Your Store Listing

Navigate to **Grow → Store presence → Main store listing**

### App Details

| Field | Value |
|-------|-------|
| **App name** | ShafeeZekr |
| **Short description** | Reminder app to send blessings upon Prophet Muhammad ﷺ |
| **Full description** | See below |

**Full Description:**

```
ShafeeZekr is a modern Android application designed to remind you to send blessings upon the Prophet Muhammad (Peace Be Upon Him).

✨ FEATURES:
• Periodic Reminders: Set automatic audio reminders at customizable intervals (1 minute to 2 hours, or custom intervals)
• Quiet Hours: Configure time-based rules to pause reminders during specific periods
• Master Volume Control: Independent volume control that works regardless of system volume
• Multiple Sound Options: Choose from various reminder sounds with instant preview
• Material Design 3: Dynamic color theming (Monet), Light/Dark mode support
• Multi-language Support: English, Arabic, German, Farsi, Indonesian, Russian, Turkish, Urdu
• Boot Persistence: Reminders automatically resume after device restart

🔒 PRIVACY FOCUSED:
• No data collection
• No tracking or analytics
• No ads
• Open source (MIT License)

📱 Works offline - no internet required

Source: https://github.com/islamalorabi/ShafeeZekr
```

### Graphics Assets

| Asset | Requirements | File |
|-------|--------------|------|
| **App icon** | 512x512 PNG, 32-bit, no alpha | Export from `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png` or create high-res |
| **Feature graphic** | 1024x500 PNG or JPEG | Create promotional banner |
| **Screenshots** | Min 2, 16:9 or 9:16 | Use files from `Screenshots/` folder |

> [!TIP]
> Recommended screenshot dimensions for phones: 1080x1920 (or 1440x2560 for high-res)

---

## Uploading Your App

### Step 1: Navigate to Release

1. Go to **Release → Production**
2. Click **Create new release**

### Step 2: App Signing

For new apps, Google recommends **Play App Signing**:

1. On first upload, you'll be prompted to enable Play App Signing
2. **Recommended**: Let Google manage your signing key
   - Upload your AAB
   - Google signs it for distribution
3. **Alternative**: Export and upload your key (more complex)

### Step 3: Upload AAB

1. Drag your `app-release.aab` file
2. Wait for processing
3. Review warnings (if any)

### Step 4: Release Notes

Add release notes for this version:

```
Version 1.7

• Quiet Hours: Configure time-based rules to pause reminders
• Master Volume Control: App volume works independently of system volume
• Improved notifications and reliability
• Multi-language support (8 languages)
• Performance improvements and bug fixes
```

---

## Content Rating and Data Safety

### Content Rating Questionnaire

Navigate to **Policy → App content → Content rating**

Answer the questionnaire based on [PLAY_STORE_DATA_SAFETY.md](PLAY_STORE_DATA_SAFETY.md):

- **Violence**: None
- **Sexual content**: None
- **Profanity**: None
- **Drugs**: None
- **Gambling**: None
- **User interaction**: None
- **Shares location**: No
- **Digital purchases**: No

**Expected Rating**: Everyone / PEGI 3

### Data Safety Form

Navigate to **Policy → App content → Data safety**

Based on our privacy design:

| Question | Answer |
|----------|--------|
| Does your app collect user data? | No |
| Does your app share user data? | No |
| Is data encrypted in transit? | Yes (N/A - no data transmitted) |
| Can users request data deletion? | N/A (no data collected) |

See [PLAY_STORE_DATA_SAFETY.md](PLAY_STORE_DATA_SAFETY.md) for detailed answers.

### Privacy Policy

Add your privacy policy URL. Options:

1. **GitHub Raw URL**: `https://raw.githubusercontent.com/islamalorabi/ShafeeZekr/main/PRIVACY_POLICY.md`
2. **GitHub Pages**: Create a `docs/` folder with HTML version
3. **Third-party hosting**: Use any web hosting

---

## Pricing and Distribution

Navigate to **Monetization → Pricing**

| Setting | Value |
|---------|-------|
| **Pricing** | Free |
| **Countries** | All countries (or select specific) |
| **Contains ads** | No |

---

## Review and Launch

### Pre-Launch Checklist

Before clicking **Start rollout to Production**:

- [ ] All store listing fields completed
- [ ] Screenshots uploaded (at least 2 phone screenshots)
- [ ] Content rating questionnaire completed
- [ ] Data safety form submitted
- [ ] Privacy policy URL added
- [ ] Target audience and content settings configured
- [ ] AAB uploaded successfully

### Submit for Review

1. Navigate to **Release → Production**
2. Click **Review release**
3. Fix any warnings/errors shown
4. Click **Start rollout to Production**

### Review Timeline

- **First submission**: 1-7 days (can take longer)
- **Updates**: Usually 1-3 days

> [!NOTE]
> Google may request additional information or changes. Monitor your email and Play Console notifications.

---

## Post-Launch Tasks

After your app is live:

1. **Monitor reviews**: Respond to user feedback
2. **Check crash reports**: Android Vitals in Play Console
3. **Update regularly**: Bug fixes, new features
4. **Watch statistics**: Track installs and ratings

---

## Troubleshooting

### AAB Upload Errors

| Error | Solution |
|-------|----------|
| "Version code already used" | Increment `versionCode` in build.gradle.kts |
| "Invalid signature" | Ensure correct signing key is used |
| "Package name mismatch" | `applicationId` must match existing app |

### R8/Shrinking Errors

| Error | Solution |
|-------|----------|
| Missing class at runtime | Add keep rules for that class |
| Reflection issues | Keep classes used with reflection |
| Gson serialization fails | Keep model classes with `@SerializedName` |

---

## Quick Commands Reference

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (signed)
./gradlew assembleRelease

# Build AAB for Play Store
./gradlew bundleRelease

# Clean and rebuild
./gradlew clean bundleRelease

# Check for dependency issues
./gradlew dependencies

# Run lint checks
./gradlew lint
```

---

## Important Files Reference

| File | Purpose |
|------|---------|
| `app/build.gradle.kts` | Build configuration, signing |
| `app/proguard-rules.pro` | R8/ProGuard rules |
| `local.properties` | Keystore credentials (gitignored) |
| `release-key.jks` | Signing key (BACK THIS UP!) |
| `PRIVACY_POLICY.md` | Privacy policy for Play Store |
| `PLAY_STORE_DATA_SAFETY.md` | Data safety form reference |

---

*Good luck with your Play Store launch!* 🚀
