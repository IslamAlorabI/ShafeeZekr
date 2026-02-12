package islamalorabi.shafeezekr.pbuh.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import islamalorabi.shafeezekr.pbuh.data.ColorScheme
import islamalorabi.shafeezekr.pbuh.data.ThemeMode
import android.app.Activity
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GreenDark = darkColorScheme(
    primary = Color(0xFF8BD98B),
    onPrimary = Color(0xFF003A00),
    primaryContainer = Color(0xFF005300),
    onPrimaryContainer = Color(0xFFA7F5A5),
    secondary = Color(0xFFB9CCB4),
    onSecondary = Color(0xFF253424),
    secondaryContainer = Color(0xFF3B4B39),
    onSecondaryContainer = Color(0xFFD5E8CF),
    tertiary = Color(0xFFA0CFD4),
    onTertiary = Color(0xFF00373A),
    tertiaryContainer = Color(0xFF1E4D51),
    onTertiaryContainer = Color(0xFFBCEBF0),
    background = Color(0xFF1A1C19),
    surface = Color(0xFF1A1C19),
    onBackground = Color(0xFFE2E3DD),
    onSurface = Color(0xFFE2E3DD)
)

private val GreenLight = lightColorScheme(
    primary = Color(0xFF006E10),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF95F98E),
    onPrimaryContainer = Color(0xFF002201),
    secondary = Color(0xFF526350),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD5E8CF),
    onSecondaryContainer = Color(0xFF101F10),
    tertiary = Color(0xFF39656A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBCEBF0),
    onTertiaryContainer = Color(0xFF001F23),
    background = Color(0xFFFCFDF6),
    surface = Color(0xFFFCFDF6),
    onBackground = Color(0xFF1A1C19),
    onSurface = Color(0xFF1A1C19)
)

private val BlueDark = darkColorScheme(
    primary = Color(0xFFAAC7FF),
    onPrimary = Color(0xFF002F65),
    primaryContainer = Color(0xFF08458E),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFFBEC6DC),
    onSecondary = Color(0xFF283141),
    secondaryContainer = Color(0xFF3F4759),
    onSecondaryContainer = Color(0xFFDAE2F9),
    tertiary = Color(0xFFDDBCE0),
    onTertiary = Color(0xFF3F2844),
    tertiaryContainer = Color(0xFF573E5C),
    onTertiaryContainer = Color(0xFFFAD8FC),
    background = Color(0xFF1A1B1F),
    surface = Color(0xFF1A1B1F),
    onBackground = Color(0xFFE3E2E6),
    onSurface = Color(0xFFE3E2E6)
)

private val BlueLight = lightColorScheme(
    primary = Color(0xFF2C5DA9),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E3FF),
    onPrimaryContainer = Color(0xFF001A40),
    secondary = Color(0xFF565F71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDAE2F9),
    onSecondaryContainer = Color(0xFF131C2C),
    tertiary = Color(0xFF705574),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFAD8FC),
    onTertiaryContainer = Color(0xFF28132E),
    background = Color(0xFFFDFBFF),
    surface = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1A1B1F),
    onSurface = Color(0xFF1A1B1F)
)

private val PurpleDark = darkColorScheme(
    primary = Color(0xFFCFBCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFE9DDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF4A2532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5)
)

private val PurpleLight = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE9DDFF),
    onPrimaryContainer = Color(0xFF22005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1E192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31101D),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

private val TealDark = darkColorScheme(
    primary = Color(0xFF4FD8EB),
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = Color(0xFFB1CBD0),
    onSecondary = Color(0xFF1C3438),
    secondaryContainer = Color(0xFF334B4F),
    onSecondaryContainer = Color(0xFFCCE7EC),
    tertiary = Color(0xFFB8C5EA),
    onTertiary = Color(0xFF222F4C),
    tertiaryContainer = Color(0xFF394664),
    onTertiaryContainer = Color(0xFFD7E2FF),
    background = Color(0xFF191C1D),
    surface = Color(0xFF191C1D),
    onBackground = Color(0xFFE1E3E3),
    onSurface = Color(0xFFE1E3E3)
)

private val TealLight = lightColorScheme(
    primary = Color(0xFF006874),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF97F0FF),
    onPrimaryContainer = Color(0xFF001F24),
    secondary = Color(0xFF4A6267),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCCE7EC),
    onSecondaryContainer = Color(0xFF051F23),
    tertiary = Color(0xFF515D7E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD7E2FF),
    onTertiaryContainer = Color(0xFF0C1A37),
    background = Color(0xFFFBFCFC),
    surface = Color(0xFFFBFCFC),
    onBackground = Color(0xFF191C1D),
    onSurface = Color(0xFF191C1D)
)

