package com.mitch.fontpicker.ui.screens.home.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mitch.fontpicker.domain.models.FontPickerLanguagePreference
import com.mitch.fontpicker.ui.util.AppNameAndroidComposeTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LanguagePickerDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val correctItemsRobot = LanguagePickerRobot(
        composeTestRule,
        listOf(FontPickerLanguagePreference.English, FontPickerLanguagePreference.Czech)
    )

    private val wrongItemsRobot = LanguagePickerRobot(
        composeTestRule,
        listOf(
            FontPickerLanguagePreference.English,
            FontPickerLanguagePreference.English,
            FontPickerLanguagePreference.Czech
        )
    )

    @Before
    fun setUp() {
        composeTestRule.setContent {
            LanguagePickerDialog(
                selectedLanguage = FontPickerLanguagePreference.Default,
                onDismiss = { },
                onConfirm = { }
            )
        }
    }

    @Test
    fun allLanguageOptionsExist() {
        with(correctItemsRobot) {
            assertLanguageExists(FontPickerLanguagePreference.English)
            assertLanguageExists(FontPickerLanguagePreference.Czech)
            assertLanguageIsSelected(FontPickerLanguagePreference.Default)
        }
    }

    @Test
    fun whenNewSelected_displaysCorrectOption() {
        with(correctItemsRobot) {
            assertLanguageIsSelected(FontPickerLanguagePreference.Default)
            selectLanguage(FontPickerLanguagePreference.Czech)
            assertLanguageIsSelected(FontPickerLanguagePreference.Czech)

            if (FontPickerLanguagePreference.Default != FontPickerLanguagePreference.Czech) {
                assertLanguageIsNotSelected(FontPickerLanguagePreference.Default)
            }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun whenItemsAreWrong_throwsError() {
        with(wrongItemsRobot) {
            assertLanguageIsSelected(FontPickerLanguagePreference.Default)
            selectLanguage(FontPickerLanguagePreference.Czech)
            assertLanguageIsSelected(FontPickerLanguagePreference.Czech)

            if (FontPickerLanguagePreference.Default != FontPickerLanguagePreference.Czech) {
                assertLanguageIsNotSelected(FontPickerLanguagePreference.Default)
            }
        }
    }
}

class LanguagePickerRobot(
    private val composeTestRule: AppNameAndroidComposeTestRule,
    private val items: List<FontPickerLanguagePreference>
) {

    fun selectLanguage(language: FontPickerLanguagePreference) {
        language.locale?.let {
            composeTestRule
                .onNodeWithText(it.displayLanguage)
                .performClick()
        }
    }

    fun assertLanguageExists(language: FontPickerLanguagePreference) {
        val item: FontPickerLanguagePreference? = items.singleOrNull { it == language }
        requireNotNull(item) {
            "item from language $language is null; check that items DO NOT have the same language"
        }

        item.locale?.let {
            composeTestRule
                .onNodeWithText(it.displayLanguage)
                .assertExists()
        }

        composeTestRule
            .onNodeWithTag(
                testTag = item.name,
                useUnmergedTree = true
            )
            .assertExists()
    }

    fun assertLanguageIsSelected(language: FontPickerLanguagePreference) {
        language.locale?.let {
            composeTestRule
                .onNodeWithText(it.displayLanguage)
                .assertIsSelected()
        }
    }

    fun assertLanguageIsNotSelected(language: FontPickerLanguagePreference) {
        language.locale?.let {
            composeTestRule
                .onNodeWithText(it.displayLanguage)
                .assertIsNotSelected()
        }
    }
}
