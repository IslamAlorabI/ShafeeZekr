package islamalorabi.shafeezekr.pbuh.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text

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
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.core.content.ContextCompat
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.data.AppSettings
import islamalorabi.shafeezekr.pbuh.data.ReminderInterval
import islamalorabi.shafeezekr.pbuh.service.ReminderScheduler
import kotlinx.coroutines.delay
import islamalorabi.shafeezekr.pbuh.util.LocaleUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalDensity

@Composable
fun HomeScreen(
    settings: AppSettings,
    onReminderEnabledChange: (Boolean) -> Unit,
    onIntervalChange: (ReminderInterval) -> Unit,
    onCustomIntervalChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            onReminderEnabledChange(true)
        }
    }

    var showCustomDialog by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            delay(4000)
            isPlaying = false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    val bar1Scale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2Scale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3Scale by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )
    
    val sharedPrefs = remember { context.getSharedPreferences("reminder_prefs", android.content.Context.MODE_PRIVATE) }
    var remainingTime by remember { mutableLongStateOf(0L) }
    var isPausedByQuiet by remember { mutableStateOf(false) }
    
    LaunchedEffect(settings.isReminderEnabled, settings.reminderInterval, settings.customIntervalMinutes, settings.periodRules) {
        while (settings.isReminderEnabled) {
            val quietBlocked = !settings.isReminderAllowedByPeriodRules()
            val prefPaused = ReminderScheduler.isPausedForQuietHours(context)
            
            if (quietBlocked && !prefPaused) {
                val quietEndMillis = settings.getQuietHoursEndMillis()
                ReminderScheduler.pauseForQuietHours(context, quietEndMillis)
            }
            
            if (!quietBlocked && prefPaused) {
                ReminderScheduler.resumeFromQuietHours(context)
            }
            
            val currentlyPaused = quietBlocked || ReminderScheduler.isPausedForQuietHours(context)
            isPausedByQuiet = currentlyPaused
            
            if (currentlyPaused) {
                remainingTime = 0L
            } else {
                val nextTrigger = sharedPrefs.getLong("next_trigger_time", 0L)
                val now = System.currentTimeMillis()
                remainingTime = if (nextTrigger > now) nextTrigger - now else 0L
            }
            delay(1000L)
        }
        remainingTime = 0L
        isPausedByQuiet = false
    }

    val timerState = when {
        !settings.isReminderEnabled -> TimerState.PAUSED
        isPausedByQuiet -> TimerState.BLOCKED_BY_QUIET
        else -> TimerState.ACTIVE
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                onClick = {
                    isPlaying = true
                    islamalorabi.shafeezekr.pbuh.util.AudioHelper.playWithMasterVolume(
                        context = context,
                        soundIndex = settings.selectedSoundIndex,
                        appVolume = settings.appVolume,
                        muteOnSilent = false,
                        muteOnDND = false,
                        customSoundPath = settings.customSoundPath,
                        isCustomSoundEnabled = settings.isCustomSoundEnabled
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(36.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp),
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isPlaying) {
                            Row(
                                modifier = Modifier.padding(end = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val barColor = MaterialTheme.colorScheme.onPrimaryContainer
                                Box(Modifier.size(3.dp, 16.dp * bar1Scale).background(barColor, RoundedCornerShape(1.dp)))
                                Box(Modifier.size(3.dp, 16.dp * bar2Scale).background(barColor, RoundedCornerShape(1.dp)))
                                Box(Modifier.size(3.dp, 16.dp * bar3Scale).background(barColor, RoundedCornerShape(1.dp)))
                            }
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_volume_up),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlaying) stringResource(R.string.playing) else stringResource(R.string.tap_to_listen),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
        
        item {
            CountdownCard(settings, remainingTime, timerState)
        }

        item {
            SettingsGroup(
                header = stringResource(R.string.enable_reminder),
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
                            Column {
                                Text(
                                    text = stringResource(R.string.enable_reminder),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (settings.isReminderEnabled && !settings.isReminderAllowedByPeriodRules()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(R.string.quiet_hours_active),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
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
                                enabled = true,
                                onCheckedChange = { enabled ->
                                    if (enabled && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        onReminderEnabledChange(enabled)
                                    }
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
            val sectionAlpha by animateFloatAsState(
                targetValue = if (settings.isReminderEnabled) 1f else 0.4f,
                animationSpec = tween(300),
                label = "sectionAlpha"
            )
            Box(
                modifier = Modifier
                    .graphicsLayer { alpha = sectionAlpha }
            ) {
                IntervalSettingsContent(
                    settings = settings,
                    onIntervalChange = if (settings.isReminderEnabled) onIntervalChange else { _ -> },
                    onShowCustomDialog = if (settings.isReminderEnabled) { { showCustomDialog = true } } else { {} }
                )
            }
        }
    }

    if (showCustomDialog) {
        CustomIntervalDialog(
            currentValue = settings.customIntervalMinutes,
            onDismiss = { showCustomDialog = false },
            onConfirm = { minutes ->
                onIntervalChange(ReminderInterval.CUSTOM)
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
private fun IntervalSettingsContent(
    settings: AppSettings,
    onIntervalChange: (ReminderInterval) -> Unit,
    onShowCustomDialog: () -> Unit
) {
    SettingsGroup(
        header = stringResource(R.string.interval_title),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.interval_reset_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 12.dp)
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 0 until 6 step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val first = intervals[i]
                            val second = intervals[i + 1]

                            IntervalPillButton(
                                label = first.second,
                                isSelected = settings.reminderInterval == first.first,
                                onClick = {
                                    onIntervalChange(first.first)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            IntervalPillButton(
                                label = second.second,
                                isSelected = settings.reminderInterval == second.first,
                                onClick = {
                                    onIntervalChange(second.first)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    val customInterval = intervals[6]
                    val customLabel = if (settings.reminderInterval == ReminderInterval.CUSTOM) {
                        "${customInterval.second} (${LocaleUtils.formatLocalizedNumber(settings.customIntervalMinutes)} ${stringResource(R.string.minutes)})"
                    } else {
                        customInterval.second
                    }
                    IntervalPillButton(
                        label = customLabel,
                        isSelected = settings.reminderInterval == ReminderInterval.CUSTOM,
                        onClick = onShowCustomDialog,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
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
                        range = 0..12,
                        onValueChange = {
                            hours = it
                            if (it == 12) minutes = 0
                        },
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
                        range = if (hours == 12) 0..0 else 0..59,
                        onValueChange = { minutes = it },
                        label = stringResource(R.string.minutes_label),
                        enabled = hours < 12
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val totalMinutes = hours * 60 + minutes
                val isMaxReached = hours == 12

                androidx.compose.animation.Crossfade(
                    targetState = isMaxReached,
                    animationSpec = tween(150),
                    label = "maxReachedTransition"
                ) { maxReached ->
                    if (maxReached) {
                        Text(
                            text = stringResource(R.string.max_interval_reached),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.total_minutes, totalMinutes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = {
                        val totalMinutes = hours * 60 + minutes
                        onConfirm(if (totalMinutes > 0) totalMinutes else 1)
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
private fun NumberPickerColumn(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    label: String,
    enabled: Boolean = true
) {
    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.38f,
        animationSpec = tween(300),
        label = "pickerAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer { this.alpha = alpha }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                androidx.compose.material3.IconButton(
                    onClick = { if (enabled && value < range.last) onValueChange(value + 1) },
                    enabled = enabled && value < range.last
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = LocaleUtils.formatLocalizedNumber(value, 2),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                androidx.compose.material3.IconButton(
                    onClick = { if (enabled && value > range.first) onValueChange(value - 1) },
                    enabled = enabled && value > range.first
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun formatLocaleTime(minutes: Int, seconds: Int, localeTag: String): String {
    val formatted = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
    return LocaleUtils.localizeString(formatted)
}

@Composable
private fun IntervalPillButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Card(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

private enum class TimerState {
    ACTIVE, PAUSED, BLOCKED_BY_QUIET
}

@Composable
private fun CountdownCard(settings: AppSettings, remainingTime: Long, timerState: TimerState) {
    val totalIntervalMinutes = if (settings.reminderInterval == ReminderInterval.CUSTOM) {
        settings.customIntervalMinutes
    } else {
        settings.reminderInterval.minutes
    }
    val isActive = timerState == TimerState.ACTIVE
    val isBlocked = timerState == TimerState.BLOCKED_BY_QUIET

    val labelText = stringResource(R.string.next_reminder)
    val labelStyle = MaterialTheme.typography.labelMedium
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val safePadding = 24.dp
    val activeMinSize = remember(labelText, labelStyle, density) {
        with(density) {
            val textWidth = textMeasurer.measure(labelText, labelStyle).size.width.toDp()
            maxOf(160.dp, textWidth + safePadding * 2)
        }
    }
    val circleSize by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isActive) activeMinSize else 100.dp,
        animationSpec = tween(400),
        label = "circleSize"
    )
    val strokeWidth by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isActive) 8.dp else 6.dp,
        animationSpec = tween(400),
        label = "strokeWidth"
    )

    val totalTimeMs = totalIntervalMinutes * 60 * 1000L
    val progress = if (totalTimeMs > 0 && isActive) {
        (remainingTime.toFloat() / totalTimeMs).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "countdownProgress"
    )

    val arcColor = when (timerState) {
        TimerState.ACTIVE -> MaterialTheme.colorScheme.primary
        TimerState.PAUSED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        TimerState.BLOCKED_BY_QUIET -> MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
    }
    val trackColor = when (timerState) {
        TimerState.ACTIVE -> MaterialTheme.colorScheme.surfaceVariant
        TimerState.PAUSED -> MaterialTheme.colorScheme.surfaceVariant
        TimerState.BLOCKED_BY_QUIET -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    }
    val statusColor = when (timerState) {
        TimerState.ACTIVE -> MaterialTheme.colorScheme.primary
        TimerState.PAUSED -> MaterialTheme.colorScheme.onSurfaceVariant
        TimerState.BLOCKED_BY_QUIET -> MaterialTheme.colorScheme.error
    }
    val statusText = when (timerState) {
        TimerState.ACTIVE -> ""
        TimerState.PAUSED -> stringResource(R.string.timer_paused)
        TimerState.BLOCKED_BY_QUIET -> stringResource(R.string.timer_blocked_by_quiet)
    }

    val minutes = (remainingTime / 60000).toInt()
    val seconds = ((remainingTime % 60000) / 1000).toInt()
    val locale = androidx.compose.ui.text.intl.Locale.current.toLanguageTag()
    val formattedTime = formatLocaleTime(minutes, seconds, locale)

    val textAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.3f,
        animationSpec = tween(400),
        label = "textAlpha"
    )
    val labelAlpha by animateFloatAsState(
        targetValue = if (isActive) 0.8f else 0f,
        animationSpec = tween(300),
        label = "labelAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "statusPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        val horizontalBias by animateFloatAsState(
            targetValue = if (isActive) 0f else -1f,
            animationSpec = tween(400),
            label = "circleBias"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            contentAlignment = androidx.compose.ui.BiasAlignment(horizontalBias, 0f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(circleSize),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(if (isActive) 8.dp else 4.dp)) {
                        val sw = strokeWidth.toPx()
                        drawCircle(
                            color = trackColor,
                            style = Stroke(width = sw)
                        )
                        drawArc(
                            color = arcColor,
                            startAngle = 270f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = sw, cap = StrokeCap.Round)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isActive) {
                            Text(
                                text = stringResource(R.string.next_reminder),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = labelAlpha),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        Text(
                            text = if (isActive) formattedTime else "--:--",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isActive) 28.sp else 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !isActive,
                    enter = fadeIn(tween(400)) + expandHorizontally(tween(400)),
                    exit = fadeOut(tween(300)) + shrinkHorizontally(tween(300))
                ) {
                    Column(
                        modifier = Modifier.padding(start = 20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pbuh),
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer { alpha = pulseAlpha },
                            tint = statusColor.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = statusColor,
                            modifier = Modifier.graphicsLayer { alpha = pulseAlpha }
                        )
                    }
                }
            }
        }
    }
}