private val OrangeDark = darkColorScheme(
    primary = Color(0xFFFFB77C),
    onPrimary = Color(0xFF4D2700),
    primaryContainer = Color(0xFF6D3900),
    onPrimaryContainer = Color(0xFFFFDCC2),
    secondary = Color(0xFFE3BFAB),
    onSecondary = Color(0xFF422B1D),
    secondaryContainer = Color(0xFF5A4132),
    onSecondaryContainer = Color(0xFFFFDCC2),
    tertiary = Color(0xFFCACA93),
    onTertiary = Color(0xFF32330B),
    tertiaryContainer = Color(0xFF494A1F),
    onTertiaryContainer = Color(0xFFE6E7AD),
    background = Color(0xFF1F1B16),
    surface = Color(0xFF1F1B16),
    onBackground = Color(0xFFEAE1D9),
    onSurface = Color(0xFFEAE1D9)
)

private val OrangeLight = lightColorScheme(
    primary = Color(0xFF8F4E00),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDCC2),
    onPrimaryContainer = Color(0xFF2E1500),
    secondary = Color(0xFF745944),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDCC2),
    onSecondaryContainer = Color(0xFF2A1707),
    tertiary = Color(0xFF606134),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE6E7AD),
    onTertiaryContainer = Color(0xFF1D1D00),
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1F1B16),
    onSurface = Color(0xFF1F1B16)
)

private val PinkDark = darkColorScheme(
    primary = Color(0xFFFFB1C8),
    onPrimary = Color(0xFF5E1133),
    primaryContainer = Color(0xFF7B294A),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = Color(0xFFE3BDC6),
    onSecondary = Color(0xFF422931),
    secondaryContainer = Color(0xFF5A3F47),
    onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary = Color(0xFFEFBD94),
    onTertiary = Color(0xFF48290C),
    tertiaryContainer = Color(0xFF623F20),
    onTertiaryContainer = Color(0xFFFFDCC1),
    background = Color(0xFF201A1B),
    surface = Color(0xFF201A1B),
    onBackground = Color(0xFFECE0E1),
    onSurface = Color(0xFFECE0E1)
)

private val PinkLight = lightColorScheme(
    primary = Color(0xFF984061),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD9E2),
    onPrimaryContainer = Color(0xFF3E001D),
    secondary = Color(0xFF74565E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD9E2),
    onSecondaryContainer = Color(0xFF2B151B),
    tertiary = Color(0xFF7C5635),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCC1),
    onTertiaryContainer = Color(0xFF2E1500),
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFFFFBFF),
    onBackground = Color(0xFF201A1B),
    onSurface = Color(0xFF201A1B)
)

private val RedDark = darkColorScheme(
    primary = Color(0xFFFFB4AB),
    onPrimary = Color(0xFF690005),
    primaryContainer = Color(0xFF93000A),
    onPrimaryContainer = Color(0xFFFFDAD6),
    secondary = Color(0xFFE7BDB8),
    onSecondary = Color(0xFF442926),
    secondaryContainer = Color(0xFF5D3F3C),
    onSecondaryContainer = Color(0xFFFFDAD6),
    tertiary = Color(0xFFE5C18D),
    onTertiary = Color(0xFF422C05),
    tertiaryContainer = Color(0xFF5B4319),
    onTertiaryContainer = Color(0xFFFFDEAD),
    background = Color(0xFF201A19),
    surface = Color(0xFF201A19),
    onBackground = Color(0xFFEDE0DE),
    onSurface = Color(0xFFEDE0DE)
)

private val RedLight = lightColorScheme(
    primary = Color(0xFFBB1A1A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = Color(0xFF410002),
    secondary = Color(0xFF775652),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDAD6),
    onSecondaryContainer = Color(0xFF2C1513),
    tertiary = Color(0xFF755A2F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDEAD),
    onTertiaryContainer = Color(0xFF281900),
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFFFFBFF),
    onBackground = Color(0xFF201A19),
    onSurface = Color(0xFF201A19)
)

@Composable
fun ShafeeZekrTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    colorScheme: ColorScheme = ColorScheme.MONET,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colors = when (colorScheme) {
        ColorScheme.MONET -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) GreenDark else GreenLight
            }
        }


        ColorScheme.GREEN -> if (darkTheme) GreenDark else GreenLight
        ColorScheme.BLUE -> if (darkTheme) BlueDark else BlueLight
        ColorScheme.PURPLE -> if (darkTheme) PurpleDark else PurpleLight
        ColorScheme.TEAL -> if (darkTheme) TealDark else TealLight
        ColorScheme.ORANGE -> if (darkTheme) OrangeDark else OrangeLight
        ColorScheme.PINK -> if (darkTheme) PinkDark else PinkLight
        ColorScheme.RED -> if (darkTheme) RedDark else RedLight
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}