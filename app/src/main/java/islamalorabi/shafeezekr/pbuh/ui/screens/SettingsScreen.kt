@file:Suppress("KotlinConstantConditions")
package islamalorabi.shafeezekr.pbuh.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.data.AppSettings
import islamalorabi.shafeezekr.pbuh.data.AudioStreamType
import islamalorabi.shafeezekr.pbuh.data.ColorScheme
import islamalorabi.shafeezekr.pbuh.data.PeriodRule
import islamalorabi.shafeezekr.pbuh.data.RuleScheduleType
import islamalorabi.shafeezekr.pbuh.data.ThemeMode
import islamalorabi.shafeezekr.pbuh.util.LocaleUtils
import java.util.Calendar
import java.util.UUID
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds


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
fun getLocalizedRuleDisplayText(rule: PeriodRule): String {
    val dayAbbreviations = listOf(
        stringResource(R.string.day_sun),
        stringResource(R.string.day_mon),
        stringResource(R.string.day_tue),
        stringResource(R.string.day_wed),
        stringResource(R.string.day_thu),
        stringResource(R.string.day_fri),
        stringResource(R.string.day_sat)
    )
    val allWeek = stringResource(R.string.all_week)
    val allDay = stringResource(R.string.all_day)

    val isEffectivelyAllDay = rule.isAllDay || 
        (rule.startHour == 0 && rule.startMinute == 0 && rule.endHour == 23 && rule.endMinute == 59)
    
    val timeRange = if (isEffectivelyAllDay) {
        allDay
    } else {
        LocaleUtils.formatLocalizedTimeRange(rule.startHour, rule.startMinute, rule.endHour, rule.endMinute)
    }

    return when (rule.scheduleType) {
        RuleScheduleType.WEEKLY_DAYS -> {
            val days = if (rule.daysOfWeek.size == 7) {
                allWeek
            } else {
                rule.daysOfWeek.sorted().joinToString(", ") { dayAbbreviations[it] }
            }
            "$days | $timeRange"
        }
        RuleScheduleType.SPECIFIC_DATE -> {
            val localizedDate = LocaleUtils.localizeString(String.format(java.util.Locale.US, "%04d-%02d-%02d", rule.year, rule.month + 1, rule.dayOfMonth))
            "$localizedDate | $timeRange"
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("BatteryLife")
@Composable
fun SettingsScreen(
    settings: AppSettings,
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
    onAudioStreamTypeChange: (AudioStreamType) -> Unit,
    onAutoDismissNotificationChange: (Boolean) -> Unit,
    onUseSystemVolumeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAddPeriodRuleDialog by remember { mutableStateOf(false) }
    var ruleToEdit by remember { mutableStateOf<PeriodRule?>(null) }
    var showSoundDialog by remember { mutableStateOf(false) }
    var showRecordDialog by remember { mutableStateOf(false) }
    var showAudioStreamDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val errorAudioTooLong = stringResource(R.string.error_audio_too_long)
    val tilePauseDhikr = stringResource(R.string.tile_pause_dhikr)

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            scope.launch {
                try {
                    val tempFile = java.io.File(context.filesDir, "temp_import.mp3")
                    context.contentResolver.openInputStream(sourceUri)?.use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    val mp = MediaPlayer()
                    mp.setDataSource(tempFile.absolutePath)
                    mp.prepare()
                    val durationMs = mp.duration
                    mp.release()

                    if (durationMs > 10_000) {
                        tempFile.delete()
                        kotlinx.coroutines.Dispatchers.Main.let {
                            android.widget.Toast.makeText(context, errorAudioTooLong, android.widget.Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    val destFile = java.io.File(context.filesDir, "custom_zikr.mp3")
                    tempFile.renameTo(destFile)
                    onCustomSoundPathChange(destFile.absolutePath)
                    onCustomSoundEnabledChange(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val phoneStatePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onMuteOnCallChange(true)
        }
    }

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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
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
                                    text = if (settings.isCustomSoundEnabled) {
                                        stringResource(R.string.custom_audio_option)
                                    } else {
                                        stringResource(R.string.sound_name, LocaleUtils.formatLocalizedNumber(settings.selectedSoundIndex))
                                    },
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

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.custom_audio_option),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = if (settings.isCustomSoundEnabled && !settings.customSoundPath.isNullOrEmpty()) {
                                        stringResource(R.string.sound_custom_selected)
                                    } else {
                                        stringResource(R.string.custom_audio_desc)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (settings.isCustomSoundEnabled && !settings.customSoundPath.isNullOrEmpty()) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        filePickerLauncher.launch("audio/*")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.select_audio_file))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            showRecordDialog = true
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(stringResource(R.string.record_voice))
                                    }

                                    if (settings.isCustomSoundEnabled) {
                                        TextButton(
                                            onClick = {
                                                onCustomSoundEnabledChange(false)
                                                onCustomSoundPathChange(null)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {},
                                    onLongClick = {
                                        onUseSystemVolumeChange(!settings.useSystemVolume)
                                    }
                                )
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
                                    text = if (settings.useSystemVolume) {
                                        stringResource(R.string.use_system_volume_active)
                                    } else {
                                        stringResource(R.string.app_volume_desc)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (settings.useSystemVolume) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Text(
                                    text = if (settings.useSystemVolume) {
                                        stringResource(R.string.use_system_volume_hint_off)
                                    } else {
                                        stringResource(R.string.use_system_volume_hint)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            if (!settings.useSystemVolume) {
                                val maxVolume = islamalorabi.shafeezekr.pbuh.util.AudioHelper.getMaxVolume(context, settings.audioStreamType)
                                val currentStep = (settings.appVolume * maxVolume).roundToInt().coerceIn(1, maxVolume)
                                Text(
                                    text = "${LocaleUtils.formatLocalizedNumber(currentStep)}/${LocaleUtils.formatLocalizedNumber(maxVolume)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        AnimatedVisibility(
                            visible = !settings.useSystemVolume,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            val maxVolume = islamalorabi.shafeezekr.pbuh.util.AudioHelper.getMaxVolume(context, settings.audioStreamType)
                            val minValue = 1f / maxVolume
                            Slider(
                                value = settings.appVolume.coerceIn(minValue, 1f),
                                onValueChange = { onVolumeChange(it.coerceAtLeast(minValue)) },
                                valueRange = 0f..1f,
                                steps = maxVolume - 1,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 16.dp)
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.mute_on_call),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Column {
                                    Text(
                                        text = stringResource(R.string.mute_on_call_desc),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = stringResource(R.string.mute_on_call_permission_note),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.PhoneInTalk,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.muteOnCall,
                                    onCheckedChange = { enabled ->
                                        if (enabled) {
                                            if (androidx.core.content.ContextCompat.checkSelfPermission(
                                                    context,
                                                    android.Manifest.permission.READ_PHONE_STATE
                                                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                                            ) {
                                                phoneStatePermissionLauncher.launch(android.Manifest.permission.READ_PHONE_STATE)
                                                return@Switch
                                            }
                                        }
                                        onMuteOnCallChange(enabled)
                                    }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.mute_on_silent),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.mute_on_silent_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.muteOnSilent,
                                    onCheckedChange = { onMuteOnSilentChange(it) }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.mute_on_dnd),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.mute_on_dnd_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.DoNotDisturbOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.muteOnDND,
                                    onCheckedChange = { onMuteOnDNDChange(it) }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.mute_on_media),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.mute_on_media_desc),
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
                                Switch(
                                    checked = settings.muteOnMedia,
                                    onCheckedChange = { onMuteOnMediaChange(it) }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.audio_stream),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = when (settings.audioStreamType) {
                                        AudioStreamType.MEDIA -> stringResource(R.string.audio_stream_media)
                                        AudioStreamType.ALARM -> stringResource(R.string.audio_stream_alarm)
                                        AudioStreamType.NOTIFICATION -> stringResource(R.string.audio_stream_notification)
                                        AudioStreamType.RING -> stringResource(R.string.audio_stream_ring)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingContent = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_volume_up),
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
                            modifier = Modifier.clickable { showAudioStreamDialog = true }
                        )

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.auto_dismiss_notification),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            supportingContent = {
                                Text(
                                    text = stringResource(R.string.auto_dismiss_notification_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = settings.autoDismissNotification,
                                    onCheckedChange = { onAutoDismissNotificationChange(it) }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
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
                Column(modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)) {
                    Text(
                        text = stringResource(R.string.period_rules_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.quiet_hours_edit_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.quiet_hours_delete_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
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
                            settings.periodRules.forEachIndexed { _, rule ->
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
                                                text = getLocalizedRuleDisplayText(rule),
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
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
                                data = "package:${context.packageName}".toUri()
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }

        item {
            SettingsGroup(
                header = stringResource(R.string.quick_settings_tile_section),
                headerColor = MaterialTheme.colorScheme.primary
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = stringResource(R.string.quick_settings_tile_title),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(R.string.quick_settings_tile_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Add,
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
                        modifier = Modifier.clickable {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                val statusBarManager = context.getSystemService(android.app.StatusBarManager::class.java)
                                statusBarManager.requestAddTileService(
                                    android.content.ComponentName(
                                        context,
                                        islamalorabi.shafeezekr.pbuh.service.DhikrTileService::class.java
                                    ),
                                    tilePauseDhikr,
                                    android.graphics.drawable.Icon.createWithResource(context, R.drawable.ic_tile_pause),
                                    context.mainExecutor
                                ) { }
                            } else {
                                val intent = Intent(Settings.ACTION_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    )
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




    if (showSoundDialog) {
        SoundSelectionDialog(
            currentIndex = settings.selectedSoundIndex,
            currentVolume = settings.appVolume,
            isCustomSoundEnabled = settings.isCustomSoundEnabled,
            customSoundPath = settings.customSoundPath,
            audioStreamType = settings.audioStreamType,
            useSystemVolume = settings.useSystemVolume,
            onDismiss = { showSoundDialog = false },
            onSelect = {
                onSoundChange(it)
                showSoundDialog = false
            },
            onCustomSoundEnabledChange = onCustomSoundEnabledChange
        )
    }

    if (showRecordDialog) {
        RecordDhikrDialog(
            onDismiss = { showRecordDialog = false },
            onRecordSaved = { path ->
                onCustomSoundPathChange(path)
                onCustomSoundEnabledChange(true)
                showRecordDialog = false
            },
            onDelete = {
                onCustomSoundPathChange(null)
                onCustomSoundEnabledChange(false)
                showRecordDialog = false
            }
        )
    }

    if (showAudioStreamDialog) {
        AudioStreamDialog(
            currentType = settings.audioStreamType,
            onDismiss = { showAudioStreamDialog = false },
            onSelect = {
                onAudioStreamTypeChange(it)
                showAudioStreamDialog = false
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
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
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
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
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
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
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
    var selectedDays by remember { mutableStateOf(ruleToEdit?.daysOfWeek ?: setOf()) }
    var isAllDay by remember { mutableStateOf(ruleToEdit?.isAllDay ?: false) }
    
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val calendar = Calendar.getInstance()
    if (ruleToEdit != null && ruleToEdit.year != 0) {
        calendar.set(ruleToEdit.year, ruleToEdit.month, ruleToEdit.dayOfMonth)
    }
    var selectedDateMillis by remember { 
        mutableLongStateOf(calendar.timeInMillis) 
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
        title = { 
            Text(
                if (ruleToEdit != null) stringResource(R.string.edit_period_rule)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            FilterChip(
                                selected = selectedDays.size == 7,
                                onClick = {
                                    selectedDays = if (selectedDays.size == 7) {
                                        emptySet()
                                    } else {
                                        setOf(0, 1, 2, 3, 4, 5, 6)
                                    }
                                },
                                label = { Text(stringResource(R.string.select_all_days)) }
                            )
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
                        Card(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
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

                if (!isAllDay) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.period_rule_start),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                onClick = { showStartTimePicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = formatTime(startHour, startMinute),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.period_rule_end),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                onClick = { showEndTimePicker = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = formatTime(endHour, endMinute),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
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
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = when (scheduleType) {
                            RuleScheduleType.WEEKLY_DAYS -> selectedDays.isNotEmpty()
                            else -> true
                        }
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
                if (onDelete != null) {
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.delete))
                    }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = { 
                        onConfirm(timePickerState.hour, timePickerState.minute) 
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        },
        dismissButton = {}
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = { 
                        datePickerState.selectedDateMillis?.let { onConfirm(it) }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        },
        dismissButton = {}
    ) {
        DatePicker(state = datePickerState)
    }
}


@Composable
private fun SoundSelectionDialog(
    currentIndex: Int,
    currentVolume: Float,
    isCustomSoundEnabled: Boolean,
    customSoundPath: String?,
    audioStreamType: AudioStreamType,
    useSystemVolume: Boolean,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
    onCustomSoundEnabledChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var tempSelected by remember { mutableIntStateOf(currentIndex) }
    var tempCustomEnabled by remember { mutableStateOf(isCustomSoundEnabled) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sound_selection)) },
        text = {
            Column(
                modifier = Modifier
                    .selectableGroup()
                    .verticalScroll(rememberScrollState())
            ) {
                (1..9).forEach { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (!tempCustomEnabled && tempSelected == index),
                                onClick = {
                                    tempSelected = index
                                    tempCustomEnabled = false
                                    
                                    mediaPlayer?.release()
                                    mediaPlayer = islamalorabi.shafeezekr.pbuh.util.AudioHelper.playWithMasterVolumeSync(
                                        context = context,
                                        soundIndex = index,
                                        appVolume = currentVolume,
                                        muteOnSilent = false,
                                        muteOnDND = false,
                                        customSoundPath = customSoundPath,
                                        isCustomSoundEnabled = false,
                                        audioStreamType = audioStreamType,
                                        useSystemVolume = useSystemVolume
                                    )
                                },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (!tempCustomEnabled && tempSelected == index), onClick = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.sound_name, LocaleUtils.formatLocalizedNumber(index)), 
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                if (!customSoundPath.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = tempCustomEnabled,
                                onClick = {
                                    tempCustomEnabled = true
                                    mediaPlayer?.release()
                                    mediaPlayer = islamalorabi.shafeezekr.pbuh.util.AudioHelper.playWithMasterVolumeSync(
                                        context = context,
                                        soundIndex = tempSelected,
                                        appVolume = currentVolume,
                                        muteOnSilent = false,
                                        muteOnDND = false,
                                        customSoundPath = customSoundPath,
                                        isCustomSoundEnabled = true,
                                        audioStreamType = audioStreamType,
                                        useSystemVolume = useSystemVolume
                                    )
                                },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = tempCustomEnabled, onClick = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = stringResource(R.string.custom_audio_option),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = {
                        onCustomSoundEnabledChange(tempCustomEnabled)
                        onSelect(tempSelected)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        },
        dismissButton = {}
    )
}

@Composable
private fun RecordDhikrDialog(
    onDismiss: () -> Unit,
    onRecordSaved: (String) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val errorRecord = stringResource(R.string.error_record)
    val errorPermission = stringResource(R.string.error_permission)
    var pendingRecordAfterPermission by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var isPlayingRecorded by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<android.media.MediaRecorder?>(null) }
    var recorderMediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val recordFile = remember { java.io.File(context.filesDir, "recorded_zikr.m4a") }
    var durationSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            durationSeconds = 0
            while (isRecording && durationSeconds < 10) {
                kotlinx.coroutines.delay(1.seconds)
                durationSeconds++
            }
            if (durationSeconds >= 10 && isRecording) {
                try {
                    mediaRecorder?.stop()
                    mediaRecorder?.release()
                    mediaRecorder = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                isRecording = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaRecorder?.release()
            } catch (_: Exception) {}
            try {
                recorderMediaPlayer?.release()
            } catch (_: Exception) {}
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.record_dialog_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.record_dialog_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = stringResource(R.string.record_max_duration_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        text = stringResource(R.string.record_permission_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                if (isRecording) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(java.util.Locale.US, "00:%02d / 00:10", durationSeconds),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (recordFile.exists()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.sound_custom_selected),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isRecording) {
                        Button(
                            onClick = {
                                try {
                                    mediaRecorder?.stop()
                                    mediaRecorder?.release()
                                    mediaRecorder = null
                                    isRecording = false
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.stop_btn))
                        }
                    } else {
                        val permissionLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestPermission()
                        ) { isGranted ->
                            if (isGranted) {
                                pendingRecordAfterPermission = true
                            } else {
                                android.widget.Toast.makeText(context, errorPermission, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }

                        fun startRecording() {
                            try {
                                if (isPlayingRecorded) {
                                    recorderMediaPlayer?.stop()
                                    recorderMediaPlayer?.release()
                                    recorderMediaPlayer = null
                                    isPlayingRecorded = false
                                }
                                if (recordFile.exists()) {
                                    recordFile.delete()
                                }
                                val recorder = android.media.MediaRecorder(context)
                                recorder.setAudioSource(android.media.MediaRecorder.AudioSource.VOICE_RECOGNITION)
                                recorder.setOutputFormat(android.media.MediaRecorder.OutputFormat.MPEG_4)
                                recorder.setAudioEncoder(android.media.MediaRecorder.AudioEncoder.AAC)
                                recorder.setAudioSamplingRate(44100)
                                recorder.setAudioEncodingBitRate(192000)
                                recorder.setAudioChannels(1)
                                recorder.setMaxDuration(10_000)
                                recorder.setOutputFile(recordFile.absolutePath)
                                recorder.prepare()
                                recorder.start()
                                mediaRecorder = recorder
                                isRecording = true
                            } catch (e: Exception) {
                                e.printStackTrace()
                                android.widget.Toast.makeText(context, errorRecord, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }

                        LaunchedEffect(pendingRecordAfterPermission) {
                            if (pendingRecordAfterPermission) {
                                pendingRecordAfterPermission = false
                                startRecording()
                            }
                        }

                        Button(
                            onClick = {
                                val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.RECORD_AUDIO
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    startRecording()
                                } else {
                                    permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Mic, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.record_btn))
                        }

                        if (recordFile.exists()) {
                            Button(
                                onClick = {
                                    if (isPlayingRecorded) {
                                        try {
                                            recorderMediaPlayer?.stop()
                                            recorderMediaPlayer?.release()
                                            recorderMediaPlayer = null
                                        } catch (_: Exception) {}
                                        isPlayingRecorded = false
                                    } else {
                                        try {
                                            val mp = MediaPlayer().apply {
                                                setDataSource(recordFile.absolutePath)
                                                prepare()
                                                start()
                                            }
                                            recorderMediaPlayer = mp
                                            isPlayingRecorded = true
                                            mp.setOnCompletionListener {
                                                isPlayingRecorded = false
                                                it.release()
                                                recorderMediaPlayer = null
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = if (isPlayingRecorded) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isPlayingRecorded) stringResource(R.string.stop_btn) else stringResource(R.string.play_btn))
                            }

                            Button(
                                onClick = {
                                    onRecordSaved(recordFile.absolutePath)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.save))
                            }

                            Button(
                                onClick = {
                                    if (isPlayingRecorded) {
                                        try {
                                            recorderMediaPlayer?.stop()
                                            recorderMediaPlayer?.release()
                                            recorderMediaPlayer = null
                                        } catch (_: Exception) {}
                                        isPlayingRecorded = false
                                    }
                                    if (recordFile.exists()) {
                                        recordFile.delete()
                                    }
                                    onDelete()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.delete_btn))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        dismissButton = {}
    )
}

@Composable
private fun AudioStreamDialog(
    currentType: AudioStreamType,
    onDismiss: () -> Unit,
    onSelect: (AudioStreamType) -> Unit
) {
    var tempSelected by remember { mutableStateOf(currentType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.audio_stream)) },
        text = {
            Column {
                AudioStreamType.entries.forEach { type ->
                    val label = when (type) {
                        AudioStreamType.MEDIA -> stringResource(R.string.audio_stream_media)
                        AudioStreamType.ALARM -> stringResource(R.string.audio_stream_alarm)
                        AudioStreamType.NOTIFICATION -> stringResource(R.string.audio_stream_notification)
                        AudioStreamType.RING -> stringResource(R.string.audio_stream_ring)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = tempSelected == type,
                                onClick = { tempSelected = type },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = tempSelected == type, onClick = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSelect(tempSelected) }) {
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
