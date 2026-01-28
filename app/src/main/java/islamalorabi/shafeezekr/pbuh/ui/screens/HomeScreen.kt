package islamalorabi.shafeezekr.pbuh.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.data.AppSettings
import islamalorabi.shafeezekr.pbuh.data.ReminderInterval
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    settings: AppSettings,
    onReminderEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (ReminderInterval) -> Unit,
    onCustomIntervalChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var showCustomDialog by remember { mutableStateOf(false) }
    
    val sharedPrefs = remember { context.getSharedPreferences("reminder_prefs", android.content.Context.MODE_PRIVATE) }
    var remainingTime by remember { mutableLongStateOf(0L) }
    
    LaunchedEffect(settings.isReminderEnabled, settings.reminderInterval, settings.customIntervalMinutes) {
        if (settings.isReminderEnabled) {
            val nextTrigger = sharedPrefs.getLong("next_trigger_time", 0L)
            val now = System.currentTimeMillis()
            
            // If we have a stale alarm (time passed), reschedule immediately
            if (nextTrigger < now) {
                val intervalMinutes = if (settings.reminderInterval == ReminderInterval.CUSTOM) {
                    settings.customIntervalMinutes
                } else {
                    settings.reminderInterval.minutes
                }
                
                // Only reschedule if we have a valid interval
                if (intervalMinutes > 0) {
                    islamalorabi.shafeezekr.pbuh.service.ReminderScheduler.scheduleNextAlarm(context)
                }
            }
        }

        while (settings.isReminderEnabled) {
            val nextTrigger = sharedPrefs.getLong("next_trigger_time", 0L)
            val now = System.currentTimeMillis()
            remainingTime = if (nextTrigger > now) nextTrigger - now else 0L
            delay(1000L)
        }
        remainingTime = 0L
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                onClick = {
                    islamalorabi.shafeezekr.pbuh.service.SoundPlayer.play(
                        context,
                        settings.selectedSoundIndex,
                        settings.appVolume
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.home_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_volume_up),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.tap_to_listen),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
        
        item {
            AnimatedVisibility(
                visible = settings.isReminderEnabled && remainingTime > 0,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val minutes = (remainingTime / 60000).toInt()
                val seconds = ((remainingTime % 60000) / 1000).toInt()
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.next_reminder),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            SettingsGroup(
                header = stringResource(R.string.enable_reminder),
                headerColor = MaterialTheme.colorScheme.primary
            ) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = stringResource(R.string.enable_reminder),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        supportingContent = {
                            Text(
                                text = stringResource(R.string.enable_reminder_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_pbuh),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingContent = {
                            Switch(
                                checked = settings.isReminderEnabled,
                                onCheckedChange = { enabled ->
                                    onReminderEnabledChange(enabled)
                                }
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }
        }

        item {
            AnimatedVisibility(visible = settings.isReminderEnabled) {
                SettingsGroup(
                    header = stringResource(R.string.interval_title),
                    headerColor = MaterialTheme.colorScheme.primary
                ) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectableGroup()
                        ) {
                            Text(
                                text = stringResource(R.string.interval_reset_note),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
                            )

                            val intervals = listOf(
                                ReminderInterval.ONE to stringResource(R.string.interval_1_min),
                                ReminderInterval.FIVE to stringResource(R.string.interval_5_min),
                                ReminderInterval.TEN to stringResource(R.string.interval_10_min),
                                ReminderInterval.THIRTY to stringResource(R.string.interval_30_min),
                                ReminderInterval.SIXTY to stringResource(R.string.interval_1_hour),
                                ReminderInterval.TWO_HOURS to stringResource(R.string.interval_2_hours),
                                ReminderInterval.CUSTOM to stringResource(R.string.interval_custom)
                            )

                            intervals.forEach { (interval, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = settings.reminderInterval == interval,
                                            onClick = {
                                                if (interval == ReminderInterval.CUSTOM) {
                                                    showCustomDialog = true
                                                }
                                                onIntervalChange(interval)
                                            },
                                            role = Role.RadioButton
                                        )
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = settings.reminderInterval == interval,
                                        onClick = null
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = if (interval == ReminderInterval.CUSTOM && settings.reminderInterval == ReminderInterval.CUSTOM) {
                                            "$label (${settings.customIntervalMinutes} ${stringResource(R.string.minutes)})"
                                        } else {
                                            label
                                        },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCustomDialog) {
        CustomIntervalDialog(
            currentValue = settings.customIntervalMinutes,
            onDismiss = { showCustomDialog = false },
            onConfirm = { minutes ->
                onCustomIntervalChange(minutes)
                showCustomDialog = false
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
private fun CustomIntervalDialog(
    currentValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var hours by remember { mutableStateOf(currentValue / 60) }
    var minutes by remember { mutableStateOf(currentValue % 60) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = { Text(stringResource(R.string.custom_interval_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberPickerColumn(
                        value = hours,
                        range = 0..23,
                        onValueChange = { hours = it },
                        label = stringResource(R.string.hours_label)
                    )
                    
                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    NumberPickerColumn(
                        value = minutes,
                        range = 0..59,
                        onValueChange = { minutes = it },
                        label = stringResource(R.string.minutes_label)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val totalMinutes = hours * 60 + minutes
                Text(
                    text = stringResource(R.string.total_minutes, totalMinutes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val totalMinutes = hours * 60 + minutes
                    onConfirm(if (totalMinutes > 0) totalMinutes else 1)
                }
            ) {
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

@Composable
private fun NumberPickerColumn(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedCard(
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                androidx.compose.material3.IconButton(
                    onClick = { if (value < range.last) onValueChange(value + 1) }
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = String.format("%02d", value),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                androidx.compose.material3.IconButton(
                    onClick = { if (value > range.first) onValueChange(value - 1) }
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Remove,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
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
