package islamalorabi.shafeezekr.pbuh

import android.app.LocaleManager
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.os.LocaleListCompat
import islamalorabi.shafeezekr.pbuh.data.AppSettings
import islamalorabi.shafeezekr.pbuh.data.ColorScheme
import islamalorabi.shafeezekr.pbuh.data.PeriodRule
import islamalorabi.shafeezekr.pbuh.data.PreferencesManager
import islamalorabi.shafeezekr.pbuh.data.ReminderInterval
import islamalorabi.shafeezekr.pbuh.data.ThemeMode
import islamalorabi.shafeezekr.pbuh.service.ReminderScheduler
import islamalorabi.shafeezekr.pbuh.ui.screens.AboutScreen
import islamalorabi.shafeezekr.pbuh.ui.screens.HomeScreen
import islamalorabi.shafeezekr.pbuh.ui.screens.SettingsScreen
import islamalorabi.shafeezekr.pbuh.ui.theme.ShafeeZekrTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val activity = context as? ComponentActivity
            val preferencesManager = remember { PreferencesManager(context) }
            val settings by preferencesManager.settingsFlow.collectAsState(initial = AppSettings())
            val scope = rememberCoroutineScope()
            
            ShafeeZekrTheme(
                themeMode = settings.themeMode,
                colorScheme = settings.colorScheme
            ) {
                MainApp(
                    settings = settings,
                    onReminderEnabledChange = { enabled ->
                        scope.launch {
                            preferencesManager.setReminderEnabled(enabled)
                            if (enabled) {
                                val intervalMinutes = if (settings.reminderInterval == ReminderInterval.CUSTOM) {
                                    settings.customIntervalMinutes
                                } else {
                                    settings.reminderInterval.minutes
                                }
                                ReminderScheduler.startReminder(context, intervalMinutes)
                            } else {
                                ReminderScheduler.stopReminder(context)
                            }
                        }
                    },
                    onIntervalChange = { interval ->
                        scope.launch {
                            preferencesManager.setReminderInterval(interval)
                            if (settings.isReminderEnabled && interval != ReminderInterval.CUSTOM) {
                                val intervalMinutes = interval.minutes
                                if (intervalMinutes > 0) {
                                    ReminderScheduler.startReminder(context, intervalMinutes)
                                }
                            }
                        }
                    },
                    onCustomIntervalChange = { minutes ->
                        scope.launch {
                            preferencesManager.setCustomIntervalMinutes(minutes)
                            if (settings.isReminderEnabled && settings.reminderInterval == ReminderInterval.CUSTOM) {
                                ReminderScheduler.startReminder(context, minutes)
                            }
                        }
                    },
                    onThemeModeChange = { mode ->
                        scope.launch { preferencesManager.setThemeMode(mode) }
                    },
                    onColorSchemeChange = { scheme ->
                        scope.launch { preferencesManager.setColorScheme(scheme) }
                    },
                    onLanguageChange = { code ->
                        scope.launch {
                            preferencesManager.setLanguageCode(code)
                            val localeManager = context.getSystemService(LocaleManager::class.java)
                            if (code.isEmpty()) {
                                localeManager.applicationLocales = LocaleList.getEmptyLocaleList()
                            } else {
                                localeManager.applicationLocales = LocaleList.forLanguageTags(code)
                            }
                        }
                    },
                    onVolumeChange = { volume ->
                        scope.launch { preferencesManager.setAppVolume(volume) }
                    },
                    onPeriodRulesChange = { rules ->
                        scope.launch { preferencesManager.setPeriodRules(rules) }
                    },
                    onSoundChange = { index ->
                        scope.launch { preferencesManager.setSelectedSoundIndex(index) }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    settings: AppSettings,
    onReminderEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (ReminderInterval) -> Unit,
    onCustomIntervalChange: (Int) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onColorSchemeChange: (ColorScheme) -> Unit,
    onLanguageChange: (String) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onPeriodRulesChange: (List<PeriodRule>) -> Unit,
    onSoundChange: (Int) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val titles = listOf(
        stringResource(R.string.nav_home),
        stringResource(R.string.nav_settings),
        stringResource(R.string.nav_about)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = titles[selectedTab],
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = titles[0]
                        )
                    },
                    label = { Text(titles[0]) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = titles[1]
                        )
                    },
                    label = { Text(titles[1]) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.Info else Icons.Outlined.Info,
                            contentDescription = titles[2]
                        )
                    },
                    label = { Text(titles[2]) }
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeScreen(
                settings = settings,
                onReminderEnabledChange = onReminderEnabledChange,
                onIntervalChange = onIntervalChange,
                onCustomIntervalChange = onCustomIntervalChange,
                modifier = Modifier.padding(innerPadding)
            )
            1 -> SettingsScreen(
                settings = settings,
                onThemeModeChange = onThemeModeChange,
                onColorSchemeChange = onColorSchemeChange,
                onLanguageChange = onLanguageChange,
                onVolumeChange = onVolumeChange,
                onPeriodRulesChange = onPeriodRulesChange,
                onSoundChange = onSoundChange,
                modifier = Modifier.padding(innerPadding)
            )
            2 -> AboutScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}