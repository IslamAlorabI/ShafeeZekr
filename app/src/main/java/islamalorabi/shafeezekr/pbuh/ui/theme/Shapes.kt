package islamalorabi.shafeezekr.pbuh.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Rounded rectangle with continuous-curvature ("squircle") corners, following
 * Figma's corner smoothing: each corner is a bezier easing in from the edge, a
 * circular arc in the middle, and a bezier easing back out.
 *
 * @param smoothing 0f = plain circular corner, 1f = maximum smoothing.
 */
class SmoothCornerShape(
    private val cornerRadius: Dp,
    private val smoothing: Float = 0.6f
) : Shape {

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val maxExtent = min(w, h) / 2f
        val radius = min(with(density) { cornerRadius.toPx() }, maxExtent)
        if (radius <= 0f) {
            return Outline.Generic(Path().apply { addRect(size.toRect()) })
        }

        val extent = min((1f + smoothing.coerceIn(0f, 1f)) * radius, maxExtent)
        val s = min(extent / radius - 1f, smoothing.coerceIn(0f, 1f))

        val arcMeasure = 90f * (1f - s)
        val arcSection = sin(rad(arcMeasure / 2f)) * radius * sqrt(2f)
        val angleAlpha = (90f - arcMeasure) / 2f
        val p3ToP4 = radius * tan(rad(angleAlpha / 2f))
        val angleBeta = 45f * s
        val c = p3ToP4 * cos(rad(angleBeta))
        val d = c * tan(rad(angleBeta))
        val b = (extent - arcSection - c - d) / 3f
        val a = 2f * b
        val g = CornerGeometry(radius, extent, a, b, c, d, rad(arcMeasure))

        val path = Path()
        path.moveTo(extent, 0f)
        // Clockwise: top-right, bottom-right, bottom-left, top-left.
        path.corner(g, Offset(w, 0f), Offset(1f, 0f), Offset(0f, 1f))
        path.corner(g, Offset(w, h), Offset(0f, 1f), Offset(-1f, 0f))
        path.corner(g, Offset(0f, h), Offset(-1f, 0f), Offset(0f, -1f))
        path.corner(g, Offset(0f, 0f), Offset(0f, -1f), Offset(1f, 0f))
        path.close()
        return Outline.Generic(path)
    }

    private class CornerGeometry(
        val radius: Float, val extent: Float,
        val a: Float, val b: Float, val c: Float, val d: Float,
        val arcRadians: Float
    )

    /** Draws one corner at [k], arriving along direction [u] and leaving along [v]. */
    private fun Path.corner(g: CornerGeometry, k: Offset, u: Offset, v: Offset) {
        fun at(back: Float, out: Float) = k - u * back + v * out

        val start = at(g.extent, 0f)
        lineTo(start.x, start.y)

        val arcStart = at(g.extent - g.a - g.b - g.c, g.d)
        val arcEnd = at(g.d, g.extent - g.a - g.b - g.c)
        at(g.extent - g.a, 0f).let { p1 ->
            at(g.extent - g.a - g.b, 0f).let { p2 ->
                cubicTo(p1.x, p1.y, p2.x, p2.y, arcStart.x, arcStart.y)
            }
        }

        // Circular arc approximated by a single cubic (arc is at most 90°).
        val center = at(g.radius, g.radius)
        val handle = 4f / 3f * tan(g.arcRadians / 4f) * g.radius
        val c1 = arcStart + tangentToward(arcStart - center, arcEnd - arcStart) * handle
        val c2 = arcEnd + tangentToward(arcEnd - center, arcStart - arcEnd) * handle
        cubicTo(c1.x, c1.y, c2.x, c2.y, arcEnd.x, arcEnd.y)

        val q1 = at(0f, g.extent - g.a - g.b)
        val q2 = at(0f, g.extent - g.a)
        val end = at(0f, g.extent)
        cubicTo(q1.x, q1.y, q2.x, q2.y, end.x, end.y)
    }

    private fun tangentToward(radial: Offset, toward: Offset): Offset {
        val len = radial.getDistance()
        if (len == 0f) return Offset.Zero
        val t = Offset(-radial.y / len, radial.x / len)
        return if (t.x * toward.x + t.y * toward.y < 0f) -t else t
    }

    private fun rad(degrees: Float) = degrees * (Math.PI.toFloat() / 180f)

    override fun equals(other: Any?): Boolean =
        other is SmoothCornerShape && other.cornerRadius == cornerRadius && other.smoothing == smoothing

    override fun hashCode(): Int = 31 * cornerRadius.hashCode() + smoothing.hashCode()
}

private fun Size.toRect() = androidx.compose.ui.geometry.Rect(Offset.Zero, this)
