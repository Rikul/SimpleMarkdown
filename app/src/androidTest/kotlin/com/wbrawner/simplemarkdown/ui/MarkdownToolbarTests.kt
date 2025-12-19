package com.wbrawner.simplemarkdown.ui

import androidx.activity.compose.setContent
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextRange
import com.wbrawner.simplemarkdown.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MarkdownToolbarTests {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun setContent(content: @Composable () -> Unit) {
        composeRule.activity.runOnUiThread {
            composeRule.activity.setContent(content = content)
        }
        composeRule.waitForIdle()
    }

    @Test
    fun testToolbarShown() {
        val state = TextFieldState()
        setContent {
            MarkdownToolbar(textFieldState = state)
        }

        composeRule.onNodeWithContentDescription("Bold").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Italic").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Heading").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Bullet List").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Link").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Code").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Quote").assertIsDisplayed()
    }

    @Test
    fun testBold() {
        val state = TextFieldState()
        setContent {
            MarkdownToolbar(textFieldState = state)
        }

        composeRule.onNodeWithContentDescription("Bold").performClick()
        assertEquals("****", state.text.toString())
        assertEquals(TextRange(2), state.selection)

        state.edit {
            replace(0, length, "Hello")
            selection = TextRange(0, 5)
        }
        composeRule.onNodeWithContentDescription("Bold").performClick()
        assertEquals("**Hello**", state.text.toString())
    }

    @Test
    fun testItalic() {
        val state = TextFieldState()
        setContent {
            MarkdownToolbar(textFieldState = state)
        }

        composeRule.onNodeWithContentDescription("Italic").performClick()
        assertEquals("**", state.text.toString())
        assertEquals(TextRange(1), state.selection)

        state.edit {
            replace(0, length, "Hello")
            selection = TextRange(0, 5)
        }
        composeRule.onNodeWithContentDescription("Italic").performClick()
        assertEquals("*Hello*", state.text.toString())
    }

    @Test
    fun testHeading() {
        val state = TextFieldState()
        setContent {
            MarkdownToolbar(textFieldState = state)
        }

        composeRule.onNodeWithContentDescription("Heading").performClick()
        assertEquals("# ", state.text.toString())

        state.edit {
            replace(0, length, "Hello")
            selection = TextRange(0, 5)
        }
        composeRule.onNodeWithContentDescription("Heading").performClick()
        assertEquals("# Hello", state.text.toString())
    }

    @Test
    fun testBulletList() {
        val state = TextFieldState()
        setContent {
            MarkdownToolbar(textFieldState = state)
        }

        composeRule.onNodeWithContentDescription("Bullet List").performClick()
        assertEquals("* ", state.text.toString())

        state.edit {
            replace(0, length, "Hello")
            selection = TextRange(0, 5)
        }
        composeRule.onNodeWithContentDescription("Bullet List").performClick()
        assertEquals("* Hello", state.text.toString())

        state.edit {
            replace(0, length, "Line 1\nLine 2")
            selection = TextRange(0, 13)
        }
        composeRule.onNodeWithContentDescription("Bullet List").performClick()
        assertEquals("* Line 1\n* Line 2", state.text.toString())
    }

    @Test
    fun testLink() {
        val state = TextFieldState()
        setContent {
            MarkdownToolbar(textFieldState = state)
        }

        composeRule.onNodeWithContentDescription("Link").performClick()
        assertEquals("[](http://)", state.text.toString())
        // Cursor should be inside brackets: [|](http://) -> index 1
        assertEquals(TextRange(1), state.selection)

        state.edit {
            replace(0, length, "Hello")
            selection = TextRange(0, 5)
        }
        composeRule.onNodeWithContentDescription("Link").performClick()
        assertEquals("[Hello](http://)", state.text.toString())
        // Cursor should be inside parentheses: [Hello](|http://) -> index 8
        assertEquals(TextRange(8), state.selection)
    }

    @Test
    fun testCode() {
        val state = TextFieldState()
        setContent {
            MarkdownToolbar(textFieldState = state)
        }

        composeRule.onNodeWithContentDescription("Code").performClick()
        assertEquals("``", state.text.toString())
        assertEquals(TextRange(1), state.selection)

        state.edit {
            replace(0, length, "Hello")
            selection = TextRange(0, 5)
        }
        composeRule.onNodeWithContentDescription("Code").performClick()
        assertEquals("`Hello`", state.text.toString())
    }

    @Test
    fun testQuote() {
        val state = TextFieldState()
        setContent {
            MarkdownToolbar(textFieldState = state)
        }

        composeRule.onNodeWithContentDescription("Quote").performClick()
        assertEquals("> ", state.text.toString())

        state.edit {
            replace(0, length, "Hello")
            selection = TextRange(0, 5)
        }
        composeRule.onNodeWithContentDescription("Quote").performClick()
        assertEquals("> Hello", state.text.toString())

        state.edit {
            replace(0, length, "Line 1\nLine 2")
            selection = TextRange(0, 13)
        }
        composeRule.onNodeWithContentDescription("Quote").performClick()
        assertEquals("> Line 1\n> Line 2", state.text.toString())
    }

    @Test
    fun testNestedFormatting() {
        val state = TextFieldState()
        setContent {
            MarkdownToolbar(textFieldState = state)
        }

        // Start with italic text
        state.edit {
            replace(0, length, "*italic text*")
            selection = TextRange(0, 13)
        }

        // Apply Bold
        composeRule.onNodeWithContentDescription("Bold").performClick()

        // Verify nested formatting
        assertEquals("***italic text***", state.text.toString())
    }
}
