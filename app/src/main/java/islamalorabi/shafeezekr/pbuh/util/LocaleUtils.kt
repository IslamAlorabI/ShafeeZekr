package islamalorabi.shafeezekr.pbuh.util

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.core.os.LocaleListCompat
import islamalorabi.shafeezekr.pbuh.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.text.intl.Locale as ComposeLocale

object LocaleUtils {
    
    fun formatLocalizedNumber(number: Int, paddedDigits: Int = 0): String {
        val lang = activeLanguage()
        
        val formatted = if (paddedDigits > 0) {
            String.format(java.util.Locale.US, "%0${paddedDigits}d", number)
        } else {
            number.toString()
        }
        
        return when (lang) {
            "ar" -> formatted.map { convertToArabicNumeral(it) }.joinToString("")
            "fa" -> formatted.map { convertToPersianNumeral(it) }.joinToString("")
            "ur" -> formatted.map { convertToUrduNumeral(it) }.joinToString("")
            else -> formatted
        }
    }
    
    fun localizeString(text: String): String {
        val lang = activeLanguage()
        
        return when (lang) {
            "ar" -> text.map { convertToArabicNumeral(it) }.joinToString("")
            "fa" -> text.map { convertToPersianNumeral(it) }.joinToString("")
            "ur" -> text.map { convertToUrduNumeral(it) }.joinToString("")
            else -> text
        }
    }
    
    fun formatLocalizedTime(hour: Int, minute: Int): String {
        val lang = activeLanguage()
        
        val isPM = hour >= 12
        val hour12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        
        val amPm = when (lang) {
            "ar" -> if (isPM) "م" else "ص"
            "fa" -> if (isPM) "ب.ظ" else "ق.ظ"
            "ur" -> if (isPM) "شام" else "صبح"
            else -> if (isPM) "PM" else "AM"
        }
        
        val timeString = String.format(java.util.Locale.US, "%d:%02d", hour12, minute)
        val localizedTime = when (lang) {
            "ar", "fa", "ur" -> timeString.map { convertToLocalizedNumeral(it, lang) }.joinToString("")
            else -> timeString
        }
        
        return "$localizedTime $amPm"
    }
    
    fun formatLocalizedTimeRange(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): String {
        val startTime = formatLocalizedTime(startHour, startMinute)
        val endTime = formatLocalizedTime(endHour, endMinute)
        return "$startTime - $endTime"
    }
    
    private fun convertToLocalizedNumeral(char: Char, lang: String): Char {
        return when (lang) {
            "ar" -> convertToArabicNumeral(char)
            "fa" -> convertToPersianNumeral(char)
            "ur" -> convertToUrduNumeral(char)
            else -> char
        }
    }
    
    private fun convertToArabicNumeral(char: Char): Char {
        return when (char) {
            '0' -> '٠'
            '1' -> '١'
            '2' -> '٢'
            '3' -> '٣'
            '4' -> '٤'
            '5' -> '٥'
            '6' -> '٦'
            '7' -> '٧'
            '8' -> '٨'
            '9' -> '٩'
            else -> char
        }
    }
    
    private fun convertToPersianNumeral(char: Char): Char {
        return when (char) {
            '0' -> '۰'
            '1' -> '۱'
            '2' -> '۲'
            '3' -> '۳'
            '4' -> '۴'
            '5' -> '۵'
            '6' -> '۶'
            '7' -> '۷'
            '8' -> '۸'
            '9' -> '۹'
            else -> char
        }
    }
    
    private fun convertToUrduNumeral(char: Char): Char {
        return when (char) {
            '0' -> '۰'
            '1' -> '۱'
            '2' -> '۲'
            '3' -> '۳'
            '4' -> '۴'
            '5' -> '۵'
            '6' -> '۶'
            '7' -> '۷'
            '8' -> '۸'
            '9' -> '۹'
            else -> char
        }
    }
    // Explicit app language first, so digits match the UI even before the default locale updates
    private fun activeLanguage(): String =
        currentAppLanguage().ifEmpty { ComposeLocale.current.language.lowercase() }

    /** Language code the user picked in the app or in system settings; "" means follow the system. */
    fun currentAppLanguage(): String {
        val locale = AppCompatDelegate.getApplicationLocales()[0] ?: return ""
        // "id" and "in" are both Indonesian; resources and the picker use "in"
        return if (locale.language == "id") "in" else locale.language
    }

    /** Applies [language] app-wide; AppCompat recreates open activities and, on Android 13+, syncs system settings. */
    fun setAppLanguage(language: String) {
        AppCompatDelegate.setApplicationLocales(
            if (language.isEmpty()) LocaleListCompat.getEmptyLocaleList()
            else LocaleListCompat.forLanguageTags(language)
        )
    }

    /**
     * Keeps the stored language and the AppCompat locale in agreement. Call after Activity.onCreate.
     * The first run moves a language saved by older versions into AppCompat; later runs pick up
     * a language changed from system settings (Android 13+).
     */
    fun syncAppLanguage(preferencesManager: PreferencesManager, scope: CoroutineScope) {
        val stored = preferencesManager.getLanguageCodeSync()
        val current = currentAppLanguage()
        if (!preferencesManager.isLanguageMigrated()) {
            preferencesManager.setLanguageMigrated()
            if (stored.isNotEmpty() && stored != current) {
                setAppLanguage(stored)
                return
            }
        }
        if (stored != current) {
            scope.launch { preferencesManager.setLanguageCode(current) }
        }
    }

    /**
     * Context with the app language for receivers, services and widgets. Android 13+ already applies
     * the per-app language to every context; older versions only apply it inside AppCompat activities.
     */
    fun localizedContext(context: Context): Context {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return context
        return updateResources(context, PreferencesManager(context).getLanguageCodeSync())
    }

    private fun updateResources(context: Context, language: String): Context {
        if (language.isEmpty()) return context
        val locale = java.util.Locale.forLanguageTag(language)
        java.util.Locale.setDefault(locale)

        val configuration = android.content.res.Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }
}
