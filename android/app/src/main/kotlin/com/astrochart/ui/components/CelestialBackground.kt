package com.astrochart.ui.components

import android.animation.ValueAnimator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.astrochart.ui.theme.AstroBgBottom
import com.astrochart.ui.theme.AstroBgMid
import com.astrochart.ui.theme.AstroBgTop
import com.astrochart.ui.theme.AstroGlow
import com.astrochart.ui.theme.GoldLight
import com.astrochart.ui.theme.Star
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** Lets a nested screen (e.g. the chart pager) push a parallax offset to the sky. */
class BackgroundMotion {
    var parallax by mutableFloatStateOf(0f)
}

val LocalBackgroundMotion = staticCompositionLocalOf { BackgroundMotion() }

private data class StarSpec(
    val x: Float,
    val y: Float,
    val radiusDp: Float,
    val baseAlpha: Float,
    val phase: Float
)

private class Ripple(val center: Offset) {
    val progress = Animatable(0f)
}

private fun generateStars(count: Int): List<StarSpec> {
    val rng = Random(42)
    return List(count) {
        StarSpec(
            x = rng.nextFloat(),
            y = rng.nextFloat(),
            radiusDp = 0.6f + rng.nextFloat() * 1.4f,
            baseAlpha = 0.35f + rng.nextFloat() * 0.6f,
            phase = rng.nextFloat() * (2f * PI.toFloat())
        )
    }
}

/**
 * The animated celestial backdrop. Renders a gradient + radial glow + a star field.
 * When the system animator is enabled it twinkles, drifts, reacts to taps with a gold
 * ripple, and parallax-shifts with [BackgroundMotion]. When animations are disabled
 * (CI / user setting) it renders a static sky so Compose UI tests reach idle.
 */
@Composable
fun CelestialBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val animated = remember { ValueAnimator.areAnimatorsEnabled() }
    val motion = remember { BackgroundMotion() }
    val stars = remember { generateStars(56) }
    val ripples = remember { mutableStateListOf<Ripple>() }
    val scope = rememberCoroutineScope()

    val gradient = Brush.verticalGradient(listOf(AstroBgTop, AstroBgMid, AstroBgBottom))

    val tapModifier = if (animated) {
        Modifier.pointerInput(Unit) {
            awaitEachGesture {
                // Observe the touch in the Initial pass without consuming it, so
                // buttons and other children still receive the gesture normally.
                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                val ripple = Ripple(down.position)
                ripples.add(ripple)
                scope.launch {
                    ripple.progress.animateTo(1f, tween(650))
                    ripples.remove(ripple)
                }
            }
        }
    } else {
        Modifier
    }

    CompositionLocalProvider(LocalBackgroundMotion provides motion) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(gradient)
                .then(tapModifier)
        ) {
            if (animated) {
                AnimatedSky(stars = stars, motion = motion, ripples = ripples)
            } else {
                StaticSky(stars = stars)
            }
            content()
        }
    }
}

@Composable
private fun AnimatedSky(
    stars: List<StarSpec>,
    motion: BackgroundMotion,
    ripples: List<Ripple>
) {
    val transition = rememberInfiniteTransition(label = "sky")
    val twinkle by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI.toFloat()),
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "twinkle"
    )
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(40000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawSky(
            stars = stars,
            twinklePhase = twinkle,
            driftY = (drift - 0.5f) * 0.02f,
            parallaxX = motion.parallax * 0.04f,
            ripples = ripples
        )
    }
}

@Composable
private fun StaticSky(stars: List<StarSpec>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawSky(stars = stars, twinklePhase = 0f, driftY = 0f, parallaxX = 0f, ripples = emptyList())
    }
}

private fun DrawScope.drawSky(
    stars: List<StarSpec>,
    twinklePhase: Float,
    driftY: Float,
    parallaxX: Float,
    ripples: List<Ripple>
) {
    // Soft purple glow near the top.
    val glowCenter = Offset(size.width * 0.5f, size.height * 0.16f)
    val glowRadius = size.maxDimension * 0.65f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(AstroGlow.copy(alpha = 0.5f), Color.Transparent),
            center = glowCenter,
            radius = glowRadius
        ),
        radius = glowRadius,
        center = glowCenter
    )

    // Stars.
    for (s in stars) {
        val tw = 0.55f + 0.45f * sin(twinklePhase + s.phase)
        val cx = wrap01(s.x + parallaxX) * size.width
        val cy = wrap01(s.y + driftY) * size.height
        drawCircle(
            color = Star.copy(alpha = (s.baseAlpha * tw).coerceIn(0f, 1f)),
            radius = s.radiusDp.dp.toPx(),
            center = Offset(cx, cy)
        )
    }

    // Tap ripples.
    for (r in ripples) {
        val p = r.progress.value
        drawCircle(
            color = GoldLight.copy(alpha = (1f - p) * 0.35f),
            radius = p * size.minDimension * 0.5f,
            center = r.center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

private fun wrap01(v: Float): Float {
    val m = v % 1f
    return if (m < 0f) m + 1f else m
}
