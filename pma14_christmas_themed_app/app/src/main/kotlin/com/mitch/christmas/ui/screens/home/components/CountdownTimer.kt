package com.mitch.christmas.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mitch.christmas.ui.designsystem.ChristmasDesignSystem
import com.mitch.christmas.ui.designsystem.ChristmasTheme


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
    days: String,
    hours: String,
    minutes: String,
    seconds: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(OUTER_PADDING)
            .widthIn(max = MAX_BOX_WIDTH) // Restrict max width for large screens or landscape
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

            val totalAvailableWidth = maxWidth - (BOX_PADDING_HORIZONTAL * 2) // Exclude padding
            val totalSpacerWidth = HORIZONTAL_SPACER_WIDTH * 6 // 6 spacers for 4 units + 3 colons
            val colonWidth = COLON_BOX_WIDTH * 3 // Width of colons
            val timeUnitBoxWidth = calculateDynamicBoxWidth(
                totalAvailableWidth = totalAvailableWidth - totalSpacerWidth - colonWidth,
            )

            val timeUnits = listOf(days, hours, minutes, seconds)
            val labels = listOf("Days", "Hours", "Minutes", "Seconds")
            val maxLabelWidth = labels.maxOf { it.length }

            // Determine label font size so the longest label fits
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
                                .width(timeUnitBoxWidth) // Dynamically calculated width
                                .align(Alignment.CenterVertically),
                            contentAlignment = Alignment.Center // Center the text
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
                            Spacer(modifier = Modifier.width(HORIZONTAL_SPACER_WIDTH))
                            Box(
                                modifier = Modifier
                                    .width(COLON_BOX_WIDTH) // Fixed width for colon
                                    .align(Alignment.CenterVertically),
                                contentAlignment = Alignment.Center // Center the colon
                            ) {
                                Text(
                                    text = ":",
                                    color = ChristmasDesignSystem.colorScheme.onPrimary,
                                    fontSize = BASE_TIME_FONT_SIZE * textScale,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(modifier = Modifier.width(HORIZONTAL_SPACER_WIDTH))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(ROW_SPACING)) // Space between rows

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

@Preview(showBackground = true)
@Composable
fun CountdownTimerPreview() {
    ChristmasTheme {
        CountdownTimer(
            days = "12",
            hours = "08",
            minutes = "35",
            seconds = "42",
            modifier = Modifier.fillMaxWidth()
        )
    }
}
