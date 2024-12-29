package com.mitch.christmas.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitch.christmas.R
import com.mitch.christmas.ui.designsystem.ChristmasDesignSystem
import com.mitch.christmas.ui.designsystem.ChristmasTheme
import kotlinx.coroutines.delay

// Constants for layout configuration
private const val BASE_WIDTH = 600
private val OUTER_PADDING = 10.dp
private val BOX_PADDING_HORIZONTAL = 16.dp
private val BOX_PADDING_VERTICAL = 16.dp
private val TIME_UNIT_BOX_WIDTH_MIN = 60.dp
private val TIME_UNIT_BOX_WIDTH_MAX = 100.dp
private val COLON_BOX_WIDTH = 16.dp
private val ROW_SPACING = 0.dp
private val HORIZONTAL_SPACER_WIDTH = 0.dp
private val BASE_LABEL_FONT_SIZE = 18.sp
private val BASE_TIME_FONT_SIZE = 54.sp
private val MAX_BOX_WIDTH = 700.dp

@Composable
fun CountdownTimer(
    targetTime: Long,
    modifier: Modifier = Modifier
) {
    var remainingTime by
    remember { mutableStateOf(calculateTimeDifference(targetTime, System.currentTimeMillis())) }

    LaunchedEffect(key1 = targetTime) {
        while (remainingTime.totalSeconds > 0) {
            remainingTime = calculateTimeDifference(targetTime, System.currentTimeMillis())
            delay(1000L) // Update every second
        }
    }

    CountdownContent(
        days = remainingTime.days.toString().padStart(2, '0'),
        hours = remainingTime.hours.toString().padStart(2, '0'),
        minutes = remainingTime.minutes.toString().padStart(2, '0'),
        seconds = remainingTime.seconds.toString().padStart(2, '0'),
        modifier = modifier
    )
}

@Composable
fun CountdownContent(
    days: String,
    hours: String,
    minutes: String,
    seconds: String,
    modifier: Modifier = Modifier
) {

    val labels = runCatching {
        listOf(
            stringResource(id = R.string.days),
            stringResource(id = R.string.hours),
            stringResource(id = R.string.minutes),
            stringResource(id = R.string.seconds)
        )
    }.getOrElse {
        listOf("Days", "Hours", "Minutes", "Seconds") // Fallback labels
    }

    require(labels.size == 4) { "Labels must contain exactly 4 strings." }
    require(labels.all { it.length in 1..10 }) { "Each label must be between 1 and 10 characters." }

    Box(
        modifier = modifier
            .padding(OUTER_PADDING)
            .widthIn(max = MAX_BOX_WIDTH)
            .background(
                color = ChristmasDesignSystem.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = BOX_PADDING_HORIZONTAL, vertical = BOX_PADDING_VERTICAL),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val maxWidth = this.maxWidth
            val textScale = calculateTextScale(maxWidth)

            val totalAvailableWidth = maxWidth - (BOX_PADDING_HORIZONTAL * 2)
            val totalSpacerWidth = HORIZONTAL_SPACER_WIDTH * 6 // 6 spacers for 4 units + 3 colons
            val colonWidth = COLON_BOX_WIDTH * 3 // Width of colons
            val timeUnitBoxWidth = calculateDynamicBoxWidth(
                totalAvailableWidth = totalAvailableWidth - totalSpacerWidth - colonWidth,
            )

            val timeUnits = listOf(days, hours, minutes, seconds)
            val maxLabelWidth = labels.maxOf { it.length }

            // Determine label font size dynamically
            val labelFontSize = calculateLabelFontSize(maxWidth, maxLabelWidth)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // First Row: Time Values and Colons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    timeUnits.forEachIndexed { index, timeUnit ->
                        Box(
                            modifier = Modifier
                                .width(timeUnitBoxWidth)
                                .align(Alignment.CenterVertically),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = timeUnit,
                                color = ChristmasDesignSystem.colorScheme.onPrimary,
                                fontSize = BASE_TIME_FONT_SIZE * textScale,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (index < timeUnits.size - 1) {
                            Spacer(modifier = Modifier.width(HORIZONTAL_SPACER_WIDTH)) // Horizontal spacer before colon
                            Box(
                                modifier = Modifier
                                    .width(COLON_BOX_WIDTH)
                                    .align(Alignment.CenterVertically),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ":",
                                    color = ChristmasDesignSystem.colorScheme.onPrimary,
                                    fontSize = BASE_TIME_FONT_SIZE * textScale,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.width(HORIZONTAL_SPACER_WIDTH)) // Horizontal spacer after colon
                        }
                    }
                }

                Spacer(modifier = Modifier.height(ROW_SPACING)) // Vertical space between rows

                // Second Row: Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Top
                ) {
                    labels.forEachIndexed { index, label ->
                        Box(
                            modifier = Modifier
                                .width(timeUnitBoxWidth) // Match width of time units
                                .align(Alignment.Top),
                            contentAlignment = Alignment.Center // Center the label
                        ) {
                            Text(
                                text = label,
                                color = ChristmasDesignSystem.colorScheme.onPrimary,
                                fontSize = labelFontSize,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (index < labels.size - 1) {
                            Spacer(modifier = Modifier.width(HORIZONTAL_SPACER_WIDTH))
                            Box(
                                modifier = Modifier
                                    .width(COLON_BOX_WIDTH) // Match width of colon
                                    .align(Alignment.Top)
                            )
                        }
                    }
                }
            }
        }
    }
}


// Utility functions for countdown logic
data class TimeDifference(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val totalSeconds: Long
)

fun calculateTimeDifference(targetTime: Long, currentTime: Long): TimeDifference {
    val totalSeconds = (targetTime - currentTime) / 1000
    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return TimeDifference(days, hours, minutes, seconds, totalSeconds)
}

// Utility function to calculate dynamic width for time unit boxes
private fun calculateDynamicBoxWidth(totalAvailableWidth: androidx.compose.ui.unit.Dp): androidx.compose.ui.unit.Dp {
    val count = 4
    val widthPerBox = totalAvailableWidth / count
    return widthPerBox.coerceIn(TIME_UNIT_BOX_WIDTH_MIN, TIME_UNIT_BOX_WIDTH_MAX)
}

// Utility function to calculate dynamic text scaling
@Composable
fun calculateTextScale(maxWidth: androidx.compose.ui.unit.Dp): Float {
    return if (maxWidth <= BASE_WIDTH.dp) {
        maxWidth / BASE_WIDTH.dp
    } else {
        1f // No scaling needed if available space exceeds base width
    }
}

// Utility function to calculate label font size dynamically
@Composable
fun calculateLabelFontSize(maxWidth: androidx.compose.ui.unit.Dp, maxLabelLength: Int): androidx.compose.ui.unit.TextUnit {
    val scalingFactor = (maxWidth / BASE_WIDTH.dp).coerceIn(0.7f, 1f) // Clamp scaling to avoid overly small fonts
    return (BASE_LABEL_FONT_SIZE * scalingFactor).takeIf { maxLabelLength <= 8 }
        ?: (BASE_LABEL_FONT_SIZE * 0.8f)
}

@Composable
@Preview(showBackground = true)
fun CountdownTimerPreview() {
    ChristmasTheme {
        CountdownTimer(
            targetTime = System.currentTimeMillis() + (10 * 24 * 60 * 60 * 1000), // 10 days into the future
            modifier = Modifier.fillMaxWidth(),
        )
    }
}