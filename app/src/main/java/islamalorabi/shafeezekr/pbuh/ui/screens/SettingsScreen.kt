package islamalorabi.shafeezekr.pbuh.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.data.AppSettings
import islamalorabi.shafeezekr.pbuh.data.ColorScheme
import islamalorabi.shafeezekr.pbuh.data.PeriodRule
import islamalorabi.shafeezekr.pbuh.data.RuleScheduleType
import java.util.UUID
import islamalorabi.shafeezekr.pbuh.data.ThemeMode
import islamalorabi.shafeezekr.pbuh.update.GithubRelease
import kotlinx.coroutines.launch
import java.util.Calendar
import android.media.MediaPlayer
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.mutableIntStateOf

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    object UpToDate : UpdateState()
    data class UpdateAvailable(val release: GithubRelease) : UpdateState()
    object Error : UpdateState()
}

data class LanguageOption(
    val code: String,
    val nameRes: Int
)

val languages = listOf(
    LanguageOption("", R.string.lang_system),
    LanguageOption("en", R.string.lang_en),
    LanguageOption("ar", R.string.lang_ar),
    LanguageOption("in", R.string.lang_id),
    LanguageOption("ru", R.string.lang_ru),
    LanguageOption("de", R.string.lang_de),
    LanguageOption("ur", R.string.lang_ur),
    LanguageOption("fa", R.string.lang_fa),
    LanguageOption("tr", R.string.lang_tr)
)

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onThemeModeChange: (ThemeMode) -> Unit,
    onColorSchemeChange: (ColorScheme) -> Unit,
    onLanguageChange: (String) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onPeriodRulesChange: (List<PeriodRule>) -> Unit,
    onSoundChange: (Int) -> Unit,
    onCheckForUpdates: suspend () -> GithubRelease?,
    modifier: Modifier = Modifier
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAddPeriodRuleDialog by remember { mutableStateOf(false) }
    var ruleToEdit by remember { mutableStateOf<PeriodRule?>(null) }
    var showSoundDialog by remember { mutableStateOf(false) }
    var updateState by remember { mutableStateOf<UpdateState>(UpdateState.Idle) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    if (showAddPeriodRuleDialog) {
        AddPeriodRuleDialog(
            ruleToEdit = ruleToEdit,
            onDismiss = {
                showAddPeriodRuleDialog = false
                ruleToEdit = null
            },
            onDelete = if (ruleToEdit != null) {
                {
                    val updatedRules = settings.periodRules.filter { it.id != ruleToEdit?.id }
                    onPeriodRulesChange(updatedRules)
                    showAddPeriodRuleDialog = false
                    ruleToEdit = null
                }
            } else null,
            onConfirm = { rule ->
                val updatedRules = if (ruleToEdit != null) {
                    settings.periodRules.map { if (it.id == rule.id) rule else it }
                } else {
                    settings.periodRules + rule
                }
                onPeriodRulesChange(updatedRules)
                showAddPeriodRuleDialog = false
                ruleToEdit = null
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SettingsGroup(
                header = stringResource(R.string.language_section),
                headerColor = MaterialTheme.colorScheme.primary
            ) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = stringResource(R.string.language),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        supportingContent = {
                            val currentLang = languages.find { it.code == settings.languageCode }
                            Text(
                                text = currentLang?.let { stringResource(it.nameRes) } ?: "English",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_language),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { showLanguageDialog = true }
                    )
                }
            }
        }

        item {
            SettingsGroup(
                header = stringResource(R.string.sound_section),
                headerColor = MaterialTheme.colorScheme.primary
            ) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.sound_selection),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.sound_name, settings.selectedSoundIndex),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { showSoundDialog = true }
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_volume_up),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.app_volume),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = stringResource(R.string.app_volume_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${(settings.appVolume * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Slider(
                            value = settings.appVolume.coerceAtLeast(0.1f),
                            onValueChange = { onVolumeChange(it.coerceAtLeast(0.1f)) },
                            valueRange = 0f..1f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                        )
                    }
                }
            }
        }

        item {
            SettingsGroup(
                header = stringResource(R.string.period_rules_section),
                headerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = stringResource(R.string.period_rules_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (settings.periodRules.isEmpty()) {
                            Text(
                                text = stringResource(R.string.period_rule_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        } else {
                            settings.periodRules.forEachIndexed { index, rule ->
                                val hasConflict = settings.periodRules.any { other ->
                                    other.id != rule.id && other.isEnabled && rule.conflictsWith(other)
                                }
                                ListItem(
                                    headlineContent = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = when (rule.scheduleType) {
                                                    RuleScheduleType.WEEKLY_DAYS -> stringResource(R.string.rule_type_weekly)
                                                    RuleScheduleType.SPECIFIC_DATE -> stringResource(R.string.rule_type_date)
                                                },
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    },
                                    supportingContent = {
                                        Column {
                                            Text(
                                                text = rule.getDisplayText(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (hasConflict && rule.isEnabled) {
                                                Text(
                                                    text = stringResource(R.string.period_rule_conflict_warning),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    },
                                    leadingContent = {
                                        Icon(
                                            imageVector = when (rule.scheduleType) {
                                                RuleScheduleType.WEEKLY_DAYS -> Icons.Default.DateRange
                                                RuleScheduleType.SPECIFIC_DATE -> Icons.Default.Event
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingContent = {
                                        Switch(
                                            checked = rule.isEnabled,
                                            onCheckedChange = { enabled ->
                                                val updatedRules = settings.periodRules.map {
                                                    if (it.id == rule.id) it.copy(isEnabled = enabled) else it
                                                }
                                                onPeriodRulesChange(updatedRules)
                                            }
                                        )
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    modifier = Modifier.clickable { 
                                        ruleToEdit = rule
                                        showAddPeriodRuleDialog = true 
                                    }
                                )
                            }
                        }
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.add_period_rule),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { 
                                ruleToEdit = null
                                showAddPeriodRuleDialog = true 
                            }
                        )
                    }
                }
            }
        }

        item {
            SettingsGroup(
                header = stringResource(R.string.theme_section),
                headerColor = MaterialTheme.colorScheme.primary
            ) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.theme_mode),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = when (settings.themeMode) {
                                        ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                        ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                        ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.DarkMode,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { showThemeDialog = true }
                        )

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.color_scheme),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = when (settings.colorScheme) {
                                        ColorScheme.MONET -> stringResource(R.string.color_monet)
                                        ColorScheme.GREEN -> stringResource(R.string.color_green)
                                        ColorScheme.BLUE -> stringResource(R.string.color_blue)
                                        ColorScheme.PURPLE -> stringResource(R.string.color_purple)
                                        ColorScheme.TEAL -> stringResource(R.string.color_teal)
                                        ColorScheme.ORANGE -> stringResource(R.string.color_orange)
                                        ColorScheme.PINK -> stringResource(R.string.color_pink)
                                        ColorScheme.RED -> stringResource(R.string.color_red)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingContent = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_palette),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { showColorDialog = true }
                        )
                    }
                }
            }
        }

        item {
            val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager
            var isIgnoringBatteryOptimizations by remember {
                mutableStateOf(powerManager.isIgnoringBatteryOptimizations(context.packageName))
            }
            
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(context.packageName)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }
            
            SettingsGroup(
                header = stringResource(R.string.battery_section),
                headerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = stringResource(R.string.battery_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = stringResource(R.string.battery_optimization),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        supportingContent = {
                            Text(
                                text = if (isIgnoringBatteryOptimizations) {
                                    stringResource(R.string.battery_optimization_disabled)
                                } else {
                                    stringResource(R.string.battery_optimization_desc)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isIgnoringBatteryOptimizations) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = if (isIgnoringBatteryOptimizations) {
                                    Icons.Default.BatteryStd
                                } else {
                                    Icons.Default.BatteryAlert
                                },
                                contentDescription = null,
                                tint = if (isIgnoringBatteryOptimizations) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }

        item {
            SettingsGroup(
                header = stringResource(R.string.updates_section),
                headerColor = MaterialTheme.colorScheme.primary
            ) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.check_updates),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            leadingContent = {
                                if (updateState is UpdateState.Checking) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable(
                                enabled = updateState !is UpdateState.Checking
                            ) {
                                scope.launch {
                                    updateState = UpdateState.Checking
                                    try {
                                        val release = onCheckForUpdates()
                                        updateState = if (release != null) {
                                            UpdateState.UpdateAvailable(release)
                                        } else {
                                            UpdateState.UpToDate
                                        }
                                    } catch (e: Exception) {
                                        updateState = UpdateState.Error
                                    }
                                }
                            }
                        )
                        
                        AnimatedVisibility(
                            visible = updateState !is UpdateState.Idle && updateState !is UpdateState.Checking,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                when (updateState) {
                                    is UpdateState.UpToDate -> {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = stringResource(R.string.no_updates),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    is UpdateState.UpdateAvailable -> {
                                        val release = (updateState as UpdateState.UpdateAvailable).release
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.update_message, release.tagName),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Button(
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(release.htmlUrl))
                                                    context.startActivity(intent)
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Download,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(stringResource(R.string.update_button))
                                            }
                                        }
                                    }
                                    is UpdateState.Error -> {
                                        Text(
                                            text = stringResource(R.string.update_error),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    else -> {}
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showThemeDialog) {
        ThemeModeDialog(
            currentMode = settings.themeMode,
            onDismiss = { showThemeDialog = false },
            onSelect = {
                onThemeModeChange(it)
                showThemeDialog = false
            }
        )
    }

    if (showColorDialog) {
        ColorSchemeDialog(
            currentScheme = settings.colorScheme,
            onDismiss = { showColorDialog = false },
            onSelect = {
                onColorSchemeChange(it)
                showColorDialog = false
            }
        )
    }

    if (showLanguageDialog) {
        LanguageDialog(
            currentCode = settings.languageCode,
            onDismiss = { showLanguageDialog = false },
            onSelect = {
                onLanguageChange(it)
                showLanguageDialog = false
            }
        )
    }

    if (showAddPeriodRuleDialog) {
        AddPeriodRuleDialog(
            ruleToEdit = ruleToEdit,
            onDismiss = { 
                showAddPeriodRuleDialog = false 
                ruleToEdit = null
            },
            onConfirm = { rule ->
                val updatedRules = if (ruleToEdit != null) {
                    settings.periodRules.map { if (it.id == rule.id) rule else it }
                } else {
                    settings.periodRules + rule
                }
                onPeriodRulesChange(updatedRules)
                showAddPeriodRuleDialog = false
                ruleToEdit = null
            }
        )
    }

    if (showSoundDialog) {
        SoundSelectionDialog(
            currentIndex = settings.selectedSoundIndex,
            currentVolume = settings.appVolume,
            onDismiss = { showSoundDialog = false },
            onSelect = {
                onSoundChange(it)
                showSoundDialog = false
            }
        )
    }
}

@Composable
private fun SettingsGroup(
    header: String,
    headerColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = header,
            style = MaterialTheme.typography.labelLarge,
            color = headerColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp)
        )
        content()
    }
}

@Composable
private fun ThemeModeDialog(
    currentMode: ThemeMode,
    onDismiss: () -> Unit,
    onSelect: (ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text(stringResource(R.string.theme_mode)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                val options = listOf(
                    ThemeMode.LIGHT to stringResource(R.string.theme_light),
                    ThemeMode.DARK to stringResource(R.string.theme_dark),
                    ThemeMode.SYSTEM to stringResource(R.string.theme_system)
                )
                options.forEach { (mode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = currentMode == mode,
                                onClick = { onSelect(mode) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentMode == mode, onClick = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun ColorSchemeDialog(
    currentScheme: ColorScheme,
    onDismiss: () -> Unit,
    onSelect: (ColorScheme) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text(stringResource(R.string.color_scheme)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                val options = listOf(
                    ColorScheme.MONET to stringResource(R.string.color_monet),
                    ColorScheme.GREEN to stringResource(R.string.color_green),
                    ColorScheme.BLUE to stringResource(R.string.color_blue),
                    ColorScheme.PURPLE to stringResource(R.string.color_purple),
                    ColorScheme.TEAL to stringResource(R.string.color_teal),
                    ColorScheme.ORANGE to stringResource(R.string.color_orange),
                    ColorScheme.PINK to stringResource(R.string.color_pink),
                    ColorScheme.RED to stringResource(R.string.color_red)
                )
                options.forEach { (scheme, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = currentScheme == scheme,
                                onClick = { onSelect(scheme) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentScheme == scheme, onClick = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun LanguageDialog(
    currentCode: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text(stringResource(R.string.language)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                languages.forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = currentCode == lang.code,
                                onClick = { onSelect(lang.code) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = currentCode == lang.code, onClick = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(lang.nameRes),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddPeriodRuleDialog(
    ruleToEdit: PeriodRule? = null,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onConfirm: (PeriodRule) -> Unit
) {
    var scheduleType by remember { mutableStateOf(ruleToEdit?.scheduleType ?: RuleScheduleType.WEEKLY_DAYS) }
    var startHour by remember { mutableIntStateOf(ruleToEdit?.startHour ?: 9) }
    var startMinute by remember { mutableIntStateOf(ruleToEdit?.startMinute ?: 0) }
    var endHour by remember { mutableIntStateOf(ruleToEdit?.endHour ?: 17) }
    var endMinute by remember { mutableIntStateOf(ruleToEdit?.endMinute ?: 0) }
    var selectedDays by remember { mutableStateOf(ruleToEdit?.daysOfWeek ?: setOf<Int>()) }
    var isAllDay by remember { mutableStateOf(ruleToEdit?.isAllDay ?: false) }
    
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val calendar = Calendar.getInstance()
    if (ruleToEdit != null && ruleToEdit.year != 0) {
        calendar.set(ruleToEdit.year, ruleToEdit.month, ruleToEdit.dayOfMonth)
    }
    var selectedDateMillis by remember { 
        mutableStateOf(calendar.timeInMillis) 
    }

    val dayNames = listOf(
        stringResource(R.string.day_sun_full),
        stringResource(R.string.day_mon_full),
        stringResource(R.string.day_tue_full),
        stringResource(R.string.day_wed_full),
        stringResource(R.string.day_thu_full),
        stringResource(R.string.day_fri_full),
        stringResource(R.string.day_sat_full)
    )

    fun formatTime(hour: Int, minute: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(cal.time)
    }
    
    fun formatDate(millis: Long): String {
        return java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM).format(java.util.Date(millis))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { 
            Text(
                if (ruleToEdit != null) stringResource(R.string.add_period_rule).replace("Add", "Edit") // Fallback if string not changed yet
                else stringResource(R.string.add_period_rule)
            ) 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.schedule_type),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = scheduleType == RuleScheduleType.WEEKLY_DAYS,
                            onClick = { scheduleType = RuleScheduleType.WEEKLY_DAYS },
                            label = { Text(stringResource(R.string.rule_type_weekly)) }
                        )
                        FilterChip(
                            selected = scheduleType == RuleScheduleType.SPECIFIC_DATE,
                            onClick = { scheduleType = RuleScheduleType.SPECIFIC_DATE },
                            label = { Text(stringResource(R.string.rule_type_date)) }
                        )
                    }
                }

                if (scheduleType == RuleScheduleType.WEEKLY_DAYS) {
                    Column {
                        Text(
                            text = stringResource(R.string.select_days),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            dayNames.forEachIndexed { index, name ->
                                FilterChip(
                                    selected = index in selectedDays,
                                    onClick = {
                                        selectedDays = if (index in selectedDays) {
                                            selectedDays - index
                                        } else {
                                            selectedDays + index
                                        }
                                    },
                                    label = { Text(name) }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.all_day),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Switch(
                                checked = isAllDay,
                                onCheckedChange = { isAllDay = it }
                            )
                        }
                    }
                }

                if (scheduleType == RuleScheduleType.SPECIFIC_DATE) {
                    Column {
                        Text(
                            text = stringResource(R.string.select_date),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedCard(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatDate(selectedDateMillis),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                if (!(scheduleType == RuleScheduleType.WEEKLY_DAYS && isAllDay)) {
                    Column {
                        Text(
                            text = stringResource(R.string.period_rule_start),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedCard(
                            onClick = { showStartTimePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatTime(startHour, startMinute),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Column {
                        Text(
                            text = stringResource(R.string.period_rule_end),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedCard(
                            onClick = { showEndTimePicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatTime(endHour, endMinute),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onDelete != null) {
                    TextButton(
                        onClick = onDelete
                    ) {
                        Text(
                            text = stringResource(R.string.delete),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
                TextButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply { 
                            timeInMillis = selectedDateMillis 
                        }
                        onConfirm(
                            PeriodRule(
                                id = ruleToEdit?.id ?: UUID.randomUUID().toString(),
                                scheduleType = scheduleType,
                                startHour = if (isAllDay) 0 else startHour,
                                startMinute = if (isAllDay) 0 else startMinute,
                                endHour = if (isAllDay) 23 else endHour,
                                endMinute = if (isAllDay) 59 else endMinute,
                                daysOfWeek = selectedDays,
                                year = cal.get(Calendar.YEAR),
                                month = cal.get(Calendar.MONTH),
                                dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
                                isEnabled = ruleToEdit?.isEnabled ?: true,
                                isAllDay = isAllDay
                            )
                        )
                    },
                    enabled = when (scheduleType) {
                        RuleScheduleType.WEEKLY_DAYS -> selectedDays.isNotEmpty()
                        else -> true
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        },
        dismissButton = {}
    )
    
    if (showStartTimePicker) {
        TimePickerDialog(
            initialHour = startHour,
            initialMinute = startMinute,
            onDismiss = { showStartTimePicker = false },
            onConfirm = { hour, minute ->
                startHour = hour
                startMinute = minute
                showStartTimePicker = false
            }
        )
    }
    
    if (showEndTimePicker) {
        TimePickerDialog(
            initialHour = endHour,
            initialMinute = endMinute,
            onDismiss = { showEndTimePicker = false },
            onConfirm = { hour, minute ->
                endHour = hour
                endMinute = minute
                showEndTimePicker = false
            }
        )
    }
    
    if (showDatePicker) {
        DatePickerModal(
            initialDateMillis = selectedDateMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = { millis ->
                selectedDateMillis = millis
                showDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(stringResource(R.string.select_time)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                onConfirm(timePickerState.hour, timePickerState.minute) 
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerModal(
    initialDateMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { 
                datePickerState.selectedDateMillis?.let { onConfirm(it) }
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}


@Composable
private fun SoundSelectionDialog(
    currentIndex: Int,
    currentVolume: Float,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val context = LocalContext.current
    var tempSelected by remember { mutableStateOf(currentIndex) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text(stringResource(R.string.sound_selection)) },
        text = {
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .verticalScroll(rememberScrollState())
            ) {
                (1..8).forEach { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = tempSelected == index,
                                onClick = {
                                    tempSelected = index
                                    
                                    try {
                                        mediaPlayer?.release()
                                        val resId = getSoundResourceId(index)
                                        mediaPlayer = MediaPlayer.create(context, resId)
                                        mediaPlayer?.setVolume(currentVolume, currentVolume)
                                        mediaPlayer?.setOnCompletionListener { it.release() }
                                        mediaPlayer?.start()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = tempSelected == index, onClick = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.sound_name, index), 
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.sound_play_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSelect(tempSelected)
            }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun getSoundResourceId(index: Int): Int {
    return when (index) {
        1 -> R.raw.zikr_sound_1
        2 -> R.raw.zikr_sound_2
        3 -> R.raw.zikr_sound_3
        4 -> R.raw.zikr_sound_4
        5 -> R.raw.zikr_sound_5
        6 -> R.raw.zikr_sound_6
        7 -> R.raw.zikr_sound_7
        8 -> R.raw.zikr_sound_8
        else -> R.raw.zikr_sound_1
    }
}
