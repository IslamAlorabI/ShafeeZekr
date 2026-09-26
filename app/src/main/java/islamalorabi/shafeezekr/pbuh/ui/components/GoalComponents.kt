package islamalorabi.shafeezekr.pbuh.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import islamalorabi.shafeezekr.pbuh.R
import islamalorabi.shafeezekr.pbuh.ui.theme.SmoothCornerShape
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

private class Particle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val colorIndex: Int,
    val spin: Float
)

/**
 * Full-screen burst of confetti plus a "goal reached" badge.
 * Shown while [visible] is true; calls [onFinished] after the animation ends.
 */
@Composable
fun GoalCelebration(visible: Boolean, onFinished: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(tween(400))
    ) {
        val colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.secondary,
            Color(0xFFFFC857)
        )
        val particles = remember {
            List(90) {
                Particle(
                    angle = Random.nextFloat() * 360f,
                    speed = 0.35f + Random.nextFloat() * 0.65f,
                    size = 6f + Random.nextFloat() * 8f,
                    colorIndex = Random.nextInt(4),
                    spin = Random.nextFloat() * 720f - 360f
                )
            }
        }
        val progress = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            progress.animateTo(1f, tween(2200, easing = LinearEasing))
            delay(0.4.seconds)
            onFinished()
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val t = progress.value
                val origin = Offset(size.width / 2, size.height * 0.4f)
                val reach = size.minDimension * 0.75f
                particles.forEach { p ->
                    val rad = Math.toRadians(p.angle.toDouble())
                    val distance = reach * p.speed * FastOutSlowInEasing.transform(t)
                    val gravity = size.height * 0.35f * t * t
                    val center = Offset(
                        origin.x + (cos(rad) * distance).toFloat(),
                        origin.y + (sin(rad) * distance).toFloat() + gravity
                    )
                    val px = p.size * density / 2
                    rotate(p.spin * t, pivot = center) {
                        drawRect(
                            color = colors[p.colorIndex].copy(alpha = (1f - t).coerceIn(0f, 1f)),
                            topLeft = Offset(center.x - px, center.y - px / 2),
                            size = Size(px * 2, px)
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = progress.value < 0.9f,
                enter = scaleIn(initialScale = 0.6f) + fadeIn(),
                exit = scaleOut(targetScale = 0.9f) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, SmoothCornerShape(32.dp))
                        .padding(horizontal = 28.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.goal_reached_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.goal_reached_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
