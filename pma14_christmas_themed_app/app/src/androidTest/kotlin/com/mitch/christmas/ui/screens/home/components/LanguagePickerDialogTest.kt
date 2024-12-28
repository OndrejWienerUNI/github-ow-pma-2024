package com.mitch.christmas.ui.screens.home.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mitch.christmas.domain.models.ChristmasLanguagePreference
import com.mitch.christmas.ui.util.AppNameAndroidComposeTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LanguagePickerDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val correctItemsRobot = LanguagePickerRobot(
        composeTestRule,
        listOf(LanguagePickerItem.English, LanguagePickerItem.Czech)
    )

    private val wrongItemsRobot = LanguagePickerRobot(
        composeTestRule,
        listOf(
            LanguagePickerItem.English,
            LanguagePickerItem.English,
            LanguagePickerItem.Czech
        )
    )

    @Before
    fun setUp() {
        composeTestRule.setContent {
            LanguagePickerDialog(
                selectedLanguage = ChristmasLanguagePreference.Default,
                onDismiss = { },
                onConfirm = { }
            )
        }
    }

    @Test
    fun allLanguageOptionsExist() {
        with(correctItemsRobot) {
            assertLanguageExists(ChristmasLanguagePreference.English)
            assertLanguageExists(ChristmasLanguagePreference.Czech)
            assertLanguageIsSelected(ChristmasLanguagePreference.Default)
        }
    }

    @Test
    fun whenNewSelected_displaysCorrectOption() {
        with(correctItemsRobot) {
            assertLanguageIsSelected(ChristmasLanguagePreference.Default)
            selectLanguage(ChristmasLanguagePreference.Czech)
            assertLanguageIsSelected(ChristmasLanguagePreference.Czech)

            if (ChristmasLanguagePreference.Default != ChristmasLanguagePreference.Czech) {
                assertLanguageIsNotSelected(ChristmasLanguagePreference.Default)
            }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun whenItemsAreWrong_throwsError() {
        with(wrongItemsRobot) {
            assertLanguageIsSelected(ChristmasLanguagePreference.Default)
            selectLanguage(ChristmasLanguagePreference.Czech)
            assertLanguageIsSelected(ChristmasLanguagePreference.Czech)

            if (ChristmasLanguagePreference.Default != ChristmasLanguagePreference.Czech) {
                assertLanguageIsNotSelected(ChristmasLanguagePreference.Default)
            }
        }
    }
}

class LanguagePickerRobot(
    private val composeTestRule: AppNameAndroidComposeTestRule,
    private val items: List<LanguagePickerItem>
) {

    fun selectLanguage(language: ChristmasLanguagePreference) {
        composeTestRule
            .onNodeWithText(language.locale.displayLanguage)
            .performClick()
    }

    fun assertLanguageExists(language: ChristmasLanguagePreference) {
        val item = items.singleOrNull { it.language == language }
        requireNotNull(item) {
            "item from language $language is null; check that items DO NOT have the same language"
        }

        composeTestRule
            .onNodeWithText(item.language.locale.displayLanguage)
            .assertExists()

        composeTestRule
            .onNodeWithTag(
                testTag = item.flagId.toString(),
                useUnmergedTree = true
            )
            .assertExists()
    }

    fun assertLanguageIsSelected(language: ChristmasLanguagePreference) {
        composeTestRule
            .onNodeWithText(language.locale.displayLanguage)
            .assertIsSelected()
    }

    fun assertLanguageIsNotSelected(language: ChristmasLanguagePreference) {
        composeTestRule
            .onNodeWithText(language.locale.displayLanguage)
            .assertIsNotSelected()
    }
}
