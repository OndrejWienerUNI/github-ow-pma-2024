package com.mitch.christmas.ui.screens.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mitch.christmas.ui.designsystem.ChristmasTheme
import java.util.Calendar

@Composable
fun ChristmasCountdownTimer(
    modifier: Modifier = Modifier
) {
    var remainingTime by remember { mutableStateOf(calculateTimeDifference(getChristmasTimeInMillis(), System.currentTimeMillis())) }

    // Update the remaining time every second
    LaunchedEffect(Unit) {
        while (remainingTime.totalSeconds > 0) {
            remainingTime = calculateTimeDifference(getChristmasTimeInMillis(), System.currentTimeMillis())
            kotlinx.coroutines.delay(1000L) // Delay for 1 second
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

fun getChristmasTimeInMillis(): Long {
    val calendar = Calendar.getInstance()

    // Set Christmas date for the current year
    val christmas = Calendar.getInstance().apply {
        set(Calendar.MONTH, Calendar.DECEMBER)
        set(Calendar.DAY_OF_MONTH, 25)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // If today is after this year's Christmas, calculate for next year
    if (calendar.timeInMillis > christmas.timeInMillis) {
        christmas.add(Calendar.YEAR, 1)
    }

    return christmas.timeInMillis
}


@Composable
@Preview(showBackground = true)
fun ChristmasCountdownPreview() {
    ChristmasTheme {
        ChristmasCountdownTimer(
            modifier = Modifier.fillMaxWidth()
        )
    }
}
