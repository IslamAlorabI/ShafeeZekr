package islamalorabi.shafeezekr.pbuh.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.intl.Locale as ComposeLocale

object LocaleUtils {
    
    fun formatLocalizedNumber(number: Int, paddedDigits: Int = 0): String {
        val locale = ComposeLocale.current.toLanguageTag()
        val lang = locale.split("-").first().lowercase()
        
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
        val locale = ComposeLocale.current.toLanguageTag()
        val lang = locale.split("-").first().lowercase()
        
        return when (lang) {
            "ar" -> text.map { convertToArabicNumeral(it) }.joinToString("")
            "fa" -> text.map { convertToPersianNumeral(it) }.joinToString("")
            "ur" -> text.map { convertToUrduNumeral(it) }.joinToString("")
            else -> text
        }
    }
    
    fun formatLocalizedTime(hour: Int, minute: Int): String {
        val locale = ComposeLocale.current.toLanguageTag()
        val lang = locale.split("-").first().lowercase()
        
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
    fun getSystemLocale(): java.util.Locale {
        return android.os.Build.VERSION.SDK_INT.let { sdk ->
            if (sdk >= android.os.Build.VERSION_CODES.N) {
                android.content.res.Resources.getSystem().configuration.locales[0]
            } else {
                @Suppress("DEPRECATION")
                android.content.res.Resources.getSystem().configuration.locale
            }
        }
    }

    fun updateResources(context: android.content.Context, language: String): android.content.Context {
        val locale = if (language.isEmpty()) {
            getSystemLocale()
        } else {
            java.util.Locale.Builder().setLanguage(language).build()
        }
        java.util.Locale.setDefault(locale)
        
        val configuration = android.content.res.Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        
        return context.createConfigurationContext(configuration)
    }
}
