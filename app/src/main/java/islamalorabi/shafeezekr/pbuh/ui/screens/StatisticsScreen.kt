package islamalorabi.shafeezekr.pbuh.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.data.AppSettings
import islamalorabi.shafeezekr.pbuh.data.DhikrStatsManager
import islamalorabi.shafeezekr.pbuh.util.LocaleUtils
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StatisticsScreen(
    settings: AppSettings,
    onDailyGoalChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val statsManager = remember { DhikrStatsManager(context) }

    var todayCount by remember { mutableIntStateOf(0) }
    var weeklyData by remember { mutableStateOf<List<Pair<LocalDate, Int>>>(emptyList()) }
    var monthlyTotal by remember { mutableIntStateOf(0) }
    var allTimeTotal by remember { mutableIntStateOf(0) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var dataLoaded by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }

    fun loadStats() {
        todayCount = statsManager.getTodayCount()
        weeklyData = statsManager.getWeeklyData()
        monthlyTotal = statsManager.getMonthlyTotal()
        allTimeTotal = statsManager.getAllTimeTotal()
        currentStreak = statsManager.getCurrentStreak()
        dataLoaded = true
    }

    DisposableEffect(statsManager) {
        loadStats()
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            loadStats()
        }
        statsManager.registerChangeListener(listener)
        onDispose {
            statsManager.unregisterChangeListener(listener)
        }
    }

    if (showGoalDialog) {
        DailyGoalDialog(
            currentGoal = settings.dailyGoal,
            onDismiss = { showGoalDialog = false },
            onConfirm = { newGoal ->
                onDailyGoalChange(newGoal)
                showGoalDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TodayCard(
                count = todayCount,
                target = settings.dailyGoal,
                onEditGoal = { showGoalDialog = true }
            )
        }

        item {
            WeeklyChart(data = weeklyData, dailyGoal = settings.dailyGoal)
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    icon = Icons.Outlined.LocalFireDepartment,
                    label = stringResource(R.string.stats_streak),
                    value = LocaleUtils.formatLocalizedNumber(currentStreak),
                    subtitle = stringResource(R.string.stats_streak_days),
                    modifier = Modifier.weight(1f),
                    animateIcon = currentStreak > 0
                )
                StatCard(
                    icon = Icons.Filled.CalendarMonth,
                    label = stringResource(R.string.stats_this_month),
                    value = LocaleUtils.formatLocalizedNumber(monthlyTotal),
                    subtitle = stringResource(R.string.stats_dhikr_unit),
                    modifier = Modifier.weight(1f),
                    animateIcon = false
                )
                StatCard(
                    icon = Icons.Filled.AllInclusive,
                    label = stringResource(R.string.stats_all_time),
                    value = LocaleUtils.formatLocalizedNumber(allTimeTotal),
                    subtitle = stringResource(R.string.stats_dhikr_unit),
                    modifier = Modifier.weight(1f),
                    animateIcon = false
                )
            }
        }

        if (dataLoaded && allTimeTotal == 0) {
            item {
                Text(
                    text = stringResource(R.string.stats_no_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun TodayCard(count: Int, target: Int, onEditGoal: () -> Unit) {
    val progress = (count.toFloat() / target).coerceIn(0f, 1f)
    var animationProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "todayProgress"
    )

    LaunchedEffect(count, target) {
        animationProgress = progress
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.stats_reminders_today),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onEditGoal() }
                ) {
                    Text(
                        text = LocaleUtils.formatLocalizedNumber(count),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = " / " + LocaleUtils.formatLocalizedNumber(target),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                    )
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(bottom = 14.dp, start = 6.dp)
                            .size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    val strokeWidth = 10.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val circleSize = Size(diameter, diameter)
                    val radius = diameter / 2
                    val centerX = size.width / 2
                    val centerY = size.height / 2

                    drawArc(
                        color = trackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = circleSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    val dashStroke = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f)
                    )
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.15f),
                        radius = radius - 12.dp.toPx(),
                        style = dashStroke
                    )

                    val sweepAngle = 360f * animatedProgress
                    drawArc(
                        color = primaryColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = circleSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    if (sweepAngle > 0f) {
                        val angleRad = Math.toRadians((-90f + sweepAngle).toDouble())
                        val tipX = centerX + radius * cos(angleRad).toFloat()
                        val tipY = centerY + radius * sin(angleRad).toFloat()

                        drawCircle(
                            color = Color.White,
                            radius = strokeWidth * 0.5f,
                            center = Offset(tipX, tipY)
                        )
                    }
                }

                Text(
                    text = LocaleUtils.formatLocalizedNumber((progress * 100).toInt()) + "%",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun WeeklyChart(data: List<Pair<LocalDate, Int>>, dailyGoal: Int) {
    if (data.isEmpty()) return

    val maxCount = maxOf((data.maxOfOrNull { it.second } ?: 1), dailyGoal).coerceAtLeast(1)
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val locale = Locale.getDefault()
    val today = LocalDate.now()

    var selectedIndex by remember { mutableStateOf(data.indexOfFirst { it.first == today }.coerceAtLeast(data.lastIndex)) }

    var animationProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 800),
        label = "chartAnimation"
    )

    LaunchedEffect(data) {
        animationProgress = 1f
    }

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
                .padding(20.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.stats_this_week),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val firstDate = data.first().first
                val lastDate = data.last().first
                val dateRange = if (firstDate.month == lastDate.month) {
                    "${firstDate.dayOfMonth} - ${lastDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy", locale))}"
                } else {
                    "${firstDate.format(DateTimeFormatter.ofPattern("d MMM", locale))} - ${lastDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))}"
                }
                Text(
                    text = dateRange,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedIndex in data.indices) {
                val item = data[selectedIndex]
                val dateText = item.first.format(DateTimeFormatter.ofPattern("d MMMM yyyy", locale))
                val dhikrUnit = stringResource(R.string.stats_dhikr_unit)
                val noDhikr = stringResource(R.string.stats_no_dhikr)
                val countText = if (item.second == 0) noDhikr else "${LocaleUtils.formatLocalizedNumber(item.second)} $dhikrUnit"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(
                            color = primaryColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = countText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 24.dp)
                ) {
                    val lines = 3
                    val stepHeight = size.height / (lines + 1)
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx()), 0f)
                    for (i in 1..lines) {
                        val y = i * stepHeight
                        drawLine(
                            color = onSurfaceVariantColor.copy(alpha = 0.08f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashEffect
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    data.forEachIndexed { index, (date, count) ->
                        val isSelected = selectedIndex == index
                        val isToday = date == today
                        val barHeightRatio = if (maxCount > 0) count.toFloat() / maxCount else 0f
                        val barHeight = (110.dp * barHeightRatio * animatedProgress)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    selectedIndex = index
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(110.dp)
                                    .background(
                                        color = if (isSelected) {
                                            primaryColor.copy(alpha = 0.12f)
                                        } else {
                                            onSurfaceVariantColor.copy(alpha = 0.05f)
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(barHeight)
                                        .background(
                                            color = if (isToday) primaryColor else primaryColor.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
                            Text(
                                text = dayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isToday) primaryColor else if (isSelected) MaterialTheme.colorScheme.onSurface else onSurfaceVariantColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    animateIcon: Boolean = false
) {
    val scale = if (animateIcon) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulseIcon")
        val animatedScale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200),
                repeatMode = RepeatMode.Reverse
            ),
            label = "iconScale"
        )
        animatedScale
    } else {
        1f
    }

    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(scale)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DailyGoalDialog(
    currentGoal: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var goalText by remember { mutableStateOf(currentGoal.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.stats_set_goal)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.stats_daily_goal_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length <= 5) {
                            goalText = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.stats_daily_goal)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = goalText.toIntOrNull()
                    if (parsed != null && parsed > 0) {
                        onConfirm(parsed)
                    }
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
