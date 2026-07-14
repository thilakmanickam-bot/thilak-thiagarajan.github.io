package com.astrochart.ui

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.astrochart.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Instrumented Compose UI tests that drive the real app on an emulator and
 * capture a screenshot of each screen. Screenshots are written to the app's
 * internal files dir so CI can pull them with `run-as` on the debug build
 * (avoids scoped-storage restrictions on adb pull).
 */
@RunWith(AndroidJUnit4::class)
class ChartFlowScreenshotTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun screenshot(name: String) {
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dir = File(context.filesDir, "screenshots").apply { mkdirs() }
        FileOutputStream(File(dir, "$name.png")).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    @Test
    fun homeScreen_showsActions() {
        composeRule.onNodeWithText("Calculate My Chart").assertExists()
        composeRule.onNodeWithText("View Saved Charts").assertExists()
        composeRule.onNodeWithText("Sample Chart").assertExists()
        screenshot("01-home")
    }

    @Test
    fun birthInputScreen_showsForm() {
        composeRule.onNodeWithText("Calculate My Chart").performClick()
        composeRule.onNodeWithText("Name").assertExists()
        composeRule.onNodeWithText("Location").assertExists()
        screenshot("02-birth-input")
    }

    @Test
    fun sampleChart_rendersDetail() {
        composeRule.onNodeWithText("Sample Chart").performClick()
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText("Placements").fetchSemanticsNodes().isNotEmpty()
        }
        screenshot("03-sample-chart")
    }
}
