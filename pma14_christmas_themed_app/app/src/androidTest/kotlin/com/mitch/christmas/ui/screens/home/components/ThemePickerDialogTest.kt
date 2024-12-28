package com.mitch.christmas.ui.screens.home.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mitch.christmas.domain.models.ChristmasThemePreference
import com.mitch.christmas.ui.util.AppNameAndroidComposeTestRule
import com.mitch.christmas.ui.util.stringResource
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ThemePickerDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val correctItemsRobot = ThemePickerRobot(
        composeTestRule,
        listOf(
            ThemePickerItem.FollowSystem,
            ThemePickerItem.Dark,
            ThemePickerItem.Light
        )
    )

    private val wrongItemsRobot = ThemePickerRobot(
        composeTestRule,
        listOf(
            ThemePickerItem.FollowSystem,
            ThemePickerItem.FollowSystem, // repeated twice -> wrong
            ThemePickerItem.Light
        )
    )

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ThemePickerDialog(
                selectedTheme = ChristmasThemePreference.Default,
                onDismiss = { },
                onConfirm = { }
            )
        }
    }

    @Test
    fun allThemeOptionsExist() {
        with(correctItemsRobot) {
            assertThemeExists(ChristmasThemePreference.FollowSystem)
            assertThemeExists(ChristmasThemePreference.Light)
            assertThemeExists(ChristmasThemePreference.Dark)
            assertThemeIsSelected(ChristmasThemePreference.Default)
        }
    }

    @Test
    fun whenNewSelected_displaysCorrectOption() {
        with(correctItemsRobot) {
            selectTheme(ChristmasThemePreference.Dark)
            assertThemeIsNotSelected(ChristmasThemePreference.Light)
            assertThemeIsNotSelected(ChristmasThemePreference.FollowSystem)
            assertThemeIsSelected(ChristmasThemePreference.Dark)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun whenItemsAreWrong_throwsError() {
        with(wrongItemsRobot) {
            selectTheme(ChristmasThemePreference.Dark)
            assertThemeIsNotSelected(ChristmasThemePreference.Light)
            assertThemeIsNotSelected(ChristmasThemePreference.FollowSystem)
            assertThemeIsSelected(ChristmasThemePreference.Dark)
        }
    }
}

class ThemePickerRobot(
    private val composeTestRule: AppNameAndroidComposeTestRule,
    private val items: List<ThemePickerItem>
) {
    fun selectTheme(theme: ChristmasThemePreference) {
        val item = items.singleOrNull { it.theme == theme }
        requireNotNull(item) {
            "item from theme $theme is null; check that items DO NOT have the same theme"
        }

        composeTestRule
            .onNodeWithText(composeTestRule.stringResource(id = item.titleId))
            .performClick()
    }

    fun assertThemeExists(theme: ChristmasThemePreference) {
        val item = items.singleOrNull { it.theme == theme }
        requireNotNull(item) {
            "item from theme $theme is null; check that items DO NOT have the same theme"
        }

        composeTestRule
            .onNodeWithText(composeTestRule.stringResource(id = item.titleId))
            .assertExists()

        composeTestRule
            .onNodeWithTag(
                testTag = item.icon.toString(),
                useUnmergedTree = true
            )
            .assertExists()
    }

    fun assertThemeIsSelected(theme: ChristmasThemePreference) {
        val item = items.singleOrNull { it.theme == theme }
        requireNotNull(item) {
            "item from theme $theme is null; check that items DO NOT have the same theme"
        }

        composeTestRule
            .onNodeWithText(composeTestRule.stringResource(id = item.titleId))
            .assertIsSelected()
    }

    fun assertThemeIsNotSelected(theme: ChristmasThemePreference) {
        val item = items.singleOrNull { it.theme == theme }
        requireNotNull(item) {
            "item from theme $theme is null; check that items DO NOT have the same theme"
        }

        composeTestRule
            .onNodeWithText(composeTestRule.stringResource(id = item.titleId))
            .assertIsNotSelected()
    }
}
