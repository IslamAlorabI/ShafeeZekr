package islamalorabi.shafeezekr.pbuh.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.data.AppSettings
import islamalorabi.shafeezekr.pbuh.data.ReminderInterval
import android.media.MediaPlayer
import islamalorabi.shafeezekr.pbuh.ui.components.ModernInfoCard
import islamalorabi.shafeezekr.pbuh.ui.components.ModernStatusCard
import islamalorabi.shafeezekr.pbuh.ui.theme.CardBackground
import islamalorabi.shafeezekr.pbuh.ui.theme.CardTextPrimary
import islamalorabi.shafeezekr.pbuh.ui.theme.CardTextSecondary
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
    
    val sharedPrefs = remember { context.getSharedPreferences("reminder_prefs", android.content.Context.MODE_PRIVATE) }
    var remainingTime by remember { mutableLongStateOf(0L) }
    
    LaunchedEffect(settings.isReminderEnabled, settings.reminderInterval, settings.customIntervalMinutes) {
        while (settings.isReminderEnabled) {
            val nextTrigger = sharedPrefs.getLong("next_trigger_time", 0L)
            val now = System.currentTimeMillis()
            remainingTime = if (nextTrigger > now) nextTrigger - now else 0L
            delay(1000L)
        }
        remainingTime = 0L
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CardBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModernStatusCard(
                title = stringResource(R.string.home_title),
                subtitle = stringResource(R.string.tap_to_listen),
                icon = Icons.Filled.PlayArrow,
                onClick = {
                    try {
                        val mp = MediaPlayer.create(context, R.raw.zikr_sound)
                        mp?.setOnCompletionListener { it.release() }
                        mp?.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            )
        }
        
        item {
            AnimatedVisibility(
                visible = settings.isReminderEnabled && remainingTime > 0,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val minutes = (remainingTime / 60000).toInt()
                val seconds = ((remainingTime % 60000) / 1000).toInt()
                ModernInfoCard {
                    Text(
                        text = stringResource(R.string.next_reminder),
                        style = MaterialTheme.typography.labelMedium,
                        color = CardTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = CardTextPrimary
                    )
                }
            }
        }

        item {
            SettingsGroup(
                header = stringResource(R.string.enable_reminder),
                headerColor = CardTextPrimary // Using white for header to match dark theme
            ) {
                ModernInfoCard(modifier = Modifier.padding(0.dp)) { // Padding handled by ModernInfoCard internal
                     Row(
                         modifier = Modifier.fillMaxWidth(),
                         verticalAlignment = Alignment.CenterVertically,
                         horizontalArrangement = Arrangement.SpaceBetween
                     ) {
                         Row(verticalAlignment = Alignment.CenterVertically) {
                             Icon(
                                 painter = painterResource(id = R.drawable.ic_pbuh),
                                 contentDescription = null,
                                 modifier = Modifier.size(24.dp),
                                 tint = CardTextPrimary
                             )
                             Spacer(modifier = Modifier.width(16.dp))
                             Column {
                                 Text(
                                     text = stringResource(R.string.enable_reminder),
                                     style = MaterialTheme.typography.bodyLarge,
                                     color = CardTextPrimary
                                 )
                                 Text(
                                     text = stringResource(R.string.enable_reminder_desc),
                                     style = MaterialTheme.typography.bodyMedium,
                                     color = CardTextSecondary
                                 )
                             }
                         }
                         Switch(
                             checked = settings.isReminderEnabled,
                             onCheckedChange = { enabled ->
                                 if (enabled && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                     permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                 } else {
                                     onReminderEnabledChange(enabled)
                                 }
                             }
                         )
                     }
                }
            }
        }

        item {
            AnimatedVisibility(visible = settings.isReminderEnabled) {
                SettingsGroup(
                    header = stringResource(R.string.interval_title),
                    headerColor = CardTextPrimary
                ) {
                    ModernInfoCard(modifier = Modifier.padding(0.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectableGroup()
                        ) {
                            Text(
                                text = stringResource(R.string.interval_reset_note),
                                style = MaterialTheme.typography.bodySmall,
                                color = CardTextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            val intervals = listOf(
                                ReminderInterval.FIVE to stringResource(R.string.interval_5_min),
                                ReminderInterval.TEN to stringResource(R.string.interval_10_min),
                                ReminderInterval.THIRTY to stringResource(R.string.interval_30_min),
                                ReminderInterval.SIXTY to stringResource(R.string.interval_1_hour),
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
                                        .padding(vertical = 12.dp),
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
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = CardTextPrimary
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
    var textValue by remember { mutableStateOf(currentValue.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_interval_title)) },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        textValue = newValue
                    }
                },
                label = { Text(stringResource(R.string.minutes)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val minutes = textValue.toIntOrNull() ?: currentValue
                    onConfirm(if (minutes > 0) minutes else 1)
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
