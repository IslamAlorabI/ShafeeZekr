package islamalorabi.shafeezekr.pbuh.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.data.AppSettings
import islamalorabi.shafeezekr.pbuh.data.ColorScheme
import islamalorabi.shafeezekr.pbuh.data.ThemeMode

data class LanguageOption(
    val code: String,
    val nameRes: Int
)

val languages = listOf(
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
    modifier: Modifier = Modifier
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

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
                                    painter = painterResource(id = R.drawable.ic_theme),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary
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
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            leadingContent = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_palette),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary
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
            val context = LocalContext.current
            val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as PowerManager
            val isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(context.packageName)
            
            SettingsGroup(
                header = stringResource(R.string.battery_section),
                headerColor = MaterialTheme.colorScheme.primary
            ) {
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
                                painter = painterResource(id = R.drawable.ic_do_not_disturb),
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
        title = { Text(stringResource(R.string.color_scheme)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                val options = listOf(
                    ColorScheme.MONET to stringResource(R.string.color_monet),
                    ColorScheme.GREEN to stringResource(R.string.color_green),
                    ColorScheme.BLUE to stringResource(R.string.color_blue),
                    ColorScheme.PURPLE to stringResource(R.string.color_purple),
                    ColorScheme.TEAL to stringResource(R.string.color_teal)
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
