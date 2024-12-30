package com.mitch.christmas.ui.animations

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mitch.christmas.R
import com.mitch.christmas.ui.designsystem.ChristmasDarkGray
import kotlin.random.Random

// Constants for animation configuration
private const val ANIMATION_INITIAL_OFFSET = -0.1f // Off-screen start
private const val ANIMATION_TARGET_OFFSET = 1.1f // Off-screen end

@Composable
fun SnowingAnimation(
    modifier: Modifier = Modifier,
    snowflakeSize: Dp = 24.dp,
    snowflakeCount: Int = 50, // Number of snowflakes
    baseDuration: Int = 15000, // Base duration of the fall animation in milliseconds
    isPreview: Boolean = false // Flag to toggle between preview and runtime
) {
    val snowflakePainter: Painter = painterResource(id = R.drawable.snowflake)

    // Generate random properties for snowflakes
    val snowflakes = remember {
        List(snowflakeCount) {
            Snowflake(
                x = Random.nextFloat(),
                scale = Random.nextFloat() * 0.5f + 0.5f,
                rotation = Random.nextFloat() * 360f,
                delay = Random.nextInt(0, baseDuration),
                speedMultiplier = Random.nextFloat() * 0.5f + 0.75f // Speed variation factor
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "")

    // Animate snowflake Y offsets
    val yOffsets = snowflakes.map { snowflake ->
        val duration = (baseDuration * snowflake.speedMultiplier).toInt() // Adjust duration based on speed multiplier
        if (isPreview) Random.nextFloat() else infiniteTransition.animateFloat(
            initialValue = ANIMATION_INITIAL_OFFSET,
            targetValue = ANIMATION_TARGET_OFFSET,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = duration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(snowflake.delay)
            ), label = ""
        ).value
    }

    // Animate horizontal drift (oscillation)
    val xDrifts = snowflakes.map { snowflake ->
        val driftDuration = (baseDuration * 0.3f * snowflake.speedMultiplier).toInt() // Drift duration proportional to speed
        infiniteTransition.animateFloat(
            initialValue = -0.02f, // Slight left offset
            targetValue = 0.02f, // Slight right offset
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = driftDuration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ), label = ""
        ).value
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            snowflakes.forEachIndexed { index, snowflake ->
                val yOffset = yOffsets[index]
                val xDrift = xDrifts[index]

                // Calculate drifting X position
                val canvasX = size.width * (snowflake.x + xDrift).coerceIn(0f, 1f) // Keep within bounds
                val canvasY = size.height * yOffset

                drawIntoCanvas { canvas ->
                    canvas.save()

                    // Translate the canvas to the position of the snowflake
                    canvas.translate(canvasX, canvasY)

                    // Rotate the canvas for the snowflake's rotation
                    canvas.rotate(snowflake.rotation)

                    // Apply snowflake size and scaling
                    val snowflakeSizePx = snowflakeSize.toPx() * snowflake.scale
                    val sizeToDraw = Size(snowflakeSizePx, snowflakeSizePx)

                    // Draw the Painter at the transformed position
                    snowflakePainter.apply {
                        draw(size = sizeToDraw)
                    }

                    canvas.restore()
                }
            }
        }
    }
}

// Snowflake data class with added speedMultiplier for varying fall speeds
data class Snowflake(
    val x: Float, // Horizontal position as a percentage of screen width
    val scale: Float, // Scaling factor for the snowflake size
    val rotation: Float, // Rotation in degrees
    val delay: Int, // Delay in milliseconds to stagger the animation start
    val speedMultiplier: Float // Multiplier for the fall speed
)

// Preview for the snowing animation
@Preview(showBackground = true)
@Composable
fun SnowingAnimationPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = ChristmasDarkGray) // Dark background
    ) {
        SnowingAnimation(
            snowflakeSize = 20.dp, // Adjust size for preview
            snowflakeCount = 30,  // Fewer snowflakes for better performance in preview
            baseDuration = 15000,  // Shorter duration for quicker visualization
            isPreview = true      // Enable preview mode
        )
    }
}
