package islamalorabi.shafeezekr.pbuh

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Leaderboard
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

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import islamalorabi.shafeezekr.pbuh.util.LocaleUtils
import islamalorabi.shafeezekr.pbuh.util.SystemNightMode
import islamalorabi.shafeezekr.pbuh.data.AppSettings
import islamalorabi.shafeezekr.pbuh.data.AudioStreamType
import islamalorabi.shafeezekr.pbuh.data.ColorScheme
import islamalorabi.shafeezekr.pbuh.data.DhikrStatsManager
import islamalorabi.shafeezekr.pbuh.data.PeriodRule
import islamalorabi.shafeezekr.pbuh.data.PreferencesManager
import islamalorabi.shafeezekr.pbuh.data.ReminderInterval
import islamalorabi.shafeezekr.pbuh.data.ThemeMode
import islamalorabi.shafeezekr.pbuh.service.ReminderScheduler
import islamalorabi.shafeezekr.pbuh.ui.components.GoalCelebration
import islamalorabi.shafeezekr.pbuh.ui.screens.AboutScreen
import islamalorabi.shafeezekr.pbuh.ui.screens.HomeScreen
import islamalorabi.shafeezekr.pbuh.ui.screens.StatisticsScreen
import islamalorabi.shafeezekr.pbuh.ui.screens.SettingsScreen
import islamalorabi.shafeezekr.pbuh.ui.theme.ShafeeZekrTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    /**
     * Auto Backup restores settings after a reinstall but not the phone permission, and revoking
     * the permission leaves the setting behind too. Turn "mute during calls" off so its switch
     * doesn't show on while it can't detect calls.
     */
    private fun clearMuteOnCallWithoutPermission() {
        val granted = androidx.core.content.ContextCompat.checkSelfPermission(
            this, android.Manifest.permission.READ_PHONE_STATE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (granted) return
        val preferencesManager = PreferencesManager(this)
        lifecycleScope.launch {
            if (preferencesManager.settingsFlow.first().muteOnCall) {
                preferencesManager.setMuteOnCall(false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleUtils.syncAppLanguage(PreferencesManager(this), lifecycleScope)
        clearMuteOnCallWithoutPermission()
        // Covers users who picked a theme before the splash followed it; a no-op once in sync
        lifecycleScope.launch {
            SystemNightMode.apply(this@MainActivity, PreferencesManager(this@MainActivity).settingsFlow.first().themeMode)
        }
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
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
                                if (!settings.isReminderAllowedByPeriodRules()) {
                                    val quietEndMillis = settings.getQuietHoursEndMillis()
                                    ReminderScheduler.startReminder(context, intervalMinutes)
                                    ReminderScheduler.pauseForQuietHours(context, quietEndMillis)
                                } else {
                                    ReminderScheduler.clearQuietHoursPause(context)
                                    ReminderScheduler.resumeReminder(context, intervalMinutes)
                                }
                            } else {
                                ReminderScheduler.clearQuietHoursPause(context)
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
                        SystemNightMode.apply(context, mode)
                    },
                    onColorSchemeChange = { scheme ->
                        scope.launch { preferencesManager.setColorScheme(scheme) }
                    },
                    onLanguageChange = { code ->
                        scope.launch {
                            preferencesManager.setLanguageCode(code)
                            // Recreates the activity with the new language
                            LocaleUtils.setAppLanguage(code)
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
                    onMuteOnCallChange = { enabled ->
                        scope.launch { preferencesManager.setMuteOnCall(enabled) }
                    },
                    onMuteOnSilentChange = { enabled ->
                        scope.launch { preferencesManager.setMuteOnSilent(enabled) }
                    },
                    onMuteOnDNDChange = { enabled ->
                        scope.launch { preferencesManager.setMuteOnDND(enabled) }
                    },
                    onMuteOnMediaChange = { enabled ->
                        scope.launch { preferencesManager.setMuteOnMedia(enabled) }
                    },
                    onCustomSoundPathChange = { path ->
                        scope.launch { preferencesManager.setCustomSoundPath(path) }
                    },
                    onCustomSoundEnabledChange = { enabled ->
                        scope.launch { preferencesManager.setCustomSoundEnabled(enabled) }
                    },
                    onDailyGoalChange = { goal ->
                        scope.launch { preferencesManager.setDailyGoal(goal) }
                    },
                    onAudioStreamTypeChange = { type ->
                        scope.launch { preferencesManager.setAudioStreamType(type) }
                    },
                    onAutoDismissNotificationChange = { enabled ->
                        scope.launch { preferencesManager.setAutoDismissNotification(enabled) }
                    },
                    onUseSystemVolumeChange = { enabled ->
                        scope.launch { preferencesManager.setUseSystemVolume(enabled) }
                    },
                    onShuffleSoundsChange = { enabled ->
                        scope.launch { preferencesManager.setShuffleSounds(enabled) }
                    }
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
    onSoundChange: (Int) -> Unit,
    onMuteOnCallChange: (Boolean) -> Unit,
    onMuteOnSilentChange: (Boolean) -> Unit,
    onMuteOnDNDChange: (Boolean) -> Unit,
    onMuteOnMediaChange: (Boolean) -> Unit,
    onCustomSoundPathChange: (String?) -> Unit,
    onCustomSoundEnabledChange: (Boolean) -> Unit,
    onDailyGoalChange: (Int) -> Unit,
    onAudioStreamTypeChange: (AudioStreamType) -> Unit,
    onAutoDismissNotificationChange: (Boolean) -> Unit,
    onUseSystemVolumeChange: (Boolean) -> Unit,
    onShuffleSoundsChange: (Boolean) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showCelebration by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val statsManager = remember { DhikrStatsManager(context) }
    val currentGoal by rememberUpdatedState(settings.dailyGoal)

    DisposableEffect(statsManager) {
        // A goal already reached before the app was opened is not celebrated late.
        statsManager.consumeGoalCelebration(currentGoal)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            if (statsManager.consumeGoalCelebration(currentGoal)) {
                showCelebration = true
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
            }
        }
        statsManager.registerChangeListener(listener)
        onDispose { statsManager.unregisterChangeListener(listener) }
    }

    val titles = listOf(
        stringResource(R.string.nav_home),
        stringResource(R.string.nav_statistics),
        stringResource(R.string.nav_settings),
        stringResource(R.string.nav_about)
    )

    Box(modifier = Modifier.fillMaxSize()) {
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
                            imageVector = if (selectedTab == 1) Icons.Filled.Leaderboard else Icons.Outlined.Leaderboard,
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
                            imageVector = if (selectedTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = titles[2]
                        )
                    },
                    label = { Text(titles[2]) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.Info else Icons.Outlined.Info,
                            contentDescription = titles[3]
                        )
                    },
                    label = { Text(titles[3]) }
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
            1 -> StatisticsScreen(
                settings = settings,
                onDailyGoalChange = onDailyGoalChange,
                modifier = Modifier.padding(innerPadding)
            )
            2 -> SettingsScreen(
                settings = settings,
                onThemeModeChange = onThemeModeChange,
                onColorSchemeChange = onColorSchemeChange,
                onLanguageChange = onLanguageChange,
                onVolumeChange = onVolumeChange,
                onPeriodRulesChange = onPeriodRulesChange,
                onSoundChange = onSoundChange,
                onMuteOnCallChange = onMuteOnCallChange,
                onMuteOnSilentChange = onMuteOnSilentChange,
                onMuteOnDNDChange = onMuteOnDNDChange,
                onMuteOnMediaChange = onMuteOnMediaChange,
                onCustomSoundPathChange = onCustomSoundPathChange,
                onCustomSoundEnabledChange = onCustomSoundEnabledChange,
                onAudioStreamTypeChange = onAudioStreamTypeChange,
                onAutoDismissNotificationChange = onAutoDismissNotificationChange,
                onUseSystemVolumeChange = onUseSystemVolumeChange,
                onShuffleSoundsChange = onShuffleSoundsChange,
                modifier = Modifier.padding(innerPadding)
            )
            3 -> AboutScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
    GoalCelebration(visible = showCelebration, onFinished = { showCelebration = false })
    }
}