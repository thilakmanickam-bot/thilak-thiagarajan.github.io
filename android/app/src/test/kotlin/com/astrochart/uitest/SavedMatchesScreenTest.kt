package com.astrochart.uitest

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.astrochart.core.i18n.Language
import com.astrochart.data.db.entities.SavedMatchEntity
import com.astrochart.ui.i18n.PoruthamStrings
import com.astrochart.ui.i18n.UiStrings
import com.astrochart.ui.screens.SavedMatchesScreen
import com.astrochart.ui.theme.AstroChartTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDateTime

/**
 * Deleting is destructive and irreversible, so the two things worth pinning are
 * that it asks first and that cancelling really does nothing.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SavedMatchesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val ps = PoruthamStrings.forLanguage(Language.EN)
    private val strings = UiStrings.forLanguage(Language.EN)

    private val deleted = mutableListOf<Long>()
    private val opened = mutableListOf<Long>()

    private fun match(id: Long, groom: String, bride: String, total: Int) = SavedMatchEntity(
        id = id,
        groomName = groom,
        brideName = bride,
        groomRasi = 0,
        groomNakshatra = 0,
        brideRasi = 1,
        brideNakshatra = 1,
        total = total,
        savedAt = LocalDateTime.of(2026, 9, 2, 10, 0)
    )

    private fun setContent(matches: List<SavedMatchEntity>) {
        composeTestRule.setContent {
            AstroChartTheme {
                SavedMatchesScreen(
                    matches = matches,
                    onMatchSelected = { opened += it },
                    onDelete = { deleted += it }
                )
            }
        }
    }

    @Test
    fun anEmptyListSaysSoRatherThanShowingNothing() {
        setContent(emptyList())

        composeTestRule.onNodeWithText(ps.noSavedMatches).assertExists()
    }

    @Test
    fun eachSavedMatchShowsBothNamesAndItsStoredScore() {
        setContent(listOf(match(1, "Ravi", "Meera", 27)))

        composeTestRule.onNodeWithText("Ravi · Meera").assertExists()
        composeTestRule.onNodeWithText("27/40").assertExists()
    }

    @Test
    fun tappingARowOpensThatMatchAndNoOther() {
        setContent(listOf(match(1, "Ravi", "Meera", 27), match(2, "Arun", "Divya", 31)))

        composeTestRule.onNodeWithText("Arun · Divya").performClick()

        assertEquals(listOf(2L), opened)
    }

    @Test
    fun deletingAsksBeforeItDeletes() {
        setContent(listOf(match(1, "Ravi", "Meera", 27)))

        composeTestRule.onNodeWithContentDescription(strings.delete).performClick()

        composeTestRule.onNodeWithText(ps.deleteMatchConfirm).assertExists()
        assertEquals("nothing is deleted until the dialog is confirmed", emptyList<Long>(), deleted)
    }

    @Test
    fun cancellingTheDialogLeavesTheMatchAlone() {
        setContent(listOf(match(1, "Ravi", "Meera", 27)))

        composeTestRule.onNodeWithContentDescription(strings.delete).performClick()
        composeTestRule.onNodeWithText(strings.cancel).performClick()

        assertEquals(emptyList<Long>(), deleted)
        composeTestRule.onNodeWithText(ps.deleteMatchConfirm).assertDoesNotExist()
    }

    @Test
    fun confirmingDeletesExactlyThatMatch() {
        setContent(listOf(match(1, "Ravi", "Meera", 27), match(2, "Arun", "Divya", 31)))

        composeTestRule.onAllNodesWithContentDescription(strings.delete).assertCountEquals(2)
        composeTestRule.onAllNodesWithContentDescription(strings.delete)[1].performClick()
        // "Delete" reaches the icons as a content description and the dialog's
        // confirm button as visible text, so a text match finds only the button.
        composeTestRule.onNodeWithText(strings.delete).performClick()

        assertEquals(listOf(2L), deleted)
    }
}
